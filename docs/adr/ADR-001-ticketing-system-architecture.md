# ADR-001: 선착순 티켓팅 시스템 아키텍처

## Context

대규모 트래픽이 짧은 시간에 집중되는 **선착순 티켓팅 시스템** 설계가 필요하다. 인스턴스 사양 증가만으로 트래픽 증가에 대응 가능한 **확장 가능한 아키텍처**를 구현한다.

### 요구사항

- **재고 정확도 100%** (Over-selling 방지)
- **FIFO 순서 보장** (공정성)
- **P95 응답시간 < 500ms**
- **인스턴스 증가 시 선형적 성능 향상** (Efficiency > 90%)

### 제약사항

- **TDD 기반 개발**

---

## Decision

### 아키텍처: Kafka + Redis Hybrid (Stateless)

```
Client → ALB → API (Stateless) → Redis (Cache/Queue)
                                → Kafka (Event Stream)
                                → PostgreSQL (Persistence)
```

### 핵심 설계 원칙

#### 1. Stateless API
- 모든 상태를 외부 저장소에 저장 (Redis, DB)
- 어느 인스턴스가 처리해도 동일한 결과
- 무한 수평 확장 가능

#### 2. 비동기 처리
- 빠른 검증 (Redis) → 이벤트 발행 (Kafka) → 즉시 응답
- 무거운 작업은 Consumer가 별도 처리
- API 응답시간 10배 단축

#### 3. 2단계 큐 시스템
- **Kafka**: 대기 중 (무제한, FIFO 보장)
- **Redis**: 활성 큐 (5천명, 실시간 순번)

#### 4. 재고 관리
- **Redis Lua Script**: 원자적 차감
- **Kafka Event**: 비동기 DB 동기화

---

## Technical Decisions

### Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | 1.9 |
| Framework | Spring Boot + WebFlux | 3.2 |
| Cache | Redis | 7 (Cluster) |
| Message Queue | Kafka | 3.6 |
| Database | PostgreSQL | 15 |
| Load Testing | k6 | Latest |
| Unit Testing | JUnit 5 + Kotest | Latest |

### 시작 구성 (최소)

```yaml
API: t2.micro × 1
Redis: t3.micro × 1
Kafka: t3.small × 1
PostgreSQL: t3.micro × 1
```

**비용**: ~$30/월
**처리량**: 500 TPS, 2,500 동시접속

### 확장 전략

1. **API**: Auto Scaling (인스턴스 추가)
2. **Redis**: Cluster (Shard 추가)
3. **Kafka**: Partition 추가
4. **DB**: Read Replica → Sharding (필요 시)

---

## API Contracts

### 1. 대기열 진입
```http
POST /api/v1/queue/enter
Content-Type: application/json

Request:
{
  "eventId": "string",
  "userId": "string"
}

Response: 200 OK
{
  "token": "string",
  "position": 1523,
  "estimatedWaitSeconds": 180
}
```

### 2. 티켓 예약
```http
POST /api/v1/tickets/reserve
X-Queue-Token: {token}
Content-Type: application/json

Request:
{
  "eventId": "string",
  "quantity": 2
}

Response: 200 OK
{
  "reservationId": "string",
  "status": "PENDING"
}
```

### 3. 실시간 순번 (SSE)
```http
GET /api/v1/queue/stream?token={token}

Event Stream:
event: position
data: {"position": 1420, "estimatedWaitSeconds": 165}

event: position
data: {"position": 1350, "estimatedWaitSeconds": 150}
```

---

## Validation Criteria

### 확장성 검증
```
목표: 인스턴스 2배 → TPS 2배 (Efficiency > 90%)

테스트:
1. Baseline: 1 instance → 500 TPS 측정
2. Scale: 2 instances → 1000 TPS 검증
3. Scale: 4 instances → 2000 TPS 검증
```

### 정확성 검증
```
시나리오:
동시 200명이 100개 재고에 1개씩 요청
→ 정확히 100명만 성공, 재고 0

검증:
- DB 재고 = 0
- 성공 예약 수 = 100
- 실패 예약 수 = 100
```

---
## Implementation Phases

### Week 1-2: 핵심 구현 (TDD)
- [ ] Stateless API (대기열, 재고)
- [ ] Redis Lua Script
- [ ] Kafka Producer/Consumer

### Week 3: 부하 테스트
- [ ] k6 시나리오 작성
- [ ] 확장성 검증 (1→2→4 instances)
- [ ] 병목 지점 파악

### Week 4: 프론트엔드 + 문서화
- [ ] 대기 페이지 (SSE)
- [ ] 관리자 대시보드
- [ ] 성능 테스트 결과 문서화

---


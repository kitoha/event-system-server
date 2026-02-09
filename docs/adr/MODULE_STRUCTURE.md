# 모듈 구성 가이드

## Overview

선착순 티켓팅 시스템은 **멀티 모듈** 구조로 설계하여 각 모듈의 책임을 명확히 분리하고, 독립적인 개발/테스트/배포를 가능하게 합니다.

```
event-ticket-server/
├── api/                    # API Gateway 모듈
├── domain/                 # 도메인 모델 (공유)
├── queue/                  # 대기열 처리 모듈
├── ticket/                 # 티켓 예약 모듈
├── event-consumer/         # Kafka Consumer 모듈
└── common/                 # 공통 유틸리티
```

---

## 1. Module Architecture

### Dependency Graph

```
api ───────┬─────> queue ──────┬─────> domain
           │                   │
           └─────> ticket ─────┤
                                │
event-consumer ────────────────┘
                                │
common ─────────────────────────┘
```

### Module Responsibilities

| Module | Responsibility | Technology |
|--------|---------------|-----------|
| `api` | HTTP 엔드포인트, 라우팅 | WebFlux, Controller |
| `domain` | 비즈니스 모델, 엔티티 | JPA, Value Objects |
| `queue` | 대기열 로직, Redis 연동 | Redis, Lettuce |
| `ticket` | 티켓 예약, 재고 관리 | Redis Lua, JPA |
| `event-consumer` | Kafka 이벤트 처리 | Kafka Consumer |
| `common` | 공통 설정, 유틸 | Config, Extensions |

---

## 2. Detailed Module Design

### 2.1 `api` Module

**역할**: HTTP API Gateway

---

### 2.2 `domain` Module

**역할**: 공유 도메인 모델 (모든 모듈에서 참조)

---

### 2.3 `queue` Module

**역할**: 대기열 관리 (Redis 기반)

**Key Features**:
- Redis Sorted Set으로 순번 관리
- JWT 기반 Queue Token 발급
- SSE (Server-Sent Events) 실시간 순번 업데이트

---

### 2.4 `ticket` Module

**역할**: 티켓 예약 및 재고 관리

**Key Features**:
- Redis Lua Script로 재고 원자적 차감
- Kafka로 예약 이벤트 발행 (비동기)
- Optimistic Locking (DB 동기화 시)

---

### 2.5 `event-consumer` Module

**역할**: Kafka 이벤트 처리 (독립 실행 가능)

**Key Features**:
- 비동기 DB 저장 (PostgreSQL)
- 멱등성 보장 (Event ID 중복 체크)
- Dead Letter Queue (DLQ) 처리

---

### 2.6 `common` Module

**역할**: 공통 설정 및 유틸리티

---

## 3. Gradle Multi-Module Setup

### Root `settings.gradle.kts`

```kotlin
rootProject.name = "event-ticket-server"

include(
    "api",
    "domain",
    "queue",
    "ticket",
    "event-consumer",
    "common"
)
```

## 4. Package Structure (Per Module)

각 모듈은 **Layered Architecture**를 따릅니다:

```
src/main/kotlin/com/ticketing/{module}/
├── controller/         # (api 모듈만)
├── service/           # 비즈니스 로직
├── repository/        # 데이터 액세스
├── config/            # 설정
├── dto/               # 데이터 전송 객체
├── exception/         # 예외 정의
└── util/              # 유틸리티
```

## 5. Benefits of This Structure

✅ **명확한 책임 분리**: 각 모듈이 독립적인 도메인 책임
✅ **독립적인 배포**: API와 Consumer를 별도 배포 가능
✅ **병렬 개발**: 팀원이 서로 다른 모듈 작업 가능
✅ **테스트 격리**: 모듈별 단위 테스트 용이
✅ **재사용성**: `domain`, `common` 모듈 공유

---

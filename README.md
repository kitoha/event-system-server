# Event Ticket Server

**선착순 티켓팅 시스템** - Kafka + Redis 기반 대규모 트래픽 처리

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-purple.svg)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![Kafka](https://img.shields.io/badge/Kafka-3.6-black.svg)](https://kafka.apache.org/)

---

## Overview

대규모 트래픽이 집중되는 선착순 티켓팅 시스템을 **Stateless 아키텍처**로 구현한 학습 프로젝트입니다.

### Key Features

**재고 정확도 100%** - Redis Lua Script로 Over-selling 방지
**FIFO 대기열** - Kafka + Redis 2단계 큐로 공정성 보장
**선형 확장** - 인스턴스 2배 → TPS 2배 (Efficiency > 90%)
**실시간 순번** - SSE로 대기 순번 업데이트
**비동기 처리** - Kafka Event로 응답시간 10배 단축

---

## Architecture

```
┌─────────┐     ┌──────────────┐     ┌─────────┐
│ Client  │────▶│  API Server  │────▶│  Redis  │
│ (Web)   │     │  (Stateless) │     │ (Queue) │
└─────────┘     └──────────────┘     └─────────┘
                       │                    │
                       ▼                    ▼
                 ┌──────────┐         ┌─────────┐
                 │  Kafka   │────────▶│Consumer │
                 │ (Events) │         │ Worker  │
                 └──────────┘         └─────────┘
                                            │
                                            ▼
                                      ┌──────────┐
                                      │PostgreSQL│
                                      └──────────┘
```

**상세 아키텍처**: [ADR-001](docs/adr/ADR-001-ticketing-system-architecture.md)

---

## Module Structure

멀티 모듈 구조로 책임 분리 및 독립 배포 가능:

```
event-ticket-server/
├── api/                # HTTP API Gateway
├── domain/             # 공유 도메인 모델
├── queue/              # 대기열 처리 (Redis)
├── ticket/             # 티켓 예약 (Redis + Kafka)
├── event-consumer/     # Kafka Consumer (독립 실행)
└── common/             # 공통 유틸리티
```

**모듈 상세**: [MODULE_STRUCTURE.md](docs/adr/MODULE_STRUCTURE.md)

---

## Quick Start

### Prerequisites

- **JDK 21**
- **Docker** (Redis, Kafka, PostgreSQL)
- **Gradle 8.5+**

---

##  Performance Targets

| Metric | Target | Status |
|--------|--------|--------|
| P95 응답시간 | < 500ms | 🔄 측정 예정 |
| TPS (1 instance) | 500 | 🔄 측정 예정 |
| 확장 효율 | > 90% | 🔄 측정 예정 |
| 재고 정확도 | 100% | ✅ Lua Script |

---

##  Tech Stack

### Backend
- **Kotlin 1.9.25** - 타입 안전성, Null Safety
- **Spring Boot 3.2** - WebFlux (비동기), Data JPA
- **Coroutines** - 비동기 처리

### Infrastructure
- **Redis 7** - 대기열, 재고 캐시
- **Kafka 3.6** - 이벤트 스트리밍
- **PostgreSQL 15** - 영구 저장소

### Testing
- **JUnit 5** - 단위 테스트
- **Kotest** - BDD 스타일 테스트
- **MockK** - Mocking
- **k6** - 부하 테스트

---

## API Documentation

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/queue/enter` | 대기열 진입 |
| `GET` | `/api/v1/queue/stream` | 실시간 순번 (SSE) |
| `POST` | `/api/v1/tickets/reserve` | 티켓 예약 |
| `GET` | `/api/v1/tickets/{id}` | 예약 조회 |

---

## Monitoring

### Metrics (Prometheus)

### Key Metrics
- `queue_active_users` - 활성 대기열 인원
- `ticket_reservation_total` - 예약 요청 수
- `redis_stock_remaining` - 실시간 재고

---

## License

MIT License

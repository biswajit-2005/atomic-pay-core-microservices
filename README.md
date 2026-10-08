# Reliable Distributed Financial Transactions Platform (Atomic-Pay)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-3.7-black.svg)](https://kafka.apache.org/)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)

**Atomic-Pay** is a production-designed distributed digital banking and wallet platform built using **Java 21**, **Spring Boot 3.3**, **PostgreSQL 16**, **Redis**, **Apache Kafka**, **Prometheus/Grafana**, and **React**. 

The system guarantees financial transaction correctness, strict idempotency, double-entry ledger balancing, distributed Saga failure recovery, transactional outbox publishing, and consumer deduplication under concurrent and failure-prone environments.

> *"A financial transaction is not considered reliable merely because the normal request succeeds. The system must remain correct when requests are duplicated, operations are concurrent, services fail, messages are delayed or duplicated, and a transaction's final outcome becomes temporarily uncertain."*

---

## Key System Principles & Architecture

1. **Exact Monetary Precision (`NUMERIC(19,4)`)**: No floating-point types (`float`/`double`) are permitted for currency values. All balance operations utilize PostgreSQL `NUMERIC(19,4)` and Java `BigDecimal`.
2. **Immutable Double-Entry Ledger**: Every successful transfer inserts paired `DEBIT` and `CREDIT` entries in the `ledger.ledger_entries` table balancing to exactly zero.
3. **Database Concurrency & Row-Level Locking**: Account balance mutations employ PostgreSQL `SELECT ... FOR UPDATE` pessimistic locking in deterministic Account ID order (`min(id), max(id)`), completely eliminating deadlocks, race conditions, and negative balances.
4. **Dual-Layer Idempotency Engine**: Idempotency is enforced using Redis fast-path caching alongside PostgreSQL `UNIQUE(user_id, idempotency_key)` and request payload SHA-256 hash checks. Duplicate requests return cached responses without re-executing state changes.
5. **Distributed Saga Engine with Backward Compensation**: Multi-step transfer workflows (`VALIDATE` -> `DEBIT` -> `CREDIT` -> `LEDGER`) automatically execute compensating backward debits/credits if downstream operations fail, maintaining system consistency.
6. **Transactional Outbox Pattern**: Events are persisted to PostgreSQL `transactions.outbox_events` within the same database transaction as financial mutations. A dedicated publisher worker polls and delivers events to Kafka, eliminating dual-write loss.
7. **Idempotent Event Consumers (Inbox Pattern)**: Consumers (`notification-service`, `audit-service`) record message execution in `processed_events(consumer_name, event_id)` inside local database transactions to ignore duplicate Kafka deliveries.
8. **Automated Reconciliation Engine**: Background worker periodically evaluates ledger entries vs account balances, flags discrepancies, and enables automated/manual audit resolution.
9. **Observability & Distributed Tracing**: Micrometer metrics (`transactions_total`, `idempotency_hits_total`, `saga_compensation_total`, latency percentiles P50/P95/P99) scraped by Prometheus and visualized via pre-built Grafana dashboards, with `X-Correlation-ID` MDC trace context.

---

## Monorepo Architecture

```text
                    ┌─────────────────────────┐
                    │  React Admin Dashboard  │
                    └────────────┬────────────┘
                                 │ HTTP/JSON
                                 ▼
                    ┌─────────────────────────┐
                    │   Spring API Gateway    │
                    └────────────┬────────────┘
                                 │ JWT / Correlation-ID
             ┌───────────────────┼───────────────────┐
             ▼                   ▼                   ▼
    ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
    │  Auth Service   │ │Transaction Engine│ │ Account Service │
    └────────┬────────┘ └────────┬────────┘ └────────┬────────┘
             │                   │                   │
             └───────────────────┼───────────────────┘
                                 ▼
                     ┌───────────────────────┐
                     │     PostgreSQL 16     │
                     │ auth/accounts/ledger/ │
                     │ transactions/audit    │
                     └───────────┬───────────┘
                                 │ Outbox Publisher
                                 ▼
                     ┌───────────────────────┐
                     │     Apache Kafka      │
                     └───────────┬───────────┘
                                 │
             ┌───────────────────┼───────────────────┐
             ▼                   ▼                   ▼
    ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
    │  Notification   │ │  Audit Service  │ │Analytics Service│
    └─────────────────┘ └─────────────────┘ └─────────────────┘
```

---

## Microservices Architecture & Directory Layout

```text
Atomic-pay/
├── docker-compose.yml              # PostgreSQL, Redis, Kafka, Prometheus, Grafana
├── pom.xml                         # Maven Parent Reactor POM
├── RELIABLE_FINANCIAL_TRANSACTIONS_AI_SPEC.md # Full Project Spec
├── docs/                           # Documentation
│   ├── architecture.md             # System Architecture & Diagrams
│   ├── database.md                 # Database Schemas & ER Diagrams
│   ├── transaction-flow.md         # Transaction State Machine & Sequence Diagrams
│   ├── failure-recovery.md         # Saga Compensation & Reconciliation Flows
│   ├── api.md                      # OpenAPI REST Specification
│   └── demo-script.md              # Interactive Live Demo Script
├── gateway/                        # Spring Cloud API Gateway (Port 8080)
├── services/
│   ├── auth-service/               # JWT Auth, User Management, KYC (Port 8081)
│   ├── account-service/            # Accounts & Balance Operations (Port 8082)
│   ├── transaction-service/        # Core Transfer Engine, Saga, Outbox (Port 8083)
│   ├── ledger-service/             # Double-Entry Bookkeeping (Port 8084)
│   ├── notification-service/       # Deduplicated Notification Consumer (Port 8085)
│   ├── audit-service/              # Deduplicated System Audit Logger (Port 8086)
│   └── analytics-service/          # Event Stream Analytics (Port 8087)
├── frontend/                       # Modern React Dashboard & Admin Portal (Port 5173)
└── infrastructure/
    ├── postgres/                   # Init SQL Schemas
    ├── kafka/                      # Topic configs
    ├── prometheus/                 # Scraping configuration
    └── grafana/                    # Provisioned Dashboards & Data Sources
```

---

## Quick Start Guide

### 1. Start Infrastructure Containers
Launch PostgreSQL 16, Redis 7, Kafka 3.7 (KRaft mode), Prometheus, and Grafana:
```bash
docker compose up -d
```

Verify running containers:
```bash
docker compose ps
```

### 2. Build Microservices Monorepo
Execute Maven reactor build across all 9 Java microservices:
```bash
./mvnw clean compile
```

Run unit & integration test suites:
```bash
./mvnw test "-Dnet.bytebuddy.experimental=true"
```

### 3. Launch React Frontend Dashboard
```bash
cd frontend
npm install
npm run dev
```
Access dashboard UI at `http://localhost:5173`.

---

## API Endpoints Summary

- **Auth**: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`
- **Accounts**: `POST /api/v1/accounts`, `GET /api/v1/accounts`, `GET /api/v1/accounts/{accountId}/balance`
- **Transfers**: `POST /api/v1/transfers` (Requires `Idempotency-Key` header), `GET /api/v1/transfers/{id}`
- **Reconciliation**: `POST /api/v1/admin/reconciliation/run`, `GET /api/v1/admin/reconciliation/issues`
- **Metrics**: `GET /actuator/prometheus`

See [`docs/api.md`](file:///c:/Users/biswa/WEB_DEV_PROJECTS/Atomic-pay/docs/api.md) for complete request/response schemas.

---

---

## License
[MIT License](LICENSE)

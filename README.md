# Reliable Distributed Financial Transactions Platform (Atomic-Pay)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.7-black.svg)](https://kafka.apache.org/)

A resume-grade, production-designed distributed digital banking and wallet backend built with Java 21, Spring Boot 3.x, PostgreSQL, Redis, Apache Kafka, and Docker.

---

## Key System Principles

- **No Floating-Point Money**: All monetary amounts are stored as `NUMERIC(19,4)` in PostgreSQL and handled as `BigDecimal` in Java.
- **Double-Entry Ledger**: Immutable append-only debits and credits balancing to zero for every completed transaction.
- **PostgreSQL Source of Truth**: Balances and financial state reside safely in PostgreSQL. Redis is used solely for idempotency fast-path, rate limiting, and short-term locks.
- **Guaranteed Idempotency**: Dual-layer protection using Redis fast-path and PostgreSQL `UNIQUE(sender_account_id, idempotency_key)` constraints.
- **Transactional Outbox Pattern**: Prevents DB/Kafka dual-write anomalies by writing domain events to `outbox_events` in the same database transaction.
- **Idempotent Kafka Consumers**: Inbox pattern with `processed_events(consumer_name, event_id)` preventing duplicate event side-effects.

---

## System Architecture

```
                    ┌──────────────────┐
                    │   React Client   │
                    └────────┬─────────┘
                             │ HTTPS
                             ▼
                    ┌──────────────────┐
                    │   API Gateway    │
                    │ Spring Gateway   │
                    └────────┬─────────┘
                             │
             ┌───────────────┼────────────────┐
             ▼               ▼                ▼
     ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
     │ Auth Service │ │Transaction   │ │Account       │
     │              │ │Service       │ │Service       │
     └──────────────┘ └──────┬───────┘ └──────┬───────┘
                             │                │
                             ▼                ▼
                      ┌────────────────────────────┐
                      │        PostgreSQL          │
                      │ users/accounts/ledger/...  │
                      └────────────────────────────┘
                             ▲
                             │
                    ┌────────┴────────┐
                    │      Redis      │
                    │ idempotency /   │
                    │ locks / cache   │
                    └─────────────────┘

                     Transaction Service
                             │
                             ▼
                    ┌─────────────────┐
                    │      Kafka      │
                    └───────┬─────────┘
                            │
             ┌──────────────┼──────────────┐
             ▼              ▼              ▼
       Notification     Audit Service   Analytics
          Service

                 ┌───────────────────────┐
                 │ Prometheus + Grafana  │
                 └───────────────────────┘
```

---

## Monorepo Project Structure

```text
reliable-financial-transactions/
├── README.md
├── LICENSE
├── docker-compose.yml
├── .env.example
├── .gitignore
├── pom.xml
│
├── docs/
│   ├── architecture.md
│   ├── database.md
│   ├── transaction-flow.md
│   ├── failure-recovery.md
│   ├── api.md
│   └── adr/
│
├── gateway/
│
├── services/
│   ├── auth-service/
│   ├── account-service/
│   ├── transaction-service/
│   ├── ledger-service/
│   ├── notification-service/
│   ├── audit-service/
│   └── analytics-service/
│
├── frontend/
│
├── infrastructure/
│   ├── postgres/
│   ├── kafka/
│   ├── redis/
│   ├── prometheus/
│   └── grafana/
│
└── scripts/
```

---

## Quick Start (Phase 1 Infrastructure)

### 1. Launch Docker Infrastructure
Spin up PostgreSQL 16, Redis 7, Kafka 3.7 (KRaft mode), Prometheus, and Grafana:

```bash
docker compose up -d
```

### 2. Verify Infrastructure Status
Check container status:

```bash
docker compose ps
```

Verify PostgreSQL schema initialization:

```bash
docker exec -it atomic_pay_postgres psql -U postgres -d atomic_pay -c "\dn"
```

Expected schemas: `auth`, `accounts`, `transactions`, `ledger`, `audit`, `notifications`, `analytics`.

### 3. Build Java Backend Microservices
Ensure Java 21+ is installed, then execute:

```bash
./mvnw clean package -DskipTests
```

---

## Phase 1 Delivery Report

- **Files Created**: `docker-compose.yml`, `.env.example`, `.gitignore`, `pom.xml`, `gateway/`, `services/*` (pom, application.yml, migrations, Spring Boot main applications), `infrastructure/*`, `docs/*`, `frontend/*`, `scripts/*`.
- **Database Schemas Initialized**: `auth`, `accounts`, `transactions`, `ledger`, `audit`, `notifications`, `analytics`.
- **Flyway Migrations Configured**: V1 migration scripts for each isolated microservice schema.
- **Observability Setup**: Prometheus scraping targets configured alongside Grafana auto-provisioned dashboards.
- **Verification**: All microservice POMs validate and compile clean under parent Maven build.

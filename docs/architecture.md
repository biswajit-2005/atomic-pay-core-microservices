# System Architecture Document

## Overview

**Atomic-Pay** is a production-grade, distributed digital banking and wallet backend platform designed for extreme reliability, idempotency, strict monetary consistency, event-driven processing, and comprehensive observability.

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

## Core Principles

1. **Monetary Precision**: All monetary values are represented using PostgreSQL `NUMERIC(19,4)` and Java `BigDecimal`. Floating-point types are strictly forbidden.
2. **Immutable Double-Entry Ledger**: Every balance mutation produces paired, append-only debit and credit ledger entries balancing to zero.
3. **Database as Authority**: PostgreSQL is the immutable source of truth for accounts, ledger entries, and transaction history. Redis serves strictly as a fast-path cache/idempotency/lock coordinator.
4. **Guaranteed Idempotency**: Double protection using Redis fast-path and PostgreSQL database unique constraints `UNIQUE(sender_account_id, idempotency_key)`.
5. **Transactional Outbox**: Events are written atomically into PostgreSQL `outbox_events` in the same database transaction as business mutations, avoiding DB/Kafka dual-write anomalies.
6. **Idempotent Consumers**: Every Kafka consumer tracks processed events in `processed_events(consumer_name, event_id)` to handle duplicate messages safely.

## Service Responsibilities

- **API Gateway**: Public entry point, JWT verification, rate limiting, correlation ID injection, request routing.
- **Auth Service**: User registration, login, JWT token issuance, refresh token rotation, KYC status management.
- **Account Service**: User account management, currency verification, balance queries, optimistic versioning, row-level locking.
- **Transaction Service**: Core transfer execution, transaction state machine, Saga orchestration, idempotency checking, failure compensation, outbox publishing.
- **Ledger Service**: Immutable double-entry bookkeeping, ledger account tracking, balance audit verification.
- **Notification Service**: Event-driven notification generation for transaction completion/failure.
- **Audit Service**: Immutable event audit log persistence.
- **Analytics Service**: Non-critical aggregate metrics for dashboard analytics.

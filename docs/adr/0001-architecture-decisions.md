# ADR 0001: Monorepo and PostgreSQL Multi-Schema Architecture

## Context
The system requires microservice separation of concerns across Auth, Accounts, Transactions, Ledger, Notifications, Audit, and Analytics while maintaining low local development overhead and high build reproducibility.

## Decision
1. Use a **Monorepo** structure containing Maven sub-modules for Java backend services and a React frontend.
2. Use a **Single PostgreSQL Instance with Schema-Per-Service** (`auth`, `accounts`, `transactions`, `ledger`, `audit`, `notifications`, `analytics`).

## Consequences
- **Pros**: Easy local spin-up with single Docker Compose PostgreSQL container while preserving database boundary isolation. Flyway manages schema migrations per service independently.
- **Cons**: Services share physical PostgreSQL compute resources in local dev. Can be split into standalone databases in production without changing application domain models.

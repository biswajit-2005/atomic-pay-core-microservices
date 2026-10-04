# Reliable Distributed Financial Transactions System --- AI Coding Specification

## 0. Purpose

Build a **resume-grade distributed digital banking / wallet backend**
based on the uploaded seminar design, but implement it as a real working
software project rather than only a conceptual prototype.

The source design focuses on: - idempotent financial transactions -
transaction state machines - Saga-based distributed processing and
compensation - concurrency control - event-driven communication -
reconciliation - auditability - Redis for fast coordination - PostgreSQL
for durable financial data - Kafka for asynchronous events - Docker and
observability

The seminar describes a React client, API Gateway, Transaction Service,
Account Service, Ledger Service, Kafka, Redis, PostgreSQL,
Notification/Audit/Analytics services, and Prometheus/Grafana. It also
explicitly discusses duplicate requests, concurrent transfers, partial
failures, pending transactions and reconciliation.

**Important:** The database schema in the seminar is only a starting
point. Replace it with the normalized, auditable schema specified below.

------------------------------------------------------------------------

# 1. Non-Negotiable Engineering Rules

1.  Never use floating-point types for money.
2.  Store monetary amounts as `NUMERIC(19,4)` in PostgreSQL.
3.  Every financial mutation must be represented by immutable ledger
    entries.
4.  Never solve financial consistency using Redis alone.
5.  PostgreSQL is the source of truth for balances and ledger state.
6.  Redis is only a coordination/cache/idempotency layer.
7.  Every externally retried write request must support idempotency.
8.  Idempotency must be enforced by a database unique constraint as the
    final protection, not only by Redis.
9.  Never publish Kafka events directly after a database transaction if
    that can create a DB/Kafka dual-write problem. Use the Transactional
    Outbox pattern.
10. Consumers must be idempotent.
11. Use pessimistic row locking or an equivalent safe concurrency
    strategy for account balance mutations.
12. Ledger entries are append-only. Do not update or delete financial
    ledger entries.
13. Financial operations must be auditable.
14. Do not expose internal service endpoints publicly.
15. Use UTC timestamps everywhere.
16. Do not store raw passwords, PINs, JWT secrets, Kafka credentials,
    database passwords, or API keys in source control.
17. Every service must have health/readiness endpoints.
18. Failures must produce deterministic transaction states.
19. Never silently swallow exceptions in financial workflows.
20. Write tests for duplicate requests, concurrent transfers,
    insufficient balance, partial failure and recovery.

------------------------------------------------------------------------

# 2. Recommended Tech Stack

## Backend

-   Java 21
-   Spring Boot 3.x
-   Spring Web
-   Spring Data JPA
-   Hibernate
-   Spring Security
-   JWT
-   Bean Validation
-   Spring Cloud Gateway
-   Spring Kafka
-   PostgreSQL
-   Flyway
-   Redis
-   Docker / Docker Compose
-   Maven

## Frontend

-   React
-   TypeScript
-   Vite
-   React Router
-   Axios
-   TanStack Query
-   Tailwind CSS or a clean component system

## Messaging

-   Apache Kafka
-   Kafka topics with explicit event contracts

## Observability

-   Micrometer
-   Prometheus
-   Grafana
-   structured JSON logging
-   correlation/request IDs

## Testing

-   JUnit 5
-   Mockito
-   Spring Boot Test
-   Testcontainers
-   PostgreSQL Testcontainer
-   Kafka Testcontainer
-   Redis Testcontainer

------------------------------------------------------------------------

# 3. Architecture

Implement the following services:

``` text
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

## Service responsibilities

### API Gateway

Responsibilities: - single public entry point - JWT validation - request
correlation ID - routing - rate limiting - CORS - request logging - hide
internal service URLs

Do not put financial business logic in the gateway.

### Auth Service

Responsibilities: - registration - login - JWT access token - refresh
token rotation - password hashing - account status - authorization
roles - optional KYC status management

### Account Service

Responsibilities: - create wallet/account - account details - available
balance - debit/credit operations - account status - concurrency control

### Transaction Service

Responsibilities: - create transfer - idempotency - transaction state
machine - Saga orchestration - transaction steps - failure handling -
compensation - transaction status - publish domain events through outbox

### Ledger Service

Responsibilities: - immutable double-entry ledger - debit/credit
entries - ledger queries - transaction-to-ledger traceability - balance
verification

### Notification Service

Consumes Kafka events and creates user notifications.

### Audit Service

Consumes Kafka events and creates immutable audit records.

### Analytics Service

Consumes Kafka events and maintains non-critical aggregates/statistics.

------------------------------------------------------------------------

# 4. Database Design

Use PostgreSQL.

For the MVP, all services may use one PostgreSQL instance with
**separate schemas**:

``` text
auth
accounts
transactions
ledger
audit
notifications
```

This keeps local development manageable while preserving service
boundaries.

If the project later needs true database-per-service deployment, the
schemas can be split into separate PostgreSQL databases without
redesigning the domain model.

------------------------------------------------------------------------

# 5. Database Schema

## 5.1 users

``` sql
users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
)
```

Roles:

``` text
USER
ADMIN
AUDITOR
```

Statuses:

``` text
ACTIVE
SUSPENDED
LOCKED
CLOSED
```

------------------------------------------------------------------------

# 6. Refresh Tokens

``` sql
refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    token_hash TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
)
```

Never store the raw refresh token.

------------------------------------------------------------------------

# 7. KYC

``` sql
kyc_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    document_type VARCHAR(50),
    document_hash TEXT,
    status VARCHAR(30) NOT NULL,
    verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
)
```

Statuses:

``` text
NOT_SUBMITTED
PENDING
VERIFIED
REJECTED
```

For the project demo, do not store real identity documents.

------------------------------------------------------------------------

# 8. Financial Accounts

Use an `accounts` table rather than making the wallet itself the ledger.

``` sql
accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    account_number VARCHAR(32) UNIQUE NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    account_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    current_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
)
```

Constraints:

``` text
current_balance >= 0
currency = INR for MVP
```

`current_balance` is a fast-access balance cache/current balance, while
the ledger remains the authoritative audit trail.

Use optimistic versioning and/or row-level locking for mutations.

------------------------------------------------------------------------

# 9. Beneficiaries

Optional but useful for a realistic wallet application.

``` sql
beneficiaries (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL REFERENCES users(id),
    beneficiary_account_id UUID NOT NULL REFERENCES accounts(id),
    nickname VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE(owner_user_id, beneficiary_account_id)
)
```

------------------------------------------------------------------------

# 10. Financial Transactions

This is the central transaction table.

``` sql
transactions (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL,
    sender_account_id UUID NOT NULL REFERENCES accounts(id),
    receiver_account_id UUID NOT NULL REFERENCES accounts(id),
    amount NUMERIC(19,4) NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(40) NOT NULL,
    failure_code VARCHAR(80),
    failure_reason TEXT,
    correlation_id VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_transaction_idempotency
        UNIQUE(sender_account_id, idempotency_key),

    CONSTRAINT chk_amount_positive
        CHECK(amount > 0),

    CONSTRAINT chk_different_accounts
        CHECK(sender_account_id <> receiver_account_id)
)
```

Transaction states:

``` text
INITIATED
VALIDATING
PROCESSING
DEBITED
CREDITED
COMPLETED
PENDING
COMPENSATING
COMPENSATED
FAILED
RECONCILIATION_REQUIRED
```

Do not reduce everything to SUCCESS/FAILED. The seminar explicitly
identifies the need for clearer intermediate states and reconciliation
states.

------------------------------------------------------------------------

# 11. Transaction Steps

Use a separate table to make Saga execution traceable.

``` sql
transaction_steps (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES transactions(id),
    step_order INT NOT NULL,
    step_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    error_code VARCHAR(80),
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL,

    UNIQUE(transaction_id, step_order)
)
```

Example steps:

``` text
VALIDATE
DEBIT
CREDIT
LEDGER
COMPLETE
COMPENSATE_DEBIT
```

------------------------------------------------------------------------

# 12. Double-Entry Ledger

This is a major improvement over a simple debit/credit table.

``` sql
ledger_accounts (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL UNIQUE REFERENCES accounts(id),
    created_at TIMESTAMPTZ NOT NULL
)
```

Ledger entries:

``` sql
ledger_entries (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES transactions(id),
    ledger_account_id UUID NOT NULL REFERENCES ledger_accounts(id),
    entry_type VARCHAR(10) NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    currency CHAR(3) NOT NULL,
    sequence_no INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_entry_type
        CHECK(entry_type IN ('DEBIT', 'CREDIT')),

    CONSTRAINT chk_entry_amount
        CHECK(amount > 0),

    UNIQUE(transaction_id, sequence_no)
)
```

For every completed transfer:

``` text
Sender account     DEBIT   ₹1000
Receiver account   CREDIT  ₹1000
```

The total debit and total credit for a transaction must balance.

Never delete ledger entries.

------------------------------------------------------------------------

# 13. Idempotency Records

Persist idempotency information.

``` sql
idempotency_records (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    idempotency_key VARCHAR(255) NOT NULL,
    request_hash CHAR(64) NOT NULL,
    transaction_id UUID REFERENCES transactions(id),
    response_status INT,
    response_body JSONB,
    status VARCHAR(30) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    UNIQUE(user_id, idempotency_key)
)
```

Important behavior:

### Same key + same request

Return the original transaction/result.

### Same key + different request body

Return:

``` text
409 IDEMPOTENCY_KEY_REUSED
```

This prevents a client from accidentally reusing an idempotency key for
a different transaction.

Redis may accelerate this lookup, but PostgreSQL remains the durable
authority.

------------------------------------------------------------------------

# 14. Outbox Pattern

Use the Transactional Outbox pattern.

``` sql
outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ
)
```

Flow:

``` text
DB transaction
   |
   +-- update financial state
   |
   +-- insert outbox event
   |
   COMMIT
       |
       ▼
Outbox Publisher
       |
       ▼
Kafka
```

This prevents:

``` text
DB COMMIT succeeded
Kafka publish failed
```

from silently losing the event.

------------------------------------------------------------------------

# 15. Inbox / Consumer Deduplication

Consumers must also be idempotent.

``` sql
processed_events (
    consumer_name VARCHAR(100) NOT NULL,
    event_id UUID NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY(consumer_name, event_id)
)
```

Before processing an event:

``` text
if event already processed:
    ignore safely
else:
    process
    record event ID
```

------------------------------------------------------------------------

# 16. Audit Logs

``` sql
audit_logs (
    id UUID PRIMARY KEY,
    event_id UUID,
    actor_user_id UUID,
    transaction_id UUID,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(80),
    entity_id UUID,
    metadata JSONB,
    ip_address INET,
    user_agent TEXT,
    correlation_id VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL
)
```

Audit logs should be append-only.

------------------------------------------------------------------------

# 17. Notifications

``` sql
notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    transaction_id UUID REFERENCES transactions(id),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
)
```

------------------------------------------------------------------------

# 18. Reconciliation

Create explicit reconciliation tables.

``` sql
reconciliation_runs (
    id UUID PRIMARY KEY,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    status VARCHAR(30) NOT NULL,
    total_transactions INT DEFAULT 0,
    mismatches INT DEFAULT 0
)
```

``` sql
reconciliation_issues (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES reconciliation_runs(id),
    transaction_id UUID REFERENCES transactions(id),
    account_id UUID REFERENCES accounts(id),
    issue_type VARCHAR(80) NOT NULL,
    expected_value NUMERIC(19,4),
    actual_value NUMERIC(19,4),
    status VARCHAR(30) NOT NULL,
    resolution TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    resolved_at TIMESTAMPTZ
)
```

Examples:

``` text
LEDGER_BALANCE_MISMATCH
MISSING_LEDGER_ENTRY
DUPLICATE_LEDGER_ENTRY
PENDING_TRANSACTION
ORPHAN_TRANSACTION
```

------------------------------------------------------------------------

# 19. Database Indexes

Create indexes for:

``` text
users(email)

accounts(user_id)
accounts(account_number)

transactions(sender_account_id, created_at)
transactions(receiver_account_id, created_at)
transactions(status, created_at)
transactions(correlation_id)

transaction_steps(transaction_id)

ledger_entries(transaction_id)
ledger_entries(ledger_account_id, created_at)

audit_logs(transaction_id)
audit_logs(actor_user_id, created_at)

notifications(user_id, read_at)

outbox_events(status, next_attempt_at)
```

Use `EXPLAIN ANALYZE` when optimizing important queries.

------------------------------------------------------------------------

# 20. Transaction Processing Algorithm

Implement the following high-level flow:

``` text
POST /api/v1/transfers
        |
        ▼
Validate JWT
        |
        ▼
Validate Idempotency-Key
        |
        ▼
Check idempotency record
        |
   ┌────┴────────────┐
   │                 │
Existing          New request
   │                 │
Return old           ▼
result         Create transaction
                     |
                     ▼
               VALIDATING
                     |
                     ▼
            Validate sender/receiver
                     |
                     ▼
                PROCESSING
                     |
                     ▼
        Lock accounts in deterministic
              order to avoid deadlock
                     |
                     ▼
             Check sender balance
                     |
          ┌──────────┴──────────┐
          │                     │
       Insufficient           Enough
          │                     │
          ▼                     ▼
        FAILED               DEBIT
                                |
                                ▼
                              CREDIT
                                |
                                ▼
                         Create ledger entries
                                |
                                ▼
                            COMPLETED
                                |
                                ▼
                    Insert outbox event
                                |
                                ▼
                              COMMIT
                                |
                                ▼
                              Kafka
                                |
                  ┌─────────────┼─────────────┐
                  ▼             ▼             ▼
             Notification     Audit       Analytics
```

------------------------------------------------------------------------

# 21. Concurrency Control

This is one of the most important parts of the project.

For a transfer:

``` text
sender account
receiver account
```

Lock both rows before modifying them.

To avoid deadlocks, always lock accounts in deterministic ID order.

Conceptually:

``` text
lock min(senderId, receiverId)
lock max(senderId, receiverId)
```

Then:

``` text
if sender.balance < amount:
    rollback
else:
    sender.balance -= amount
    receiver.balance += amount
```

The database transaction must atomically commit the balance changes and
corresponding ledger entries.

Write an integration test where many concurrent transfers target the
same sender account.

------------------------------------------------------------------------

# 22. Idempotency Requirements

Example request:

``` http
POST /api/v1/transfers
Authorization: Bearer <jwt>
Idempotency-Key: 7c1e-demo-123
Content-Type: application/json
```

``` json
{
  "receiverAccountId": "uuid",
  "amount": 1000,
  "currency": "INR"
}
```

If the same request is submitted 10 times concurrently:

``` text
Expected:
1 financial transfer
1 transaction ID
1 debit
1 credit
1 ledger pair
10 responses referring to the same transaction/result
```

Never allow:

``` text
10 debits
10 credits
```

------------------------------------------------------------------------

# 23. Redis Usage

Use Redis for:

### Idempotency fast path

``` text
idempotency:{userId}:{key}
```

### Distributed locks where genuinely needed

``` text
lock:account:{accountId}
```

### Rate limiting

``` text
rate-limit:{userId}:{endpoint}
```

### Short-lived transaction/status cache

Do not use Redis as the source of truth for money.

------------------------------------------------------------------------

# 24. Kafka Event Model

Create versioned event contracts.

Example:

``` json
{
  "eventId": "uuid",
  "eventType": "TRANSACTION_COMPLETED",
  "eventVersion": 1,
  "transactionId": "uuid",
  "correlationId": "uuid",
  "occurredAt": "2026-10-04T10:00:00Z",
  "payload": {
    "senderAccountId": "uuid",
    "receiverAccountId": "uuid",
    "amount": 1000,
    "currency": "INR"
  }
}
```

Suggested topics:

``` text
transaction.events
ledger.events
notification.events
audit.events
```

Start with one main transaction topic if that keeps the MVP simpler.

Use consumer groups:

``` text
notification-service
audit-service
analytics-service
```

------------------------------------------------------------------------

# 25. Saga Design

Use an orchestration-based Saga in the Transaction Service.

Saga steps:

``` text
1. Validate
2. Debit sender
3. Credit receiver
4. Create ledger entries
5. Complete transaction
```

If credit fails after debit:

``` text
Transaction:
PROCESSING
    ↓
COMPENSATING
    ↓
reverse debit
    ↓
COMPENSATED / FAILED
```

If the outcome is unknown:

``` text
PROCESSING
    ↓
PENDING
    ↓
RECONCILIATION_REQUIRED
```

Do not blindly compensate an operation if its real outcome is unknown.
First reconcile/query the authoritative state.

------------------------------------------------------------------------

# 26. Transaction State Machine

Implement explicit allowed transitions.

``` text
INITIATED -> VALIDATING
VALIDATING -> PROCESSING
VALIDATING -> FAILED

PROCESSING -> DEBITED
PROCESSING -> PENDING
PROCESSING -> FAILED

DEBITED -> CREDITED
DEBITED -> COMPENSATING
DEBITED -> PENDING

CREDITED -> COMPLETED
CREDITED -> PENDING

COMPENSATING -> COMPENSATED
COMPENSATING -> PENDING

PENDING -> COMPLETED
PENDING -> FAILED
PENDING -> RECONCILIATION_REQUIRED

RECONCILIATION_REQUIRED -> COMPLETED
RECONCILIATION_REQUIRED -> COMPENSATED
RECONCILIATION_REQUIRED -> FAILED
```

Do not allow arbitrary status changes.

Create a `TransactionStateMachine` class or equivalent domain component.

------------------------------------------------------------------------

# 27. REST API

## Auth

``` http
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
```

## Accounts

``` http
POST /api/v1/accounts
GET  /api/v1/accounts
GET  /api/v1/accounts/{accountId}
GET  /api/v1/accounts/{accountId}/balance
```

## Transfers

``` http
POST /api/v1/transfers
GET  /api/v1/transfers/{transactionId}
GET  /api/v1/transfers
```

## Ledger

``` http
GET /api/v1/accounts/{accountId}/ledger
GET /api/v1/transactions/{transactionId}/ledger
```

## Notifications

``` http
GET /api/v1/notifications
PATCH /api/v1/notifications/{id}/read
```

## Admin

``` http
GET /api/v1/admin/transactions
GET /api/v1/admin/reconciliation/issues
POST /api/v1/admin/reconciliation/run
GET /api/v1/admin/audit-logs
```

------------------------------------------------------------------------

# 28. API Response Format

Use a consistent format.

Success:

``` json
{
  "success": true,
  "data": {},
  "timestamp": "2026-10-04T10:00:00Z",
  "requestId": "uuid"
}
```

Error:

``` json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_BALANCE",
    "message": "Insufficient balance",
    "details": {}
  },
  "timestamp": "2026-10-04T10:00:00Z",
  "requestId": "uuid"
}
```

Never leak stack traces to clients.

------------------------------------------------------------------------

# 29. Error Codes

At minimum:

``` text
INVALID_REQUEST
UNAUTHORIZED
FORBIDDEN
ACCOUNT_NOT_FOUND
ACCOUNT_SUSPENDED
INSUFFICIENT_BALANCE
INVALID_AMOUNT
CURRENCY_NOT_SUPPORTED
SELF_TRANSFER_NOT_ALLOWED
IDEMPOTENCY_KEY_REQUIRED
IDEMPOTENCY_KEY_REUSED
TRANSACTION_NOT_FOUND
TRANSACTION_PENDING
CONCURRENT_OPERATION
SAGA_FAILED
RECONCILIATION_REQUIRED
INTERNAL_ERROR
```

------------------------------------------------------------------------

# 30. Security

Implement:

-   BCrypt/Argon2 password hashing
-   JWT access tokens
-   refresh token rotation
-   role-based authorization
-   endpoint authorization
-   request validation
-   rate limiting
-   CORS
-   security headers
-   no sensitive data in logs
-   masked account numbers in UI
-   secrets through environment variables
-   no secrets committed to Git

For demo PIN functionality, hash the PIN. Never store plaintext PINs.

------------------------------------------------------------------------

# 31. Admin Dashboard

Build a simple React admin dashboard.

Show:

``` text
Total Users
Total Accounts
Transactions Today
Successful Transactions
Failed Transactions
Pending Transactions
Reconciliation Issues
Recent Audit Events
```

Transaction table:

``` text
Transaction ID
Sender
Receiver
Amount
Status
Created At
Updated At
```

Add transaction detail page showing:

``` text
Transaction
   |
   +-- state transitions
   +-- Saga steps
   +-- ledger entries
   +-- events
   +-- audit records
```

This will make the distributed-system concepts visible in the demo.

------------------------------------------------------------------------

# 32. User Dashboard

Show:

``` text
Current balance
Account number
Recent transactions
Transfer money
Transaction status
Notifications
Ledger/history
```

Transfer screen:

``` text
Receiver account
Amount
Currency
Idempotency key
Submit
```

The frontend should generate a unique idempotency key for each new
transfer attempt and preserve it when retrying the same request.

------------------------------------------------------------------------

# 33. Observability

Expose:

``` text
/actuator/health
/actuator/prometheus
```

Track metrics such as:

``` text
transactions_total
transactions_completed_total
transactions_failed_total
transactions_pending_total
transaction_processing_seconds
idempotency_hits_total
saga_compensation_total
reconciliation_issues_total
kafka_events_published_total
kafka_events_consumed_total
```

Grafana dashboard should contain:

-   transaction throughput
-   success/failure rate
-   pending transactions
-   transaction latency
-   Kafka consumer lag if available
-   reconciliation issues
-   service health

------------------------------------------------------------------------

# 34. Logging

Use structured logs.

Every transaction-related log should contain:

``` text
timestamp
level
service
requestId
correlationId
transactionId
userId
eventType
message
```

Never log:

``` text
password
JWT
refresh token
PIN
full sensitive identity document
```

------------------------------------------------------------------------

# 35. Failure Simulation

This project must demonstrate failures.

Add a development-only mechanism such as:

``` text
X-Simulate-Failure: CREDIT_SERVICE
```

or an admin-only failure simulation endpoint.

Possible scenarios:

``` text
FAIL_DEBIT
FAIL_CREDIT
FAIL_LEDGER
DELAY_CREDIT
KAFKA_UNAVAILABLE
NOTIFICATION_UNAVAILABLE
```

Do not enable dangerous failure simulation in production configuration.

------------------------------------------------------------------------

# 36. Required Demonstrations

The finished project must demonstrate these scenarios.

## Demo 1 --- Normal transfer

``` text
₹10,000
      |
₹2,000 transfer
      |
Sender = ₹8,000
Receiver += ₹2,000
Transaction = COMPLETED
Ledger balanced
```

## Demo 2 --- Duplicate request

Send the exact same request multiple times.

Expected:

``` text
one transaction
one debit
one credit
one ledger pair
same transaction returned
```

## Demo 3 --- Insufficient balance

Expected:

``` text
transaction = FAILED
no debit
no credit
no ledger mutation
```

## Demo 4 --- Concurrent transfers

Start multiple transfers simultaneously against one account.

Expected:

``` text
no negative balance
no lost updates
ledger matches account state
```

## Demo 5 --- Credit failure

Simulate:

``` text
debit succeeds
credit fails
```

Expected:

``` text
COMPENSATING
     ↓
compensation
     ↓
COMPENSATED / FAILED
```

No money should disappear.

## Demo 6 --- Unknown outcome

Simulate network/service interruption after an uncertain operation.

Expected:

``` text
PENDING
   ↓
RECONCILIATION_REQUIRED
   ↓
reconciliation
   ↓
final state
```

## Demo 7 --- Kafka consumer duplication

Publish the same event twice.

Expected:

``` text
notification/audit is created only once
```

------------------------------------------------------------------------

# 37. Testing Strategy

Minimum required tests:

### Unit tests

-   transaction state transitions
-   idempotency service
-   balance validation
-   ledger balancing
-   Saga compensation
-   authorization
-   validation

### Integration tests

-   PostgreSQL
-   Redis
-   Kafka
-   transaction flow

### Concurrency test

Create a test with:

``` text
100 concurrent transfer requests
```

against an account with a controlled initial balance.

Verify:

``` text
final balance is correct
ledger total is correct
no duplicate transactions
no negative balance
```

### Idempotency stress test

Send:

``` text
20 identical concurrent requests
```

Verify only one financial operation occurs.

Use Testcontainers so the tests execute against real
PostgreSQL/Redis/Kafka containers.

------------------------------------------------------------------------

# 38. Docker Compose

Provide:

``` text
postgres
redis
kafka
zookeeper (only if required by chosen Kafka setup)
prometheus
grafana
api-gateway
auth-service
account-service
transaction-service
ledger-service
notification-service
audit-service
analytics-service
frontend
```

Keep the local developer experience simple:

``` bash
docker compose up -d
```

Then:

``` bash
mvn clean install
```

or use Maven inside Docker if appropriate.

Provide `.env.example`.

Never commit `.env`.

------------------------------------------------------------------------

# 39. Repository Structure

Use a monorepo:

``` text
reliable-financial-transactions/
│
├── README.md
├── LICENSE
├── docker-compose.yml
├── .env.example
├── .gitignore
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

------------------------------------------------------------------------

# 40. Coding Standards

Use:

-   clean architecture principles
-   DTOs instead of exposing JPA entities
-   service layer
-   repository layer
-   domain/business layer where useful
-   global exception handler
-   validation annotations
-   constructor injection
-   immutable event DTOs where practical
-   meaningful names
-   no giant service classes
-   no duplicated business logic
-   database migrations with Flyway
-   API versioning `/api/v1`
-   OpenAPI/Swagger documentation

Avoid:

``` text
field injection
System.out.println
hardcoded secrets
magic numbers
generic Exception everywhere
catch-and-ignore
business logic in controllers
business logic in Kafka listeners
direct entity exposure
```

------------------------------------------------------------------------

# 41. API Documentation

Generate OpenAPI documentation.

Every API must document:

-   request
-   response
-   authentication
-   validation errors
-   possible business errors
-   idempotency behavior
-   example payload

------------------------------------------------------------------------

# 42. Database Migration Strategy

Do not manually create production tables from application startup.

Use Flyway:

``` text
V1__create_users.sql
V2__create_refresh_tokens.sql
V3__create_kyc_profiles.sql
V4__create_accounts.sql
V5__create_transactions.sql
V6__create_transaction_steps.sql
V7__create_ledger.sql
V8__create_idempotency.sql
V9__create_outbox.sql
V10__create_processed_events.sql
V11__create_audit.sql
V12__create_notifications.sql
V13__create_reconciliation.sql
V14__create_indexes.sql
```

Adjust migration ordering if needed.

------------------------------------------------------------------------

# 43. Seed Data

Provide development seed data:

``` text
Admin user
Alice
Bob
Charlie
```

Example:

``` text
Alice account = ₹100,000
Bob account = ₹20,000
Charlie account = ₹5,000
```

Clearly label seed data as development-only.

------------------------------------------------------------------------

# 44. README Requirements

The final README must contain:

1.  Project overview
2.  Architecture diagram
3.  Tech stack
4.  Service responsibilities
5.  Database design
6.  Transaction lifecycle
7.  Idempotency explanation
8.  Saga explanation
9.  Concurrency control explanation
10. Outbox pattern explanation
11. Reconciliation explanation
12. Kafka event flow
13. Redis usage
14. API documentation
15. Local setup
16. Environment variables
17. Running tests
18. Failure simulation
19. Monitoring
20. Screenshots/GIF placeholders
21. Resume bullet points
22. Future improvements

------------------------------------------------------------------------

# 45. Implementation Order

Do not try to build every microservice simultaneously.

Implement in this exact order.

## Phase 1 --- Repository + infrastructure

Create:

``` text
monorepo
Docker Compose
PostgreSQL
Redis
Kafka
Prometheus
Grafana
```

Verify all infrastructure works.

## Phase 2 --- Auth

Implement:

``` text
register
login
JWT
refresh
roles
```

## Phase 3 --- Account

Implement:

``` text
create account
get account
get balance
```

Seed users/accounts.

## Phase 4 --- Transaction core

Implement:

``` text
create transfer
idempotency
state machine
row locking
debit
credit
transaction record
```

Do this correctly before Kafka.

## Phase 5 --- Ledger

Implement:

``` text
double-entry ledger
ledger queries
balance verification
```

## Phase 6 --- Saga + failure handling

Implement:

``` text
transaction steps
compensation
pending
reconciliation_required
```

## Phase 7 --- Outbox + Kafka

Implement:

``` text
outbox
publisher
Kafka
consumer
processed_events
```

## Phase 8 --- Notifications + Audit

Implement Kafka consumers.

## Phase 9 --- Reconciliation

Implement:

``` text
reconciliation run
mismatch detection
issue management
```

## Phase 10 --- Frontend

Implement:

``` text
login
dashboard
transfer
transaction details
ledger
notifications
admin dashboard
```

## Phase 11 --- Observability

Implement:

``` text
metrics
Grafana
structured logging
correlation IDs
```

## Phase 12 --- Testing

Implement all unit/integration/concurrency/idempotency/failure tests.

## Phase 13 --- Documentation

Finish:

``` text
README
architecture diagram
ER diagram
sequence diagrams
failure diagrams
API docs
demo script
```

------------------------------------------------------------------------

# 46. AI Coding Assistant Rules

You are the lead software engineer for this project.

Follow these rules while coding:

1.  Do not rewrite working code unnecessarily.
2.  Before implementing a feature, inspect the existing repository.
3.  Preserve existing architecture unless there is a strong technical
    reason to change it.
4.  Implement one phase at a time.
5.  Do not generate fake implementations just to make compilation pass.
6.  Never replace PostgreSQL financial consistency with in-memory logic.
7.  Never use floating-point money.
8.  Never remove tests to make a build pass.
9.  When changing a database model, update the Flyway migration and
    relevant tests.
10. When changing an event schema, update producers, consumers and event
    tests.
11. Every financial mutation must have tests.
12. Every distributed failure path must have a test.
13. Return clear errors instead of generic 500 responses where a domain
    error exists.
14. Use transactions deliberately and document transaction boundaries.
15. For concurrency-sensitive code, explain why the locking strategy is
    safe.
16. For every Kafka consumer, implement duplicate-event protection.
17. For every externally retriable write endpoint, implement
    idempotency.
18. Never expose internal service ports through the public gateway
    unless required.
19. Never commit credentials.
20. At the end of each implementation phase, provide:

-   files created
-   files modified
-   database changes
-   APIs added
-   tests added
-   how to run
-   known limitations

------------------------------------------------------------------------

# 47. Definition of Done

The project is considered complete only when:

-   [ ] user can register/login
-   [ ] JWT authentication works
-   [ ] accounts can be created
-   [ ] balances are stored safely
-   [ ] money transfers work
-   [ ] transfer amount uses decimal precision
-   [ ] idempotency works
-   [ ] duplicate concurrent requests produce one transfer
-   [ ] row locking/concurrency control works
-   [ ] double-entry ledger works
-   [ ] transaction state machine works
-   [ ] Saga compensation works
-   [ ] pending transactions are supported
-   [ ] reconciliation works
-   [ ] outbox pattern works
-   [ ] Kafka events work
-   [ ] duplicate Kafka events are safe
-   [ ] notifications work
-   [ ] audit logs work
-   [ ] admin dashboard works
-   [ ] Prometheus metrics work
-   [ ] Grafana dashboard works
-   [ ] Docker Compose starts the system
-   [ ] integration tests use containers
-   [ ] concurrency test passes
-   [ ] failure simulation works
-   [ ] README is complete
-   [ ] no secrets are committed
-   [ ] no financial mutation bypasses the ledger/transaction rules

------------------------------------------------------------------------

# 48. Resume Positioning

Do NOT describe this as merely a CRUD banking app.

Position it as:

**Reliable Distributed Financial Transaction Platform**

Suggested resume bullets after the implementation is actually complete:

-   Built a distributed financial transaction platform using Java,
    Spring Boot, PostgreSQL, Redis, Kafka and Docker with idempotent
    transfer processing and explicit transaction state management.
-   Implemented concurrency-safe account mutations using database row
    locking and double-entry ledgering, preventing duplicate transfers
    and lost balance updates under concurrent requests.
-   Designed Saga-based failure recovery with compensating transactions,
    pending states and reconciliation workflows for partial/uncertain
    transaction outcomes.
-   Implemented Transactional Outbox and idempotent Kafka consumers to
    provide reliable event-driven communication for notifications, audit
    logging and analytics.
-   Added Prometheus/Grafana observability and Testcontainers-based
    integration/concurrency tests for failure, duplicate-request and
    high-contention scenarios.

Only use metrics such as throughput, latency, failure reduction or test
counts if they are actually measured by the implementation.

------------------------------------------------------------------------

# 49. Important Project Scope Decision

This is a **simulated digital wallet/banking system**, not a real
banking/payment product.

Do not integrate real bank accounts, UPI, card networks or real money
unless the project is later redesigned for the required
regulatory/security environment.

Use seeded/demo balances only.

The purpose is to demonstrate:

``` text
Distributed Systems
+
Database Transactions
+
Concurrency Control
+
Idempotency
+
Saga
+
Event-Driven Architecture
+
Reliable Messaging
+
Reconciliation
+
Observability
```

------------------------------------------------------------------------

# 50. First Task for the AI Coding Assistant

Before writing application code:

1.  Inspect the repository.
2.  Create the complete folder structure.
3.  Create architecture documentation.
4.  Create the database ER diagram documentation.
5.  Create Docker Compose for PostgreSQL, Redis, Kafka, Prometheus and
    Grafana.
6.  Create the initial Spring Boot project structure.
7.  Configure environment variables.
8.  Configure Flyway.
9.  Implement the initial database migrations.
10. Add health checks.
11. Add a minimal CI build.
12. Run the project and verify infrastructure connectivity.
13. Only after this succeeds, start Phase 2.

Do not implement the entire application in one giant response.

Work incrementally and keep the repository buildable after every phase.

------------------------------------------------------------------------

# Final Engineering Goal

The final application should make the following statement demonstrable:

> A financial transaction is not considered reliable merely because the
> normal request succeeds. The system must remain correct when requests
> are duplicated, operations are concurrent, services fail, messages are
> delayed or duplicated, and a transaction's final outcome becomes
> temporarily uncertain.

Build the project so that this claim can be demonstrated live through
API calls, database records, Kafka events, dashboards and automated
tests.

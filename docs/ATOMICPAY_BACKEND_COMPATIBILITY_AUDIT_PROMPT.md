# AtomicPay — Backend Capability & Frontend Compatibility Audit Prompt

## Purpose

You are auditing the **EXISTING AtomicPay codebase**.

This is an **AUDIT ONLY** task.

### Strict rules

- DO NOT modify existing code.
- DO NOT create or delete files.
- DO NOT implement missing features.
- DO NOT redesign the architecture during this audit.
- DO NOT create frontend pages.
- DO NOT assume a feature exists just because a class, DTO, repository, entity, database table, Kafka topic, or frontend component exists.
- Verify the complete flow wherever possible:
  **Frontend → Gateway → Controller → Service → Repository → Database / Kafka / Redis → Response**
- Clearly distinguish: **Implemented**, **Partially implemented**, **Broken**, **Missing**, **Implemented but not exposed**, and **Implemented but not usable by the frontend**.
- Give evidence using actual file paths, class names, method names, endpoints, database tables, Kafka topics, Redis keys, configuration files, and tests.
- If possible, include line numbers for important findings.
- Do not make assumptions based only on documentation or README files.

The goal is to determine exactly:

1. What the backend already supports.
2. What APIs already exist.
3. Which APIs are actually functional end-to-end.
4. Which frontend features can be built immediately.
5. Which frontend pages require backend work.
6. Which backend APIs are missing.
7. Which database changes are required.
8. Which Kafka/event changes are required.
9. Which authentication/authorization changes are required.
10. Which parts of the architecture are production-ready and which are only demonstrations.
11. What percentage of the required AtomicPay backend is already complete.

---

# 1. Project Context

AtomicPay is intended to be a **multi-user digital wallet/payment platform**.

The intended product direction is:

- Real user registration
- Real login
- JWT-based authentication
- User-specific wallets/accounts
- User-specific balances
- Account numbers
- KYC/profile information
- Beneficiaries
- Money transfer between users
- Transaction history
- Transaction details
- Double-entry ledger
- Idempotent financial operations
- Distributed transaction processing
- Saga-based recovery/compensation
- Kafka event-driven communication
- Redis caching where appropriate
- Notifications
- Audit logs
- Reconciliation
- Admin monitoring
- System/transaction observability
- Developer-only chaos testing

Initially, money may be **virtual/demo money** rather than real bank money. The architecture should nevertheless behave like a serious financial system.

Future versions may integrate real payment providers/banks, but this audit concerns the existing codebase.

---

# 2. First Task — Understand the Repository

Before making conclusions, inspect the repository structure.

Identify:

- Backend services
- API Gateway
- Authentication service
- Account service
- Transaction service
- Ledger service
- Notification service
- Audit service
- Analytics service, if present
- Frontend
- Database migrations
- SQL/schema files
- Docker files
- Docker Compose
- Kafka configuration
- Redis configuration
- Environment configuration
- Tests
- API documentation
- Postman collections
- OpenAPI/Swagger definitions
- Seed data
- Demo/seed users
- Monitoring configuration
- Prometheus
- Grafana
- Any scripts used to run the system

Create an initial architecture map based on the **actual repository**, not assumptions.

Example:

```text
Frontend
   |
   v
API Gateway
   |
   +--> Auth Service
   +--> Account Service
   +--> Transaction Service
   +--> Ledger Service
   +--> Notification Service
   +--> Audit Service
   +--> Analytics Service
   |
   +--> PostgreSQL
   +--> Redis
   +--> Kafka
```

Replace the example with the real architecture found in the repository.

---

# 3. Service Inventory

Create:

| Service | Exists | Technology | Port | Database | Kafka | Redis | Main Responsibility | Status |
|---|---|---|---|---|---|---|---|---|

For every service identify:

- Main application class
- Controllers
- Services
- Repositories
- Entities/models
- DTOs
- Configuration
- Security configuration
- Kafka producers
- Kafka consumers
- Redis usage
- Database access
- Tests
- Docker configuration

Also identify services that exist in code but are not actually started by Docker Compose or normal run configuration.

---

# 4. API Inventory

Inspect every controller and route.

Create:

| Method | Endpoint | Service | Auth Required | Role | Request | Response | Status Codes | Actually Works? |
|---|---|---|---|---|---|---|---|---|

For every API determine:

- Exact HTTP method
- Exact URL
- Controller
- Service method
- Repository/database calls
- Authentication requirement
- Authorization requirement
- Request DTO
- Response DTO
- Validation
- Error handling
- Database effects
- Kafka effects
- Redis effects
- Transaction boundaries
- Idempotency behavior
- Whether the endpoint is reachable through the gateway
- Whether the frontend currently uses it
- Whether it works with a real logged-in user

### Important

Do not list a hypothetical API. Only list an API if it actually exists in the code.

Clearly separate:

```text
ACTUAL EXISTING API
```

from:

```text
PROPOSED/MISSING API
```

---

# 5. Authentication Audit

Inspect the complete authentication flow.

## Registration

Determine whether:

- User can register
- Email validation exists
- Password hashing exists
- Password rules exist
- Duplicate email is handled
- User role is assigned safely
- User is persisted
- Wallet/account is created automatically
- Account number is generated
- Initial balance is created
- KYC state is initialized

## Login

Determine:

- Login endpoint
- Credential verification
- Password hashing algorithm
- JWT creation
- JWT claims
- Access-token expiration
- Refresh-token support
- Refresh-token persistence
- Refresh-token rotation
- Logout/invalidation
- Failed-login handling

## JWT

Inspect:

- JWT secret/configuration
- Algorithm
- Claims
- Subject
- User ID
- Email
- Role
- Expiration
- Gateway validation
- Downstream validation
- Claim propagation

Determine whether a request can be made by User A to access User B's data.

---

# 6. Authorization and User Isolation

This is a critical financial-system audit.

Check every user-facing endpoint for IDOR/BOLA vulnerabilities.

For example, determine whether:

```text
GET /accounts/{accountId}
```

actually verifies that the account belongs to the authenticated user.

Check:

- User ID from JWT vs request body
- Account ownership
- Transaction ownership
- Ledger ownership
- Beneficiary ownership
- Notification ownership
- Profile ownership
- KYC ownership

Look specifically for unsafe patterns where request-supplied user IDs are trusted instead of the authenticated JWT principal.

Report every potential cross-user data-access issue.

---

# 7. Wallet / Account Capability Audit

Determine whether the backend supports real user-specific wallet onboarding.

Check:

- Account creation
- Wallet creation
- Account number generation
- Unique account numbers
- Balance
- Available balance
- Currency
- Account status
- Ownership
- Multiple accounts
- Account lookup
- Account summary
- Account statement
- Account locking
- Account activation/deactivation

Determine whether registration automatically creates a wallet.

If it does not, clearly report:

```text
Registration → User created
Registration → Wallet NOT created
```

---

# 8. Virtual Money / Faucet Audit

Inspect whether there is a safe virtual-money mechanism.

Check for:

- Initial wallet funding
- Faucet endpoint
- System treasury account
- SYSTEM_FAUCET or equivalent
- Deposit transaction
- Ledger entry
- Idempotency
- Per-user limits
- Rate limiting
- Audit logging
- Notification
- Balance update
- Duplicate faucet prevention

Determine whether a newly registered user can obtain virtual money without direct database manipulation.

---

# 9. Transfer Capability Audit

Trace a complete transfer:

```text
User A
  |
  v
Transfer API
  |
  v
Transaction Service
  |
  v
Account validation
  |
  v
Debit
  |
  v
Credit
  |
  v
Ledger
  |
  v
Kafka event
  |
  +--> Notification
  +--> Audit
  +--> Other consumers
```

Check:

- Sender identification
- Recipient identification
- Account-number transfer
- Email transfer
- User-ID transfer
- Beneficiary transfer
- Amount validation
- Currency validation
- Self-transfer prevention
- Insufficient funds
- Account status
- Transaction creation
- Transaction state
- Debit
- Credit
- Ledger entries
- Notification
- Audit
- Kafka events
- Failure recovery

Determine whether transfer currently requires a raw UUID. If yes, mark that as unsuitable for the intended user-facing experience.

---

# 10. Idempotency Audit

Inspect the complete idempotency implementation.

Check:

- Idempotency-Key header
- Idempotency table/entity
- Unique constraints
- Request hashing
- Stored response
- Duplicate request handling
- Concurrent duplicate requests
- Transaction boundaries
- Expiration/cleanup
- Scope by user
- Scope by operation

Trace:

```text
POST transfer
Idempotency-Key: ABC123
```

followed by the same request again.

Expected:

```text
Only one financial transaction
Only one debit
Only one credit
Only one logical transfer
```

Report whether this is actually guaranteed.

---

# 11. Concurrency Control Audit

Inspect financial locking.

Check:

- Pessimistic locks
- Optimistic locks
- Database row locks
- Lock ordering
- Deadlock prevention
- Transaction isolation
- Deterministic account ordering
- Concurrent transfer behavior

Analyze:

```text
A → B
B → A
```

executing concurrently.

Determine whether the implementation can deadlock.

Also analyze:

```text
A → B
A → C
A → D
```

executing concurrently.

Determine whether the sender balance can become negative or be incorrectly updated.

---

# 12. Ledger Audit

Inspect the ledger implementation in detail.

Determine whether AtomicPay uses double-entry bookkeeping.

Check:

- Ledger accounts
- Ledger entries
- Debit
- Credit
- Transaction references
- Amount precision
- Currency
- Balance calculation
- Immutable entries
- Reversal/compensation
- System accounts
- User accounts

Verify the invariant:

```text
Total Debits = Total Credits
```

for every completed financial transaction.

Check whether money can be created or destroyed accidentally.

Pay special attention to `double` or `float` being used for monetary values. Prefer `NUMERIC/DECIMAL` and `BigDecimal` where appropriate.

---

# 13. Transaction State Machine Audit

Determine whether transactions have explicit states.

Examples:

```text
CREATED
PENDING
PROCESSING
COMPLETED
FAILED
COMPENSATING
COMPENSATED
```

Inspect:

- Transaction status enum
- State transitions
- Allowed transitions
- Invalid transitions
- Retry behavior
- Failure behavior
- Compensation behavior
- Final state guarantees

Create a state diagram using actual states found in the repository.

---

# 14. Saga Pattern Audit

Inspect distributed transaction orchestration.

Determine:

- Saga coordinator
- Saga steps
- Step persistence
- Forward actions
- Compensation actions
- Retry
- Timeout
- Failure state
- Compensation state
- Recovery after service restart

Example:

```text
Transfer
  |
  +--> Validate
  |
  +--> Debit
  |
  +--> Credit
  |
  +--> Ledger
  |
  +--> Complete
```

Failure:

```text
Credit fails
   |
   v
Compensate debit
   |
   v
Transaction FAILED/COMPENSATED
```

Determine whether this actually exists or is only described in documentation.

---

# 15. Kafka / Event-Driven Architecture Audit

Inspect all Kafka producers and consumers.

Create:

| Topic | Producer | Consumer | Event | Key | Retry | DLQ | Idempotency | Status |
|---|---|---|---|---|---|---|---|---|

Check:

- Kafka bootstrap configuration
- Topics
- Event DTOs
- Producers
- Consumers
- Consumer groups
- Retry
- Dead-letter topics
- Offset handling
- Error handling
- Duplicate event handling
- Event ordering
- Transaction events
- Notification events
- Audit events
- Reconciliation events

Determine whether Kafka is actually used or merely configured.

---

# 16. Outbox / Inbox Audit

If Transactional Outbox exists, inspect it fully.

Check:

- Outbox table
- Event persistence
- Same database transaction as financial update
- Poller/relay
- Kafka publication
- Published flag/status
- Retry
- Failure recovery

For Inbox/processed-events:

- Event ID
- Duplicate detection
- Consumer transaction
- Processed marker
- Retry behavior

Determine whether the implementation provides reliable DB-commit + event-publication semantics.

---

# 17. Redis Audit

Inspect Redis usage.

Check:

- What data is cached
- Cache keys
- TTL
- Invalidation
- User isolation
- Rate limiting
- Distributed locks
- Session/token storage
- Idempotency
- Temporary data

Report Redis features that are actually used, configured but unused, used incorrectly, or missing.

---

# 18. Notification Capability

Inspect notification support.

Check:

- In-app notifications
- Email
- Kafka-driven notifications
- Notification persistence
- Read/unread state
- User ownership
- Transaction notifications
- Failure notifications
- Security notifications

Determine whether the frontend can display a notification center.

---

# 19. Audit Logging

Inspect audit logs.

Check:

- User actions
- Authentication events
- Financial transactions
- Admin actions
- Security events
- Failed operations
- Actor ID
- Timestamp
- IP/device metadata if supported
- Before/after values
- Correlation ID
- Transaction ID

Determine whether audit records are immutable and properly associated with users/transactions.

---

# 20. Reconciliation Capability

Inspect reconciliation.

Determine:

- Reconciliation service
- Reconciliation runs
- Difference detection
- Ledger vs account balance checks
- Missing debit/credit detection
- Duplicate detection
- Transaction mismatch detection
- Issue persistence
- Resolution status
- Admin API
- Scheduled execution
- Manual execution

Determine whether an admin can actually inspect reconciliation issues from the backend.

---

# 21. Beneficiary Capability

Inspect whether beneficiaries exist.

Check:

- Create beneficiary
- List beneficiaries
- Update beneficiary
- Delete beneficiary
- Account number validation
- Ownership
- Duplicate beneficiary prevention
- User isolation
- Transfer integration

If beneficiaries do not exist, explicitly mark the required backend APIs as missing.

---

# 22. Profile and Security Capabilities

Audit APIs for:

### Profile

- Get profile
- Update profile
- Email
- Phone
- Name
- KYC status
- Profile image if supported

### Security

- Change password
- Refresh token
- Logout
- Active sessions
- Device management
- Security events
- Account lock
- MFA/2FA if present

Do not assume these features exist.

---

# 23. Admin Capability Audit

Inspect admin-only functionality.

Check:

- Admin authentication
- Role-based authorization
- User list
- User search
- User detail
- Account list
- Transaction monitoring
- Transaction investigation
- Transaction retry/recovery
- Reconciliation
- Audit logs
- Kafka events
- System health
- Metrics
- Logs
- Chaos testing

Determine which admin APIs are actually available.

---

# 24. Chaos Testing / Failure Injection

Inspect any chaos/failure injection functionality.

Determine:

- Where it exists
- Who can access it
- Whether normal users can trigger it
- Whether it is protected by admin/developer role
- Which failure types exist
- Whether it is enabled in production configuration
- Whether it can affect normal financial operations

If chaos controls are exposed inside normal user transfer screens, mark this as a serious architectural/security issue.

The intended structure is:

```text
Normal User Transfer
        |
        X
     NO CHAOS

Developer/Admin Chaos Lab
        |
        +--> Delay
        +--> Failure
        +--> Kafka failure
        +--> Service failure
        +--> DB failure
```

---

# 25. Database Audit

Inspect actual database schema/migrations.

Create:

| Table | Purpose | Important Columns | PK | FK | Unique Constraints | Indexes | Used By | Status |
|---|---|---|---|---|---|---|---|---|

Inspect, where present:

- users
- refresh_tokens
- kyc_profiles
- accounts
- beneficiaries
- transactions
- transaction_steps
- ledger_accounts
- ledger_entries
- idempotency_records
- outbox_events
- processed_events
- audit_logs
- notifications
- reconciliation_runs
- reconciliation_issues

Only include tables that actually exist.

Identify missing tables needed for the intended product.

Check:

- Foreign keys
- Cascades
- Unique constraints
- Monetary precision
- Indexes
- Nullable columns
- Enum/status handling
- Created/updated timestamps
- Soft deletion
- Auditability

---

# 26. Frontend API Compatibility Audit

The intended frontend is much larger than a simple 4-page demo.

Audit backend readiness for these pages.

## Public

```text
/
/features
/security
/architecture
/login
/register
/forgot-password
/reset-password
```

## User

```text
/app/dashboard
/app/wallet
/app/transfer
/app/transactions
/app/transactions/:transactionId
/app/ledger
/app/beneficiaries
/app/notifications
/app/profile
/app/security
/app/settings
```

## Admin

```text
/admin/dashboard
/admin/users
/admin/users/:userId
/admin/accounts
/admin/transactions
/admin/transactions/:transactionId
/admin/reconciliation
/admin/reconciliation/:issueId
/admin/audit-logs
/admin/events
/admin/system
/admin/chaos
```

Do not create these pages. Only determine whether the backend already supports them.

---

# 27. Page-by-Page Compatibility Matrix

Create:

| Page | Existing API | Backend Ready | Missing API | DB Change | Kafka Change | Auth Change | Status | Notes |
|---|---|---|---|---|---|---|---|---|

Use only:

```text
READY
PARTIAL
NOT READY
```

Do not use example results as actual findings.

---

# 28. Backend Capability Matrix

Create:

| Capability | Exists | End-to-End | API | Database | Kafka | Redis | Auth | Frontend Ready | Status |
|---|---|---|---|---|---|---|---|---|---|

Include:

- Registration
- Login
- JWT
- Refresh token
- Logout
- Wallet creation
- Account number
- Balance
- Virtual funding
- Transfer
- Recipient lookup
- Beneficiaries
- Transaction history
- Transaction detail
- Ledger
- Idempotency
- Concurrency
- Saga
- Kafka
- Outbox
- Inbox
- Notifications
- Audit logs
- Reconciliation
- Admin
- Monitoring
- Chaos testing

---

# 29. Existing Backend Functionality That Is Already Good

Do not only focus on missing features.

Find everything already implemented correctly.

For every strong feature provide evidence.

Format:

```text
FEATURE:
<feature>

LOCATION:
<actual file path>

IMPLEMENTATION:
<actual class/method/table>

STATUS:
IMPLEMENTED

WHY IT IS GOOD:
<short explanation>

LIMITATION:
<any limitation>
```

Pay special attention to existing:

- Pessimistic row locking
- Deterministic lock ordering
- Double-entry ledger
- NUMERIC monetary precision
- Idempotency
- Saga compensation
- Transaction state machine
- Transactional outbox
- Inbox/processed-event pattern
- Kafka events
- Redis caching
- Audit logging
- Reconciliation
- Prometheus metrics
- Grafana dashboards

Only mark these as implemented if verified in the actual code.

---

# 30. Identify Simulation/Demo Code

Find anything that makes the application look like a demo rather than a real multi-user product.

Look for:

- Hardcoded users
- Alice/Bob/Charlie
- Hardcoded UUIDs
- Fake JWT
- Mock balances
- Static transactions
- Static notifications
- Frontend-only authentication
- LocalStorage-only identity
- Direct DB UUID transfer
- Hardcoded account IDs
- Fake API responses
- Seed-only workflows
- Developer shortcuts
- Chaos controls inside normal user flows

For each item:

```text
LOCATION
CURRENT BEHAVIOR
WHY IT IS A PROBLEM
REQUIRED CHANGE
```

Do not implement the change.

---

# 31. Frontend Authentication Audit

Inspect the existing frontend.

Check:

- AuthContext
- Login flow
- Register flow
- Token storage
- Refresh handling
- Logout
- Protected routes
- Role-based routes
- API client
- Authorization headers
- User state
- Hardcoded users
- Hardcoded account IDs
- Hardcoded transaction IDs

Determine whether the frontend is connected to real backend authentication.

---

# 32. Frontend API Mismatch Audit

Compare frontend requests against actual backend APIs.

Look for:

- Wrong endpoint
- Wrong HTTP method
- Wrong request body
- Wrong response field
- `balance` vs `availableBalance`
- UUID vs account number
- Different enum names
- Different status names
- Missing authentication header
- Wrong port
- Wrong gateway route
- Wrong error format

Create:

| Frontend File | Expected API | Actual Backend API | Mismatch | Fix Required |
|---|---|---|---|---|

---

# 33. Gateway Audit

Inspect API Gateway routing.

Verify actual routing between frontend, gateway, and services.

Check:

- Route mappings
- Service URLs
- Authentication filter
- JWT validation
- JWT claim propagation
- CORS
- Rate limiting
- Error handling
- Correlation IDs
- Service discovery/configuration

Identify backend APIs that work directly but are unreachable through the gateway.

---

# 34. Docker and Deployment Audit

Inspect Docker Compose and Dockerfiles.

Determine:

- Which services are containerized
- Which services are missing
- Which ports are exposed
- PostgreSQL
- Redis
- Kafka
- Zookeeper/KRaft if applicable
- Gateway
- Auth service
- Account service
- Transaction service
- Ledger service
- Notification service
- Audit service
- Analytics service
- Frontend

Check whether the complete application can be started with one command.

Report what actually happens with:

```text
docker compose up
```

What starts? What fails? What is missing?

---

# 35. Observability Audit

Inspect:

- Health endpoints
- Actuator
- Prometheus
- Grafana
- Metrics
- Logs
- Correlation IDs
- Transaction IDs
- Trace IDs
- Error metrics
- Kafka metrics
- Database metrics
- Redis metrics

Determine whether an admin/system page can consume real operational data.

---

# 36. Security Risk Report

Rank every significant issue:

```text
CRITICAL
HIGH
MEDIUM
LOW
```

Create:

| Severity | Issue | Location | Impact | Recommended Fix |
|---|---|---|---|---|

Pay special attention to:

- Cross-user access
- Fake authentication
- Hardcoded credentials
- Hardcoded JWT secrets
- Weak password hashing
- Exposed admin APIs
- Chaos APIs accessible to normal users
- Missing ownership checks
- SQL injection
- No validation
- Sensitive data leakage
- Incorrect monetary precision
- Race conditions
- Duplicate financial transactions
- Broken idempotency
- Missing audit logs

---

# 37. Required Backend Changes

After completing the audit, produce a prioritized list.

Group into:

## P0 — Critical

Security or financial correctness.

## P1 — Required for Real User MVP

Features necessary for a real multi-user wallet.

## P2 — Required for Full Frontend

Features needed by the complete UI.

## P3 — Production Hardening

Observability, resilience, deployment, security hardening, etc.

For each:

| Priority | Change | Service | API | DB | Kafka | Redis | Reason |
|---|---|---|---|---|---|---|---|

---

# 38. Required API Changes

Create separate lists:

```text
EXISTING API
```

and:

```text
NEW API REQUIRED
```

For new APIs specify:

- Method
- Path
- Authentication
- Role
- Request
- Response
- Validation
- Database effect
- Kafka event
- Error cases

Do not implement them.

---

# 39. Required Database Changes

Create:

| Change | Table | Column/Constraint | Reason | Priority |
|---|---|---|---|---|

Only recommend changes actually required by the audit.

---

# 40. Required Kafka/Event Changes

Create:

| Event | Producer | Consumer | Topic | Payload | Why Needed | Priority |
|---|---|---|---|---|---|---|

Only recommend events required by the actual architecture.

---

# 41. Recommended Implementation Order

Based on the audit, recommend the safest implementation sequence.

A possible sequence is:

```text
1. Authentication
2. User isolation/security
3. Wallet/account provisioning
4. Virtual money/faucet
5. Recipient lookup
6. Transfer
7. Idempotency/concurrency verification
8. Transaction history
9. Ledger
10. Notifications
11. Beneficiaries
12. Profile/security
13. Admin
14. Reconciliation
15. Chaos lab
16. Observability
17. Docker/deployment
18. Full frontend
```

However, **do not blindly follow this order**. Change the order if the actual repository requires something else and explain why.

---

# 42. Final Backend Readiness Score

Give a percentage estimate based on evidence.

Use a breakdown such as:

```text
Backend Readiness: XX%

Financial Core: XX%
Authentication: XX%
User Isolation: XX%
Wallet: XX%
Transfer: XX%
Ledger: XX%
Idempotency: XX%
Saga: XX%
Kafka: XX%
Notifications: XX%
Admin: XX%
Reconciliation: XX%
Frontend Compatibility: XX%
Deployment: XX%
```

Explain how the overall percentage was calculated.

---

# 43. Final Report Structure

Your final response MUST use this structure:

# AtomicPay Backend Audit Report

## 1. Executive Summary
## 2. Repository Architecture
## 3. Service Inventory
## 4. Existing API Inventory
## 5. Authentication Audit
## 6. Authorization/User Isolation Audit
## 7. Wallet/Account Audit
## 8. Virtual Money/Faucet Audit
## 9. Transfer Audit
## 10. Idempotency Audit
## 11. Concurrency Audit
## 12. Ledger Audit
## 13. Transaction State Machine Audit
## 14. Saga Audit
## 15. Kafka Audit
## 16. Outbox/Inbox Audit
## 17. Redis Audit
## 18. Notification Audit
## 19. Audit Log Audit
## 20. Reconciliation Audit
## 21. Beneficiary Audit
## 22. Profile/Security Audit
## 23. Admin Audit
## 24. Chaos Testing Audit
## 25. Database Audit
## 26. Gateway Audit
## 27. Frontend Authentication Audit
## 28. Frontend API Compatibility
## 29. Page-by-Page Backend Readiness
## 30. Existing Strong Features
## 31. Simulation/Demo Code
## 32. Security Risk Report
## 33. Missing APIs
## 34. Required Database Changes
## 35. Required Kafka/Event Changes
## 36. Required Backend Changes
## 37. Recommended Implementation Order
## 38. Docker/Deployment Readiness
## 39. Observability Readiness
## 40. Final Backend Readiness Score
## 41. Final Recommendation

---

# 44. Most Important Final Output

At the end, answer these exact questions:

### A. What backend functionality already exists?

### B. What backend functionality is fully working?

### C. What backend functionality is partially implemented?

### D. What backend functionality is missing?

### E. What functionality is currently hardcoded/simulated?

### F. Which frontend pages can be built immediately?

### G. Which frontend pages require backend changes?

### H. Which backend APIs need to be added?

### I. Which database changes are required?

### J. Which Kafka changes are required?

### K. Which security problems must be fixed before deployment?

### L. What percentage of the backend is already complete?

### M. What should be implemented FIRST?

---

# 45. Evidence Requirement

For every important conclusion, provide evidence.

Use this format:

```text
Finding:
<finding>

Evidence:
<actual file paths>

Observed flow:
<actual request flow>

Impact:
<impact>

Status:
IMPLEMENTED / PARTIAL / MISSING / BROKEN

Priority:
P0 / P1 / P2 / P3
```

Do not make unsupported claims.

---

# 46. Do Not Modify Anything

This is an audit only.

Do NOT:

- edit Java
- edit TypeScript
- edit React
- edit SQL
- edit Docker
- edit YAML
- edit environment files
- create APIs
- create pages
- create migrations
- install packages
- change dependencies
- run destructive database commands

You may inspect files and, if the environment permits, run safe read-only commands/tests.

The final output must be a **diagnostic report**, not an implementation.

---

# 47. Final Decision

After the complete audit, answer:

```text
Can AtomicPay now support a real multi-user frontend?

YES / PARTIALLY / NO
```

Then explain exactly why.

Also answer:

```text
Can the existing backend support:
- real registration?
- real login?
- user-specific wallet?
- account number?
- virtual money?
- transfer?
- transaction history?
- ledger?
- notifications?
- beneficiaries?
- admin monitoring?
- reconciliation?
- chaos testing?
```

For each, use:

```text
READY
PARTIAL
MISSING
BROKEN
```

The purpose of this audit is to prevent building frontend screens that the backend cannot actually support.

**Inspect first. Report second. Implement nothing.**

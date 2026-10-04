# Reliable Distributed Financial Transactions (Atomic-Pay) — Live Demo Script

This script guides developers and evaluators through a step-by-step live demonstration of the Atomic-Pay distributed financial transactions platform.

---

## Prerequisites & Environment Setup

1. **Start Infrastructure Containers**:
   ```bash
   docker compose up -d
   ```
   *Verifies PostgreSQL (ports 5432), Redis (port 6379), Kafka (port 9092), Prometheus (port 9090), and Grafana (port 3000).*

2. **Start Backend Microservices**:
   ```bash
   ./mvnw clean compile -DskipTests
   ```
   *(Or start services via IDE or application JARs).*

3. **Start React Frontend Portal**:
   ```bash
   cd frontend
   npm run dev
   ```
   *Access the web dashboard at `http://localhost:5173`.*

---

## Step 1: User Registration & Authentication

1. **Register User 1 (Sender)**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "email": "sender@atomicpay.com",
       "password": "Password123!",
       "fullName": "Alice Sender"
     }'
   ```

2. **Register User 2 (Receiver)**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "email": "receiver@atomicpay.com",
       "password": "Password123!",
       "fullName": "Bob Receiver"
     }'
   ```

3. **Authenticate & Acquire JWT Token**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "email": "sender@atomicpay.com",
       "password": "Password123!"
     }'
   ```
   *Save the returned `accessToken` as `JWT_TOKEN`.*

---

## Step 2: Account Creation & Initial Balance Seeding

1. **Create Account for Sender (Alice)**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/accounts \
     -H "Authorization: Bearer $JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "accountType": "SAVINGS",
       "currency": "INR",
       "initialBalance": "10000.0000"
     }'
   ```
   *Note `senderAccountId`.*

2. **Create Account for Receiver (Bob)**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/accounts \
     -H "Authorization: Bearer $JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "accountType": "CHECKING",
       "currency": "INR",
       "initialBalance": "1000.0000"
     }'
   ```
   *Note `receiverAccountId`.*

---

## Step 3: Happy-Path Idempotent Transfer Execution

1. **Execute Transfer with Idempotency Key**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/transfers \
     -H "Authorization: Bearer $JWT_TOKEN" \
     -H "Idempotency-Key: demo-key-001" \
     -H "X-Correlation-ID: demo-corr-100" \
     -H "Content-Type: application/json" \
     -d '{
       "senderAccountId": "<SENDER_ACCOUNT_ID>",
       "receiverAccountId": "<RECEIVER_ACCOUNT_ID>",
       "amount": "1500.0000",
       "currency": "INR"
     }'
   ```
   *Response: Status `COMPLETED`, Debited `1500.0000`.*

2. **Test Idempotency Key Deduplication (Re-submit Same Request)**:
   Re-send the exact same command with `Idempotency-Key: demo-key-001`.
   *Response: Returns identical cached transaction result immediately without duplicate balance deductions.*

3. **Verify Double-Entry Ledger Entries**:
   ```bash
   curl -X GET http://localhost:8080/api/v1/transactions/<TRANSACTION_ID>/ledger \
     -H "Authorization: Bearer $JWT_TOKEN"
   ```
   *Verifies paired `DEBIT` 1500.0000 on sender and `CREDIT` 1500.0000 on receiver balancing to exactly zero.*

---

## Step 4: Simulated Saga Failure & Backward Compensation

1. **Simulate Credit Failure (`simulateFailure=FAIL_CREDIT`)**:
   ```bash
   curl -X POST "http://localhost:8080/api/v1/transfers?simulateFailure=FAIL_CREDIT" \
     -H "Authorization: Bearer $JWT_TOKEN" \
     -H "Idempotency-Key: demo-fail-credit-001" \
     -H "Content-Type: application/json" \
     -d '{
       "senderAccountId": "<SENDER_ACCOUNT_ID>",
       "receiverAccountId": "<RECEIVER_ACCOUNT_ID>",
       "amount": "500.0000",
       "currency": "INR"
     }'
   ```
   *Response: 422 Unprocessable Entity (`SAGA_FAILED`).*

2. **Verify Compensation Outcome**:
   Check transaction steps: step 2 `DEBIT` succeeded, step 3 `CREDIT` failed, step 10 `COMPENSATE_DEBIT` completed.
   *Sender account balance remains intact (restored).*

---

## Step 5: Automated Financial Reconciliation

1. **Trigger Financial Reconciliation Job**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/admin/reconciliation/run \
     -H "Authorization: Bearer $JWT_TOKEN"
   ```

2. **Inspect Reconciliation Dashboard**:
   ```bash
   curl -X GET http://localhost:8080/api/v1/admin/reconciliation/issues \
     -H "Authorization: Bearer $JWT_TOKEN"
   ```
   *Displays evaluated transactions, ledger mismatches, and status.*

---

## Step 6: Prometheus Metrics & Grafana Dashboard Inspection

1. **Scrape Prometheus Metrics**:
   ```bash
   curl http://localhost:8080/actuator/prometheus | grep transactions
   ```
   *Outputs counters for `transactions_total`, `transactions_completed_total`, `idempotency_hits_total`, `saga_compensation_total`.*

2. **View Grafana Observability Dashboard**:
   Navigate to `http://localhost:3000` (admin/admin).
   *Open dashboard `Atomic-Pay Financial Transactions & System Health` to monitor real-time throughput, latency percentiles, saga compensations, and outbox publisher rates.*

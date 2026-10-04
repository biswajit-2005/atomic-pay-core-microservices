# Failure Recovery, Saga Compensation, & Reconciliation

## 1. Failure Scenarios and Guarantees

### Scenario A: Partial Failure during Saga Execution (Credit Fails after Debit)
1. Sender is debited successfully (Status: `DEBITED`).
2. Credit operation to receiver fails due to transient node network error or account lock timeout.
3. Saga Orchestrator catches failure, updates status to `COMPENSATING`.
4. Orchestrator executes compensating transaction: Credits sender account back by transfer amount.
5. Transaction state is finalized as `COMPENSATED`.
6. Ledger reflects the original debit and compensating credit entry. No money is lost or created.

### Scenario B: Uncertain Transaction Outcome (Network Partition / Timeout)
1. Operation status cannot be definitively confirmed as Success or Failure (e.g. database connection drops mid-commit).
2. Transaction transitions to `PENDING`.
3. Background Reconciliation Engine picks up `PENDING` transactions older than 30 seconds.
4. Reconciliation queries authoritative ledger entries and account balances:
   - If ledger entries exist: state completes to `COMPLETED`.
   - If no ledger entries exist: state transitions to `RECONCILIATION_REQUIRED` for human/automated audit resolution.

### Scenario C: Kafka Consumer Duplicate Delivery
1. Kafka broker delivers the same `TRANSACTION_COMPLETED` event multiple times to Notification/Audit Service.
2. Consumer checks `processed_events` table within its schema `(consumer_name, event_id)` inside a database transaction.
3. If `event_id` exists: event is safely ignored (Idempotent Consumer).
4. If `event_id` does not exist: process event and record `(consumer_name, event_id)`.

# Failure Recovery, Saga Compensation, & Reconciliation — Atomic-Pay

## 1. Saga Failure Handling & Backward Compensation

When a multi-step transaction fails midway (e.g., credit operation to receiver fails after sender has been debited), the distributed Saga engine initiates backward step-by-step compensation to restore balance integrity.

```mermaid
sequenceDiagram
    autonumber
    participant TxEngine as Saga Orchestrator
    participant Sender as Sender Account
    participant Receiver as Receiver Account
    participant Idempotency as Idempotency Engine
    participant Outbox as Outbox Events

    Note over TxEngine: Step 1: VALIDATE Request - Passed
    Note over TxEngine: Step 2: DEBIT Sender Account - Passed
    TxEngine->>Sender: Execute Debit (Status -> DEBITED)
    
    Note over TxEngine: Step 3: CREDIT Receiver Account - FAILED!
    TxEngine->>Receiver: Execute Credit (Simulated Failure / Timeout)
    Receiver-->>TxEngine: Exception / Failure Signal
    
    Note over TxEngine: Trigger Backward Saga Compensation
    TxEngine->>TxEngine: Transition State -> COMPENSATING
    TxEngine->>Sender: Execute Compensation Debit (Refund Sender)
    Sender-->>TxEngine: Debit Reversed Successfully
    
    TxEngine->>TxEngine: Finalize State -> COMPENSATED
    TxEngine->>Idempotency: Mark Idempotency Record (Status=COMPLETED, HTTP=422)
    TxEngine->>Outbox: Record TRANSACTION_COMPENSATED Event
```

---

## 2. Uncertain Outcome Handling & Reconciliation Workflow

```mermaid
flowchart TD
    A[Transaction In-Flight] --> B{Step Timeout / Network Failure?}
    B -- No --> C[Finalize State COMPLETED / FAILED]
    B -- Yes --> D[Transition State to PENDING]
    
    D --> E[Automated Reconciliation Job Runs Every 30s]
    E --> F{Evaluate Ledger Entries vs Accounts}
    
    F -- Double-Entry Ledger Exists & Balanced --> G[Resolve State to COMPLETED]
    F -- Partial Debit Exists & No Credit --> H[Execute Auto Compensation -> COMPENSATED]
    F -- Unresolvable / Missing Data --> I[Flag Status RECONCILIATION_REQUIRED]
    
    I --> J[Admin Dashboard Inspection & Manual Audit Resolution]
    J --> K[Admin Resolves Issue via UI]
```

---

## 3. Idempotent Consumer Deduplication (Inbox Pattern)

```mermaid
sequenceDiagram
    autonumber
    participant Kafka as Apache Kafka Broker
    participant Consumer as Consumer Service (Notification / Audit)
    participant Inbox as DB processed_events Table
    participant DB as Consumer DB Schema

    Kafka->>Consumer: Deliver Domain Event (event_id=123)
    Consumer->>Inbox: SELECT 1 FROM processed_events WHERE consumer_name=? AND event_id=123
    
    alt Event Already Processed (Duplicate Delivery)
        Inbox-->>Consumer: Record Exists
        Consumer-->>Consumer: Log duplicate message & skip processing
    else First Time Event Delivery
        Inbox-->>Consumer: No Record Found
        Consumer->>DB: Process Business Logic (Save Notification / Audit Log)
        Consumer->>Inbox: INSERT INTO processed_events (consumer_name, event_id, created_at)
        Consumer-->>Consumer: Commit DB Transaction & Acknowledge Kafka Message
    end
```

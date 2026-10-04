# Transaction Lifecycle & Execution Flow

## 1. High-Level Transfer Sequence

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Gateway as API Gateway
    participant TxService as Transaction Service
    participant Redis as Redis Cache/Lock
    participant AccountService as Account Service (Postgres)
    participant LedgerService as Ledger Service (Postgres)
    participant Outbox as Outbox Table
    participant Kafka as Apache Kafka

    Client->>Gateway: POST /api/v1/transfers (Header: Idempotency-Key)
    Gateway->>TxService: Forward Request + Bearer JWT + Correlation-ID
    
    TxService->>Redis: Check Idempotency Key (Fast Path)
    alt Idempotency Hit
        Redis-->>TxService: Return cached transaction response
        TxService-->>Client: 200 OK (Cached Result)
    else Idempotency Miss
        TxService->>TxService: Acquire Pessimistic Account Locks (Min/Max ID Order)
        TxService->>AccountService: Verify Sender Balance >= Amount
        alt Insufficient Balance
            TxService->>AccountService: Set State FAILED (INSUFFICIENT_BALANCE)
            TxService-->>Client: 400 Bad Request
        else Balance Available
            TxService->>AccountService: Debit Sender Balance
            TxService->>AccountService: Credit Receiver Balance
            TxService->>LedgerService: Create Immutable DEBIT & CREDIT Ledger Entries
            TxService->>TxService: Update Transaction Status -> COMPLETED
            TxService->>Outbox: Insert TRANSACTION_COMPLETED Event
            TxService->>Redis: Cache Idempotency Key & Result
            TxService-->>Client: 200 OK (Transaction COMPLETED)
            
            async Outbox Publisher->>Kafka: Publish Event to transaction.events
        end
    end
```

## 2. Transaction State Transitions

```
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

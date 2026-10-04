# REST API Specification — Atomic-Pay

All API requests and responses use standard JSON payloads, follow unified response wrappers, and require authentication via standard HTTP `Authorization: Bearer <JWT_TOKEN>` headers except for public authentication endpoints.

---

## Unified Response Wrapper Format

### Success Response (HTTP 200 / 201)
```json
{
  "success": true,
  "data": {
    "id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "senderAccountId": "54d80852-407c-4693-b7d5-4fa3b0031f09",
    "receiverAccountId": "aef98e9f-0bbc-49db-9057-bea41166e7a7",
    "amount": "1000.0000",
    "currency": "INR",
    "status": "COMPLETED",
    "createdAt": "2026-10-05T05:00:00Z"
  },
  "timestamp": "2026-10-05T05:00:00Z",
  "requestId": "c1f7a1b2-3c4d-5e6f-7a8b-9c0d1e2f3a4b"
}
```

### Error Response (HTTP 4xx / 5xx)
```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_BALANCE",
    "message": "Sender account does not have sufficient available balance for this transfer",
    "details": {
      "requestedAmount": "1000.0000",
      "availableBalance": "250.0000"
    }
  },
  "timestamp": "2026-10-05T05:00:00Z",
  "requestId": "c1f7a1b2-3c4d-5e6f-7a8b-9c0d1e2f3a4b"
}
```

---

## 1. Authentication Service (`/api/v1/auth`)

### `POST /api/v1/auth/register`
- **Description**: Register a new user account.
- **Request Body**:
  ```json
  {
    "email": "user@atomicpay.com",
    "password": "Password123!",
    "fullName": "Alice Smith"
  }
  ```
- **Response**: User details object.

### `POST /api/v1/auth/login`
- **Description**: Authenticate user and issue JWT tokens.
- **Request Body**:
  ```json
  {
    "email": "user@atomicpay.com",
    "password": "Password123!"
  }
  ```
- **Response**: Access token and refresh token.

### `POST /api/v1/auth/refresh`
- **Description**: Obtain a fresh access token using a valid refresh token.

---

## 2. Account Service (`/api/v1/accounts`)

### `POST /api/v1/accounts`
- **Description**: Create a new digital wallet account.
- **Request Body**:
  ```json
  {
    "accountType": "SAVINGS",
    "currency": "INR",
    "initialBalance": "1000.0000"
  }
  ```

### `GET /api/v1/accounts`
- **Description**: Retrieve all accounts owned by authenticated user.

### `GET /api/v1/accounts/{accountId}/balance`
- **Description**: Query current real-time balance of an account.

---

## 3. Transaction Service (`/api/v1/transfers`)

### `POST /api/v1/transfers`
- **Description**: Execute a monetary transfer between accounts.
- **Headers**:
  - `Idempotency-Key` (Required, String): Client UUID/unique key for request deduplication.
  - `X-Correlation-ID` (Optional, String): Distributed trace ID.
- **Request Body**:
  ```json
  {
    "senderAccountId": "54d80852-407c-4693-b7d5-4fa3b0031f09",
    "receiverAccountId": "aef98e9f-0bbc-49db-9057-bea41166e7a7",
    "amount": "1000.0000",
    "currency": "INR"
  }
  ```
- **Simulated Failure Query Parameters**:
  - `simulateFailure=FAIL_CREDIT`: Forces Saga credit failure to demonstrate backward compensation.
  - `simulateFailure=DELAY_CREDIT`: Forces timeout to test reconciliation engine.

### `GET /api/v1/transfers/{transactionId}`
- **Description**: Get full transaction state details and step execution log.

---

## 4. Ledger & Reconciliation Endpoints (`/api/v1/admin`)

### `GET /api/v1/transactions/{transactionId}/ledger`
- **Description**: Retrieve paired immutable double-entry ledger entries for a transaction.

### `POST /api/v1/admin/reconciliation/run`
- **Description**: Manually trigger automated financial reconciliation job.

### `GET /api/v1/admin/reconciliation/issues`
- **Description**: Query detected reconciliation issues and mismatches.

---

## 5. Metrics & Observability (`/actuator/prometheus`)

- **URL**: `http://localhost:8080/actuator/prometheus` (or directly per microservice on port 8081-8087).
- **Exported Custom Metrics**:
  - `transactions_total`: Total transfer attempts.
  - `transactions_completed_total`: Total successful transfers.
  - `transactions_failed_total`: Total failed transfers.
  - `idempotency_hits_total`: Deduplicated request hit counter.
  - `saga_compensation_total`: Backward compensations executed.
  - `transaction_processing_seconds`: Processing latency timer percentiles (P50, P95, P99).

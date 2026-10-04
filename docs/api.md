# REST API Specification

All API requests and responses use standard JSON payloads and follow unified response wrappers.

## Response Formats

### Success Response (200 / 201)
```json
{
  "success": true,
  "data": {
    "transactionId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "status": "COMPLETED",
    "amount": "1000.0000",
    "currency": "INR"
  },
  "timestamp": "2026-10-04T12:00:00Z",
  "requestId": "c1f7a1b2-3c4d-5e6f-7a8b-9c0d1e2f3a4b"
}
```

### Error Response (4xx / 5xx)
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
  "timestamp": "2026-10-04T12:00:00Z",
  "requestId": "c1f7a1b2-3c4d-5e6f-7a8b-9c0d1e2f3a4b"
}
```

## Auth Endpoints
- `POST /api/v1/auth/register` - Register a new user
- `POST /api/v1/auth/login` - User authentication and JWT issuance
- `POST /api/v1/auth/refresh` - Refresh access token
- `POST /api/v1/auth/logout` - Invalidate refresh token

## Account Endpoints
- `POST /api/v1/accounts` - Create new wallet account
- `GET /api/v1/accounts` - List authenticated user's accounts
- `GET /api/v1/accounts/{accountId}` - Get specific account details
- `GET /api/v1/accounts/{accountId}/balance` - Get available balance

## Transfer Endpoints
- `POST /api/v1/transfers` - Execute monetary transfer (Requires `Idempotency-Key` header)
- `GET /api/v1/transfers/{transactionId}` - Get transfer status and steps
- `GET /api/v1/transfers` - Query transfer history

## Ledger Endpoints
- `GET /api/v1/accounts/{accountId}/ledger` - Get ledger history for an account
- `GET /api/v1/transactions/{transactionId}/ledger` - Get double-entry ledger entries for a transaction

## Admin Endpoints
- `GET /api/v1/admin/transactions` - Admin transaction overview
- `GET /api/v1/admin/reconciliation/issues` - View reconciliation mismatches
- `POST /api/v1/admin/reconciliation/run` - Trigger automated reconciliation job
- `GET /api/v1/admin/audit-logs` - Query append-only audit trail

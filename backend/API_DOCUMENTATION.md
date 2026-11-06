# Finova Banking API & Financial Ledger Documentation

This document describes the banking transaction endpoints, financial integrity guarantees, and ledger management implemented in the Finova backend service (Phase 6).

---

## Authentication & Authorization Architecture

All banking transaction endpoints require JWT Bearer authentication in the HTTP `Authorization` header:

```http
Authorization: Bearer <jwt-token>
```

- Unauthenticated requests return `HTTP 401 Unauthorized`.
- Account ownership is derived from the authenticated token's security context (never from client-supplied IDs).
- Accessing resources belonging to other customers without authorized staff permissions returns `HTTP 403 Forbidden`.
- Allowed staff roles for transaction oversight: `SUPER_ADMIN`, `BRANCH_MANAGER`, `EMPLOYEE`, `COMPLIANCE_OFFICER`.

---

## Financial Integrity & Concurrency Architecture

1. **Monetary Precision:** All amounts, balance snapshots (`balanceBefore`, `balanceAfter`), and calculations use `BigDecimal` with a scale of 2. Floating-point types are strictly prohibited in the monetary core.
2. **Pessimistic Write Locking (`PESSIMISTIC_WRITE`):**
   - In `WithdrawalService`, the account entity is queried with `LockModeType.PESSIMISTIC_WRITE` prior to reading balances or testing for sufficient funds.
   - This places a row-level exclusive database lock (`SELECT ... FOR UPDATE`), preventing race conditions, balance double-spends, and overdrafts under concurrent withdrawal requests.
3. **Atomic Ledger Transactions:**
   - Account balance mutation and `BankTransaction` ledger insertion occur within the same `@Transactional` database boundary.
   - If either operation fails, the entire transaction rolls back, preserving ledger consistency.
4. **Idempotency Protection:**
   - Every mutating request requires an `idempotencyKey` (UUID / structured string).
   - A unique database index on `(bank_account_id, idempotency_key)` guarantees that identical requests executed concurrently will not execute balance updates twice.
   - Repeated requests with the identical idempotency key replay the original `TransactionResponse` without mutating account balances.
   - Reusing an existing idempotency key with conflicting parameters (mismatched amount, operation type, or target account) is rejected with `HTTP 409 Conflict`.
5. **No Client-Controlled Financial State:**
   - Clients can only supply `amount`, `idempotencyKey`, and optional `description`.
   - Balances, transaction status, transaction references, currencies, and timestamps are always computed server-side.

---

## Transaction Lifecycle

```
[Client / Frontend]
         |
         | POST /api/accounts/{id}/deposits OR withdrawals
         v
[JwtAuthenticationFilter]  ----> 401 Unauthorized (if invalid)
         |
         v
[TransactionController]    ----> 400 Bad Request (if input validation fails)
         |
         v
[DepositService / WithdrawalService]
         |
         +--> Verify Account Ownership (403 Forbidden if mismatched)
         +--> Check Idempotency Key in Ledger
         |       |
         |       +--> Key exists & matching details? Return original TransactionResponse (replay)
         |       +--> Key exists & conflicting details? Throw 409 Conflict
         |
         +--> Acquire PESSIMISTIC_WRITE lock on BankAccount
         +--> Validate Account Status == ACTIVE (422 / 400 if FROZEN or CLOSED)
         +--> Validate Balance >= Amount (Withdrawal only; 400 / 422 if insufficient)
         |
         +--> Calculate balanceAfter = balanceBefore +/- amount (BigDecimal, scale 2)
         +--> Mutate BankAccount balance
         +--> Generate unique transactionReference (FIN-TXN-...)
         +--> Persist BankTransaction record atomically
         |
         v
[200 OK / 201 Created with TransactionResponse DTO]
```

---

## 1. Customer Deposit

Executes an atomic, idempotent deposit into the specified bank account.

- **Path:** `/api/accounts/{accountId}/deposits`
- **Method:** `POST`
- **Access:** Authenticated account owner (`CUSTOMER`)
- **Expected Status Codes:**
  - `201 Created`: Deposit successfully applied.
  - `400 Bad Request`: Invalid amount (<= 0), scale > 2, or account inactive (`FROZEN`/`CLOSED`).
  - `401 Unauthorized`: Missing or invalid JWT token.
  - `403 Forbidden`: Authenticated user is not the account owner.
  - `404 Not Found`: Account ID does not exist.
  - `409 Conflict`: Idempotency key reused with mismatched transaction parameters.

### Request Body Example
```json
{
  "amount": 250.00,
  "description": "Salary direct deposit",
  "idempotencyKey": "FIN-DEP-550e8400-e29b-41d4-a716-446655440000"
}
```

### Response Body Example (`HTTP 201 Created`)
```json
{
  "id": 101,
  "transactionReference": "FIN-TXN-A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6",
  "accountNumber": "100000000001",
  "transactionType": "DEPOSIT",
  "transactionStatus": "COMPLETED",
  "amount": 250.00,
  "currency": "INR",
  "balanceBefore": 1000.00,
  "balanceAfter": 1250.00,
  "description": "Salary direct deposit",
  "createdAt": "2026-11-06T10:15:00"
}
```

---

## 2. Customer Withdrawal

Executes an atomic, idempotent debit from the specified bank account with pessimistic lock overdraft protection.

- **Path:** `/api/accounts/{accountId}/withdrawals`
- **Method:** `POST`
- **Access:** Authenticated account owner (`CUSTOMER`)
- **Expected Status Codes:**
  - `200 OK`: Withdrawal successfully applied.
  - `400 Bad Request`: Insufficient funds, invalid amount (<= 0), scale > 2, or account inactive (`FROZEN`/`CLOSED`).
  - `401 Unauthorized`: Missing or invalid JWT token.
  - `403 Forbidden`: Authenticated user is not the account owner.
  - `404 Not Found`: Account ID does not exist.
  - `409 Conflict`: Idempotency key reused with mismatched transaction parameters.

### Request Body Example
```json
{
  "amount": 100.00,
  "description": "ATM cash withdrawal",
  "idempotencyKey": "FIN-WTH-6ba7b810-9dad-11d1-80b4-00c04fd430c8"
}
```

### Response Body Example (`HTTP 200 OK`)
```json
{
  "id": 102,
  "transactionReference": "FIN-TXN-B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q7",
  "accountNumber": "100000000001",
  "transactionType": "WITHDRAWAL",
  "transactionStatus": "COMPLETED",
  "amount": 100.00,
  "currency": "INR",
  "balanceBefore": 1250.00,
  "balanceAfter": 1150.00,
  "description": "ATM cash withdrawal",
  "createdAt": "2026-11-06T10:16:00"
}
```

---

## 3. Account Transaction History

Retrieves paginated, sorted, and filtered transaction history for an account.

- **Path:** `/api/accounts/{accountId}/transactions`
- **Method:** `GET`
- **Access:** Account owner (`CUSTOMER`) or authorized staff (`SUPER_ADMIN`, `BRANCH_MANAGER`, `EMPLOYEE`, `COMPLIANCE_OFFICER`)
- **Expected Status Codes:**
  - `200 OK`: Paginated history successfully returned.
  - `400 Bad Request`: Invalid pagination, invalid sort field, or `startDate` after `endDate`.
  - `401 Unauthorized`: Missing or invalid JWT token.
  - `403 Forbidden`: Customer attempting to access another customer's transactions.
  - `404 Not Found`: Account ID does not exist.

### Query Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `page` | integer | `0` | Zero-based page index (`>= 0`). |
| `size` | integer | `20` | Page size (`1` to `100`). |
| `sortBy` | string | `createdAt` | Property to sort by (`id`, `transactionReference`, `amount`, `currency`, `balanceBefore`, `balanceAfter`, `transactionType`, `transactionStatus`, `createdAt`). |
| `direction` | string | `desc` | Sort direction (`asc` or `desc`). |
| `type` | string | *(none)* | Transaction type filter (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER_DEBIT`, `TRANSFER_CREDIT`). |
| `status` | string | *(none)* | Transaction status filter (`PENDING`, `COMPLETED`, `FAILED`, `REVERSED`). |
| `startDate`| string | *(none)* | Start date filter in `YYYY-MM-DD` or ISO-8601 format. |
| `endDate` | string | *(none)* | End date filter in `YYYY-MM-DD` or ISO-8601 format. |

### Request Example
```http
GET /api/accounts/1/transactions?page=0&size=10&sortBy=createdAt&direction=desc&type=DEPOSIT&startDate=2026-11-01&endDate=2026-11-06
```

### Response Body Example (`HTTP 200 OK`)
```json
{
  "content": [
    {
      "id": 101,
      "transactionReference": "FIN-TXN-A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6",
      "accountNumber": "100000000001",
      "transactionType": "DEPOSIT",
      "transactionStatus": "COMPLETED",
      "amount": 250.00,
      "currency": "INR",
      "balanceBefore": 1000.00,
      "balanceAfter": 1250.00,
      "description": "Salary direct deposit",
      "createdAt": "2026-11-06T10:15:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "empty": false
}
```

---

## 4. Transaction Lookup by Reference

Retrieves a single transaction by its unique reference string.

- **Path:** `/api/transactions/{transactionReference}`
- **Method:** `GET`
- **Access:** Account owner (`CUSTOMER`) or authorized staff
- **Expected Status Codes:**
  - `200 OK`: Transaction details returned.
  - `401 Unauthorized`: Missing or invalid JWT token.
  - `403 Forbidden`: Customer attempting to access another customer's transaction.
  - `404 Not Found`: Transaction reference does not exist.

### Request Example
```http
GET /api/transactions/FIN-TXN-A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6
```

### Response Body Example (`HTTP 200 OK`)
```json
{
  "id": 101,
  "transactionReference": "FIN-TXN-A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6",
  "accountNumber": "100000000001",
  "transactionType": "DEPOSIT",
  "transactionStatus": "COMPLETED",
  "amount": 250.00,
  "currency": "INR",
  "balanceBefore": 1000.00,
  "balanceAfter": 1250.00,
  "description": "Salary direct deposit",
  "createdAt": "2026-11-06T10:15:00"
}
```

---

## Automated Verification

### Backend Integration & Unit Suites (275 tests)
```powershell
Set-Location -Path "finova\backend"
.\mvnw.cmd clean test
```

### Frontend Workflows & ESLint (15 tests)
```powershell
Set-Location -Path "finova\frontend"
npm run lint
npm test
npm run build
```

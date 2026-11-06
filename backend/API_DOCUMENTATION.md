# Finova Banking API Documentation

This document describes the banking transaction endpoints implemented in the Finova backend service.

---

## Authentication

All endpoints require JWT Bearer authentication in the HTTP `Authorization` header:

```http
Authorization: Bearer <jwt-token>
```

- Unauthenticated requests return `HTTP 401 Unauthorized`.
- Accessing resources belonging to other customers without authorized staff permissions returns `HTTP 403 Forbidden`.

---

## 1. Customer Deposit

Executes an atomic, idempotent deposit into the specified bank account.

- **Path:** `/api/accounts/{accountId}/deposits`
- **Method:** `POST`
- **Access:** Authenticated account owner (`CUSTOMER`)
- **Expected Status Codes:**
  - `201 Created`: Deposit successfully applied.
  - `400 Bad Request`: Invalid amount, scale > 2, or account inactive (`FROZEN`/`CLOSED`).
  - `401 Unauthorized`: Missing or invalid JWT token.
  - `403 Forbidden`: Authenticated user is not the account owner.
  - `404 Not Found`: Account ID does not exist.
  - `409 Conflict`: Idempotency key reused with mismatched transaction parameters.

### Request Body Example
```json
{
  "amount": 250.00,
  "description": "Salary deposit",
  "idempotencyKey": "DEP-REQ-20261106-001"
}
```

### Response Body Example (`HTTP 201 Created`)
```json
{
  "id": 101,
  "transactionReference": "FIN-TXN-A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6",
  "accountNumber": "FIN1111222233",
  "transactionType": "DEPOSIT",
  "transactionStatus": "COMPLETED",
  "amount": 250.00,
  "currency": "INR",
  "balanceBefore": 1000.00,
  "balanceAfter": 1250.00,
  "description": "Salary deposit",
  "createdAt": "2026-11-06T10:15:00"
}
```

---

## 2. Customer Withdrawal

Executes an atomic, idempotent debit from the specified bank account with overdraft protection.

- **Path:** `/api/accounts/{accountId}/withdrawals`
- **Method:** `POST`
- **Access:** Authenticated account owner (`CUSTOMER`)
- **Expected Status Codes:**
  - `200 OK`: Withdrawal successfully applied.
  - `400 Bad Request`: Insufficient funds, invalid amount, scale > 2, or account inactive (`FROZEN`/`CLOSED`).
  - `401 Unauthorized`: Missing or invalid JWT token.
  - `403 Forbidden`: Authenticated user is not the account owner.
  - `404 Not Found`: Account ID does not exist.
  - `409 Conflict`: Idempotency key reused with mismatched transaction parameters.

### Request Body Example
```json
{
  "amount": 100.00,
  "description": "ATM withdrawal",
  "idempotencyKey": "WTH-REQ-20261106-002"
}
```

### Response Body Example (`HTTP 200 OK`)
```json
{
  "id": 102,
  "transactionReference": "FIN-TXN-B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q7",
  "accountNumber": "FIN1111222233",
  "transactionType": "WITHDRAWAL",
  "transactionStatus": "COMPLETED",
  "amount": 100.00,
  "currency": "INR",
  "balanceBefore": 1250.00,
  "balanceAfter": 1150.00,
  "description": "ATM withdrawal",
  "createdAt": "2026-11-06T10:16:00"
}
```

---

## 3. Account Transaction History

Retrieves paginated and filtered transaction history for an account.

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
      "accountNumber": "FIN1111222233",
      "transactionType": "DEPOSIT",
      "transactionStatus": "COMPLETED",
      "amount": 250.00,
      "currency": "INR",
      "balanceBefore": 1000.00,
      "balanceAfter": 1250.00,
      "description": "Salary deposit",
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

Retrieves a single transaction by its business reference code.

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
  "accountNumber": "FIN1111222233",
  "transactionType": "DEPOSIT",
  "transactionStatus": "COMPLETED",
  "amount": 250.00,
  "currency": "INR",
  "balanceBefore": 1000.00,
  "balanceAfter": 1250.00,
  "description": "Salary deposit",
  "createdAt": "2026-11-06T10:15:00"
}
```


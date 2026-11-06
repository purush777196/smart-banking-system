# Finova — Smart Banking & Financial Management System

Finova is a full-stack smart banking and financial management application built as an enterprise-grade academic simulation.

## Technology Stack

### Frontend
- React 19
- Vite
- Tailwind CSS v4
- Axios
- Recharts
- Lucide React

### Backend
- Java 17+
- Spring Boot 3
- Spring Security 6
- JWT Authentication
- Spring Data JPA
- PostgreSQL / H2 (test profile)
- Maven
- JUnit 5 & Mockito

---

## Features

1. **Role-Based Access Control (RBAC):** Customer, Teller, Branch Manager, Compliance Officer, Super Admin.
2. **10 Specialized Dashboards:** Tailored interfaces for executive oversight, operations, risk, fraud, and retail customers.
3. **Customer & KYC Management:** Unique customer code generator, profile verification, and audit logging.
4. **Bank Account Management:** Secure account number generation, savings/current products, balance tracking, and status controls (ACTIVE, FROZEN, CLOSED).
5. **Deposit & Withdrawal Management (Phase 6):** Pessimistic-lock protected atomic ledger mutations, unique reference generation, double-submit prevention, and idempotency tracking.
6. **Financial Ledger & Transaction History:** Server-side pagination, multi-column sorting, date range and type/status filtering, and reference lookups.
7. **Loan Management & Fraud Monitoring:** Risk scoring, loan lifecycles, and transaction surveillance.
8. **Audit & Compliance:** Complete financial snapshots (`balanceBefore`, `balanceAfter`) and tamper-resistant transaction histories.

---

## Project Milestones

- **Phase 1: Project Foundation** — Completed
- **Phase 2: UI Foundation & 10 Dashboards** — Completed
- **Phase 3: Authentication & Authorization (JWT + RBAC)** — Completed
- **Phase 4: Account Number Generation & Business Logic (PR #7, PR #8)** — Completed
- **Phase 5: Financial Ledger Domain Model & Concurrency Locking (PR #9, PR #10)** — Completed
- **Phase 6: Complete Deposit, Withdrawal & Transaction Management** — Completed
  - Atomic, idempotent deposits (`POST /api/accounts/{accountId}/deposits`)
  - Pessimistic write lock protected withdrawals (`POST /api/accounts/{accountId}/withdrawals`)
  - Auditable transaction history with pagination, sorting & filtering (`GET /api/accounts/{accountId}/transactions`)
  - Cryptographic transaction reference lookup (`GET /api/transactions/{transactionReference}`)
  - Retail customer dashboard integration with live balance sync and masked account cards
  - Double-submit and idempotency protection in frontend Deposit & Withdrawal modal workflows
  - Comprehensive 275 backend tests + 15 frontend tests passing with 0 failures

---

## REST API Overview (Phase 6)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/accounts/{accountId}/deposits` | Deposit funds atomically with idempotency protection | Account Owner (`CUSTOMER`) |
| `POST` | `/api/accounts/{accountId}/withdrawals` | Withdraw funds with pessimistic lock and overdraft block | Account Owner (`CUSTOMER`) |
| `GET` | `/api/accounts/{accountId}/transactions` | Paginated, filtered account transaction history | Owner or Authorized Staff |
| `GET` | `/api/transactions/{transactionReference}` | Unique transaction reference lookup | Owner or Authorized Staff |
| `GET` | `/api/accounts/me` | List authenticated customer's bank accounts | Account Owner (`CUSTOMER`) |

Detailed documentation with request/response schemas is available in [`backend/API_DOCUMENTATION.md`](backend/API_DOCUMENTATION.md).

---

## Verification & Testing

### Backend Tests (275 tests, 0 failures, 0 errors)
```powershell
cd finova\backend
.\mvnw.cmd clean test
```

### Frontend Verification & Tests (15 tests, 0 failures, 0 lint warnings)
```powershell
cd finova\frontend
npm run lint      # ESLint verification
npm test          # Node.js automated test runner
npm run build     # Production Vite build
```

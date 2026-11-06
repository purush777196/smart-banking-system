\# Finova - Smart Banking \& Financial Management System



Finova is a full-stack banking management application built as an academic project.



\## Technology Stack



\- React.js

\- Vite

\- Tailwind CSS

\- Java

\- Spring Boot

\- Spring Security

\- JWT Authentication

\- Spring Data JPA

\- PostgreSQL



\## Features



1\. Role-based authentication

2\. 10 role-specific dashboards

3\. Customer management

4\. Account management

5\. Transaction management

6\. Loan management

7\. Fraud detection

8\. Finance management

9\. Audit and compliance

10\. Customer support

11\. Banking analytics



\## Project Progress



\- Phase 1: Project foundation — Completed

\- Phase 2: UI foundation and 10 dashboards — Completed

\- Phase 3: Authentication and authorization — Completed

\- Phase 4: Account & Transaction Operations — Completed (PR #7 through PR #11)

\## Transaction REST APIs (PR #11)

\- `POST /api/accounts/{accountId}/deposits`: Atomic, idempotent customer deposits
\- `POST /api/accounts/{accountId}/withdrawals`: Atomic, idempotent customer withdrawals with overdraft protection
\- `GET /api/accounts/{accountId}/transactions`: Paginated, sorted, and filtered transaction history
\- `GET /api/transactions/{transactionReference}`: Transaction reference lookup

See `backend/API_DOCUMENTATION.md` for full endpoint specifications.



\## Disclaimer



This is an academic/demo banking application using simulated banking data. It is not intended for real financial transactions.


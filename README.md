# 💸 UPI Payment System — Backend

<div align="center">

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

**A production-grade, fintech-quality UPI payment backend built with Spring Boot.**  
Featuring JWT authentication, double-entry ledger accounting, optimistic locking, idempotency support, and standardized API responses.

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Architecture](#-architecture)
- [Database Schema](#-database-schema)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [API Reference](#-api-reference)
  - [Auth APIs](#-auth-apis)
  - [Wallet APIs](#-wallet-apis)
- [Request & Response Examples](#-request--response-examples)
- [Error Handling](#-error-handling)
- [Security](#-security)
- [Idempotency](#-idempotency)
- [Optimistic Locking](#-optimistic-locking)
- [Double-Entry Ledger](#-double-entry-ledger)
- [Enums Reference](#-enums-reference)
- [Roadmap](#-roadmap)

---

## 🌐 Overview

This is an **industry-grade UPI (Unified Payments Interface) backend service** that simulates the core payment infrastructure used in modern fintech applications. It handles user authentication, wallet management, peer-to-peer money transfers, and maintains a complete financial audit trail using a double-entry ledger system.

The system is designed around **ACID compliance**, **concurrent-safe transfers**, and **clean API contracts** — all requirements for a production payment system.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔐 **JWT Authentication** | Stateless Bearer token auth with configurable expiry |
| 👛 **Wallet System** | One wallet per user, INR-denominated |
| 💸 **Money Transfer** | Atomic peer-to-peer transfers with full validation |
| 📒 **Double-Entry Ledger** | Every transfer creates a DEBIT + CREDIT ledger entry |
| 📜 **Transaction History** | Paginated history with debit/credit direction flag |
| 🔁 **Idempotency Keys** | `X-Idempotency-Key` header prevents duplicate submissions |
| 🔒 **Optimistic Locking** | `@Version` on Wallet prevents concurrent balance exploitation |
| ✅ **Bean Validation** | Request-level validation on all inputs |
| 🚨 **Global Exception Handling** | Typed exceptions mapped to clean HTTP status codes |
| 📦 **Standardized Responses** | All endpoints return `ApiResponse<T>` envelope |
| 🛡️ **Self-Transfer Prevention** | Cannot transfer to your own account |
| 💰 **Negative Transfer Prevention** | Amounts must be ≥ 0.01 INR |

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security + JJWT 0.11.5 |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL 16 |
| Validation | Jakarta Bean Validation |
| Build Tool | Maven |
| Utilities | Lombok |

---

## 📁 Project Structure

```
backend/auth-service/
├── pom.xml
└── src/
    └── main/
        ├── java/com/upi/auth_service/
        │   │
        │   ├── AuthServiceApplication.java          # Application entry point
        │   │
        │   ├── config/
        │   │   └── SecurityConfig.java              # Spring Security + JWT filter chain
        │   │
        │   ├── controller/
        │   │   ├── AuthController.java              # POST /auth/register, /auth/login
        │   │   └── WalletController.java            # Wallet & transfer endpoints
        │   │
        │   ├── dto/
        │   │   ├── ApiResponse.java                 # Universal response envelope <T>
        │   │   ├── AuthResponse.java                # { token }
        │   │   ├── LoginRequest.java                # { email, password }
        │   │   ├── RegisterRequest.java             # { name, email, phone, password }
        │   │   ├── TransferRequest.java             # { receiverEmail, amount }
        │   │   ├── TransferResponse.java            # Transfer result DTO
        │   │   ├── BalanceResponse.java             # { email, balance, currency }
        │   │   ├── WalletResponse.java              # Wallet creation result DTO
        │   │   ├── TransactionResponse.java         # Safe transaction view DTO
        │   │   └── PagedResponse.java               # Generic pagination wrapper <T>
        │   │
        │   ├── entity/
        │   │   ├── User.java                        # Users table
        │   │   ├── Wallet.java                      # Wallets table (@Version for OCC)
        │   │   ├── Transaction.java                 # Transactions table (idempotency key)
        │   │   ├── LedgerEntry.java                 # Ledger entries table
        │   │   ├── Role.java                        # Enum: USER, ADMIN
        │   │   ├── TransactionStatus.java           # Enum: PENDING, SUCCESS, FAILED
        │   │   └── LedgerType.java                  # Enum: DEBIT, CREDIT
        │   │
        │   ├── exception/
        │   │   ├── BusinessException.java           # Base typed exception with HttpStatus
        │   │   ├── InsufficientBalanceException.java # → 422 Unprocessable Entity
        │   │   ├── DuplicateTransactionException.java# → 409 Conflict
        │   │   └── GlobalExceptionHandler.java      # @RestControllerAdvice
        │   │
        │   ├── repository/
        │   │   ├── UserRepository.java
        │   │   ├── WalletRepository.java
        │   │   ├── TransactionRepository.java       # findByIdempotencyKey, findByWallet
        │   │   └── LedgerRepository.java            # findByTransactionId
        │   │
        │   ├── security/
        │   │   ├── JwtService.java                  # Token generation & extraction
        │   │   ├── JwtAuthenticationFilter.java     # OncePerRequestFilter
        │   │   └── CustomUserDetailsService.java    # UserDetailsService impl
        │   │
        │   └── service/
        │       ├── AuthService.java                 # Registration & login logic
        │       └── WalletService.java               # All financial operations
        │
        └── resources/
            └── application.yml                      # DB, JPA, JWT config
```

---

## 🏛 Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     HTTP Client                         │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│              JwtAuthenticationFilter                    │
│   Validates Bearer token → populates SecurityContext    │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌──────────────────┐  ┌──────────────────────────────────┐
│  AuthController  │  │         WalletController          │
│  /auth/**        │  │  /wallet/**                       │
└────────┬─────────┘  └───────────────┬──────────────────┘
         │                            │
         ▼                            ▼
┌──────────────────┐  ┌──────────────────────────────────┐
│   AuthService    │  │          WalletService            │
│  register/login  │  │  createWallet / addMoney          │
└────────┬─────────┘  │  transferMoney / getTransactions  │
         │            └───────────────┬──────────────────┘
         │                            │
         ▼                            ▼
┌─────────────────────────────────────────────────────────┐
│                    JPA Repositories                     │
│   UserRepo  WalletRepo  TransactionRepo  LedgerRepo     │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                     PostgreSQL                          │
│   users  wallets  transactions  ledger_entries          │
└─────────────────────────────────────────────────────────┘
```

---

## 🗄 Database Schema

### `users`
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| name | VARCHAR | NOT NULL |
| email | VARCHAR | NOT NULL, UNIQUE |
| phone | VARCHAR | NOT NULL, UNIQUE |
| password | VARCHAR | NOT NULL (BCrypt) |
| role | VARCHAR | NOT NULL (USER/ADMIN) |
| created_at | TIMESTAMP | NOT NULL |

### `wallets`
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| user_id | BIGINT | FK → users.id, UNIQUE |
| balance | DECIMAL(19,4) | NOT NULL |
| version | BIGINT | NOT NULL (optimistic lock) |
| created_at | TIMESTAMP | NOT NULL |

### `transactions`
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| transaction_id | VARCHAR | NOT NULL, UNIQUE |
| idempotency_key | VARCHAR | UNIQUE, NULLABLE |
| sender_wallet_id | BIGINT | FK → wallets.id |
| receiver_wallet_id | BIGINT | FK → wallets.id |
| amount | DECIMAL(19,4) | NOT NULL |
| status | VARCHAR | NOT NULL (PENDING/SUCCESS/FAILED) |
| created_at | TIMESTAMP | NOT NULL |

### `ledger_entries`
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| transaction_id | VARCHAR | |
| wallet_id | BIGINT | FK → wallets.id |
| type | VARCHAR | DEBIT or CREDIT |
| amount | DECIMAL(19,4) | |
| created_at | TIMESTAMP | |

---

## 🚀 Getting Started

### Prerequisites

- **Java 21+** — [Download](https://adoptium.net/)
- **Maven 3.9+** — [Download](https://maven.apache.org/download.cgi)
- **PostgreSQL 14+** — [Download](https://www.postgresql.org/download/)

### 1. Clone the repository

```bash
git clone https://github.com/your-username/upi-system.git
cd upi-system/backend/auth-service
```

### 2. Create the PostgreSQL database

```sql
CREATE DATABASE upi_auth;
```

### 3. Configure the application

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/upi_auth
    username: your_postgres_username
    password: your_postgres_password

jwt:
  secret: your_minimum_32_character_secret_key_here
```

> Hibernate is configured with `ddl-auto: update` — tables are created automatically on first run.

### 4. Run the application

```bash
mvn spring-boot:run
```

The server starts at **http://localhost:8081**

---

## ⚙️ Configuration

Full `application.yml` reference:

```yaml
server:
  port: 8081                          # HTTP port

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/upi_auth
    username: postgres
    password: your_password

  jpa:
    hibernate:
      ddl-auto: update                # auto-creates/updates schema
    show-sql: true                    # logs SQL queries
    properties:
      hibernate:
        format_sql: true              # pretty-prints SQL

jwt:
  secret: mySuperSecureJwtSecretKey   # min 32 chars recommended
```

---

## 📡 API Reference

> **Base URL:** `http://localhost:8081`  
> **Content-Type:** `application/json`  
> **Auth:** Bearer token required on all `/wallet/**` endpoints

---

### 🔑 Auth APIs

#### `POST /auth/register`

Register a new user account.

- **Auth required:** ❌ No
- **Request Body:**

```json
{
  "name": "Avinash Kumar",
  "email": "avinash@example.com",
  "phone": "9876543210",
  "password": "SecurePass123"
}
```

- **Validation rules:**
  - `name` — must not be blank
  - `email` — must be a valid email format
  - `phone` — must not be blank
  - `password` — must not be blank

- **Response `201 Created`:**

```json
{
  "success": true,
  "message": "Account registered successfully.",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

#### `POST /auth/login`

Authenticate and receive a JWT token.

- **Auth required:** ❌ No
- **Request Body:**

```json
{
  "email": "avinash@example.com",
  "password": "SecurePass123"
}
```

- **Response `200 OK`:**

```json
{
  "success": true,
  "message": "Login successful.",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

### 👛 Wallet APIs

> All wallet endpoints require `Authorization: Bearer <token>` header.

---

#### `POST /wallet/create`

Create a wallet for the authenticated user. One wallet per user is allowed.

- **Auth required:** ✅ Yes
- **Request Body:** None
- **Response `201 Created`:**

```json
{
  "success": true,
  "message": "Wallet created successfully.",
  "data": {
    "walletId": 1,
    "ownerEmail": "avinash@example.com",
    "balance": 0,
    "createdAt": "2026-05-25T16:30:00"
  }
}
```

---

#### `GET /wallet/balance`

Get the current balance for the authenticated user's wallet.

- **Auth required:** ✅ Yes
- **Response `200 OK`:**

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "email": "avinash@example.com",
    "balance": 5000.00,
    "currency": "INR"
  }
}
```

---

#### `POST /wallet/add-money`

Add money to the authenticated user's wallet.

- **Auth required:** ✅ Yes
- **Query Params:**

| Param | Type | Required | Validation |
|---|---|---|---|
| `amount` | `BigDecimal` | ✅ | Must be ≥ 0.01 |

- **Example:**
```
POST /wallet/add-money?amount=1000.00
```

- **Response `200 OK`:**

```json
{
  "success": true,
  "message": "Money added successfully.",
  "data": null
}
```

---

#### `POST /wallet/transfer`

Transfer money to another user by email.

- **Auth required:** ✅ Yes
- **Headers:**

| Header | Required | Description |
|---|---|---|
| `Authorization` | ✅ | `Bearer <token>` |
| `X-Idempotency-Key` | ❌ Optional | Unique string to prevent duplicate processing |

- **Request Body:**

```json
{
  "receiverEmail": "bob@example.com",
  "amount": 250.00
}
```

- **Validation rules:**
  - `receiverEmail` — must not be blank, must be valid email
  - `amount` — must not be null, must be ≥ 0.01, max 10 integer digits, 2 decimal places

- **Response `200 OK`:**

```json
{
  "success": true,
  "message": "Transfer completed successfully.",
  "data": {
    "transactionId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "senderEmail": "avinash@example.com",
    "receiverEmail": "bob@example.com",
    "amount": 250.00,
    "status": "SUCCESS",
    "timestamp": "2026-05-25T16:30:00"
  }
}
```

- **Error cases:**

| Scenario | HTTP | Message |
|---|---|---|
| Self-transfer | `400` | Cannot transfer money to your own account. |
| Negative/zero amount | `400` | Transfer amount must be at least 0.01 |
| Receiver not found | `404` | Receiver account not found for email: ... |
| Insufficient balance | `422` | Insufficient balance. Available: X INR. |
| Duplicate idempotency key | `409` | Transaction with idempotency key '...' was already processed. |
| Concurrent modification | `409` | Concurrent modification detected. Please retry the transfer. |

---

#### `GET /wallet/transactions`

Retrieve paginated transaction history for the authenticated user.

- **Auth required:** ✅ Yes
- **Query Params:**

| Param | Type | Default | Description |
|---|---|---|---|
| `page` | `int` | `0` | Zero-based page number |
| `size` | `int` | `10` | Number of records per page |

- **Example:**
```
GET /wallet/transactions?page=0&size=10
```

- **Response `200 OK`:**

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "transactionId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
        "senderEmail": "avinash@example.com",
        "receiverEmail": "bob@example.com",
        "amount": 250.00,
        "status": "SUCCESS",
        "timestamp": "2026-05-25T16:30:00",
        "debit": true
      },
      {
        "transactionId": "a1b2c3d4-...",
        "senderEmail": "charlie@example.com",
        "receiverEmail": "avinash@example.com",
        "amount": 1000.00,
        "status": "SUCCESS",
        "timestamp": "2026-05-25T15:00:00",
        "debit": false
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1,
    "last": true
  }
}
```

> 💡 The `debit` field is `true` when the authenticated user was the **sender**, `false` when they were the **receiver**. Use this to render ➖ / ➕ indicators in your UI.

---

## 🧱 Request & Response Examples

### Complete Transfer Flow (curl)

```bash
# 1. Register Alice
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","phone":"9000000001","password":"Pass1234"}'

# 2. Register Bob
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob","email":"bob@example.com","phone":"9000000002","password":"Pass1234"}'

# 3. Login as Alice (save token)
TOKEN=$(curl -s -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"Pass1234"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")

# 4. Create Alice's wallet
curl -X POST http://localhost:8081/wallet/create \
  -H "Authorization: Bearer $TOKEN"

# 5. Add ₹2000 to Alice's wallet
curl -X POST "http://localhost:8081/wallet/add-money?amount=2000" \
  -H "Authorization: Bearer $TOKEN"

# 6. Transfer ₹500 to Bob (with idempotency key)
curl -X POST http://localhost:8081/wallet/transfer \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: alice-to-bob-001" \
  -d '{"receiverEmail":"bob@example.com","amount":500}'

# 7. Check Alice's balance
curl http://localhost:8081/wallet/balance \
  -H "Authorization: Bearer $TOKEN"

# 8. View Alice's transaction history
curl "http://localhost:8081/wallet/transactions?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🚨 Error Handling

All errors follow the same `ApiResponse<T>` envelope:

```json
{
  "success": false,
  "message": "Human-readable description of what went wrong.",
  "data": null
}
```

### HTTP Status Code Map

| Exception | HTTP Status | Scenario |
|---|---|---|
| `BusinessException` | Configurable | General domain errors (400 / 401 / 404 / 409 / 500) |
| `InsufficientBalanceException` | `422 Unprocessable Entity` | Wallet balance < transfer amount |
| `DuplicateTransactionException` | `409 Conflict` | Reused idempotency key or concurrent write |
| `MethodArgumentNotValidException` | `400 Bad Request` | Bean Validation failure on request body |
| `ConstraintViolationException` | `400 Bad Request` | Constraint violation on query params |
| `OptimisticLockingFailureException` | `409 Conflict` | Race condition on wallet update |
| `Exception` (catch-all) | `500 Internal Server Error` | Unexpected error — no stack trace leaked |

### Validation Error Example

```json
{
  "success": false,
  "message": "receiverEmail: must be a valid email address, amount: Transfer amount must be at least 0.01",
  "data": null
}
```

---

## 🔐 Security

### Authentication Flow

```
Client                     Server
  │                           │
  │  POST /auth/login         │
  │ ─────────────────────── ▶ │
  │                           │  Validate credentials
  │                           │  Generate JWT (24h expiry)
  │  { token: "eyJ..." }      │
  │ ◀ ─────────────────────── │
  │                           │
  │  GET /wallet/balance      │
  │  Authorization: Bearer    │
  │ ─────────────────────── ▶ │
  │                           │  JwtAuthenticationFilter
  │                           │  → extract email from token
  │                           │  → load UserDetails
  │                           │  → set SecurityContext
  │  { balance: 5000 }        │
  │ ◀ ─────────────────────── │
```

### JWT Details

| Property | Value |
|---|---|
| Algorithm | HMAC-SHA256 (HS256) |
| Token Expiry | 24 hours |
| Subject Claim | User email |
| Header name | `Authorization: Bearer <token>` |

### Password Storage

Passwords are hashed using **BCrypt** before storage. Raw passwords are never persisted or logged.

---

## 🔁 Idempotency

The `/wallet/transfer` endpoint supports client-supplied idempotency keys via the `X-Idempotency-Key` header.

**How it works:**

```
Request 1 (new key "pay-001"):
  → Transfer processes normally → 200 OK

Request 2 (same key "pay-001"):
  → Key found in DB → 409 Conflict returned instantly
  → No money is moved
```

**When to use it:**
- Network timeouts where you're unsure if the request succeeded
- Retry logic in mobile/frontend apps
- Preventing accidental double-taps on payment buttons

**Key requirements:**
- Must be unique per intended payment
- Any string format works (UUID recommended)
- Optional — omitting the header disables duplicate detection

---

## 🔒 Optimistic Locking

The `Wallet` entity uses JPA's `@Version` field for **Optimistic Concurrency Control (OCC)**.

```java
@Version
@Column(nullable = false)
private Long version;
```

**How it prevents balance exploitation:**

```
Thread A reads wallet (version=5, balance=1000)
Thread B reads wallet (version=5, balance=1000)

Thread A saves (version → 6, balance=500) ✅
Thread B tries to save (version=5 → STALE) ❌
  → OptimisticLockingFailureException
  → Caught → DuplicateTransactionException (409)
```

This eliminates the need for pessimistic database locks while still guaranteeing balance integrity under concurrent load.

---

## 📒 Double-Entry Ledger

Every transfer creates **two ledger entries** atomically within the same `@Transactional` block:

```
Transfer: Alice → Bob, ₹500

ledger_entries:
┌────────────────┬──────────┬────────┬────────┐
│ transaction_id │ wallet   │ type   │ amount │
├────────────────┼──────────┼────────┼────────┤
│ uuid-xxx       │ Alice    │ DEBIT  │ 500.00 │
│ uuid-xxx       │ Bob      │ CREDIT │ 500.00 │
└────────────────┴──────────┴────────┴────────┘
```

This mirrors real-world double-entry bookkeeping and provides:
- Full audit trail for every rupee
- Ability to reconcile wallet balances against ledger totals
- Compliance-friendly financial records

---

## 📖 Enums Reference

### `Role`
| Value | Description |
|---|---|
| `USER` | Standard user (default on registration) |
| `ADMIN` | Administrator |

### `TransactionStatus`
| Value | Description |
|---|---|
| `PENDING` | Transaction initiated but not settled |
| `SUCCESS` | Transfer completed successfully |
| `FAILED` | Transfer failed (reserved for async flows) |

### `LedgerType`
| Value | Description |
|---|---|
| `DEBIT` | Money leaving a wallet |
| `CREDIT` | Money entering a wallet |

---

## 🗺 Roadmap

- [ ] UPI PIN verification layer
- [ ] Transaction limits (daily/monthly caps)
- [ ] Scheduled/recurring transfers
- [ ] Admin dashboard APIs
- [ ] Notification hooks (SMS/email on transfer)
- [ ] Redis-backed idempotency with TTL expiry
- [ ] Rate limiting per user
- [ ] Swagger / OpenAPI documentation
- [ ] Dockerization + Docker Compose
- [ ] Unit & integration test suite

---

## 📄 License

This project is licensed under the **MIT License**.

---

<div align="center">

Built with ❤️ using Spring Boot · PostgreSQL · JWT

</div>

# Credit Card Management System

A fully-featured Spring Boot application simulating credit card lifecycle management with JWT security and Quartz Scheduler.

## Prerequisites

- **Java 21 LTS** — [Download Temurin 21](https://adoptium.net/temurin/releases/?version=21)
- **Maven 3.9+** — `brew install maven` (macOS)

## Quick Start

```bash
# 1. Clone the repository
cd CreditCardManagmentSystem

# 2. Set up Java 21 environment
source ./setup-java21.sh

# 3. Build and run the application
mvn spring-boot:run
```

Server starts at: **http://localhost:8080**  
H2 Console: **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:creditcarddb`)

**Note**: Always run `source ./setup-java21.sh` before executing Maven commands to ensure Java 21 is used.

## Default Users

| Username | Password | Role       |
|----------|----------|------------|
| admin    | admin123 | ADMIN+USER |
| john     | john123  | USER       |
| jane     | jane123  | USER       |

---

## API Reference

### Auth Endpoints (Public)

| Method | Endpoint             | Description           |
|--------|----------------------|-----------------------|
| POST   | /api/auth/register   | Register new user     |
| POST   | /api/auth/login      | Login & receive JWT   |

### Card Endpoints

| Method | Endpoint                    | Role  | Description              |
|--------|-----------------------------|-------|--------------------------|
| POST   | /api/cards/issue            | ADMIN | Issue new credit card    |
| GET    | /api/cards/{cardId}         | USER  | Get card details         |
| GET    | /api/cards/user/{userId}    | USER  | Get cards by user        |
| GET    | /api/cards/all              | ADMIN | Get all active cards     |
| PUT    | /api/cards/{cardId}/block   | ADMIN | Block a card             |
| PUT    | /api/cards/{cardId}/activate| ADMIN | Activate a card          |

### Transaction Endpoints

| Method | Endpoint                                 | Role | Description             |
|--------|------------------------------------------|------|-------------------------|
| POST   | /api/transactions/process                | USER | Process a transaction   |
| GET    | /api/transactions/card/{cardNumber}      | USER | Get card transactions   |
| GET    | /api/transactions/card/{num}/range       | USER | Transactions by date    |
| GET    | /api/transactions/{id}                   | USER | Get single transaction  |

### Statement Endpoints

| Method | Endpoint                           | Role  | Description                  |
|--------|------------------------------------|-------|------------------------------|
| GET    | /api/statements/card/{cardId}      | USER  | Get card statements          |
| GET    | /api/statements/{statementId}      | USER  | Get statement details        |
| POST   | /api/statements/generate/{cardId}  | ADMIN | Generate statement manually  |
| POST   | /api/statements/generate-all       | ADMIN | Trigger bulk generation      |

---

## Sample API Calls

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 2. Issue Credit Card
```bash
curl -X POST http://localhost:8080/api/cards/issue \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"userId": 2, "creditLimit": 5000.00}'
```

### 3. Process Transaction
```bash
curl -X POST http://localhost:8080/api/transactions/process \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4XXX XXXX XXXX XXXX",
    "amount": 150.00,
    "merchantName": "Amazon",
    "description": "Electronics purchase",
    "type": "PURCHASE"
  }'
```

### 4. Generate Monthly Statement
```bash
curl -X POST http://localhost:8080/api/statements/generate/1 \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   CLIENT (REST / Postman)                │
└──────────────────────────┬──────────────────────────────┘
                           │ HTTP + JWT Bearer
┌──────────────────────────▼──────────────────────────────┐
│         Spring Security Filter Chain (JWT Auth)          │
└──────────────────────────┬──────────────────────────────┘
              ┌────────────┼──────────────┐
     ┌────────▼──┐  ┌──────▼──────┐ ┌────▼────────────┐
     │AuthCtrl   │  │CardCtrl     │ │TransactionCtrl  │
     │/api/auth  │  │/api/cards   │ │/api/transactions│
     └────────┬──┘  └──────┬──────┘ └────┬────────────┘
              └────────────┼──────────────┘
                           │
              ┌────────────┼──────────────┐
     ┌────────▼──┐  ┌──────▼──────┐ ┌────▼──────────┐
     │AuthService│  │CardService  │ │TxnService     │
     └────────┬──┘  └──────┬──────┘ └────┬──────────┘
              └────────────┼──────────────┘
                           │
     ┌─────────────────────▼─────────────────────────┐
     │           H2 In-Memory Database (JPA)          │
     │  Users | CreditCards | Transactions | Statements│
     └─────────────────────┬─────────────────────────┘
                           │
     ┌─────────────────────▼─────────────────────────┐
     │    Quartz Scheduler – Cron: 0 0 1 1 * ?        │
     │    Runs at 1 AM on 1st of every month          │
     │    Auto-generates statements for all cards     │
     └───────────────────────────────────────────────┘
```

## Key Learning Points

- **Card Issuance**: Simulates real card lifecycle with status management (ACTIVE, BLOCKED, EXPIRED)
- **Transaction Processing**: Real-time balance validation, multi-type support (PURCHASE, PAYMENT, REFUND, CASH_ADVANCE, FEE)
- **Quartz Scheduler**: Cron-based monthly statement generation (`0 0 1 1 * ?`)
- **JWT Security**: Stateless auth with role-based access control (ADMIN vs USER)
- **Spring Security**: Method-level security with `@PreAuthorize`

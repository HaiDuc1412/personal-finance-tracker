# Personal Finance Tracker

A RESTful backend API for personal finance management, built with **Spring Boot 4.1.0** and **Java 21**. Designed as a portfolio project targeting Java Backend positions in banking/fintech.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.1.0, Java 21, Maven |
| Database | PostgreSQL 15 + Flyway |
| Security | Spring Security + JWT (JJWT 0.12.6) |
| Cache | Spring Cache (in-memory) |
| Email | Spring Mail (Gmail SMTP) |
| API Docs | SpringDoc OpenAPI 3.0.0 |
| Infrastructure | Docker + Docker Compose |

---

## Features

- **Authentication** — Register, Login, Refresh Token (JWT-based)
- **Category Management** — System-wide and user-defined INCOME/EXPENSE categories
- **Transaction Management** — CRUD with filtering by type, category, date range and pagination
- **Budget Management** — Monthly budget per category, with automatic alert tracking
- **Budget Alert Emails** — Automatic email notification when spending reaches 80% and 100% of budget limit
- **Dashboard** — Monthly summary (income, expense, net), budget overview, recent transactions, expense breakdown by category
- **Export** — Download transactions as CSV or PDF

---

## Project Structure

```
src/main/java/com/haiduc/personalfinancetracker/
├── auth/               # Login, Register, Refresh Token
├── user/               # User entity
├── category/           # Category API + caching
├── transaction/        # Transaction API + budget alert trigger
├── budget/             # Budget API + email alert service
│   ├── event/          # BudgetAlertEvent
│   └── EmailService    # JavaMailSender implementation
├── dashboard/          # Dashboard summary API + caching
├── export/             # CSV & PDF export
└── config/             # Security, OpenAPI config
```

---

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- Docker & Docker Compose
- Gmail account (for email alerts)

### 1. Clone the repository

```bash
git clone https://github.com/HaiDuc1412/personal-finance-tracker.git
cd personal-finance-tracker
```

### 2. Configure environment variables

Copy the example file and fill in your values:

```bash
cp .env.example .env
```

| Variable | Description |
|---|---|
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Base64-encoded JWT secret key |
| `MAIL_HOST` | SMTP host (e.g. `smtp.gmail.com`) |
| `MAIL_PORT` | SMTP port (e.g. `587`) |
| `MAIL_USERNAME` | Gmail address |
| `MAIL_PASSWORD` | Gmail App Password |
| `MAIL_FROM` | Sender email address |

### 3. Start PostgreSQL

```bash
docker-compose up -d
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

### 5. Access API docs

```
http://localhost:8080/swagger-ui/index.html
```

---

## API Overview

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Register new user |
| `POST` | `/api/v1/auth/login` | Login, get access + refresh token |
| `POST` | `/api/v1/auth/refresh` | Refresh access token |
| `GET` | `/api/v1/categories` | List categories |
| `POST` | `/api/v1/categories` | Create custom category |
| `GET` | `/api/v1/transactions` | List transactions (paginated, filterable) |
| `POST` | `/api/v1/transactions` | Create transaction |
| `PUT` | `/api/v1/transactions/{id}` | Update transaction |
| `DELETE` | `/api/v1/transactions/{id}` | Soft-delete transaction |
| `GET` | `/api/v1/budgets` | List budgets for a month |
| `POST` | `/api/v1/budgets` | Create budget |
| `PUT` | `/api/v1/budgets/{id}` | Update budget |
| `DELETE` | `/api/v1/budgets/{id}` | Delete budget |
| `GET` | `/api/v1/dashboard` | Get dashboard summary |
| `GET` | `/api/v1/export/csv` | Export transactions as CSV |
| `GET` | `/api/v1/export/pdf` | Export transactions as PDF |

---

## Business Rules

- Budgets can only be set for **EXPENSE** categories
- Each user can have **one budget per category per month**
- Budget alert emails are sent **once** when spending crosses 80%, and **once** when crossing 100%
- Alert flags reset automatically when a budget is updated
- Caching is applied on dashboard, budget list, and category list for performance

---

## License

This project is for portfolio/learning purposes.

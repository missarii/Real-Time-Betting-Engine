# Implementation Plan: Real-Time Betting Engine (Spring Boot + Thymeleaf)

This plan outlines the design, architecture, database schemas, messaging architecture, and step-by-step construction of a production-grade, highly concurrent sports betting application.

---

## 🏗️ Architecture Design & Flow

### 1. Database Schema
To handle high concurrency and financial transactions safely, we use PostgreSQL with explicit indexing, strict constraints, and **Optimistic Locking** on wallets and odds.

```mermaid
erDiagram
    USERS ||--|| WALLETS : has
    USERS ||--o{ BETS : places
    WALLETS ||--o{ TRANSACTIONS : records
    EVENTS ||--o{ ODDS : contains
    EVENTS ||--o{ BETS : target
    ODDS ||--o{ BETS : matches
```

#### SQL Schema Details (`sql/schema.sql`)
- **`users`**:
  - `id` (UUID, Primary Key)
  - `username` (VARCHAR(50), Unique, Indexed)
  - `email` (VARCHAR(100), Unique)

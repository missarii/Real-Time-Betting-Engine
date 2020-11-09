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
  - `password` (VARCHAR(100))
  - `role` (VARCHAR(20), e.g., 'USER', 'ADMIN')
  - `created_at` (TIMESTAMP)
- **`wallets`**:
  - `id` (UUID, Primary Key)
  - `user_id` (UUID, Foreign Key, Unique, Indexed)
  - `balance` (NUMERIC(15,2), Check >= 0)
  - `currency` (VARCHAR(3), Default 'USD')
  - `version` (INT, Optimistic Lock Version)
- **`transactions`**:
  - `id` (UUID, Primary Key)
  - `wallet_id` (UUID, Foreign Key, Indexed)
  - `type` (VARCHAR(20), e.g., 'DEPOSIT', 'WITHDRAW', 'BET_PLACED', 'BET_SETTLED', 'BET_REFUNDED')
  - `amount` (NUMERIC(15,2))
  - `reference_id` (UUID, Nullable, e.g., Bet ID or external Payment ID)
  - `description` (TEXT)
  - `created_at` (TIMESTAMP)
- **`events`**:
  - `id` (UUID, Primary Key)
  - `sport` (VARCHAR(50), Indexed)
  - `home_team` (VARCHAR(100))
  - `away_team` (VARCHAR(100))
  - `home_score` (INT, Default 0)
  - `away_score` (INT, Default 0)
  - `status` (VARCHAR(20), e.g., 'SCHEDULED', 'LIVE', 'SUSPENDED', 'FINISHED', Indexed)
  - `start_time` (TIMESTAMP, Indexed)
  - `ended_at` (TIMESTAMP, Nullable)
- **`odds`**:
  - `id` (UUID, Primary Key)
  - `event_id` (UUID, Foreign Key, Indexed)
  - `market_name` (VARCHAR(50), e.g., '1X2', 'OVER_UNDER')
  - `selection_name` (VARCHAR(50), e.g., 'HOME_WIN', 'DRAW', 'AWAY_WIN')
  - `odds_value` (NUMERIC(6,2), Check > 1.0)
  - `status` (VARCHAR(20), e.g., 'ACTIVE', 'SUSPENDED')
  - `version` (INT, Optimistic Lock Version)
- **`bets`**:
  - `id` (UUID, Primary Key)
  - `user_id` (UUID, Foreign Key, Indexed)
  - `event_id` (UUID, Foreign Key, Indexed)
  - `odds_id` (UUID, Foreign Key)
  - `selection_name` (VARCHAR(50))
  - `odds_value` (NUMERIC(6,2))
  - `stake` (NUMERIC(15,2))
  - `potential_payout` (NUMERIC(15,2))
  - `status` (VARCHAR(20), e.g., 'PENDING', 'WON', 'LOST', 'VOIDED', Indexed)
  - `placed_at` (TIMESTAMP)
  - `settled_at` (TIMESTAMP, Nullable)

---

### 2. Kafka Event Flow
Kafka serves as our asynchronous messaging backbone. It decouples high-throughput betting operations and settlements from the main user-facing Web API threads.

```
[Web API: Bet Placement]
       │
       ▼ (Database Transaction: Deduct Wallet + Save PENDING Bet)
[Publish to Kafka: "bet-placed" Topic]
       │
       ├────────────────────────────────────┐
       ▼                                    ▼
[Kafka Consumer: Risk Service]    [Kafka Consumer: Metrics/Audit]
```

```
[Admin Service: Settle Match]
       │
       ▼ (Database: Match status -> FINISHED)
[Publish to Kafka: "event-settlement" Topic]
       │
       ▼
[Kafka Consumer: Settlement Service]
       │
       ├─► Loop all bets for event -> Calculate win/loss
       ├─► Update bet status (WON/LOST)
       ├─► Credit winning wallets (+ Version increment)
       └─► Publish to Kafka: "bet-settled" Topic -> Alerts user via WebSocket
```

- **Topics**:
  - `odds-updates`: Emitted whenever odds change. Listened to by WebSocket service to push updates to client browsers.
  - `bet-placements`: Emitted when a bet is validated and locked in the database. Used for auditing, risk assessment, and tracking.
  - `bet-settlements`: Emitted when an event completes to trigger asynchronous bulk settlement calculations and wallet crediting.

---

### 3. Caching Architecture (Redis)
- **Odds Cache**: Read-heavy operations (e.g., fetching active matches and odds) will read from Redis first. Cache is updated on any odds change (`cache-aside` + `write-through` on odds-updates).
- **Wallet Buffer**: Current balance is cached in Redis for fast page-render checks. DB acts as transactional source-of-truth.
- **Session Store**: User sessions are persisted in Redis to support horizontal scalability.

---

### 4. WebSocket Real-Time Updates
- **Endpoint**: `/ws/odds`
- **Behavior**: Client connects on dashboard/events page. When an `odds-updates` event occurs, the WebSocket Handler broadcasts a JSON message containing the event ID, market name, selection, and new odds.
- **UI Animation**: When an update is received, the corresponding HTML elements blink:
  - **Green** for odds increase.
  - **Red** for odds decrease.
  - **Gray/Lock Icon** if odds are suspended.

---

## 🚦 REST API Endpoints

### Authentication & Users
- `POST /api/auth/register` (Register user)
- `POST /api/auth/login` (Login, returns JWT or sets session cookie)
- `POST /api/auth/logout` (Log out user)

### Sports Events
- `GET /api/events` (Fetch active and live events)
- `GET /api/events/{id}` (Fetch event details)

### Betting

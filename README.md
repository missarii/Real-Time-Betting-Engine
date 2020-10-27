# 🏗️ Real-Time Betting Engine (iGaming Monolith)

A production-grade, highly concurrent sports betting engine built as a Spring Boot and Thymeleaf monolith. This application demonstrates the core high-concurrency, transactional safety, caching, and real-time streaming architectures required for enterprise sportsbook environments.

---

## ⚡ High-Concurrency & Architectural Highlights

### 1. Financial Transactional Safety (Anti-Double-Spending)
In real-world sportsbooks, users can trigger rapid, parallel requests to place bets or withdraw funds. 
- **Optimistic Locking**: The `Wallet` and `Odds` database tables employ Hibernate `@Version` columns.
- **Race Condition Prevention**: If two concurrent transactions attempt to deduct/withdraw from the same wallet, the second transaction is immediately blocked and rolled back with an `ObjectOptimisticLockingFailureException`.
- **Double-Spending Defense**: Checks are enforced inside atomic database transactions (`@Transactional`) to lock stakes and verify balances before modifying balances.

### 2. Cache-Aside Odds Feeds (Redis Caching)
Sportsbooks suffer immense read-to-write ratios (millions of users reading active matches vs. periodic score and odds updates).
- Read requests for active match odds look up values in the **Redis Cache** first.
- On cache misses, values are fetched from the database, written to Redis with a 60-minute TTL, and returned.
- On any admin odds updates, the cache is evicted/updated (`write-through`), preventing stale reads.

### 3. Asynchronous Decoupling (Apache Kafka Event Streams)
Decoupling transactional execution from heavy risk-audits or bulk settlements is vital for scaling.
- **`bet-placements`**: Fired when a bet slip is validated and recorded. Listened to asynchronously by a consumer to simulate auditing and risk management.
- **`bet-settlements`**: Emitted upon match completion to trigger background bulk settlements.
- **`odds-updates`**: Emitted on live odds price changes.

### 4. Reactive Real-Time Blinks (WebSockets)
- When an admin adjusts a match price, the change publishes an `OddsUpdatedEvent` to Kafka.
- The `BetConsumer` receives this event in the background and broadcasts the payload to all active browser sessions via `ws://localhost:8080/ws/odds`.
- The user sportsbooks dynamically update odds values on-screen and perform **green flash animations for price increases** or **red flashes for price decreases**.

---

## 🏗️ System Architecture Flow

```
[User Web Browser] 
       │ 
       ├─► (HTTPS Post) ──► [BettingController] ──► (Lock Wallet Balance + Save Bet) ──► [Publish to Kafka]
       │
       └─► (WS Connection) ◄── [OddsWebSocketHandler] ◄── [BetConsumer] ◄── [Kafka odds-updates Topic]
```

---

## ⚙️ Technology Stack
- **Core Platform**: Java 17, Spring Boot 3.2.5, Spring MVC
- **Security & Session**: Spring Security (Form-based session authentication with cookie protection)
- **Database**: PostgreSQL 15, Spring Data JPA, Hibernate, H2 (for self-contained tests)
- **Cache**: Redis 7
- **Messaging**: Apache Kafka 3.7 (decoupled broker KRaft mode)
- **Frontend**: Thymeleaf (Server-Side Rendering), HTML5, Vanilla CSS3 (Glassmorphism & animations), Vanilla JS (WebSockets & UI updates)

---

## 📁 Repository Structure
```bash
betting-engine/
│
├── docker/
│   └── docker-compose.yml       # PostgreSQL, Redis, KRaft Kafka containers
│
├── sql/
│   ├── schema.sql               # Production database tables & indexes
│   └── seed.sql                 # Sample users, wallets, matches, and odds
│
├── src/
│   ├── main/
│   │   ├── java/com/betting/
│   │   │   ├── BettingEngineApplication.java
│   │   │   │
│   │   │   ├── config/          # Spring Security, Redis, Kafka, WebSockets
│   │   │   ├── controller/      # MVC & REST Mappings (Auth, Wallet, Betting, Admin)
│   │   │   ├── dto/             # Validation Requests & Event Payloads
│   │   │   ├── exception/       # Concurrency and Wallet Business Exception Handlers
│   │   │   ├── messaging/       # Kafka Event Producers and Consumers
│   │   │   ├── model/           # JPA Entities (User, Wallet, Transaction, Event, Odds, Bet)
│   │   │   ├── repository/      # JPA Data Layers
│   │   │   ├── service/         # Business & Wallet logic, custom user details, settlements
│   │   │   └── websocket/       # Session tracking & message broadcasts
│   │   │
│   │   └── resources/
│   │       ├── static/          # style.css and app.js WebSockets handlers
│   │       ├── templates/       # Thymeleaf MVC View templates
│   │       └── application.yml  # Database, Cache, and Event broker settings
│   │
│   └── test/
│       ├── java/com/betting/    # Standalone JUnit tests (concurrency wallet locks)
│       └── resources/           # application-test.yml (isolated H2 config)
│
└── pom.xml                      # Maven dependencies
```

---

## 🚦 REST API Specifications

### Authentication & Users
- `POST /register`: Registers a new user and creates their default $1,000 USD wallet.
- `POST /login`: Performs session authentication.
- `GET /logout`: Securely terminates the session and deletes cookie configurations.

### Sportsbook & Bets
- `POST /api/bets`: Places a live bet ticket.
  - *Payload*:
    ```json
    {
      "eventId": "33333333-3333-3333-3333-333333333333",
      "oddsId": "33333333-3333-3333-3333-333333333301",
      "selectionName": "HOME_WIN",
      "oddsValue": 1.65,
      "stake": 100.00
    }
    ```
- `GET /bets`: Fetches chronological bet slips.

### Wallet
- `GET /wallet`: Renders statement lists.
- `POST /wallet/deposit`: Deposits funds.
- `POST /wallet/withdraw`: Withdraws payouts.

### Control Panel (Role: ADMIN)
- `POST /admin/events`: Seeds a new sports fixture and its starting decimal odds.
- `POST /admin/events/{id}/score`: Updates a live scoreline and status state (e.g. SUSPENDED, LIVE).
- `POST /admin/events/{id}/odds`: Fluctuates decimal prices (updates cache + sends Kafka event).

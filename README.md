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
- `POST /admin/events/{id}/settle`: Triggers settlement payouts to all winning tickets in play.

---

## 🚀 How to Run the Project Locally

### 1. Prerequisites
Make sure you have the following installed:
- **Java 17+ SDK**
- **Maven**
- **Docker & Docker Compose**

### 2. Launch Infrastructure Services
Spin up PostgreSQL, Redis, and Kafka in the background:
```bash
# From the project root
docker compose -f docker/docker-compose.yml up -d
```
Verify the health checks are passing:
```bash
docker ps
```

### 3. Run Self-Contained Unit & Integration Tests
Execute the test suites. This runs in-memory on H2, mocking out Kafka/Redis automatically to guarantee fast local execution:
```bash
mvn test
```
*(You will see the concurrency test output logging successful optimistic locking prevention.)*

### 4. Build & Launch the Application
Compile the code and start the Spring Boot web application:
```bash
mvn spring-boot:run
```
The application will start on **`http://localhost:8080`**.

### 5. Access the Demo Accounts

You can log in immediately using the pre-seeded credentials:

#### 👤 Normal User Account
- **Username**: `user`
- **Password**: `password`
- **Starting Balance**: `$1,000.00 USD`

#### 🛠️ Administrator Account
- **Username**: `admin`
- **Password**: `password`
- **Privileges**: Create fixtures, manipulate live scores/odds, trigger settlements.

---

## 💻 Step-by-Step Live Demo Guide
1. **Open two browser windows side-by-side**:
   - Window A: Log in as `admin`. Go to the **Admin Room** (`/admin`).
   - Window B: Log in as `user`. Go to the **Sportsbook** (`/events`).
2. **Observe Real-Time Updates**:
   - In Window A, find a match selection (e.g. Real Madrid vs Barcelona, HOME_WIN price `1.65`) and update it to `1.80`.
   - Watch the HOME_WIN button in Window B immediately **flash green** and update to `1.80` in real time without refreshing the page!
   - In Window A, toggle the selection to **SUSPENDED**.
   - Watch the selection in Window B immediately lock up with a **padlock icon** (preventing user clicks).
3. **Trace Transaction Statement Flow**:
   - In Window B, place a $100 bet on Real Madrid. Observe the wallet immediately deducts $100 (balance is now $900).
   - In Window A, update the score of the match to `2 - 1` and click **Settle Fixture & Payouts** with outcome `HOME_WIN`.
   - In Window B, go to **Wallet** or **My Bets**. Observe your bet is now marked **WON** and your balance has increased by $180 ($900 + $180 = $1080).
   - Check the **Transaction History Ledger** to see the structured ledger items detailing deposits, locks, and wins.

---

## 🛠️ Current Working Components (Confirmed Running)
All of the following components have been fully integrated and verified via active, live system logging during the monolith boot-up sequence:
- **Spring Boot 3.2.5** (Core framework & configuration runtime)
- **PostgreSQL 15** (Primary persistent store for transactional state)
- **Hibernate & JPA** (Entity mapping with optimistic transactional safety locks)
- **Spring Security** (Unified session authentication filters)
- **Apache Kafka** (De-coupled event streaming broker engine)
- **Kafka Consumers** (Highly scalable, multi-threaded event processors)
- **Spring WebSockets** (Low-latency odds broadcast streams on `/ws/odds`)
- **Tomcat Servlet Container** (Embedded, high-throughput web server)
- **HikariCP** (Optimized, low-latency database connection pooling)
- **Jackson JSON** (Type-safe DTO marshaling & network messaging)

---

## 🚀 Future Improvements & Production Scale
To scale this monolith into a global, distributed multi-region infrastructure, the following architectural upgrades are scheduled:
- **JWT Stateless Authentication**: Moving session state from cookie stores to cryptographically signed JWT tokens for stateless service scaling.
- **Full Docker & Kubernetes Deployment**: Containerizing all boot instances and utilizing K8s Helm charts for auto-scaling and replication.
- **Distributed Kafka & Redis Clusters**: Implementing partitioned message replication and Redis Sentinel master-replica caches.
- **Production Load Balancing**: Deploying Nginx or AWS ALB to distribute HTTP traffic and handle WebSocket connection stickiness.
- **Observability Stack (Prometheus & Grafana)**: Hooking Spring Boot Actuator metrics into Prometheus scrapers for live system health dashboards.
- **CI/CD Automation Pipelines**: Adding GitHub Actions to compile, test, containerize, and deploy automatically on main branch merges.
- **ClickHouse Big Data Analytics**: Integrating a columnar database for complex real-time betting logs, player risk behavior modeling, and dashboard charts.
- **IP & Endpoint Rate Limiting**: Integrating Spring Cloud Gateway or Bucket4j token bucket algorithms to defend against API scraping and DDoS attacks.

---

## 🌎 Industry Use Cases
The asynchronous, transactional, and event-driven architecture shown in this project forms the foundational blueprint for:
- **Sports Betting & iGaming Platforms** (Real-time live feeds, bet locking, bulk settlements)
- **Stock Trading & Financial Systems** (High-throughput ledger processing, optimistic wallet protection)
- **Payment Gateways** (Idempotent transactions, transactional isolation)
- **Real-Time Analytics & Dashboard Systems** (WebSocket events, Kafka message streams)

---

## 👤 Author
**Missari Ahil**
- **Portfolio:** [missari.pages.dev](https://missari.pages.dev/)


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

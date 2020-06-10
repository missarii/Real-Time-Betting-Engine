# 🏗️ Real-Time Betting Engine (iGaming Monolith)

A production-grade, highly concurrent sports betting engine built as a Spring Boot and Thymeleaf monolith. This application demonstrates the core high-concurrency, transactional safety, caching, and real-time streaming architectures required for enterprise sportsbook environments.

---

## ⚡ High-Concurrency & Architectural Highlights

### 1. Financial Transactional Safety (Anti-Double-Spending)
In real-world sportsbooks, users can trigger rapid, parallel requests to place bets or withdraw funds. 
- **Optimistic Locking**: The `Wallet` and `Odds` database tables employ Hibernate `@Version` columns.
- **Race Condition Prevention**: If two concurrent transactions attempt to deduct/withdraw from the same wallet, the second transaction is immediately blocked and rolled back with an `ObjectOptimisticLockingFailureException`.

# 📖 BET-X Sportsbook — User Manual

**Version**: 1.0.0  
**Last Updated**: May 2021  
**Application URL**: http://localhost:8080

---

## Table of Contents

1. [What is BET-X?](#1-what-is-bet-x)
2. [How the Betting Engine Works](#2-how-the-betting-engine-works)
3. [Getting Started — Registration & Login](#3-getting-started--registration--login)
4. [Your Dashboard](#4-your-dashboard)
5. [Placing a Bet (Sportsbook)](#5-placing-a-bet-sportsbook)
6. [My Bets — Tracking Your Tickets](#6-my-bets--tracking-your-tickets)
7. [Wallet — Deposits & Withdrawals](#7-wallet--deposits--withdrawals)
8. [Real-Time Odds Updates (Live Flashing)](#8-real-time-odds-updates-live-flashing)
9. [Admin Control Panel](#9-admin-control-panel)
10. [Understanding Bet Settlement & Payouts](#10-understanding-bet-settlement--payouts)
11. [Glossary of Terms](#11-glossary-of-terms)

---

## 1. What is BET-X?

**BET-X** is a production-grade, real-time sports betting engine. It simulates a complete sportsbook experience — from browsing live match odds to placing bets and receiving payouts — all backed by a robust backend with financial safety guarantees.

### What Makes It Special?
| Feature | What It Means for You |
|---|---|
| **Real-Time Odds** | Odds prices update live in your browser — no page reload needed |
| **Instant Bet Placement** | Bets are processed in milliseconds with full financial validation |
| **Secure Wallet** | Your balance is protected against concurrent transactions |
| **Full Settlement Engine** | When a match finishes, winnings are automatically paid to your wallet |

---

## 2. How the Betting Engine Works

Understanding the flow helps you use BET-X confidently.

### 2.1 The Betting Flow (Step by Step)

```
You (Browser) ──► Click an Odds Button
                        │
                        ▼
              Betslip Drawer Opens
              (Enter your stake amount)
                        │
                        ▼
              Click "PLACE BET"
                        │
                        ▼
       ┌── Server validates your request ──┐
       │  ✓ Are odds still ACTIVE?         │
       │  ✓ Do odds match current price?   │
       │  ✓ Is your balance sufficient?    │
       └───────────────────────────────────┘
                        │
              ┌─────────┴──────────┐
              │ VALID BET          │ INVALID BET
              ▼                    ▼
   Stake deducted from wallet   Error message shown
   Bet ticket saved as PENDING  Nothing is charged
              │
              ▼
   Kafka event published
   (For auditing & risk management)
              │
              ▼
   Bet appears in "My Bets" page
```

### 2.2 How Live Odds Updates Work

```
Admin changes odds price
        │
        ▼
Database updated
        │
        ▼
Redis cache updated (fast reads)
        │
        ▼
Kafka "odds-updates" event fired
        │
        ▼
Background consumer picks it up
        │
        ▼
WebSocket broadcasts to ALL browsers
        │
        ▼
Your odds button FLASHES:
  🟢 Green = Price went UP (better odds)
  🔴 Red   = Price went DOWN (worse odds)
```

### 2.3 How Bet Settlement Works


# 📖 BET-X Sportsbook — User Manual

**Version**: 1.0.0  
**Last Updated**: May 2026  
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

When a match finishes, an admin enters the final result. The system then:

1. Marks the event as `FINISHED`
2. Scans ALL pending bets for that event
3. For each bet:
   - If your selection matches the winner → Bet marked **WON**, payout credited to wallet
   - If your selection doesn't match → Bet marked **LOST**, no funds returned
4. Settlement events published to Kafka for auditing

### 2.4 Financial Safety (Optimistic Locking)

BET-X uses **optimistic locking** to protect your wallet balance:
- If two actions try to modify your wallet at the exact same millisecond (e.g., placing a bet and receiving a payout simultaneously), the system safely resolves the conflict
- This prevents double-spending and balance corruption

---

## 3. Getting Started — Registration & Login

### 3.1 Creating an Account

1. Open your browser and go to: **http://localhost:8080**
2. You will be redirected to the **Login Page**
3. Click the link **"Create an account"** at the bottom
4. Fill in the registration form:

| Field | Requirements | Example |
|---|---|---|
| **Username** | Minimum 3 characters, no spaces | `missari` |
| **Email** | Valid email format | `missari@example.com` |
| **Password** | Minimum 6 characters | `123456` |

5. Click **"CREATE ACCOUNT"**
6. You will see a green success alert and be redirected to the Login page

> ✅ **New accounts automatically get a $1,000.00 USD starting wallet balance!**

### 3.2 Logging In

1. Go to **http://localhost:8080/login**
2. Enter your **Username** and **Password**
3. Click **"SIGN IN"**
4. You will be redirected to your **Dashboard**

### 3.3 Pre-Seeded Test Accounts

| Role | Username | Password | Starting Balance |
|---|---|---|---|
| 👤 Standard User | `user` | `password` | $1,000.00 |
| 🛠️ Administrator | `admin` | `password` | $1,000.00 |

---

## 4. Your Dashboard

After logging in, you land on the **Dashboard** (`/dashboard`).

```
┌──────────────────────────────────────────────────────────┐
│  🏆 BET-X  Dashboard  Sportsbook  My Bets  Wallet       │
│                               💵 $1,000.00  [LOGOUT]    │
├──────────────────────────────────────────────────────────┤
│                                          │               │
│  Welcome back, missari!                  │  🧾 Betslip  │
│                                          │               │
│  🔥 Live Matches In Play                 │  (Click any  │
│  ┌────────────────────────────────────┐  │   odds to    │
│  │ ⚽ LIVE  Real Madrid vs Barcelona  │  │   populate)  │
│  │ 2 - 1                             │  │              │
│  │ [1: 1.65]  [X: 3.20]  [2: 4.50]  │  │              │
│  └────────────────────────────────────┘  │              │
│                                          │              │
│  🎫 Recent Bet Slips                     │              │
│  (Table of your last 5 bets)             │              │
└──────────────────────────────────────────────────────────┘
```

### Dashboard Sections

| Section | Description |
|---|---|
| **💵 Balance Pill** | Shows your current wallet balance (top right) |
| **🔥 Live Matches** | Active matches you can bet on right now |
| **🧾 Active Betslip** | Interactive drawer to build and submit your bet |
| **🎫 Recent Bet Slips** | Your last 5 bet tickets with their status |

---

## 5. Placing a Bet (Sportsbook)

### Step 1: Go to Sportsbook

Click **"Sportsbook"** in the top navigation bar. You will see all available matches grouped by status (LIVE, UPCOMING, SUSPENDED).

### Step 2: Choose a Match

You will see match cards like this:

```
┌──────────────────────────────────────────┐
│ ⚽ SOCCER          ● LIVE               │
│                                          │
│  Manchester City   2 - 1   Real Madrid  │
│  📅 Starts: 2026-05-29 20:00             │
│                                          │
│  [ 1: 1.80 ]   [ X: 3.40 ]  [ 2: 4.20 ] │
│  (Home Win)      (Draw)      (Away Win)  │
└──────────────────────────────────────────┘
```

### Step 3: Click an Odds Button

Click the odds button for your chosen outcome:
- **1** = Home Team wins
- **X** = Match ends in a Draw
- **2** = Away Team wins

The number shown (e.g., `1.80`) is the **decimal odds multiplier**.

### Step 4: Enter Your Stake

After clicking an odds button, the **Active Betslip** drawer on the right updates:

```
┌─────────────────────────────────┐
│ 🧾 Active Betslip         SINGLE│
│                                  │
│ Man City vs Real Madrid          │
│ HOME WIN                   1.80  │
│                                  │
│ STAKE: [ 100.00 ] USD            │
│                                  │
│ POTENTIAL PAYOUT: $180.00        │
│                                  │
│       [ PLACE BET ]              │
└─────────────────────────────────┘
```

- Change the **STAKE** amount to how much you want to bet
- The **POTENTIAL PAYOUT** updates automatically (Stake × Odds)
- Minimum stake is **$0.10**

### Step 5: Confirm Your Bet

Click the green **"PLACE BET"** button.

- ✅ **Success**: A green notification appears, your balance decreases by the stake amount, the page refreshes
- ❌ **Error**: A red notification explains why the bet was rejected (e.g., insufficient balance, odds changed)

### Understanding Decimal Odds

```
Stake × Odds = Potential Payout

Example: $100 stake at odds 1.80
→ $100 × 1.80 = $180 total return
→ $80 net profit + $100 stake returned = $180
```

---

## 6. My Bets — Tracking Your Tickets

Click **"My Bets"** in the navigation to see your complete betting history.

### Bet Status Explained

| Status Badge | Meaning |
|---|---|
| 🟡 **PENDING** (pulsing) | Match is still in progress. Waiting for settlement. |
| 🟢 **WON** | Your selection won! Payout has been credited to your wallet. |
| ⚫ **LOST** | Your selection lost. The stake has been forfeited. |
| 🔵 **VOIDED** | Bet was cancelled (e.g., match abandoned). Stake refunded. |

### Bet Ticket Details

Each row in the table shows:

| Column | Description |
|---|---|
| **Ticket ID** | First 8 characters of your unique bet reference |
| **Placed Time** | Exact timestamp when you placed the bet |
| **Sport** | The sport category |
| **Match Event** | The two teams/players |
| **My Selection** | What you bet on (Home Win, Draw, Away Win) |
| **Odds Value** | The decimal odds locked in at time of placement |
| **Stake Amount** | How much you bet |
| **Potential Payout** | What you'll receive if you win |
| **Status** | Current state of the bet |
| **Settled Time** | When the match result was officially applied |

---

## 7. Wallet — Deposits & Withdrawals

Click **"Wallet"** in the navigation to manage your funds.

### 7.1 Making a Deposit

1. On the Wallet page, find the **📥 Simulate Deposit** card (green border)
2. Enter your desired amount in the **"Deposit Amount (USD)"** field (minimum $10)
3. The card number and payment details are simulated (no real payment)
4. Click **"SECURE DEPOSIT"**
5. Your balance updates immediately and a `DEPOSIT` entry appears in the transaction ledger

### 7.2 Making a Withdrawal

1. Find the **📤 Withdraw Payouts** card (red border)
2. Enter the amount you want to withdraw
3. Click **"PROCEED WITHDRAWAL"**
4. Your balance decreases and a `WITHDRAWAL` entry appears in the transaction ledger

> ⚠️ **Important**: You cannot withdraw more than your current balance. Attempting to do so will show an error.

### 7.3 Transaction History Ledger

The bottom table shows every financial event in your account:

| Transaction Type | What It Means |
|---|---|
| `DEPOSIT` | Funds added to your wallet |
| `WITHDRAWAL` | Funds removed from your wallet |
| `BET_PLACED` | Stake deducted when you placed a bet |
| `BET_SETTLED` | Payout credited after winning a bet |
| `BET_REFUNDED` | Stake returned for a voided bet |

**Amount color coding:**
- 🟢 **Green / Positive (+)**: Money coming INTO your wallet
- 🔴 **Red / Negative (-)**: Money going OUT of your wallet

---

## 8. Real-Time Odds Updates (Live Flashing)

BET-X features live odds that update without needing to refresh the page.

### What the Flashing Colors Mean

When an admin changes match odds, the affected odds button flashes:

| Flash Color | Meaning | What to Do |
|---|---|---|
| 🟢 **Green Flash** | Price INCREASED (better value) | You get more if you win |
| 🔴 **Red Flash** | Price DECREASED (worse value) | You get less if you win |
| 🔒 **Padlock Icon** | Market SUSPENDED | Cannot place bets on this selection |

### Odds Changed While on Betslip

If you already have a selection in your betslip and the odds change:
- The odds value in your betslip updates automatically
- The potential payout recalculates in real-time
- A blue notification appears: *"Slip updated: Odds changed to X.XX"*

> 💡 **Tip**: If odds change between when you selected and when you click "PLACE BET", the system detects the mismatch and rejects the bet with an explanation. This protects you from unexpected pricing.

---

## 9. Admin Control Panel

> 🛠️ **This section is only visible if you are logged in with an ADMIN account.**

Access the admin panel by clicking the **"Admin Room"** link (dashed amber border) in the navigation.

### 9.1 Creating a New Match Fixture

Fill in the **"➕ Seed New Fixture"** form on the right side:

| Field | Description | Example |
|---|---|---|
| **Sport Type** | Category of sport | `SOCCER` |
| **Home Team** | Name of the home team | `Chelsea` |
| **Away Team** | Name of the away team | `Arsenal` |
| **Home Win** | Opening decimal odds for a home win | `2.10` |
| **Draw** | Opening decimal odds for a draw | `3.40` |
| **Away Win** | Opening decimal odds for away win | `3.00` |

Click **"SEED FIXTURE"** to instantly create the match and seed odds into both the database and Redis cache.

### 9.2 Updating a Live Score & Status

For each match in the list, there is a score update form:

1. Change the **Home Score** number
2. Change the **Away Score** number
3. Change the **Match Status** dropdown:
   - `SCHEDULED` — Match hasn't started yet
   - `LIVE` — Match is currently in progress
   - `SUSPENDED` — Betting temporarily halted (e.g., incident on field)
   - `FINISHED` — Match is over (triggers settlement)
4. Click **"UPDATE STATE"**

### 9.3 Fluctuating Live Odds (Real-Time)

For each odds selection in a match:

1. Change the **Price** number in the small input box
2. Change the **Status** dropdown to `ACTIVE` or `SUSPENDED`
3. Click **"FLUCTUATE"**

This immediately:
- Updates the price in the database
- Refreshes the Redis cache
- Publishes a Kafka event
- Causes browsers to **flash the new price in real-time**!

### 9.4 Settling a Match (Distributing Payouts)

When a match is finished:

1. Find the match in the list
2. In the **"🏆 Settle Match Outcome"** dropdown, select the winning result:
   - `[HomeTeam] (Home Win)` 
   - `Draw Match`
   - `[AwayTeam] (Away Win)`
3. Click **"SETTLE FIXTURE & PAYOUTS"**

The system will immediately:
- Scan all PENDING bets for this match
- Credit winning payouts to each winner's wallet
- Mark losing bets as LOST
- Publish settlement events to Kafka

---

## 10. Understanding Bet Settlement & Payouts

### Payout Calculation

```
Winning Payout = Stake × Locked Odds

Example:
  Stake:          $100.00
  Odds at bet:    1.80 (Home Win)
  Final result:   Home Win ✅

  Payout:         $100 × 1.80 = $180.00
  Net Profit:     $180 - $100 = $80.00

This $180.00 is credited to your wallet automatically.
```

### What Happens to Your Stake When You Bet?

```
Balance BEFORE: $1,000.00
Place $100 bet
Balance AFTER:    $900.00  ← Stake immediately deducted

[Match in progress — bet is PENDING...]

If you WIN:
Balance:          $900.00 + $180.00 = $1,080.00  ✅

If you LOSE:
Balance:          $900.00  (stake is gone)  ❌
```

### The Odds Changed — Will My Bet Be Affected?

**No.** Odds are **locked in** at the exact moment you click "PLACE BET". Even if the price changes after your bet is placed, your payout is calculated using the odds that were displayed when you confirmed the bet.

---

## 11. Glossary of Terms

| Term | Definition |
|---|---|
| **Odds** | A number (e.g., 1.80) representing the potential return for every $1 staked |
| **Stake** | The amount of money you put on a bet |
| **Payout** | Total money returned to you if you win (Stake × Odds) |
| **Net Profit** | Your winnings minus your original stake |
| **PENDING** | Your bet is waiting for the match result |
| **WON** | Your selection was correct; payout credited |
| **LOST** | Your selection was wrong; stake forfeited |
| **VOIDED** | Bet cancelled; stake fully refunded |
| **Market** | A specific betting option (e.g., "1X2 - Match Result") |
| **Selection** | The specific outcome you bet on (Home Win, Draw, Away Win) |
| **1X2 Market** | Standard football market: 1 = Home Win, X = Draw, 2 = Away Win |
| **Settlement** | The process of distributing payouts after a match finishes |
| **Cache** | Redis-based memory store for ultra-fast odds reads |
| **WebSocket** | Technology that pushes real-time odds updates to your browser |
| **Kafka** | Message broker that decouples bet events for audit and processing |
| **Optimistic Locking** | Database safety mechanism preventing duplicate balance changes |
| **SUSPENDED** | Market temporarily closed; no bets can be placed |
| **Decimal Odds** | Odds format where 2.00 means double your stake on a win |

---

## 🆘 Common Questions (FAQ)

**Q: I placed a bet but my balance didn't change?**  
A: Refresh the page. The balance is always accurate — the deduction happens the moment you click PLACE BET.

**Q: My bet says PENDING but the match finished hours ago?**  
A: The admin needs to click "SETTLE FIXTURE & PAYOUTS" in the Admin Panel for the match. Ask your admin to settle it.

**Q: The odds button shows a padlock icon?**  
A: The market is SUSPENDED. You cannot place bets on that selection until the admin reactivates it.

**Q: The "PLACE BET" button shows an error: "Odds have changed"?**  
A: The price changed between when you selected it and when you clicked PLACE BET. Click the odds button again to get the latest price and try again.

**Q: I can't see the "Admin Room" link?**  
A: Only accounts with the ADMIN role can see and access the Admin Panel. Standard user accounts cannot access it.

**Q: How do I see my full transaction history?**  
A: Click **Wallet** in the navigation. The bottom section shows every deposit, withdrawal, bet placement, and payout in chronological order.

---

*BET-X — Powered by Spring Boot, Redis, Apache Kafka, and WebSockets.*  
*A production-grade iGaming monolith demonstrating enterprise concurrency patterns.*

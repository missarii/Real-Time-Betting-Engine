-- Drop tables if they exist (for easy resetting in development)
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS bets CASCADE;
DROP TABLE IF EXISTS odds CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS wallets CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);

-- Wallets Table (with Optimistic Locking version column)
CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    balance NUMERIC(15,2) NOT NULL DEFAULT 0.00 CONSTRAINT chk_wallet_balance_positive CHECK (balance >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_wallets_user ON wallets(user_id);

-- Transactions Table
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    reference_id UUID,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_wallet ON transactions(wallet_id);

-- Events (Matches) Table
CREATE TABLE events (
    id UUID PRIMARY KEY,
    sport VARCHAR(50) NOT NULL,
    home_team VARCHAR(100) NOT NULL,
    away_team VARCHAR(100) NOT NULL,
    home_score INT NOT NULL DEFAULT 0,
    away_score INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, LIVE, SUSPENDED, FINISHED
    start_time TIMESTAMP NOT NULL,
    ended_at TIMESTAMP
);

CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_sport ON events(sport);
CREATE INDEX idx_events_start_time ON events(start_time);

-- Odds Table (with Optimistic Locking version column)
CREATE TABLE odds (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    market_name VARCHAR(50) NOT NULL, -- e.g., '1X2', 'OVER_UNDER'
    selection_name VARCHAR(50) NOT NULL, -- e.g., 'HOME_WIN', 'DRAW', 'AWAY_WIN'
    odds_value NUMERIC(6,2) NOT NULL CONSTRAINT chk_odds_min CHECK (odds_value > 1.0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_event_market_selection UNIQUE (event_id, market_name, selection_name)
);

CREATE INDEX idx_odds_event ON odds(event_id);

-- Bets Table
CREATE TABLE bets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    odds_id UUID NOT NULL REFERENCES odds(id) ON DELETE CASCADE,
    selection_name VARCHAR(50) NOT NULL,
    odds_value NUMERIC(6,2) NOT NULL,
    stake NUMERIC(15,2) NOT NULL CONSTRAINT chk_bet_stake CHECK (stake > 0),
    potential_payout NUMERIC(15,2) NOT NULL CONSTRAINT chk_bet_payout CHECK (potential_payout > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, WON, LOST, VOIDED
    placed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settled_at TIMESTAMP
);

CREATE INDEX idx_bets_user ON bets(user_id);
CREATE INDEX idx_bets_event ON bets(event_id);
CREATE INDEX idx_bets_status ON bets(status);

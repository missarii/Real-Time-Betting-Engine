-- Seed Users (Password is 'password' in BCrypt)
-- Hash: $2a$10$8.UnVuG9HHgffUDAlk8GPOSYYRqiP0chQP50eW.uK.q1jT8gq6qdq
INSERT INTO users (id, username, email, password, role, created_at) VALUES
('11111111-1111-1111-1111-111111111111', 'admin', 'admin@betting.com', '$2a$10$8.UnVuG9HHgffUDAlk8GPOSYYRqiP0chQP50eW.uK.q1jT8gq6qdq', 'ADMIN', CURRENT_TIMESTAMP),
('22222222-2222-2222-2222-222222222222', 'user', 'user@betting.com', '$2a$10$8.UnVuG9HHgffUDAlk8GPOSYYRqiP0chQP50eW.uK.q1jT8gq6qdq', 'USER', CURRENT_TIMESTAMP);

-- Seed Wallets
INSERT INTO wallets (id, user_id, balance, currency, version) VALUES
('11111111-1111-1111-1111-111111111112', '11111111-1111-1111-1111-111111111111', 10000.00, 'USD', 0),
('22222222-2222-2222-2222-222222222223', '22222222-2222-2222-2222-222222222222', 1000.00, 'USD', 0);

-- Seed Initial Wallet Transaction for user
INSERT INTO transactions (id, wallet_id, type, amount, reference_id, description, created_at) VALUES
('22222222-2222-2222-2222-222222222224', '22222222-2222-2222-2222-222222222223', 'DEPOSIT', 1000.00, NULL, 'Initial Welcome Deposit', CURRENT_TIMESTAMP);

-- Seed Events
-- Event 1: Real Madrid vs Barcelona (LIVE, Soccer)
INSERT INTO events (id, sport, home_team, away_team, home_score, away_score, status, start_time, ended_at) VALUES
('33333333-3333-3333-3333-333333333333', 'SOCCER', 'Real Madrid', 'Barcelona', 2, 1, 'LIVE', CURRENT_TIMESTAMP - INTERVAL '45 minutes', NULL);

-- Event 2: LA Lakers vs Boston Celtics (SCHEDULED, Basketball)
INSERT INTO events (id, sport, home_team, away_team, home_score, away_score, status, start_time, ended_at) VALUES
('44444444-4444-4444-4444-444444444444', 'BASKETBALL', 'LA Lakers', 'Boston Celtics', 0, 0, 'SCHEDULED', CURRENT_TIMESTAMP + INTERVAL '2 hours', NULL);

-- Event 3: Novak Djokovic vs Rafael Nadal (LIVE, Tennis)
INSERT INTO events (id, sport, home_team, away_team, home_score, away_score, status, start_time, ended_at) VALUES
('55555555-5555-5555-5555-555555555555', 'TENNIS', 'Novak Djokovic', 'Rafael Nadal', 1, 0, 'LIVE', CURRENT_TIMESTAMP - INTERVAL '1 hour', NULL);

-- Event 4: Manchester City vs Liverpool (SCHEDULED, Soccer)
INSERT INTO events (id, sport, home_team, away_team, home_score, away_score, status, start_time, ended_at) VALUES
('66666666-6666-6666-6666-666666666666', 'SOCCER', 'Manchester City', 'Liverpool', 0, 0, 'SCHEDULED', CURRENT_TIMESTAMP + INTERVAL '1 day', NULL);

-- Seed Odds for Event 1 (Real Madrid vs Barcelona)
INSERT INTO odds (id, event_id, market_name, selection_name, odds_value, status, version) VALUES
('33333333-3333-3333-3333-333333333301', '33333333-3333-3333-3333-333333333333', '1X2', 'HOME_WIN', 1.65, 'ACTIVE', 0),
('33333333-3333-3333-3333-333333333302', '33333333-3333-3333-3333-333333333333', '1X2', 'DRAW', 3.40, 'ACTIVE', 0),
('33333333-3333-3333-3333-333333333303', '33333333-3333-3333-3333-333333333333', '1X2', 'AWAY_WIN', 4.20, 'ACTIVE', 0);

-- Seed Odds for Event 2 (LA Lakers vs Boston Celtics)
INSERT INTO odds (id, event_id, market_name, selection_name, odds_value, status, version) VALUES
('44444444-4444-4444-4444-444444444401', '44444444-4444-4444-4444-444444444444', '1X2', 'HOME_WIN', 1.95, 'ACTIVE', 0),
('44444444-4444-4444-4444-444444444402', '44444444-4444-4444-4444-444444444444', '1X2', 'DRAW', 15.0, 'ACTIVE', 0), -- Draw is very rare in basketball, high odds
('44444444-4444-4444-4444-444444444403', '44444444-4444-4444-4444-444444444444', '1X2', 'AWAY_WIN', 1.85, 'ACTIVE', 0);

-- Seed Odds for Event 3 (Novak Djokovic vs Rafael Nadal)
INSERT INTO odds (id, event_id, market_name, selection_name, odds_value, status, version) VALUES
('55555555-5555-5555-5555-555555555501', '55555555-5555-5555-5555-555555555555', '1X2', 'HOME_WIN', 1.40, 'ACTIVE', 0),
('55555555-5555-5555-5555-555555555503', '55555555-5555-5555-5555-555555555555', '1X2', 'AWAY_WIN', 2.80, 'ACTIVE', 0); -- Tennis has no Draw

-- Seed Odds for Event 4 (Manchester City vs Liverpool)
INSERT INTO odds (id, event_id, market_name, selection_name, odds_value, status, version) VALUES
('66666666-6666-6666-6666-666666666601', '66666666-6666-6666-6666-666666666666', '1X2', 'HOME_WIN', 2.10, 'ACTIVE', 0),
('66666666-6666-6666-6666-666666666602', '66666666-6666-6666-6666-666666666666', '1X2', 'DRAW', 3.50, 'ACTIVE', 0),
('66666666-6666-6666-6666-666666666603', '66666666-6666-6666-6666-666666666666', '1X2', 'AWAY_WIN', 3.10, 'ACTIVE', 0);

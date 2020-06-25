-- Seed Users (Password is 'password' in BCrypt)
-- Hash: $2a$10$8.UnVuG9HHgffUDAlk8GPOSYYRqiP0chQP50eW.uK.q1jT8gq6qdq
INSERT INTO users (id, username, email, password, role, created_at) VALUES
('11111111-1111-1111-1111-111111111111', 'admin', 'admin@betting.com', '$2a$10$8.UnVuG9HHgffUDAlk8GPOSYYRqiP0chQP50eW.uK.q1jT8gq6qdq', 'ADMIN', CURRENT_TIMESTAMP),
('22222222-2222-2222-2222-222222222222', 'user', 'user@betting.com', '$2a$10$8.UnVuG9HHgffUDAlk8GPOSYYRqiP0chQP50eW.uK.q1jT8gq6qdq', 'USER', CURRENT_TIMESTAMP);

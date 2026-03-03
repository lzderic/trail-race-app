-- Seed users (same as command service — BCrypt hashed passwords)
-- admin@trailrace.com / admin123
-- jane@example.com   / password
INSERT INTO users (id, first_name, last_name, email, dob, role, password)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Admin', 'User', 'admin@trailrace.com', '1985-04-15', 'ADMINISTRATOR',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    ('a0000000-0000-0000-0000-000000000002', 'Jane', 'Applicant', 'jane@example.com', '1992-07-22', 'APPLICANT',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.')
ON CONFLICT (email) DO NOTHING;


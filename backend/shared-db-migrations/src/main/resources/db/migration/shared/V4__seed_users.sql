-- Insert seed users (passwords are BCrypt-hashed: 'admin123' and 'applicant123')
INSERT INTO users (id, first_name, last_name, email, dob, role, password)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Admin', 'User', 'admin@trailrace.com', '1985-04-15', 'ADMINISTRATOR',
     '$2b$10$ScxPMN17HSdcOBi0zR50oOKciD7wA.IECFuszFODVOvkzgWS9SFLa'),
    ('a0000000-0000-0000-0000-000000000002', 'Jane', 'Applicant', 'jane@example.com', '1992-07-22', 'APPLICANT',
     '$2b$10$KhkS50lfxDOckQZvDOec2emjyX6YZU29eWAw0oyFjVHg6r24V5TCi'),
    ('a0000000-0000-0000-0000-000000000003', 'Marco', 'Runner', 'marco@example.com', '1988-11-03', 'APPLICANT',
     '$2b$10$KhkS50lfxDOckQZvDOec2emjyX6YZU29eWAw0oyFjVHg6r24V5TCi'),
    ('a0000000-0000-0000-0000-000000000004', 'Lena', 'Trail', 'lena@example.com', '1995-02-18', 'APPLICANT',
     '$2b$10$KhkS50lfxDOckQZvDOec2emjyX6YZU29eWAw0oyFjVHg6r24V5TCi')
ON CONFLICT (email) DO NOTHING;


INSERT INTO users (id, username, email, password_hash, role, active, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'admin',
    'admin@lottery.mx',
    '$2a$10$hUxaCaqIlIv1MhL.l.JYbOOS1AWuchWslLtFzNjRsRlgxht7JY11S',
    'ADMIN',
    true,
    NOW()
)
ON CONFLICT (email) DO UPDATE SET role = 'ADMIN';

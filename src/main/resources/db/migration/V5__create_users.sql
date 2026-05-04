CREATE TABLE IF NOT EXISTS users (
    id            VARCHAR(36)   PRIMARY KEY,
    username      VARCHAR(50)   NOT NULL,
    email         VARCHAR(255)  NOT NULL,
    password_hash VARCHAR(255)  NOT NULL,
    role          VARCHAR(20)   NOT NULL DEFAULT 'USER',
    active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email)
);

CREATE INDEX IF NOT EXISTS idx_users_email    ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);

COMMENT ON TABLE  users               IS 'Usuarios registrados en la plataforma';
COMMENT ON COLUMN users.role          IS 'Rol del usuario: USER o ADMIN';
COMMENT ON COLUMN users.password_hash IS 'Contraseña hasheada con BCrypt';
COMMENT ON COLUMN users.active        IS 'FALSE = cuenta desactivada (soft delete)';

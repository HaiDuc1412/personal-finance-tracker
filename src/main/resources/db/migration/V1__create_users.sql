-- V1__create_users.sql

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
                       id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
                       email       VARCHAR(255) NOT NULL UNIQUE,
                       password    VARCHAR(255) NOT NULL,
                       full_name   VARCHAR(100) NOT NULL,
                       role        VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
                       is_active   BOOLEAN      NOT NULL DEFAULT true,
                       created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
                       updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users(email);

COMMENT ON TABLE users IS 'Registered user accounts';
COMMENT ON COLUMN users.password IS 'BCrypt hashed password - never store plain text';
COMMENT ON COLUMN users.role IS 'ROLE_USER or ROLE_ADMIN';
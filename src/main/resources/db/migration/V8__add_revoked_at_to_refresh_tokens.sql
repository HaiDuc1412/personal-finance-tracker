-- V8__add_revoked_at_to_refresh_tokens.sql

ALTER TABLE refresh_tokens
    ADD COLUMN revoked_at TIMESTAMPTZ;

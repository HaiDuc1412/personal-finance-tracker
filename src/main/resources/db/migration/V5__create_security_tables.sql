-- V5__create_security_tables.sql

CREATE TABLE refresh_tokens (
                                id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
                                token_hash  VARCHAR(255) NOT NULL UNIQUE,
                                user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                expires_at  TIMESTAMPTZ  NOT NULL,
                                is_revoked  BOOLEAN      NOT NULL DEFAULT false,
                                created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user
    ON refresh_tokens(user_id)
    WHERE is_revoked = false;

COMMENT ON COLUMN refresh_tokens.token_hash IS 'SHA-256 hash of the actual token - never store raw token';

-- -------------------------------------------------------

CREATE TABLE audit_logs (
                            id           UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
                            entity_type  VARCHAR(50)  NOT NULL,
                            entity_id    UUID         NOT NULL,
                            action       VARCHAR(20)  NOT NULL,
                            old_value    JSONB,
                            new_value    JSONB,
                            performed_by UUID         NOT NULL REFERENCES users(id),
                            performed_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

                            CONSTRAINT chk_audit_action
                                CHECK (action IN ('CREATE', 'UPDATE', 'DELETE'))
    );

CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_user   ON audit_logs(performed_by, performed_at DESC);

COMMENT ON TABLE audit_logs IS 'Append-only audit trail - never UPDATE or DELETE rows in this table';
COMMENT ON COLUMN audit_logs.old_value IS 'JSON snapshot before change, NULL for CREATE';
COMMENT ON COLUMN audit_logs.new_value IS 'JSON snapshot after change, NULL for DELETE';
-- V3__create_transactions.sql

CREATE TABLE transactions (
                              id               UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
                              amount           NUMERIC(15, 2) NOT NULL,
                              type             VARCHAR(10)    NOT NULL,
                              description      VARCHAR(500),
                              transaction_date DATE           NOT NULL,
                              note             TEXT,
                              is_deleted       BOOLEAN        NOT NULL DEFAULT false,
                              user_id          UUID           NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              category_id      UUID           NOT NULL REFERENCES categories(id),
                              created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
                              updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),

                              CONSTRAINT chk_transaction_amount
                                  CHECK (amount > 0),
                              CONSTRAINT chk_transaction_type
                                  CHECK (type IN ('INCOME', 'EXPENSE')),
                              CONSTRAINT chk_transaction_date
                                  CHECK (transaction_date <= CURRENT_DATE)
);

CREATE INDEX idx_transactions_user_date
    ON transactions(user_id, transaction_date DESC)
    WHERE is_deleted = false;

CREATE INDEX idx_transactions_user_category
    ON transactions(user_id, category_id)
    WHERE is_deleted = false;

CREATE INDEX idx_transactions_user_type_date
    ON transactions(user_id, type, transaction_date DESC)
    WHERE is_deleted = false;

COMMENT ON COLUMN transactions.amount IS 'Always positive. INCOME/EXPENSE determined by type column';
COMMENT ON COLUMN transactions.transaction_date IS 'Date the transaction actually occurred, NOT when it was recorded';
COMMENT ON COLUMN transactions.is_deleted IS 'Soft delete flag - records are never physically deleted';
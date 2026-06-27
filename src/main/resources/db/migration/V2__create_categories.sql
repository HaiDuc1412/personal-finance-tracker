-- V2__create_categories.sql

CREATE TABLE categories (
                            id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
                            name        VARCHAR(100) NOT NULL,
                            icon        VARCHAR(50),
                            color       VARCHAR(7),
                            type        VARCHAR(10)  NOT NULL,
                            is_default  BOOLEAN      NOT NULL DEFAULT false,
                            user_id     UUID         REFERENCES users(id) ON DELETE CASCADE,
                            created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

                            CONSTRAINT chk_category_type
                                CHECK (type IN ('INCOME', 'EXPENSE')),
                            CONSTRAINT uq_category_name_user
                                UNIQUE (name, user_id)
);

CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_categories_default ON categories(is_default) WHERE is_default = true;

COMMENT ON COLUMN categories.user_id IS 'NULL = system default category, visible to all users';
-- V4__create_budgets.sql

CREATE TABLE budgets (
                         id            UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
                         monthly_limit NUMERIC(15, 2) NOT NULL,
                         month         SMALLINT       NOT NULL,
                         year          SMALLINT       NOT NULL,
                         alert_sent_80  BOOLEAN       NOT NULL DEFAULT false,
                         alert_sent_100 BOOLEAN       NOT NULL DEFAULT false,
                         user_id       UUID           NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                         category_id   UUID           NOT NULL REFERENCES categories(id),
                         created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
                         updated_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),

                         CONSTRAINT chk_budget_limit
                             CHECK (monthly_limit > 0),
                         CONSTRAINT chk_budget_month
                             CHECK (month BETWEEN 1 AND 12),
    CONSTRAINT chk_budget_year
        CHECK (year BETWEEN 2000 AND 2100),
    CONSTRAINT uq_budget_per_category_month
        UNIQUE (user_id, category_id, month, year)
);

CREATE INDEX idx_budgets_user_month_year
    ON budgets(user_id, month, year);

COMMENT ON COLUMN budgets.alert_sent_80  IS 'True after 80% alert email has been sent this month';
COMMENT ON COLUMN budgets.alert_sent_100 IS 'True after 100% alert email has been sent this month';
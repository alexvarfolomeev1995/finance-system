CREATE TABLE budgets (
    id              BIGSERIAL PRIMARY KEY,
    department_id   BIGINT NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    category_id     BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    period          VARCHAR(20) NOT NULL,
    period_year     INT NOT NULL,
    period_number   INT NOT NULL,
    planned_amount  NUMERIC(19, 2) NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_budget_period CHECK (period IN ('MONTH', 'QUARTER')),
    CONSTRAINT chk_budget_amount_positive CHECK (planned_amount > 0),
    CONSTRAINT chk_budget_year CHECK (period_year >= 2000 AND period_year <= 2100),
    CONSTRAINT chk_budget_number CHECK (period_number >= 1 AND period_number <= 12),

    CONSTRAINT uq_budget UNIQUE (department_id, category_id, period, period_year, period_number)
);

CREATE INDEX idx_budgets_department ON budgets(department_id);
CREATE INDEX idx_budgets_period ON budgets(period_year, period_number);
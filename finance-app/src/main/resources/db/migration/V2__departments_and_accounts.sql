CREATE TABLE departments (
                             id                BIGSERIAL PRIMARY KEY,
                             name              VARCHAR(255) NOT NULL,
                             code              VARCHAR(50)  NOT NULL UNIQUE,
                             responsible_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
                             created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
                             updated_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_departments_code ON departments(code);

CREATE TABLE accounts (
                          id                BIGSERIAL PRIMARY KEY,
                          department_id     BIGINT NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
                          name              VARCHAR(255) NOT NULL,
                          type              VARCHAR(50) NOT NULL,
                          currency          VARCHAR(3)  NOT NULL,
                          initial_balance   NUMERIC(19, 2) NOT NULL DEFAULT 0,
                          created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
                          CONSTRAINT chk_account_type CHECK (type IN ('CHECKING', 'CASH', 'CORPORATE_CARD'))
);

CREATE INDEX idx_accounts_department ON accounts(department_id);
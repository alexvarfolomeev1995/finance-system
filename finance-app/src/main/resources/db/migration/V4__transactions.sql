CREATE TABLE transactions (
                              id                 BIGSERIAL PRIMARY KEY,
                              type               VARCHAR(20) NOT NULL,
                              status             VARCHAR(20) NOT NULL,
                              amount             NUMERIC(19, 2) NOT NULL,
                              currency           VARCHAR(3) NOT NULL,
                              transaction_date   DATE NOT NULL,
                              account_id         BIGINT NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
                              target_account_id  BIGINT REFERENCES accounts(id) ON DELETE RESTRICT,
                              category_id        BIGINT REFERENCES categories(id) ON DELETE RESTRICT,
                              counterparty_id    BIGINT REFERENCES counterparties(id) ON DELETE SET NULL,
                              department_id      BIGINT NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
                              author_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                              reversal_of_id     BIGINT REFERENCES transactions(id) ON DELETE RESTRICT,
                              description        TEXT,
                              created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
                              updated_at         TIMESTAMP NOT NULL DEFAULT NOW(),
                              CONSTRAINT chk_tx_type   CHECK (type   IN ('INCOME', 'EXPENSE', 'TRANSFER')),
                              CONSTRAINT chk_tx_status CHECK (status IN ('DRAFT', 'CONFIRMED', 'REVERSED')),
                              CONSTRAINT chk_tx_amount_positive CHECK (amount > 0),
                              CONSTRAINT chk_tx_transfer_target CHECK (
                                  (type = 'TRANSFER' AND target_account_id IS NOT NULL)
                                      OR (type <> 'TRANSFER' AND target_account_id IS NULL)
                                  ),
                              CONSTRAINT chk_tx_category CHECK (
                                  (type IN ('INCOME', 'EXPENSE') AND category_id IS NOT NULL)
                                      OR (type = 'TRANSFER' AND category_id IS NULL)
                                  )
);

CREATE INDEX idx_tx_account        ON transactions(account_id, status, transaction_date);
CREATE INDEX idx_tx_department     ON transactions(department_id, transaction_date);
CREATE INDEX idx_tx_target_account ON transactions(target_account_id) WHERE target_account_id IS NOT NULL;
CREATE INDEX idx_tx_reversal_of    ON transactions(reversal_of_id) WHERE reversal_of_id IS NOT NULL;
CREATE INDEX idx_tx_status         ON transactions(status);
CREATE INDEX idx_tx_date           ON transactions(transaction_date);
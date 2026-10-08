CREATE TABLE categories (
                            id          BIGSERIAL PRIMARY KEY,
                            name        VARCHAR(255) NOT NULL,
                            parent_id   BIGINT REFERENCES categories(id) ON DELETE RESTRICT,
                            type        VARCHAR(20) NOT NULL,
                            created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                            updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                            CONSTRAINT chk_category_type CHECK (type IN ('INCOME', 'EXPENSE'))
);

CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE INDEX idx_categories_type ON categories(type);

CREATE TABLE counterparties (
                                id          BIGSERIAL PRIMARY KEY,
                                name        VARCHAR(255) NOT NULL,
                                inn         VARCHAR(20),
                                type        VARCHAR(20) NOT NULL,
                                created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                                CONSTRAINT chk_counterparty_type CHECK (type IN ('SUPPLIER', 'CUSTOMER', 'OTHER'))
);

CREATE INDEX idx_counterparties_inn ON counterparties(inn);
CREATE INDEX idx_counterparties_type ON counterparties(type);
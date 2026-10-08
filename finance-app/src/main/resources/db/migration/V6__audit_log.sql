CREATE TABLE audit_log (
                           id           BIGSERIAL PRIMARY KEY,
                           user_id      BIGINT REFERENCES users(id) ON DELETE SET NULL,
                           user_email   VARCHAR(255),
                           action       VARCHAR(50) NOT NULL,
                           entity_type  VARCHAR(50) NOT NULL,
                           entity_id    BIGINT,
                           details      TEXT,
                           ip_address   VARCHAR(45),
                           created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_user     ON audit_log(user_id);
CREATE INDEX idx_audit_entity   ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_created  ON audit_log(created_at DESC);
CREATE INDEX idx_audit_action   ON audit_log(action);
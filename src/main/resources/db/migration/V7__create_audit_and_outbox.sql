CREATE TABLE audit_logs(
    id UUID PRIMARY KEY,
    organization_id UUID,
    actor_user_id UUID,
    target_user_id UUID,
    event_type VARCHAR(100) NOT NULL,
    resource_type VARCHAR(80),
    resource_id VARCHAR(100),
    request_id VARCHAR(100),
    ip_address INET,
    user_agent TEXT,
    details JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_organization
                   FOREIGN KEY (organization_id)
                   REFERENCES organizations(id)
                   ON DELETE SET NULL,

    CONSTRAINT fk_audit_actor
                   FOREIGN KEY (actor_user_id)
                   REFERENCES users(id)
                   ON DELETE SET NULL,

    CONSTRAINT fk_audit_target
                   FOREIGN KEY (target_user_id)
                   REFERENCES users(id)
                   ON DELETE SET NULL,

    CONSTRAINT ck_audit_details
                   CHECK (details IS NULL OR jsonb_typeof(details) = 'object')
);
CREATE INDEX idx_audit_organization_created
        ON audit_logs(organization_id, created_at DESC);

CREATE INDEX idx_audit_target_created
        ON audit_logs(target_user_id, created_at DESC);

CREATE INDEX idx_audit_actor_created
    ON audit_logs(actor_user_id, created_at DESC);

CREATE INDEX idx_audit_event_created
    ON audit_logs(event_type, created_at DESC);

CREATE TABLE outbox_events(
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    available_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,

    CONSTRAINT ck_outbox_attempts
                  CHECK (attempts >= 0),

    CONSTRAINT ck_outbox_payload
                  CHECK (jsonb_typeof(payload) = 'object')
);
CREATE INDEX idx_outbox_pending
        ON outbox_events(available_at, created_at)
        WHERE processed_at IS NULL;
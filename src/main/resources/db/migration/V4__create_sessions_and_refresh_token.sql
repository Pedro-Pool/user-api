CREATE TABLE user_sessions(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    device_name VARCHAR(150),
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    revocation_reason VARCHAR(100),

    CONSTRAINT fk_session_user
                    FOREIGN KEY (user_id)
                    REFERENCES users(id)
                    ON DELETE RESTRICT,

    CONSTRAINT ck_session_expiration
                      CHECK ( expires_at > created_at )
);
CREATE INDEX idx_session_user_expiration
        ON user_sessions(user_id, expires_at);

CREATE INDEX idx_session_active
        ON user_sessions(user_id)
        WHERE revoked_at IS NULL;

CREATE TABLE refresh_tokens(
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    family_id UUID NOT NULL,
    token_hash BYTEA NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    replaced_by_token_id UUID,

    CONSTRAINT fk_refresh_tokens_session
                   FOREIGN KEY (session_id)
                   REFERENCES user_sessions(id)
                   ON DELETE RESTRICT,

    CONSTRAINT fk_refresh_tokens_replaced
                   FOREIGN KEY (replaced_by_token_id)
                   REFERENCES refresh_tokens(id)
                   ON DELETE RESTRICT,

    CONSTRAINT uq_refresh_tokens_hash
                   UNIQUE (token_hash),

    CONSTRAINT ck_refresh_tokens_hash
                   CHECK (octet_length(token_hash) = 32),

    CONSTRAINT ck_refresh_tokens_expiration
                   CHECK (expires_at > issued_at)
);
CREATE INDEX idx_refresh_tokens_session
        ON refresh_tokens(session_id);

CREATE INDEX idx_refresh_tokens_family
        ON refresh_tokens(family_id);

CREATE INDEX idx_refresh_token_expiration
        ON refresh_tokens(expires_at);

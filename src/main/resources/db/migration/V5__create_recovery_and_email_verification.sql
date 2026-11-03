CREATE TABLE password_recovery_tokens(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash BYTEA NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,

    CONSTRAINT fk_recovery_user
                FOREIGN KEY (user_id)
                REFERENCES users(id)
                ON DELETE RESTRICT,

    CONSTRAINT ck_recovery_token_hash
                    CHECK (octet_length(token_hash) = 32),

    CONSTRAINT ck_recovery_expiration
                     CHECK (expires_at > created_at)
);
CREATE INDEX idx_recovery_user_expiration
        ON password_recovery_tokens(user_id, expires_at);

CREATE TABLE email_verification_tokens(
    id UUID PRIMARY KEY,
    email_id UUID NOT NULL,
    token_hash BYTEA NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,

    CONSTRAINT fk_email_verification_email
                    FOREIGN KEY (email_id)
                    REFERENCES user_emails(id)
                    ON DELETE RESTRICT,

    CONSTRAINT ck_email_verification_hash
                    CHECK (octet_length(token_hash) = 32),

    CONSTRAINT ck_email_verification_expiration
                    CHECK (expires_at > created_at)
);
CREATE INDEX idx_email_verification_email
        ON email_verification_tokens(email_id);
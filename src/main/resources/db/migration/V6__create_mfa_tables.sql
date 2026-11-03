CREATE TABLE user_mfa_methods(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    method_type VARCHAR(20) NOT NULL,
    label VARCHAR(100),
    secret_encrypted BYTEA,
    destination_email_id UUID,
    destination_phone_encrypted BYTEA,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMPTZ,

    CONSTRAINT fk_mfa_user
                 FOREIGN KEY (user_id)
                 REFERENCES users(id)
                 ON DELETE RESTRICT,

    CONSTRAINT ck_mfa_type
                 CHECK (method_type IN ('TOTP','EMAIL','SMS')),

    CONSTRAINT ck_mfa_configuration
        CHECK (
            (
                method_type = 'TOTP'
                AND secret_encrypted IS NOT NULL
                AND destination_email_id IS NULL
                AND destination_phone_encrypted IS NULL
                )
            OR
            (
                method_type = 'EMAIL'
                AND secret_encrypted IS NULL
                AND destination_email_id IS NOT NULL
                AND destination_phone_encrypted IS NULL
                )
            OR
            (
                method_type = 'SMS'
                AND secret_encrypted IS NULL
                AND destination_email_id IS NULL
                AND destination_phone_encrypted IS NOT NULL
                )
            )
);
CREATE INDEX idx_mfa_user_enabled
        ON user_mfa_methods(user_id, enabled);

CREATE TABLE user_mfa_recovery_codes(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    code_hash BYTEA NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMPTZ,

    CONSTRAINT fk_mfa_recovery_user
                FOREIGN KEY (user_id)
                REFERENCES users(id)
                ON DELETE RESTRICT,

    CONSTRAINT ck_mfa_recovery_hash
                CHECK (octet_length(code_hash) = 32),

    CONSTRAINT uq_mfa_recovery_user_code
                UNIQUE (user_id, code_hash)
);
CREATE INDEX idx_mfa_recovery_unused
        ON user_mfa_recovery_codes(user_id)
        WHERE used_at IS NULL;

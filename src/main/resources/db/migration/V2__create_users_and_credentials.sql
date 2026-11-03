CREATE TABLE users(
    id UUID PRIMARY KEY,
    display_name VARCHAR(120) NOT NULL,
    first_name VARCHAR(80),
    last_name VARCHAR(120),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_users_status CHECK (
        status IN (
            'PENDING_VERIFICATION',
            'ACTIVE',
            'LOCKED',
            'DISABLED',
            'DELETED'
            )
        ),
    CONSTRAINT ck_users_version CHECK (version >= 0)
);
CREATE INDEX idx_users_status ON users(status);

CREATE TABLE user_emails(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    email VARCHAR(254) NOT NULL,
    email_normalized VARCHAR(254) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    retired_at TIMESTAMPTZ,

    CONSTRAINT fk_user_emails_user
                    FOREIGN KEY (user_id)
                    REFERENCES users(id)
                    ON DELETE CASCADE,

    CONSTRAINT uq_user_emails_user_id
                    UNIQUE (user_id, id),

    CONSTRAINT ck_user_emails_primary_verified
                    CHECK ( NOT is_primary OR verified_at IS NOT NULL),

    CONSTRAINT ck_user_emails_retired_primary
                    CHECK ( NOT (is_primary AND retired_at IS NOT NULL))
);
CREATE UNIQUE INDEX uq_user_emails_active_normalized
    ON user_emails(email_normalized)
    WHERE retired_at IS NULL;

CREATE UNIQUE INDEX uq_user_emails_primary
    ON user_emails(user_id)
    WHERE is_primary = TRUE AND retired_at IS NULL;

CREATE UNIQUE INDEX uq_user_emails_pending
    ON user_emails(user_id)
    WHERE verified_at IS NULL AND retired_at IS NULL;

CREATE INDEX idx_user_emails_user
    ON user_emails(user_id);

CREATE TABLE user_credentials(
    user_id UUID PRIMARY KEY,
    password_hash VARCHAR(512) NOT NULL,
    password_changed_at TIMESTAMPTZ NOT NULL,
    password_expires_at TIMESTAMPTZ,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_credentials_user
                    FOREIGN KEY (user_id)
                    REFERENCES users(id)
                    ON DELETE CASCADE
);
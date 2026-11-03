CREATE TABLE organizations(
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_organizations_status
                    CHECK ( status IN ('ACTIVE', 'SUSPENDED', 'DISABLED') )
);
CREATE TABLE organization_memberships(
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    invited_by UUID,
    joined_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    removed_at TIMESTAMPTZ,

    CONSTRAINT fk_memberships_organization
                    FOREIGN KEY (organization_id)
                    REFERENCES organizations(id)
                    ON DELETE RESTRICT,

    CONSTRAINT fk_memberships_user
                    FOREIGN KEY (user_id)
                    REFERENCES users(id)
                    ON DELETE RESTRICT ,

    CONSTRAINT fk_memberships_invited_by
                    FOREIGN KEY (invited_by)
                    REFERENCES users(id)
                    ON DELETE SET NULL,

    CONSTRAINT uq_membership_organization_user
                    UNIQUE (organization_id, user_id),

    CONSTRAINT uq_membership_organization_id
                    UNIQUE (organization_id, id),

    CONSTRAINT ck_memberships_status
                    CHECK ( status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'REMOVED') )
);
CREATE INDEX idx_memberships_user
        ON organization_memberships(user_id);

CREATE INDEX idx_memberships_organization_status
        ON organization_memberships(organization_id, status);

CREATE TABLE permissions(
    id UUID PRIMARY KEY,
    code VARCHAR(120) NOT NULL UNIQUE,
    resource VARCHAR(80) NOT NULL,
    action VARCHAR(40) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_permissions_resource_action
                    UNIQUE (resource, action)
);
CREATE TABLE organization_roles(
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    system_role BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_roles_organization
                    FOREIGN KEY (organization_id)
                    REFERENCES organizations(id)
                    ON DELETE RESTRICT,

    CONSTRAINT uq_organization_role_code
                    UNIQUE (organization_id, code),

    CONSTRAINT uq_organization_role_id
                    UNIQUE (organization_id, id)
);
CREATE TABLE membership_roles(
    organization_id UUID NOT NULL,
    membership_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID,
    expires_at TIMESTAMPTZ,

    PRIMARY KEY (membership_id, role_id),

    CONSTRAINT fk_membership_roles_membership
                    FOREIGN KEY (organization_id, membership_id)
                    REFERENCES organization_memberships(organization_id, id)
                    ON DELETE CASCADE,

    CONSTRAINT fk_membership_roles_role
                    FOREIGN KEY (organization_id, role_id)
                    REFERENCES organization_roles(organization_id, id)
                    ON DELETE CASCADE,

    CONSTRAINT fk_membership_roles_assigned_by
                    FOREIGN KEY (assigned_by)
                    REFERENCES users(id)
                    ON DELETE SET NULL ,

    CONSTRAINT ck_membership_role_expiration
                     CHECK ( expires_at IS NULL OR expires_at > assigned_at)
);
CREATE INDEX idx_membership_roles_role
        ON membership_roles(organization_id, role_id);

CREATE TABLE role_permissions(
    organization_id UUID NOT NULL,
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID,

    PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permission_role
                    FOREIGN KEY (organization_id, role_id)
                    REFERENCES organization_roles(organization_id, id)
                    ON DELETE CASCADE,

    CONSTRAINT fk_role_permission_permission
                    FOREIGN KEY (permission_id)
                    REFERENCES permissions(id)
                    ON DELETE RESTRICT,

    CONSTRAINT fk_role_permission_assigned_by
                    FOREIGN KEY (assigned_by)
                    REFERENCES users(id)
                    ON DELETE SET NULL
);
CREATE INDEX idx_role_permissions_permission
        ON role_permissions(permission_id);
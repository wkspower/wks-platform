-- Story 1.2 — Authentication & session management.
-- Creates users/roles/user_roles and seeds the `admin` role with a fixed UUID.
-- All identifiers lowercase; TIMESTAMP WITH TIME ZONE (UTC on the wire).
-- The fixed role UUID 0000...0001 is referenced by AdminUserSeeder and later
-- stories (RBAC, case-type role bindings) — do NOT randomise across migrations.

CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(320) NOT NULL UNIQUE,
    password_hash   VARCHAR(512) NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE roles (
    id      UUID PRIMARY KEY,
    name    VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

INSERT INTO roles (id, name)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin');

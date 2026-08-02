CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE SCHEMA IF NOT EXISTS auth;
CREATE TABLE auth.users (
                            id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            org_id         UUID NOT NULL,
                            email          VARCHAR(255) NOT NULL,
                            password_hash  VARCHAR(255) NOT NULL,
                            status         VARCHAR(20) NOT NULL DEFAULT 'INVITED',
                            created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                            updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX uq_users_email ON auth.users (lower(email));

CREATE TABLE auth.roles (
                            id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE auth.permissions (
                                  id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE auth.role_permissions (
                                       role_id       UUID NOT NULL REFERENCES auth.roles(id) ON DELETE CASCADE,
                                       permission_id UUID NOT NULL REFERENCES auth.permissions(id) ON DELETE CASCADE,
                                       PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE auth.user_roles (
                                 id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
                                 role_id UUID NOT NULL REFERENCES auth.roles(id) ON DELETE CASCADE,
                                 org_id  UUID NOT NULL,
                                 CONSTRAINT uq_user_role_org UNIQUE (user_id, role_id, org_id)
);

CREATE TABLE auth.refresh_tokens (
                                     id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     user_id    UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
                                     token_hash VARCHAR(255) NOT NULL UNIQUE,
                                     expires_at TIMESTAMPTZ NOT NULL,
                                     revoked_at TIMESTAMPTZ
);

INSERT INTO auth.roles (name) VALUES ('ADMIN'), ('LEAD'), ('ENGINEER');
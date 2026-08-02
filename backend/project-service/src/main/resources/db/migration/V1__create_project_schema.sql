CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE project.organizations (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX uq_organizations_name ON project.organizations (lower(name));

CREATE TABLE project.projects (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      UUID NOT NULL REFERENCES project.organizations(id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_projects_org_id ON project.projects (org_id);

CREATE TABLE project.repositories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID NOT NULL REFERENCES project.projects(id) ON DELETE CASCADE,
    git_url         VARCHAR(500) NOT NULL,
    default_branch  VARCHAR(100) NOT NULL DEFAULT 'main',
    sync_status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_synced_at  TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_repositories_project_id ON project.repositories (project_id);

CREATE TABLE project.members (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id    UUID NOT NULL REFERENCES project.organizations(id) ON DELETE CASCADE,
    user_id   UUID NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_member_org_user UNIQUE (org_id, user_id)
);

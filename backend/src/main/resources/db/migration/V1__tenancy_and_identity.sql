-- Tenancy root and identity: tenants, roles (+ permissions), branches, staff users.
--
-- Conventions used by every tenant-scoped table:
--   * tenant_id uuid not null references tenants(id)
--   * unique (id, tenant_id) so children can use a composite FK (x_id, tenant_id), which makes it
--     impossible at the database level to point a row at another tenant's record.
--   * audit columns created_at/created_by/updated_at/updated_by + optimistic-lock version.

create table tenants (
    id             uuid        primary key default uuidv7(),
    slug           text        not null unique check (slug ~ '^[a-z0-9][a-z0-9-]{1,48}[a-z0-9]$'),
    name           text        not null,
    status         text        not null default 'ACTIVE' check (status in ('ACTIVE', 'SUSPENDED')),
    default_region text        not null default 'IN',           -- ISO 3166 region for parsing local phone numbers
    timezone       text        not null default 'Asia/Kolkata',
    settings       jsonb       not null default '{}'::jsonb,
    created_at     timestamptz not null default now(),
    created_by     uuid,
    updated_at     timestamptz not null default now(),
    updated_by     uuid,
    version        bigint      not null default 0
);

-- Roles are per tenant so every business can rename them, add its own, and tune permissions.
-- The four PRD roles are seeded per tenant as is_system = true templates (see identity.DefaultRoles).
create table roles (
    id          uuid        primary key default uuidv7(),
    tenant_id   uuid        not null references tenants (id),
    code        text        not null check (code ~ '^[A-Z][A-Z0-9_]{1,49}$'),
    name        text        not null,
    description text,
    data_scope  text        not null default 'ALL' check (data_scope in ('ALL', 'BRANCH', 'OWN')),
    assignable  boolean     not null default false,          -- users with this role can own records (e.g. counsellor)
    is_system   boolean     not null default false,          -- seeded template; cannot be deleted
    created_at  timestamptz not null default now(),
    created_by  uuid,
    updated_at  timestamptz not null default now(),
    updated_by  uuid,
    version     bigint      not null default 0,
    unique (tenant_id, code),
    unique (tenant_id, name),
    unique (id, tenant_id)
);

-- Permission values are the identity.Permission enum; kept as text so new permissions need no migration.
create table role_permissions (
    role_id    uuid not null references roles (id) on delete cascade,
    permission text not null,
    primary key (role_id, permission)
);

create table branches (
    id         uuid        primary key default uuidv7(),
    tenant_id  uuid        not null references tenants (id),
    name       text        not null,
    city       text,
    active     boolean     not null default true,
    created_at timestamptz not null default now(),
    created_by uuid,
    updated_at timestamptz not null default now(),
    updated_by uuid,
    version    bigint      not null default 0,
    unique (tenant_id, name),
    unique (id, tenant_id)
);

-- Staff users. Login is phone-number based; auth_subject is the identity provider's user id and is
-- filled when the user first signs in. One person may be staff in several tenants (one row each).
create table app_users (
    id           uuid        primary key default uuidv7(),
    tenant_id    uuid        not null references tenants (id),
    auth_subject text,
    full_name    text        not null,
    phone_e164   text        not null check (phone_e164 ~ '^\+[1-9][0-9]{6,14}$'),
    email        text,
    role_id      uuid        not null,
    branch_id    uuid,
    status       text        not null default 'ACTIVE' check (status in ('INVITED', 'ACTIVE', 'DISABLED')),
    created_at   timestamptz not null default now(),
    created_by   uuid,
    updated_at   timestamptz not null default now(),
    updated_by   uuid,
    version      bigint      not null default 0,
    unique (tenant_id, phone_e164),
    unique (tenant_id, auth_subject),
    unique (id, tenant_id),
    foreign key (role_id, tenant_id) references roles (id, tenant_id),
    foreign key (branch_id, tenant_id) references branches (id, tenant_id)
);

create unique index app_users_tenant_email_uq on app_users (tenant_id, lower(email)) where email is not null;
create index app_users_role_idx on app_users (role_id);
create index app_users_branch_idx on app_users (branch_id);
create index app_users_auth_subject_idx on app_users (auth_subject) where auth_subject is not null;

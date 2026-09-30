-- SoftZenith platform administration: the people who onboard and configure tenants. Not tenant-scoped, and a separate
-- identity from tenant staff (username + password here, phone OTP for staff), so no staff role can ever reach it.

create table platform_admins (
    id              uuid        primary key default uuidv7(),
    username        text        not null unique check (username ~ '^[a-z0-9._-]{3,50}$'),
    password_hash   text        not null,
    failed_attempts int         not null default 0,
    locked_until    timestamptz,
    last_login_at   timestamptz,
    created_at      timestamptz not null default now(),
    created_by      uuid,
    updated_at      timestamptz not null default now(),
    updated_by      uuid,
    version         bigint      not null default 0
);

-- Every platform action (sign-in, tenant onboarded, ...), who did it and on what. Never updated or deleted.
create table platform_audit (
    id          uuid        primary key default uuidv7(),
    occurred_at timestamptz not null default now(),
    admin       text        not null,
    action      text        not null,
    target      text,
    detail      text
);
create index platform_audit_occurred_idx on platform_audit (occurred_at desc);

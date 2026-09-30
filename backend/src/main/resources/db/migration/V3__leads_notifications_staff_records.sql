-- Phase 1: leads, their activity trail, notification log, per-tenant counters, basic staff records.

-- Generic per-tenant counters for human-readable numbers (lead numbers now; students/applications later).
create table tenant_counters (
    tenant_id uuid   not null references tenants (id),
    name      text   not null,
    value     bigint not null default 0,
    primary key (tenant_id, name)
);

-- Basic employee record-keeping on staff users.
alter table app_users
    add column employee_code text,
    add column designation   text,
    add column joined_on     date;

create unique index app_users_tenant_employee_code_uq on app_users (tenant_id, employee_code) where employee_code is not null;

-- New permission: receive new-lead alerts. Granted to the seeded Admin and Receptionist roles of existing tenants
-- (new tenants get it from identity.DefaultRoles).
insert into role_permissions (role_id, permission)
select id, 'LEAD_NEW_ALERT' from roles where is_system and code in ('ADMIN', 'RECEPTIONIST')
on conflict do nothing;

create table leads (
    id                uuid        primary key default uuidv7(),
    tenant_id         uuid        not null references tenants (id),
    lead_number       text        not null,
    full_name         text        not null,
    phone_raw         text        not null,
    phone_e164        text        not null check (phone_e164 ~ '^\+[1-9][0-9]{6,14}$'),
    email             text,
    service_interest  text,
    preferred_country text,
    message           text,
    branch_id         uuid,
    source_type       text        not null check (source_type in
                          ('WEBSITE_FORM', 'META_LEAD_AD', 'WHATSAPP', 'WALK_IN', 'PHONE', 'REFERRAL', 'OTHER')),
    source_detail     text,
    utm_source        text,
    utm_medium        text,
    utm_campaign      text,
    external_ref      text,
    status            text        not null default 'NEW' check (status in ('NEW', 'CONTACTED', 'ASSIGNED', 'CLOSED')),
    assigned_to       uuid,
    assigned_at       timestamptz,
    assigned_by       uuid,
    closed_reason     text,
    closed_at         timestamptz,
    custom_fields     jsonb       not null default '{}'::jsonb,
    created_at        timestamptz not null default now(),
    created_by        uuid,
    updated_at        timestamptz not null default now(),
    updated_by        uuid,
    version           bigint      not null default 0,
    unique (tenant_id, lead_number),
    unique (id, tenant_id),
    foreign key (branch_id, tenant_id) references branches (id, tenant_id),
    foreign key (assigned_to, tenant_id) references app_users (id, tenant_id),
    check (status <> 'CLOSED' or closed_reason is not null),
    check (status <> 'ASSIGNED' or assigned_to is not null)
);

-- One open lead per person (phone + email, a missing email counts as a value) per tenant.
-- Repeat enquiries attach to it instead of creating a duplicate.
create unique index leads_open_person_uq on leads (tenant_id, phone_e164, lower(email)) nulls not distinct
    where status <> 'CLOSED';
create index leads_tenant_status_created_idx on leads (tenant_id, status, created_at desc);
create index leads_tenant_assigned_idx on leads (tenant_id, assigned_to);
create index leads_tenant_phone_idx on leads (tenant_id, phone_e164);

-- Append-only history of what happened to a lead (seed of the Phase 3 feed).
create table lead_activities (
    id          uuid        primary key default uuidv7(),
    tenant_id   uuid        not null references tenants (id),
    lead_id     uuid        not null,
    type        text        not null,
    from_value  text,
    to_value    text,
    note        text,
    created_at  timestamptz not null default now(),
    created_by  uuid,
    updated_at  timestamptz not null default now(),
    updated_by  uuid,
    version     bigint      not null default 0,
    foreign key (lead_id, tenant_id) references leads (id, tenant_id)
);
create index lead_activities_lead_idx on lead_activities (lead_id, created_at);

-- Every notification attempt, whatever the outcome, so delivery is auditable.
create table notification_log (
    id           uuid        primary key default uuidv7(),
    tenant_id    uuid        not null references tenants (id),
    lead_id      uuid,
    channel      text        not null check (channel in ('EMAIL', 'WHATSAPP')),
    kind         text        not null,
    recipient    text        not null,
    subject      text,
    body         text        not null,
    status       text        not null check (status in ('SENT', 'FAILED', 'DEMO')),
    provider     text        not null,
    provider_ref text,
    error        text,
    created_at   timestamptz not null default now(),
    created_by   uuid,
    updated_at   timestamptz not null default now(),
    updated_by   uuid,
    version      bigint      not null default 0,
    foreign key (lead_id, tenant_id) references leads (id, tenant_id)
);
create index notification_log_lead_idx on notification_log (lead_id, created_at);

-- Repeat enquiries (owner, 2026-09-26): a returning enquirer is added to their open lead, and that lead now
-- moves to the top of the list. last_enquiry_at / enquiry_count are bumped by an atomic update, not through
-- the entity, so a concurrent staff edit can never make a public enquiry fail.
alter table leads
    add column last_enquiry_at timestamptz,
    add column enquiry_count   integer     not null default 1 check (enquiry_count >= 1);

update leads l
set last_enquiry_at = coalesce((select max(a.created_at) from lead_activities a
                                where a.lead_id = l.id and a.type = 'REPEAT_ENQUIRY'), l.created_at),
    enquiry_count   = 1 + (select count(*) from lead_activities a
                           where a.lead_id = l.id and a.type = 'REPEAT_ENQUIRY');

alter table leads alter column last_enquiry_at set not null,
                  alter column last_enquiry_at set default now();

create index leads_tenant_last_enquiry_idx on leads (tenant_id, last_enquiry_at desc);

-- Counsellors update the status of the leads they work on (owner, 2026-09-26). Reopening stays LEAD_REOPEN.
insert into role_permissions (role_id, permission)
select id, 'LEAD_CHANGE_STATUS' from roles where is_system and code = 'COUNSELLOR'
on conflict do nothing;

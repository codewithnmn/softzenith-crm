-- Default-role changes (owner, 2026-09-25) applied to existing tenants' seeded roles; new tenants get them
-- from identity.DefaultRoles.
--   * Receptionist can assign leads to counsellors.
--   * Admin can see the lead dashboard (new REPORTS_VIEW permission).
insert into role_permissions (role_id, permission)
select id, 'LEAD_ASSIGN' from roles where is_system and code = 'RECEPTIONIST'
on conflict do nothing;

insert into role_permissions (role_id, permission)
select id, 'REPORTS_VIEW' from roles where is_system and code = 'ADMIN'
on conflict do nothing;

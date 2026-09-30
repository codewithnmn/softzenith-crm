-- Who sees which leads (owner, 2026-09-26): a Counsellor only the leads assigned to them, a Branch Manager the leads of
-- their branch (and any assigned to them); Admin and Receptionist keep seeing every lead. Applies to the seeded
-- (is_system) roles of existing tenants; new tenants get the same from DefaultRoles.
update roles set data_scope = 'OWN'    where is_system and code = 'COUNSELLOR';
update roles set data_scope = 'BRANCH' where is_system and code = 'BRANCH_MANAGER';

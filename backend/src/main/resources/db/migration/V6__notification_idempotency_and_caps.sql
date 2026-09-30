-- Re-delivered lead events must not message anyone twice: each attempt records the event it came from.
alter table notification_log add column event_id uuid;
create index notification_log_event_idx on notification_log (event_id) where event_id is not null;

-- SKIPPED = not sent on purpose (daily cap per enquirer reached), recorded so staff can see why.
alter table notification_log drop constraint notification_log_status_check;
alter table notification_log add constraint notification_log_status_check
    check (status in ('SENT', 'FAILED', 'DEMO', 'SKIPPED'));

-- Daily cap lookup: messages of one kind to one recipient in the last 24 hours.
create index notification_log_recipient_idx on notification_log (tenant_id, recipient, kind, created_at);

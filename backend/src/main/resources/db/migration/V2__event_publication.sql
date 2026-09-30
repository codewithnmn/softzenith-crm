-- Spring Modulith event publication registry (transactional outbox for cross-module events such as
-- LeadCreated -> notifications). Mirrors Modulith's JPA entities; managed by the library, not by us.

create table event_publication (
    id                     uuid        primary key,
    listener_id            text        not null,
    event_type             text        not null,
    serialized_event       text        not null,
    publication_date       timestamptz not null,
    completion_date        timestamptz,
    last_resubmission_date timestamptz,
    completion_attempts    integer     not null default 0,
    status                 text
);

create index event_publication_serialized_event_hash_idx on event_publication using hash (serialized_event);
create index event_publication_by_completion_date_idx on event_publication (completion_date);

create table event_publication_archive (
    id                     uuid        primary key,
    listener_id            text        not null,
    event_type             text        not null,
    serialized_event       text        not null,
    publication_date       timestamptz not null,
    completion_date        timestamptz,
    last_resubmission_date timestamptz,
    completion_attempts    integer     not null default 0,
    status                 text
);

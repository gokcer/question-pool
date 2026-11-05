create table exam (
    id         bigserial primary key,
    title      text   not null,
    forms      int    not null default 2 check (forms between 1 and 4),
    seed       bigint not null,
    created_at timestamptz not null default now()
);

create table exam_item (
    id          bigserial primary key,
    exam_id     bigint not null references exam (id) on delete cascade,
    question_id bigint not null references question (id) on delete cascade,
    unique (exam_id, question_id)
);

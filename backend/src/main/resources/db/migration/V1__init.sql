create table app_user (
    id            bigserial primary key,
    email         text not null unique,
    full_name     text not null,
    student_no    text,
    role          text not null default 'STUDENT' check (role in ('STUDENT', 'INSTRUCTOR')),
    nickname      text not null unique,
    active        boolean not null default true,
    created_at    timestamptz not null default now(),
    last_login_at timestamptz
);

create table login_token (
    token_hash text primary key,
    user_id    bigint not null references app_user (id) on delete cascade,
    created_at timestamptz not null default now(),
    expires_at timestamptz not null,
    used_at    timestamptz
);

create table user_session (
    token_hash text primary key,
    user_id    bigint not null references app_user (id) on delete cascade,
    created_at timestamptz not null default now(),
    expires_at timestamptz not null
);

create table chapter (
    id     serial primary key,
    number int  not null unique,
    title  text not null,
    doc_en text not null default '',
    doc_tr text not null default '',
    open   boolean not null default true
);

create table question (
    id              bigserial primary key,
    chapter_id      int    not null references chapter (id),
    author_id       bigint not null references app_user (id),
    stem            text   not null,
    option_a        text   not null,
    option_b        text   not null,
    option_c        text   not null,
    option_d        text   not null,
    correct         char(1) not null check (correct in ('A', 'B', 'C', 'D')),
    rationale_a     text   not null,
    rationale_b     text   not null,
    rationale_c     text   not null,
    rationale_d     text   not null,
    reference       text   not null,
    status          text   not null default 'PENDING'
        check (status in ('PENDING', 'NEEDS_REVISION', 'APPROVED', 'REJECTED')),
    ai_status       text   not null default 'NONE'
        check (ai_status in ('NONE', 'RUNNING', 'DONE', 'FAILED', 'SKIPPED')),
    ai_review       jsonb,
    instructor_note text,
    explanation     text,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    approved_at     timestamptz
);
create index question_chapter_idx on question (chapter_id, status);
create index question_author_idx on question (author_id);

create table attempt (
    id          bigserial primary key,
    question_id bigint  not null references question (id) on delete cascade,
    user_id     bigint  not null references app_user (id) on delete cascade,
    selected    char(1) not null check (selected in ('A', 'B', 'C', 'D')),
    correct     boolean not null,
    created_at  timestamptz not null default now(),
    unique (question_id, user_id)
);

create table comment (
    id          bigserial primary key,
    question_id bigint not null references question (id) on delete cascade,
    author_id   bigint references app_user (id) on delete cascade,
    is_ai       boolean not null default false,
    parent_id   bigint references comment (id) on delete cascade,
    body        text   not null,
    deleted     boolean not null default false,
    created_at  timestamptz not null default now()
);
create index comment_question_idx on comment (question_id);

create table flag (
    id          bigserial primary key,
    question_id bigint not null references question (id) on delete cascade,
    user_id     bigint not null references app_user (id) on delete cascade,
    reason      text   not null,
    resolved    boolean not null default false,
    created_at  timestamptz not null default now()
);

insert into chapter (number, title) values
    (1, 'Week 1 - Introduction to Software Testing'),
    (2, 'Week 2 - Test Design'),
    (3, 'Week 3 - Test Automation'),
    (4, 'Week 4 - Traditional vs Agile Testing'),
    (5, 'Week 5 - Testing Criteria'),
    (6, 'Week 6 - Static Testing'),
    (7, 'Week 7 - Specification-Based Testing');

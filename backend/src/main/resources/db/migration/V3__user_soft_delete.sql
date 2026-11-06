-- Deleting a person must not touch what they wrote. The account row stays and only gets a date, so the
-- questions, answers, comments and flags keep their author and the participation report stays correct.
alter table app_user add column deleted_at timestamptz;
create index app_user_live_idx on app_user (role, deleted_at);

--liquibase formatted sql

--changeset app:003-app-user-enabled-index
-- Usernames are case-insensitive and unique among active (enabled) users.
-- Disabled users are left out of the index, so it stays small and their
-- usernames can be reused.
CREATE UNIQUE INDEX app_user_enabled_username_idx ON app_user (lower(username)) WHERE enabled;

--liquibase formatted sql

--changeset app:002-create-app-user
CREATE TABLE app_user (
    id            UUID         PRIMARY KEY DEFAULT uuidv7(),
    username      VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

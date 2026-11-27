--liquibase formatted sql

--changeset app:001-create-item
CREATE TABLE item (
    id         UUID         PRIMARY KEY DEFAULT uuidv7(),
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

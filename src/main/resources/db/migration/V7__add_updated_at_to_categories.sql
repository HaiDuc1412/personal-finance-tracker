-- V7__add_updated_at_to_categories.sql

ALTER TABLE categories
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
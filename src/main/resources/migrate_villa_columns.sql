-- Migration: Add new columns to villas table
-- Run this once on the Railway PostgreSQL database

ALTER TABLE villas
    ADD COLUMN IF NOT EXISTS property_type VARCHAR(50) DEFAULT 'VILLA',
    ADD COLUMN IF NOT EXISTS property_number VARCHAR(100),
    ADD COLUMN IF NOT EXISTS region VARCHAR(100),
    ADD COLUMN IF NOT EXISTS whatsapp_link VARCHAR(500);

-- Update existing rows to have default property type
UPDATE villas SET property_type = 'VILLA' WHERE property_type IS NULL;

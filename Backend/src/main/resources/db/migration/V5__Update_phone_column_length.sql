-- Migration to increase phone column length in users table

-- Increase phone column length to accommodate longer phone numbers
ALTER TABLE users MODIFY COLUMN phone VARCHAR(20);
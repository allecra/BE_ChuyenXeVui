-- Add id_card field to users table for storing CMND/CCCD
ALTER TABLE users ADD COLUMN id_card VARCHAR(20) NULL COMMENT 'CMND/CCCD number';

-- Add index for id_card for faster lookup
CREATE INDEX idx_users_id_card ON users(id_card);
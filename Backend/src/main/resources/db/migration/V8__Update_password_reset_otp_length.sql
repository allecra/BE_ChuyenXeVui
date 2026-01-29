-- Update password_resets table to increase otp column length
-- This allows storing longer passwords/tokens if needed in the future
ALTER TABLE password_resets MODIFY COLUMN otp VARCHAR(255) NOT NULL;

-- Add comment for clarity
ALTER TABLE password_resets MODIFY COLUMN otp VARCHAR(255) NOT NULL COMMENT 'OTP code or temporary password';
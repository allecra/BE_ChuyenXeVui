-- Migration to fix SeatType enum values

-- First, delete any existing seats that might have invalid enum values
DELETE FROM seats WHERE seat_type NOT IN ('NORMAL', 'VIP', 'SLEEPER_LOWER', 'SLEEPER_UPPER', 'LUXURY', 'STANDARD');

-- Update seats table to support new seat types with proper enum values
ALTER TABLE seats MODIFY COLUMN seat_type ENUM(
    'NORMAL',
    'VIP', 
    'SLEEPER_LOWER',
    'SLEEPER_UPPER',
    'LUXURY',
    'STANDARD'
) NOT NULL;
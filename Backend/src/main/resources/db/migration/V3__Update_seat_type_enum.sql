-- Migration to update SeatType enum values

-- Update seats table to support new seat types
ALTER TABLE seats MODIFY COLUMN seat_type ENUM(
    'NORMAL',
    'VIP', 
    'SLEEPER_LOWER',
    'SLEEPER_UPPER',
    'LUXURY',
    'STANDARD'
) NOT NULL;
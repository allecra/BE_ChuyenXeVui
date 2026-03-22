-- V17__Update_seat_and_ticket_status.sql
-- Update seat and ticket status enums to support new booking workflow

-- Update seat status enum to include LOCKED
ALTER TABLE seats MODIFY COLUMN status 
    ENUM('AVAILABLE', 'LOCKED', 'BOOKED', 'MAINTENANCE', 'DELETED') 
    NOT NULL DEFAULT 'AVAILABLE';

-- Update ticket status enum to include PENDING and EXPIRED
ALTER TABLE tickets MODIFY COLUMN status 
    ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED', 'BOOKED') 
    NOT NULL DEFAULT 'PENDING';

-- Add new columns to tickets table for passenger information and payment deadline
ALTER TABLE tickets 
ADD COLUMN passenger_name VARCHAR(100) AFTER discount_amount,
ADD COLUMN passenger_phone VARCHAR(20) AFTER passenger_name,
ADD COLUMN passenger_email VARCHAR(100) AFTER passenger_phone,
ADD COLUMN passenger_id_card VARCHAR(20) AFTER passenger_email,
ADD COLUMN payment_deadline TIMESTAMP AFTER passenger_id_card,
ADD COLUMN notes VARCHAR(500) AFTER payment_deadline;

-- Add indexes for better performance
ALTER TABLE tickets 
ADD INDEX idx_status (status),
ADD INDEX idx_payment_deadline (payment_deadline),
ADD INDEX idx_passenger_phone (passenger_phone);

-- Add comments
ALTER TABLE tickets COMMENT = 'Bus tickets with passenger information and payment tracking';
ALTER TABLE seats COMMENT = 'Bus seats with availability status including temporary locks';
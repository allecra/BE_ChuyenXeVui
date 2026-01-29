-- Update SeatStatus enum to include MAINTENANCE and DELETED
ALTER TABLE seats MODIFY COLUMN status ENUM('AVAILABLE', 'BOOKED', 'MAINTENANCE', 'DELETED') NOT NULL DEFAULT 'AVAILABLE';

-- Add index for better performance on status queries
CREATE INDEX idx_seats_status ON seats(status);
CREATE INDEX idx_seats_bus_status ON seats(bus_id, status);
CREATE INDEX idx_seats_seat_number ON seats(seat_number);
-- Migration for Bus and Seat related tables

-- Add bus_company_id to users table
ALTER TABLE users ADD COLUMN bus_company_id INTEGER;
ALTER TABLE users ADD CONSTRAINT fk_users_bus_company FOREIGN KEY (bus_company_id) REFERENCES bus_companies(id);

-- Create deleted_buses table for soft delete
CREATE TABLE deleted_buses (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    original_bus_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    descriptions TEXT,
    license_plate VARCHAR(50),
    capacity INTEGER NOT NULL,
    company_id INTEGER,
    company_name VARCHAR(255),
    bus_type ENUM('GIUONG_NAM', 'GHE_NGOI', 'LIMOUSINE') NOT NULL,
    original_created_at TIMESTAMP,
    original_updated_at TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_by INTEGER,
    deletion_reason TEXT,
    INDEX idx_deleted_buses_company_id (company_id),
    INDEX idx_deleted_buses_deleted_by (deleted_by),
    INDEX idx_deleted_buses_deleted_at (deleted_at)
);

-- Update buses table if needed (ensure all columns exist)
-- Note: This assumes the buses table already exists from your entity
-- If not, you would need to create it first

-- Ensure seats table has all required columns
-- Note: This assumes the seats table already exists from your entity
-- If not, you would need to create it first

-- Add indexes for better performance
CREATE INDEX idx_buses_company_id ON buses(company_id);
CREATE INDEX idx_buses_status ON buses(status);
CREATE INDEX idx_buses_bus_type ON buses(bus_type);
CREATE INDEX idx_buses_license_plate ON buses(license_plate);

CREATE INDEX idx_seats_bus_id ON seats(bus_id);
CREATE INDEX idx_seats_status ON seats(status);
CREATE INDEX idx_seats_seat_number ON seats(seat_number);
CREATE INDEX idx_seats_row_column ON seats(row_num, column_num);
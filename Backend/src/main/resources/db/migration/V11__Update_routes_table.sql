-- Update routes table to support the new Route API requirements
-- Add missing columns and constraints

-- Add new columns to routes table
ALTER TABLE routes 
ADD COLUMN IF NOT EXISTS route_name VARCHAR(255) NOT NULL DEFAULT 'Unnamed Route',
ADD COLUMN IF NOT EXISTS start_location VARCHAR(255) NOT NULL DEFAULT '',
ADD COLUMN IF NOT EXISTS end_location VARCHAR(255) NOT NULL DEFAULT '',
ADD COLUMN IF NOT EXISTS descriptions TEXT,
ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
ADD COLUMN IF NOT EXISTS bus_company_id INTEGER,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Add foreign key constraint for bus_company_id
ALTER TABLE routes 
ADD CONSTRAINT fk_routes_bus_company 
FOREIGN KEY (bus_company_id) REFERENCES bus_companies(id);

-- Add check constraint for status
ALTER TABLE routes 
ADD CONSTRAINT chk_routes_status 
CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED'));

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_routes_status ON routes(status);
CREATE INDEX IF NOT EXISTS idx_routes_bus_company_id ON routes(bus_company_id);
CREATE INDEX IF NOT EXISTS idx_routes_start_location ON routes(start_location);
CREATE INDEX IF NOT EXISTS idx_routes_end_location ON routes(end_location);
CREATE INDEX IF NOT EXISTS idx_routes_price ON routes(price);
CREATE INDEX IF NOT EXISTS idx_routes_created_at ON routes(created_at);

-- Create composite indexes for common search patterns
CREATE INDEX IF NOT EXISTS idx_routes_company_status ON routes(bus_company_id, status);
CREATE INDEX IF NOT EXISTS idx_routes_locations ON routes(start_location, end_location);
CREATE INDEX IF NOT EXISTS idx_routes_price_range ON routes(price, status);

-- Update existing routes to have default values (if any exist)
UPDATE routes 
SET 
    route_name = CONCAT(
        COALESCE((SELECT name FROM stations WHERE id = departure_station_id), 'Unknown'),
        ' - ',
        COALESCE((SELECT name FROM stations WHERE id = arrival_station_id), 'Unknown')
    ),
    start_location = COALESCE((SELECT location FROM stations WHERE id = departure_station_id), 'Unknown'),
    end_location = COALESCE((SELECT location FROM stations WHERE id = arrival_station_id), 'Unknown'),
    status = 'ACTIVE'
WHERE route_name = 'Unnamed Route' OR route_name IS NULL;

-- Add unique constraint for route name within the same company
CREATE UNIQUE INDEX IF NOT EXISTS idx_routes_unique_name_company 
ON routes(route_name, bus_company_id) 
WHERE status != 'DELETED';
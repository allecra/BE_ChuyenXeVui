-- Create route_stations table for managing intermediate stations in routes
-- This enables segment-based pricing and multi-station routes

CREATE TABLE IF NOT EXISTS route_stations (
    id SERIAL PRIMARY KEY,
    route_id INTEGER NOT NULL,
    station_id INTEGER NOT NULL,
    order_index INTEGER NOT NULL,
    distance_from_previous INTEGER NOT NULL DEFAULT 0,
    price_from_previous DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_route_stations_route FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
    CONSTRAINT fk_route_stations_station FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE CASCADE,
    
    -- Unique constraints
    CONSTRAINT uk_route_stations_route_station UNIQUE (route_id, station_id),
    CONSTRAINT uk_route_stations_route_order UNIQUE (route_id, order_index),
    
    -- Check constraints
    CONSTRAINT chk_route_stations_order_index CHECK (order_index >= 0),
    CONSTRAINT chk_route_stations_distance CHECK (distance_from_previous >= 0),
    CONSTRAINT chk_route_stations_price CHECK (price_from_previous >= 0)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_route_stations_route_id ON route_stations(route_id);
CREATE INDEX IF NOT EXISTS idx_route_stations_station_id ON route_stations(station_id);
CREATE INDEX IF NOT EXISTS idx_route_stations_route_order ON route_stations(route_id, order_index);

-- Create composite index for route-station pair queries
CREATE INDEX IF NOT EXISTS idx_route_stations_route_station ON route_stations(route_id, station_id);

-- Create index for order-based queries
CREATE INDEX IF NOT EXISTS idx_route_stations_order_index ON route_stations(order_index);

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_route_stations_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_route_stations_updated_at
    BEFORE UPDATE ON route_stations
    FOR EACH ROW
    EXECUTE FUNCTION update_route_stations_updated_at();

-- Insert sample data for existing routes (if any)
-- This will create basic route structures with departure and arrival stations
INSERT INTO route_stations (route_id, station_id, order_index, distance_from_previous, price_from_previous, notes)
SELECT 
    r.id as route_id,
    r.departure_station_id as station_id,
    0 as order_index,
    0 as distance_from_previous,
    0.00 as price_from_previous,
    'Bến xuất phát' as notes
FROM routes r
WHERE r.departure_station_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM route_stations rs 
    WHERE rs.route_id = r.id AND rs.station_id = r.departure_station_id
);

INSERT INTO route_stations (route_id, station_id, order_index, distance_from_previous, price_from_previous, notes)
SELECT 
    r.id as route_id,
    r.arrival_station_id as station_id,
    1 as order_index,
    COALESCE(r.distance, 0) as distance_from_previous,
    COALESCE(r.price, 0.00) as price_from_previous,
    'Bến đến' as notes
FROM routes r
WHERE r.arrival_station_id IS NOT NULL
AND r.departure_station_id != r.arrival_station_id
AND NOT EXISTS (
    SELECT 1 FROM route_stations rs 
    WHERE rs.route_id = r.id AND rs.station_id = r.arrival_station_id
);

-- Add comment to table
COMMENT ON TABLE route_stations IS 'Stores the relationship between routes and stations with order and pricing information';
COMMENT ON COLUMN route_stations.order_index IS 'Order of station in route (0 = first station)';
COMMENT ON COLUMN route_stations.distance_from_previous IS 'Distance in kilometers from previous station in route';
COMMENT ON COLUMN route_stations.price_from_previous IS 'Price in VND from previous station to this station';
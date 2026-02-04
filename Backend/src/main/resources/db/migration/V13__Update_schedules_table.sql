-- V13: Update schedules table with new fields for enhanced schedule management
-- This migration adds new fields to support the enhanced Schedule API

-- Add new columns to schedules table
ALTER TABLE schedules 
ADD COLUMN start_station_id INTEGER,
ADD COLUMN end_station_id INTEGER,
ADD COLUMN price DECIMAL(15,2),
ADD COLUMN notes TEXT;

-- Add foreign key constraints for station references
ALTER TABLE schedules 
ADD CONSTRAINT fk_schedules_start_station 
    FOREIGN KEY (start_station_id) REFERENCES stations(id),
ADD CONSTRAINT fk_schedules_end_station 
    FOREIGN KEY (end_station_id) REFERENCES stations(id);

-- Update existing ScheduleStatus enum values
-- Change AVAILABLE to ACTIVE for consistency
UPDATE schedules SET status = 'ACTIVE' WHERE status = 'AVAILABLE';

-- Add index for better query performance
CREATE INDEX idx_schedules_start_station ON schedules(start_station_id);
CREATE INDEX idx_schedules_end_station ON schedules(end_station_id);
CREATE INDEX idx_schedules_departure_time ON schedules(departure_time);
CREATE INDEX idx_schedules_status ON schedules(status);
CREATE INDEX idx_schedules_price ON schedules(price);

-- Add composite indexes for common query patterns
CREATE INDEX idx_schedules_stations_time ON schedules(start_station_id, end_station_id, departure_time);
CREATE INDEX idx_schedules_bus_time ON schedules(bus_id, departure_time, arrival_time);
CREATE INDEX idx_schedules_route_status ON schedules(route_id, status);

-- Add comments for documentation
COMMENT ON COLUMN schedules.start_station_id IS 'ID của bến xuất phát';
COMMENT ON COLUMN schedules.end_station_id IS 'ID của bến đến';
COMMENT ON COLUMN schedules.price IS 'Giá vé cho đoạn từ bến đi đến bến đến (VND)';
COMMENT ON COLUMN schedules.notes IS 'Ghi chú về lịch trình';

-- Update table comment
COMMENT ON TABLE schedules IS 'Bảng lưu trữ lịch trình xe - Enhanced version with station and pricing support';
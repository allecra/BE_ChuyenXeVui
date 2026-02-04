-- V14: Create schedule_buses table for multi-bus schedule support
-- This migration creates the junction table to support multiple buses per schedule

-- Create schedule_buses table
CREATE TABLE schedule_buses (
    id SERIAL PRIMARY KEY,
    schedule_id INTEGER NOT NULL,
    bus_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_schedule_buses_schedule 
        FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_buses_bus 
        FOREIGN KEY (bus_id) REFERENCES buses(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate bus assignments
    CONSTRAINT uk_schedule_buses_schedule_bus 
        UNIQUE (schedule_id, bus_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_schedule_buses_schedule_id ON schedule_buses(schedule_id);
CREATE INDEX idx_schedule_buses_bus_id ON schedule_buses(bus_id);
CREATE INDEX idx_schedule_buses_status ON schedule_buses(status);
CREATE INDEX idx_schedule_buses_schedule_status ON schedule_buses(schedule_id, status);

-- Add check constraint for status values
ALTER TABLE schedule_buses 
ADD CONSTRAINT chk_schedule_buses_status 
CHECK (status IN ('ACTIVE', 'INACTIVE'));

-- Make bus_id nullable in schedules table for backward compatibility
-- This allows gradual migration from single-bus to multi-bus schedules
ALTER TABLE schedules ALTER COLUMN bus_id DROP NOT NULL;

-- Add comments for documentation
COMMENT ON TABLE schedule_buses IS 'Bảng liên kết lịch trình và xe - hỗ trợ nhiều xe cho một lịch trình';
COMMENT ON COLUMN schedule_buses.schedule_id IS 'ID của lịch trình';
COMMENT ON COLUMN schedule_buses.bus_id IS 'ID của xe';
COMMENT ON COLUMN schedule_buses.status IS 'Trạng thái xe trong lịch trình (ACTIVE, INACTIVE)';
COMMENT ON COLUMN schedule_buses.created_at IS 'Thời gian gán xe vào lịch trình';
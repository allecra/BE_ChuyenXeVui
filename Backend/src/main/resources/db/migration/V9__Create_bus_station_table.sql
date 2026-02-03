-- Create bus_station junction table for many-to-many relationship
CREATE TABLE IF NOT EXISTS bus_station (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bus_id INT NOT NULL,
    station_id INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (bus_id) REFERENCES buses(id) ON DELETE CASCADE,
    FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE CASCADE,
    UNIQUE KEY unique_bus_station (bus_id, station_id),
    INDEX idx_bus_station_bus_id (bus_id),
    INDEX idx_bus_station_station_id (station_id),
    INDEX idx_bus_station_active (is_active)
);
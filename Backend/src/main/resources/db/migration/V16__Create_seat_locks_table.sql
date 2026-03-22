-- V16__Create_seat_locks_table.sql
-- Create seat_locks table for managing temporary seat reservations

CREATE TABLE seat_locks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seat_id INT NOT NULL,
    schedule_id INT NOT NULL,
    user_id INT NOT NULL,
    locked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    session_id VARCHAR(255),
    status ENUM('ACTIVE', 'EXPIRED', 'CONVERTED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_seat_schedule (seat_id, schedule_id),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_status (status),
    INDEX idx_status_expires (status, expires_at),

    FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Add comment to table
ALTER TABLE seat_locks COMMENT = 'Temporary seat locks for booking process (10 minute duration)';
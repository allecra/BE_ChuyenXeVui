-- V15: Create enhanced payments table with payment providers
-- This migration creates the payment system tables

-- Create payment_providers table
CREATE TABLE payment_providers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    provider_name VARCHAR(50) NOT NULL UNIQUE,
    provider_type ENUM('CARD', 'E_WALLET', 'BANK_TRANSFER', 'QR_CODE') NOT NULL,
    api_endpoint VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_provider_name (provider_name),
    INDEX idx_provider_type (provider_type)
);

-- Drop existing payments table if exists (backup data first if needed)
DROP TABLE IF EXISTS payments;

-- Create enhanced payments table
CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    provider_transaction_id VARCHAR(255),
    payment_method ENUM('CASH', 'ONLINE') NOT NULL DEFAULT 'ONLINE',
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    description TEXT,
    qr_code_url TEXT,
    payment_url TEXT,
    expired_at TIMESTAMP NULL,
    paid_at TIMESTAMP NULL,
    callback_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign keys
    payment_provider_id INT NOT NULL,
    user_id INT NOT NULL,
    ticket_id INT,
    
    -- Indexes
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_provider_transaction_id (provider_transaction_id),
    INDEX idx_status (status),
    INDEX idx_user_id (user_id),
    INDEX idx_ticket_id (ticket_id),
    INDEX idx_payment_provider_id (payment_provider_id),
    INDEX idx_created_at (created_at),
    INDEX idx_expired_at (expired_at),
    
    -- Foreign key constraints
    CONSTRAINT fk_payments_payment_provider 
        FOREIGN KEY (payment_provider_id) REFERENCES payment_providers(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_payments_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_payments_ticket 
        FOREIGN KEY (ticket_id) REFERENCES tickets(id) 
        ON DELETE SET NULL ON UPDATE CASCADE,
        
    -- Constraints
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_expired_at_future CHECK (expired_at IS NULL OR expired_at > created_at)
);

-- Insert default payment providers
INSERT INTO payment_providers (provider_name, provider_type, api_endpoint) VALUES
('MOMO', 'E_WALLET', 'https://test-payment.momo.vn/v2/gateway/api/create'),
('SEPAY', 'QR_CODE', 'https://my.sepay.vn/userapi');

-- Add comment to tables
ALTER TABLE payment_providers COMMENT = 'Bảng nhà cung cấp thanh toán';
ALTER TABLE payments COMMENT = 'Bảng giao dịch thanh toán';
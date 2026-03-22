-- Create payment_providers table
CREATE TABLE payment_providers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    provider_name VARCHAR(50) NOT NULL UNIQUE,
    provider_type ENUM('E_WALLET', 'QR_CODE', 'BANK_CARD', 'BANK_TRANSFER') NOT NULL,
    api_endpoint VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    api_key VARCHAR(255),
    secret_key VARCHAR(255),
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_provider_name (provider_name),
    INDEX idx_provider_type (provider_type),
    INDEX idx_is_active (is_active)
);

-- Create discount_codes table
CREATE TABLE discount_codes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    discount_type ENUM('PERCENTAGE', 'FIXED_AMOUNT') NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    max_discount_amount DECIMAL(12,2),
    min_order_amount DECIMAL(12,2),
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    usage_limit INT NOT NULL,
    used_count INT NOT NULL DEFAULT 0,
    usage_limit_per_user INT NOT NULL DEFAULT 1,
    status ENUM('ACTIVE', 'INACTIVE', 'EXPIRED', 'USED_UP') NOT NULL DEFAULT 'ACTIVE',
    scope ENUM('PLATFORM', 'COMPANY', 'ROUTE') NOT NULL DEFAULT 'PLATFORM',
    bus_company_id INT,
    route_id INT,
    terms TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by INT,
    
    FOREIGN KEY (bus_company_id) REFERENCES bus_companies(id) ON DELETE CASCADE,
    FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_discount_code (code),
    INDEX idx_discount_status (status),
    INDEX idx_discount_scope (scope),
    INDEX idx_discount_company (bus_company_id),
    INDEX idx_discount_route (route_id),
    INDEX idx_discount_dates (start_date, end_date)
);

-- Create discount_usages table
CREATE TABLE discount_usages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    discount_code_id INT NOT NULL,
    user_id INT NOT NULL,
    ticket_id INT,
    order_amount DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) NOT NULL,
    used_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (discount_code_id) REFERENCES discount_codes(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE SET NULL,
    
    INDEX idx_usage_discount (discount_code_id),
    INDEX idx_usage_user (user_id),
    INDEX idx_usage_ticket (ticket_id),
    INDEX idx_usage_date (used_at)
);

-- Create reviews table
CREATE TABLE reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    ticket_id INT NOT NULL,
    route_id INT NOT NULL,
    bus_company_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    admin_notes TEXT,
    reviewed_by_admin_id INT,
    reviewed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
    FOREIGN KEY (bus_company_id) REFERENCES bus_companies(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by_admin_id) REFERENCES users(id) ON DELETE SET NULL,
    
    UNIQUE KEY unique_user_ticket_review (user_id, ticket_id),
    INDEX idx_review_status (status),
    INDEX idx_review_rating (rating),
    INDEX idx_review_company (bus_company_id),
    INDEX idx_review_route (route_id),
    INDEX idx_review_date (created_at)
);

-- Add discount_code_id to tickets table for tracking applied discounts
ALTER TABLE tickets 
ADD COLUMN discount_code_id INT,
ADD COLUMN discount_amount DECIMAL(10,2) DEFAULT 0,
ADD FOREIGN KEY (discount_code_id) REFERENCES discount_codes(id) ON DELETE SET NULL;
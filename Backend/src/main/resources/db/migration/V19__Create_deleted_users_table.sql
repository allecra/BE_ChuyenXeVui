-- Create deleted_users table for soft delete functionality
CREATE TABLE deleted_users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    original_user_id INT NOT NULL COMMENT 'ID của user gốc trước khi xóa',
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    id_card VARCHAR(20),
    bus_company_name VARCHAR(255) COMMENT 'Tên công ty xe buýt (nếu có)',
    delete_reason TEXT NOT NULL COMMENT 'Lý do xóa',
    delete_notes TEXT COMMENT 'Ghi chú thêm',
    deleted_by_admin_id INT NOT NULL COMMENT 'ID admin thực hiện xóa',
    deleted_by_admin_name VARCHAR(255) NOT NULL COMMENT 'Tên admin thực hiện xóa',
    deleted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian xóa',
    original_created_at TIMESTAMP COMMENT 'Thời gian tạo user gốc'
);

-- Add indexes for better performance
CREATE INDEX idx_deleted_users_original_id ON deleted_users(original_user_id);
CREATE INDEX idx_deleted_users_deleted_at ON deleted_users(deleted_at);
CREATE INDEX idx_deleted_users_deleted_by ON deleted_users(deleted_by_admin_id);
CREATE INDEX idx_deleted_users_email ON deleted_users(email);
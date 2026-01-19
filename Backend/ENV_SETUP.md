# Setup .env File

Để cấu hình các biến môi trường cho dự án, hãy làm theo các bước sau:

## 1. Tạo file .env

Copy file `.env.example` thành `.env`:

```bash
cp .env.example .env
```

## 2. Chỉnh sửa file .env

Mở file `.env` và cập nhật các giá trị của bạn:

```env
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ck_datveexe
DB_USERNAME=root
DB_PASSWORD=your_password_here

# Server Configuration
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api

# Application
APP_NAME=ck-datveexe-backend

# Logging
LOG_LEVEL=INFO
```

## 3. Các biến được hỗ trợ

| Biến                          | Mô tả             | Giá trị mặc định    |
| ----------------------------- | ----------------- | ------------------- |
| `DB_HOST`                     | Database host     | localhost           |
| `DB_PORT`                     | Database port     | 3306                |
| `DB_NAME`                     | Database name     | ck_datveexe         |
| `DB_USERNAME`                 | Database username | root                |
| `DB_PASSWORD`                 | Database password | (trống)             |
| `SERVER_PORT`                 | Server port       | 8080                |
| `SERVER_SERVLET_CONTEXT_PATH` | API context path  | /api                |
| `APP_NAME`                    | Application name  | ck-datveexe-backend |
| `LOG_LEVEL`                   | Logging level     | INFO                |

## 4. Lưu ý bảo mật

⚠️ **IMPORTANT**: File `.env` chứa thông tin nhạy cảm như mật khẩu

- File `.env` được thêm vào `.gitignore` để không bị commit
- Không chia sẻ file `.env` công khai
- Sử dụng file `.env.example` làm template cho các developer khác
- Trên production, sử dụng biến môi trường hệ thống thay vì file .env

## 5. Cách hoạt động

Application sẽ tự động load file `.env` khi khởi động nhờ class `EnvConfig`.
Các biến từ `.env` sẽ được sử dụng trong `application.yml` thông qua syntax:

```yaml
${ENV_VARIABLE_NAME:default_value}
```

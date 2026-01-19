# CK DatVeXe Backend

Spring Boot Backend application for CK DatVeXe project.

## Project Structure

```
src/
├── main/
│   ├── java/com/example/ckdatveexe/
│   │   ├── controller/          # REST API controllers
│   │   ├── service/             # Business logic layer
│   │   ├── repository/          # Data access layer
│   │   ├── entity/              # JPA entities
│   │   ├── dto/                 # Data transfer objects
│   │   ├── config/              # Configuration classes
│   │   ├── exception/           # Custom exceptions
│   │   └── CkDatVeXeApplication.java
│   └── resources/
│       └── application.yml      # Application configuration
└── test/
    └── java/com/example/ckdatveexe/
```

## Requirements

- Java 17+
- Maven 3.6+
- MySQL 5.7+

## Setup & Configuration

### 1. Database Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ck_datveexe
    username: root
    password: your_password
```

### 2. Build Project

```bash
mvn clean install
```

### 3. Run Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Health Check

- `GET /api/health` - Check API status

### Authentication

- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

## Dependencies

- Spring Boot Web
- Spring Data JPA
- Spring Security
- MySQL Connector
- Lombok
- Validation

## Testing

```bash
mvn test
```

## License

All rights reserved

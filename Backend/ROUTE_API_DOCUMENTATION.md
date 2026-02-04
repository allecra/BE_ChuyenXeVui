# Route Management API Documentation

## Overview

Comprehensive RESTful API for route management and search functionality in the bus booking system. The API supports two main roles: **USER** (read-only access) and **BUS_COMPANY** (full CRUD operations).

## Architecture

### Entities

- **Route**: Main route entity with enhanced fields
- **RouteStatus**: Enum (ACTIVE, INACTIVE, DELETED)
- **Station**: Departure and arrival stations
- **BusCompany**: Route owner
- **Schedule**: Time schedules for routes
- **Bus**: Vehicles operating on routes

### Database Schema Updates

- Added `route_name`, `start_location`, `end_location` fields
- Added `status`, `bus_company_id`, `deleted_at` for soft delete
- Added `descriptions` for route details
- Created indexes for optimal query performance
- Added unique constraint for route names within companies

## API Endpoints

### 🔓 USER APIs (No Authentication Required)

#### 1. Get All Active Routes

```http
GET /api/routes
```

**Parameters:**

- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "createdAt"): Sort field
- `sortDirection` (string, default: "desc"): Sort direction

**Response:** Paginated list of active routes with basic information.

#### 2. Get Route Detail

```http
GET /api/routes/{routeId}
```

**Response:** Detailed route information including buses list (only active buses).

#### 3. Search Routes (Advanced Filtering)

```http
GET /api/routes/search
```

**Parameters (all optional):**

- `startLocation` (string): Departure location
- `endLocation` (string): Arrival location
- `minPrice` (double): Minimum price filter
- `maxPrice` (double): Maximum price filter
- `busCompanyId` (int): Filter by company ID
- `busCompanyName` (string): Filter by company name
- `departureDate` (datetime): Departure date filter
- `departureTimeFrom` (datetime): Departure time range start
- `departureTimeTo` (datetime): Departure time range end
- `arrivalDate` (datetime): Arrival date filter
- `arrivalTimeFrom` (datetime): Arrival time range start
- `arrivalTimeTo` (datetime): Arrival time range end
- Pagination and sorting parameters

**Business Rules:**

- Only returns routes with `status = ACTIVE`
- Only shows routes with active buses and available schedules
- All filters can be combined
- Supports fuzzy text search for locations and company names

#### 4. Get Buses on Route

```http
GET /api/routes/{routeId}/buses
```

**Response:** List of active buses operating on the route.

### 🔐 BUS_COMPANY APIs (Authentication Required)

#### 1. Create Route

```http
POST /api/bus-company/routes
```

**Request Body:**

```json
{
  "routeName": "Hà Nội - Hồ Chí Minh (Cao tốc)",
  "startLocation": "Hà Nội",
  "endLocation": "Hồ Chí Minh",
  "price": 500000,
  "duration": 1200,
  "distance": 1700,
  "descriptions": "Tuyến cao tốc, đi qua các tỉnh miền Trung",
  "departureStationId": 1,
  "arrivalStationId": 2
}
```

**Validations:**

- Route name must be unique within the company
- Departure and arrival stations must be different
- All required fields must be provided
- Price, duration, distance must be positive

#### 2. Get All Company Routes

```http
GET /api/bus-company/routes
```

**Response:** Paginated list of all routes belonging to the company (ACTIVE + INACTIVE, excluding DELETED).

#### 3. Get Route Detail

```http
GET /api/bus-company/routes/{routeId}
```

**Response:** Detailed route information including all buses (active + inactive).

#### 4. Update Route

```http
PUT /api/bus-company/routes/{routeId}
```

**Request Body (all fields optional):**

```json
{
  "routeName": "Updated Route Name",
  "startLocation": "Updated Start",
  "endLocation": "Updated End",
  "price": 550000,
  "duration": 1100,
  "distance": 1650,
  "descriptions": "Updated description",
  "status": "INACTIVE",
  "departureStationId": 1,
  "arrivalStationId": 2
}
```

#### 5. Search Company Routes

```http
GET /api/bus-company/routes/search
```

**Parameters:**

- Same as user search but includes `status` filter
- Only returns routes belonging to the authenticated company

#### 6. Delete Route

```http
DELETE /api/bus-company/routes/{routeId}
```

**Request Body:**

```json
{
  "hardDelete": false,
  "reason": "Route no longer profitable"
}
```

**Delete Types:**

- **Soft Delete** (`hardDelete: false`): Sets status to DELETED, keeps data
- **Hard Delete** (`hardDelete: true`): Permanently removes from database

**Constraints:**

- Hard delete only allowed if no active schedules exist
- Company can only delete their own routes

#### 7. Get Buses on Route

```http
GET /api/bus-company/routes/{routeId}/buses
```

**Response:** List of all buses on the route (including inactive buses).

## Search Functionality

### User Search Features

- **Location-based**: Fuzzy search on start/end locations
- **Price range**: Min/max price filtering
- **Company filtering**: By ID or name (fuzzy search)
- **Time-based filtering**:
  - Departure date/time ranges
  - Arrival date/time ranges
- **Active routes only**: Automatically filters for active routes with available schedules

### Company Search Features

- **All user search features** plus:
- **Status filtering**: Can search by ACTIVE, INACTIVE status
- **Company-scoped**: Only searches within authenticated company's routes

### Search Performance

- Optimized database indexes for common search patterns
- Efficient JOIN queries with proper filtering
- Pagination support for large result sets

## Business Rules

### User Restrictions

- ✅ View active routes only
- ✅ View active buses only
- ✅ View available schedules only
- ❌ No create/update/delete operations
- ❌ Cannot see deleted or inactive routes

### Company Permissions

- ✅ Full CRUD operations on own routes
- ✅ View all route statuses (except deleted)
- ✅ Soft and hard delete capabilities
- ✅ Advanced search and filtering
- ❌ Cannot access other companies' routes
- ❌ Cannot hard delete routes with active schedules

### Data Integrity

- Route names must be unique within each company
- Departure and arrival stations must be different
- Soft deleted routes maintain referential integrity
- Hard delete only when no active dependencies exist

## Response Format

### Standard API Response

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    /* Response data */
  }
}
```

### Route Response Object

```json
{
  "id": 1,
  "routeName": "Hà Nội - Hồ Chí Minh",
  "startLocation": "Hà Nội",
  "endLocation": "Hồ Chí Minh",
  "price": 500000,
  "duration": 1200,
  "distance": 1700,
  "descriptions": "Route description",
  "status": "ACTIVE",
  "busCompanyId": 1,
  "busCompanyName": "Phương Trang Express",
  "departureStationId": 1,
  "departureStationName": "Bến xe Miền Đông",
  "arrivalStationId": 2,
  "arrivalStationName": "Bến xe Miền Tây",
  "activeScheduleCount": 5,
  "activeBusCount": 3,
  "createdAt": "2026-02-04T10:30:00",
  "updatedAt": "2026-02-04T10:30:00",
  "deletedAt": null,
  "buses": [
    /* Bus list for detail view */
  ]
}
```

## Error Handling

### HTTP Status Codes

- **200 OK**: Successful operation
- **201 CREATED**: Resource created successfully
- **400 BAD_REQUEST**: Invalid request data
- **401 UNAUTHORIZED**: Authentication required
- **403 FORBIDDEN**: Insufficient permissions
- **404 NOT_FOUND**: Resource not found
- **500 INTERNAL_SERVER_ERROR**: Server error

### Error Response Format

```json
{
  "success": false,
  "message": "Error description in Vietnamese",
  "data": null
}
```

## Security

### Authentication

- User APIs: No authentication required (public access)
- Company APIs: JWT Bearer token required
- Role-based access control: `ROLE_BUS_COMPANY`

### Authorization

- Companies can only access their own routes
- Automatic company ID extraction from JWT token
- Route ownership validation on all operations

## Performance Optimizations

### Database Indexes

- Status-based queries: `idx_routes_status`
- Company-based queries: `idx_routes_bus_company_id`
- Location searches: `idx_routes_start_location`, `idx_routes_end_location`
- Price filtering: `idx_routes_price`
- Composite indexes for common search patterns

### Query Optimization

- Efficient JOIN operations with proper filtering
- Pagination to handle large datasets
- Lazy loading for related entities
- Optimized search queries with minimal data transfer

## Integration Points

### Related APIs

- **Station API**: For departure/arrival station management
- **Bus API**: For vehicle management on routes
- **Schedule API**: For time schedule management
- **BusCompany API**: For company information

### External Dependencies

- JWT authentication service
- Database connection pool
- Logging and monitoring systems

## Future Enhancements

### Planned Features

- Route analytics and reporting
- Dynamic pricing based on demand
- Route optimization algorithms
- Integration with mapping services
- Real-time schedule updates
- Route popularity tracking

### Scalability Considerations

- Database sharding by company
- Caching layer for frequent searches
- Asynchronous processing for heavy operations
- API rate limiting and throttling

---

## Quick Start Examples

### Search Routes (User)

```bash
# Basic search
GET /api/routes/search?startLocation=Hà Nội&endLocation=Hồ Chí Minh

# Advanced search with time and price filters
GET /api/routes/search?startLocation=Hà Nội&endLocation=Hồ Chí Minh&minPrice=300000&maxPrice=800000&departureDate=2026-02-10T00:00:00&sortBy=price&sortDirection=asc
```

### Manage Routes (Company)

```bash
# Create route
POST /api/bus-company/routes
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "routeName": "Hà Nội - Đà Nẵng",
  "startLocation": "Hà Nội",
  "endLocation": "Đà Nẵng",
  "price": 350000,
  "duration": 720,
  "distance": 800,
  "departureStationId": 1,
  "arrivalStationId": 3
}

# Update route price
PUT /api/bus-company/routes/1
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "price": 380000
}
```

This comprehensive Route API provides a robust foundation for route management in the bus booking system with proper role-based access control, advanced search capabilities, and optimal performance.

# Bus Booking System - Complete API Documentation

## Overview

This document provides comprehensive documentation for all API endpoints in the Bus Booking System. The system supports multiple user roles with different access levels and provides RESTful APIs for managing buses, routes, stations, schedules, tickets, payments, and user accounts.

## System Architecture

### User Roles

- **USER**: Regular customers who can book tickets and view public information
- **BUS_COMPANY**: Bus operators who manage their buses, routes, and schedules
- **ADMIN**: System administrators with full access to all resources

### Authentication

- JWT Bearer token authentication for protected endpoints
- Role-based access control (RBAC)
- Some public endpoints require no authentication

### Base URL

```
https://api.busbook.com
```

### Response Format

All APIs return responses in the following format:

```json
{
  "success": true|false,
  "message": "Response message in Vietnamese",
  "data": { /* Response data */ }
}
```

## API Modules

The system is organized into the following modules:

1. **Authentication & Authorization** - User login, registration, password management
2. **Bus Management** - Vehicle information and management
3. **Route Management** - Route creation and search functionality
4. **Station Management** - Bus station information and management
5. **Schedule Management** - Trip schedules and timing
6. **Ticket Management** - Ticket booking and management
7. **Payment Management** - Payment processing and tracking
8. **Seat Management** - Seat layout and availability
9. **User Management** - User profiles and administration
10. **Media Management** - File upload and media handling

---

# 1. Authentication & Authorization APIs

## Base Path: `/auth`

### 1.1 User Login

```http
POST /auth/login
```

**Description:** Authenticate user and return access/refresh tokens

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "jwt_access_token",
    "refreshToken": "jwt_refresh_token",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "user@example.com",
      "phone": "0123456789",
      "roles": ["USER"],
      "status": "ACTIVE"
    }
  }
}
```

### 1.2 User Registration

```http
POST /auth/register
```

**Description:** Create new user account

**Request Body:**

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "user@example.com",
  "password": "password123",
  "phone": "0123456789"
}
```

### 1.3 Refresh Token

```http
POST /auth/refresh
```

**Description:** Generate new access token using refresh token

**Request Body:**

```json
{
  "refreshToken": "jwt_refresh_token"
}
```

### 1.4 Logout

```http
POST /auth/logout
```

**Authentication:** Required
**Description:** Logout user and invalidate tokens

### 1.5 Forgot Password

```http
POST /auth/forgot-password
```

**Description:** Send OTP to email for password reset

**Request Body:**

```json
{
  "email": "user@example.com"
}
```

### 1.6 Verify OTP

```http
POST /auth/verify-otp
```

**Description:** Verify OTP code for password reset

**Request Body:**

```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

### 1.7 Set New Password

```http
POST /auth/set-new-password
```

**Description:** Set new password after OTP verification

**Request Body:**

```json
{
  "email": "user@example.com",
  "otp": "123456",
  "newPassword": "newpassword123"
}
```

### 1.8 Get Current User

```http
GET /auth/me
```

**Authentication:** Required
**Description:** Get current authenticated user information

---

# 2. Bus Management APIs

## 2.1 User Bus APIs

**Base Path:** `/api/user/buses`
**Authentication:** Required (USER, ADMIN, BUS_COMPANY roles)

### 2.1.1 Get All Buses

```http
GET /api/user/buses
```

**Description:** Get list of all available buses

**Query Parameters:**

- `keyword` (string, optional): Search keyword
- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "createdAt"): Sort field
- `sortDirection` (string, default: "desc"): Sort direction

### 2.1.2 Get Bus Detail

```http
GET /api/user/buses/{busId}
```

**Description:** Get detailed information of a specific bus

### 2.1.3 Search Buses

```http
GET /api/user/buses/search
```

**Description:** Search buses with keyword

**Query Parameters:**

- `keyword` (string, required): Search keyword
- Pagination parameters

### 2.1.4 Get Buses by Company

```http
GET /api/user/buses/company/{companyId}
```

**Description:** Get buses belonging to a specific company

## 2.2 Bus Company APIs

**Base Path:** `/api/bus-company/buses`
**Authentication:** Required (BUS_COMPANY role)

### 2.2.1 Create Bus

```http
POST /api/bus-company/buses
```

**Description:** Create new bus and auto-generate seats

**Request Body:**

```json
{
  "name": "Xe Limousine VIP",
  "licensePlate": "30A-12345",
  "capacity": 22,
  "busType": "GIUONG_NAM",
  "descriptions": "Xe limousine cao cấp với ghế massage"
}
```

**Bus Types:**

- `GHE_NGOI`: Regular seats
- `GIUONG_NAM`: Sleeper beds
- `LIMOUSINE`: Luxury limousine

### 2.2.2 Get Company Buses

```http
GET /api/bus-company/buses
```

**Description:** Get all buses belonging to the company

**Query Parameters:**

- `keyword` (string, optional): Search keyword
- `status` (enum, optional): Bus status filter
- Pagination and sorting parameters

### 2.2.3 Get Bus Detail

```http
GET /api/bus-company/buses/{busId}
```

**Description:** Get detailed bus information for company

### 2.2.4 Update Bus

```http
PUT /api/bus-company/buses/{busId}
```

**Description:** Update bus information

**Request Body:**

```json
{
  "name": "Updated Bus Name",
  "descriptions": "Updated description",
  "status": "ACTIVE"
}
```

### 2.2.5 Delete Bus

```http
DELETE /api/bus-company/buses/{busId}
```

**Description:** Delete bus (soft or hard delete)

**Request Body:**

```json
{
  "hardDelete": false,
  "reason": "Bus retired from service"
}
```

### 2.2.6 Update Seat Layout

```http
PUT /api/bus-company/buses/{busId}/seat-layout
```

**Description:** Update seat arrangement of the bus

### 2.2.7 Regenerate Seats

```http
POST /api/bus-company/buses/{busId}/regenerate-seats
```

**Description:** Regenerate seats based on bus type

### 2.2.8 Get Deleted Buses

```http
GET /api/bus-company/buses/deleted
```

**Description:** Get list of soft-deleted buses

### 2.2.9 Restore Deleted Bus

```http
POST /api/bus-company/buses/deleted/{deletedBusId}/restore
```

**Description:** Restore a soft-deleted bus

## 2.3 Admin Bus APIs

**Base Path:** `/api/admin/buses`
**Authentication:** Required (ADMIN role)

### 2.3.1 Get All Buses (Admin)

```http
GET /api/admin/buses
```

**Description:** Get all buses in the system with advanced filtering

**Query Parameters:**

- `keyword` (string, optional): Search keyword
- `busType` (enum, optional): Bus type filter
- `status` (enum, optional): Status filter
- `companyId` (int, optional): Company filter
- Pagination and sorting parameters

### 2.3.2 Get Bus Detail (Admin)

```http
GET /api/admin/buses/{busId}
```

**Description:** Get detailed bus information (admin view)

### 2.3.3 Update Bus (Admin)

```http
PUT /api/admin/buses/{busId}
```

**Description:** Update any bus in the system

### 2.3.4 Get Buses by Company (Admin)

```http
GET /api/admin/buses/company/{companyId}
```

**Description:** Get all buses of a specific company

---

# 3. Route Management APIs

## 3.1 User Route APIs

**Base Path:** `/api/routes`
**Authentication:** Not required (Public access)

### 3.1.1 Get All Active Routes

```http
GET /api/routes
```

**Description:** Get list of all active routes

**Query Parameters:**

- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "createdAt"): Sort field
- `sortDirection` (string, default: "desc"): Sort direction

### 3.1.2 Get Route Detail

```http
GET /api/routes/{routeId}
```

**Description:** Get detailed route information

### 3.1.3 Search Routes

```http
GET /api/routes/search
```

**Description:** Advanced route search with multiple filters

**Query Parameters:**

- `startLocation` (string, optional): Departure location
- `endLocation` (string, optional): Arrival location
- `minPrice` (double, optional): Minimum price
- `maxPrice` (double, optional): Maximum price
- `busCompanyId` (int, optional): Company ID filter
- `busCompanyName` (string, optional): Company name filter
- `departureDate` (datetime, optional): Departure date
- `departureTimeFrom` (datetime, optional): Departure time range start
- `departureTimeTo` (datetime, optional): Departure time range end
- `arrivalDate` (datetime, optional): Arrival date
- `arrivalTimeFrom` (datetime, optional): Arrival time range start
- `arrivalTimeTo` (datetime, optional): Arrival time range end
- Pagination parameters

### 3.1.4 Get Buses on Route

```http
GET /api/routes/{routeId}/buses
```

**Description:** Get active buses operating on the route

### 3.1.5 Calculate Segment Price

```http
POST /api/routes/calculate-price
```

**Description:** Calculate ticket price for route segments

**Request Body:**

```json
{
  "routeId": 1,
  "departureStationId": 1,
  "arrivalStationId": 3
}
```

## 3.2 Bus Company Route APIs

**Base Path:** `/api/bus-company/routes`
**Authentication:** Required (BUS_COMPANY role)

### 3.2.1 Create Route

```http
POST /api/bus-company/routes
```

**Description:** Create new route

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

### 3.2.2 Get Company Routes

```http
GET /api/bus-company/routes
```

**Description:** Get all routes belonging to the company

### 3.2.3 Get Route Detail

```http
GET /api/bus-company/routes/{routeId}
```

**Description:** Get detailed route information for company

### 3.2.4 Update Route

```http
PUT /api/bus-company/routes/{routeId}
```

**Description:** Update route information

**Request Body:**

```json
{
  "routeName": "Updated Route Name",
  "price": 550000,
  "status": "INACTIVE",
  "descriptions": "Updated description"
}
```

### 3.2.5 Search Company Routes

```http
GET /api/bus-company/routes/search
```

**Description:** Search routes within company

### 3.2.6 Delete Route

```http
DELETE /api/bus-company/routes/{routeId}
```

**Description:** Delete route (soft or hard delete)

**Request Body:**

```json
{
  "hardDelete": false,
  "reason": "Route no longer profitable"
}
```

### 3.2.7 Get Buses on Route

```http
GET /api/bus-company/routes/{routeId}/buses
```

**Description:** Get all buses on route (including inactive)

### 3.2.8 Add Station to Route

```http
POST /api/bus-company/routes/{routeId}/stations
```

**Description:** Add station to route with order and pricing

**Request Body:**

```json
{
  "stationId": 2,
  "orderIndex": 1,
  "distanceFromPrevious": 150,
  "priceFromPrevious": 200000,
  "notes": "Bến trung gian, dừng 15 phút"
}
```

### 3.2.9 Get Route Stations

```http
GET /api/bus-company/routes/{routeId}/stations
```

**Description:** Get all stations in route by order

### 3.2.10 Update Route Station

```http
PUT /api/bus-company/routes/{routeId}/stations/{stationId}
```

**Description:** Update station information in route

### 3.2.11 Remove Station from Route

```http
DELETE /api/bus-company/routes/{routeId}/stations/{stationId}
```

**Description:** Remove station from route (intermediate stations only)

---

# 4. Station Management APIs

## 4.1 User Station APIs

**Base Path:** `/api/user/stations`
**Authentication:** Required (USER, ADMIN, BUS_COMPANY roles)

### 4.1.1 Get All Stations

```http
GET /api/user/stations
```

**Description:** Get list of all stations

**Query Parameters:**

- `keyword` (string, optional): Search keyword
- `location` (string, optional): Location filter
- Pagination and sorting parameters

### 4.1.2 Get Station Detail

```http
GET /api/user/stations/{stationId}
```

**Description:** Get detailed station information

### 4.1.3 Get Buses at Station

```http
GET /api/user/stations/{stationId}/buses
```

**Description:** Get active buses at the station

### 4.1.4 Search Stations

```http
GET /api/user/stations/search
```

**Description:** Search stations by keyword and location

### 4.1.5 Search Buses at Station

```http
GET /api/user/stations/{stationId}/buses/search
```

**Description:** Search buses at specific station

## 4.2 Bus Company Station APIs

**Base Path:** `/api/bus-company/stations`
**Authentication:** Required (BUS_COMPANY role)

### 4.2.1 Create Station

```http
POST /api/bus-company/stations
```

**Description:** Create new station

**Request Body:**

```json
{
  "name": "Bến xe Miền Đông",
  "location": "Hồ Chí Minh",
  "address": "292 Đinh Bộ Lĩnh, Bình Thạnh, TP.HCM",
  "phone": "028-38234567",
  "descriptions": "Bến xe lớn nhất miền Nam"
}
```

### 4.2.2 Get All Stations

```http
GET /api/bus-company/stations
```

**Description:** Get all stations (for company use)

### 4.2.3 Get Station Detail

```http
GET /api/bus-company/stations/{stationId}
```

**Description:** Get detailed station information

### 4.2.4 Update Station

```http
PUT /api/bus-company/stations/{stationId}
```

**Description:** Update station information

### 4.2.5 Assign Buses to Station (Basic)

```http
POST /api/bus-company/stations/assign-buses
```

**Description:** Assign buses to station (basic mode)

**Request Body:**

```json
{
  "stationId": 1,
  "busIds": [3, 4, 5],
  "replaceAll": false
}
```

### 4.2.6 Assign Buses to Station (Detailed)

```http
POST /api/bus-company/stations/assign-buses-detailed
```

**Description:** Assign buses with detailed information

**Request Body:**

```json
{
  "stationId": 1,
  "replaceAll": false,
  "busStations": [
    {
      "busId": 3,
      "stationId": 1,
      "notes": "Xe chính tuyến Hà Nội - Hồ Chí Minh",
      "isActive": true
    }
  ]
}
```

### 4.2.7 Get BusStation Details

```http
GET /api/bus-company/stations/{stationId}/bus-stations
```

**Description:** Get detailed BusStation information at station

### 4.2.8 Remove Buses from Station

```http
DELETE /api/bus-company/stations/{stationId}/buses
```

**Description:** Remove buses from station

### 4.2.9 Get Buses at Station

```http
GET /api/bus-company/stations/{stationId}/buses
```

**Description:** Get all buses at station (including inactive)

### 4.2.10 Search Stations

```http
GET /api/bus-company/stations/search
```

**Description:** Search stations by keyword

### 4.2.11 Search Buses at Station

```http
GET /api/bus-company/stations/{stationId}/buses/search
```

**Description:** Search buses at specific station

### 4.2.12 Delete Station

```http
DELETE /api/bus-company/stations/{stationId}
```

**Description:** Delete station (soft or hard delete)

### 4.2.13 Update Bus Status at Station

```http
PUT /api/bus-company/stations/{stationId}/buses/status
```

**Description:** Update bus status and notes at station

**Request Body:**

```json
{
  "busId": 3,
  "isActive": false,
  "notes": "Xe đang bảo trì định kỳ"
}
```

### 4.2.14 Debug BusStation Data

```http
GET /api/bus-company/stations/debug/bus-station-data
```

**Description:** Debug endpoint for BusStation data

## 4.3 Admin Station APIs

**Base Path:** `/api/admin/stations`
**Authentication:** Required (ADMIN role)

### 4.3.1 Create Station (Admin)

```http
POST /api/admin/stations
```

**Description:** Admin create new station

### 4.3.2 Get All Stations (Admin)

```http
GET /api/admin/stations
```

**Description:** Admin get all stations with full information

### 4.3.3 Get Station Detail (Admin)

```http
GET /api/admin/stations/{stationId}
```

**Description:** Admin get detailed station information

### 4.3.4 Update Station (Admin)

```http
PUT /api/admin/stations/{stationId}
```

**Description:** Admin update station information

### 4.3.5 Assign Buses to Station (Admin)

```http
POST /api/admin/stations/assign-buses
```

**Description:** Admin assign buses to station (unified mode)

### 4.3.6 Remove Buses from Station (Admin)

```http
DELETE /api/admin/stations/{stationId}/buses
```

**Description:** Admin remove buses from station

### 4.3.7 Get Buses at Station (Admin)

```http
GET /api/admin/stations/{stationId}/buses
```

**Description:** Admin get all buses at station

### 4.3.8 Get BusStation Details (Admin)

```http
GET /api/admin/stations/{stationId}/bus-stations
```

**Description:** Admin get detailed BusStation information

### 4.3.9 Delete Station (Admin)

```http
DELETE /api/admin/stations/{stationId}
```

**Description:** Admin delete station

### 4.3.10 Update Bus Status at Station (Admin)

```http
PUT /api/admin/stations/{stationId}/buses/status
```

**Description:** Admin update bus status at station

---

# 5. Schedule Management APIs

## 5.1 User Schedule APIs

**Base Path:** `/api/schedules`
**Authentication:** Not required (Public access)

### 5.1.1 Get All Active Schedules

```http
GET /api/schedules
```

**Description:** Get list of all active schedules

**Query Parameters:**

- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "departureTime"): Sort field
- `sortDirection` (string, default: "asc"): Sort direction

### 5.1.2 Get Schedule Detail

```http
GET /api/schedules/{scheduleId}
```

**Description:** Get detailed schedule information

### 5.1.3 Search Schedules

```http
GET /api/schedules/search
```

**Description:** Advanced schedule search with filters

**Query Parameters:**

- `startStationId` (int, optional): Departure station ID
- `endStationId` (int, optional): Arrival station ID
- `departureDate` (datetime, optional): Departure date
- `timeFrom` (datetime, optional): Time range start
- `timeTo` (datetime, optional): Time range end
- `minPrice` (double, optional): Minimum price
- `maxPrice` (double, optional): Maximum price
- `busCompanyId` (int, optional): Company filter
- `busType` (enum, optional): Bus type filter
- `minSeats` (int, optional): Minimum available seats
- Pagination parameters

### 5.1.4 Get Buses in Schedule

```http
GET /api/schedules/{scheduleId}/buses
```

**Description:** Get active buses in schedule

## 5.2 Company Schedule APIs

**Base Path:** `/api/company/schedules`
**Authentication:** Required (COMPANY role)

### 5.2.1 Create Schedule

```http
POST /api/company/schedules
```

**Description:** Create new schedule for route

**Request Body:**

```json
{
  "routeId": 1,
  "busId": 3,
  "departureTime": "2026-03-15T08:00:00",
  "arrivalTime": "2026-03-15T20:00:00",
  "price": 500000,
  "availableSeats": 40,
  "status": "ACTIVE"
}
```

### 5.2.2 Get Company Schedules

```http
GET /api/company/schedules
```

**Description:** Get all schedules belonging to company

### 5.2.3 Get Schedule Detail

```http
GET /api/company/schedules/{scheduleId}
```

**Description:** Get detailed schedule information for company

### 5.2.4 Update Schedule

```http
PUT /api/company/schedules/{scheduleId}
```

**Description:** Update schedule information

**Request Body:**

```json
{
  "departureTime": "2026-03-15T09:00:00",
  "arrivalTime": "2026-03-15T21:00:00",
  "price": 520000,
  "status": "ACTIVE"
}
```

### 5.2.5 Search Company Schedules

```http
GET /api/company/schedules/search
```

**Description:** Search schedules within company

**Query Parameters:**

- `routeId` (int, optional): Route filter
- `busId` (int, optional): Bus filter
- `startStationId` (int, optional): Start station filter
- `endStationId` (int, optional): End station filter
- `departureTimeFrom` (datetime, optional): Departure time range start
- `departureTimeTo` (datetime, optional): Departure time range end
- `arrivalTimeFrom` (datetime, optional): Arrival time range start
- `arrivalTimeTo` (datetime, optional): Arrival time range end
- `status` (enum, optional): Schedule status
- `minPrice` (double, optional): Minimum price
- `maxPrice` (double, optional): Maximum price
- Pagination parameters

### 5.2.6 Cancel Schedule

```http
DELETE /api/company/schedules/{scheduleId}
```

**Description:** Cancel schedule (set status to CANCELLED)

### 5.2.7 Assign Buses to Schedule

```http
POST /api/company/schedules/{scheduleId}/buses
```

**Description:** Assign buses to schedule

**Request Body:**

```json
{
  "busIds": [3, 4, 5],
  "replaceAll": false,
  "scheduleBuses": [
    {
      "busId": 3,
      "status": "ACTIVE",
      "notes": "Main bus for this schedule"
    }
  ]
}
```

### 5.2.8 Get Buses in Schedule

```http
GET /api/company/schedules/{scheduleId}/buses
```

**Description:** Get all buses in schedule (including inactive)

### 5.2.9 Update Bus Status in Schedule

```http
PUT /api/company/schedules/{scheduleId}/buses/{busId}/status
```

**Description:** Update bus status in schedule

**Request Body:**

```json
{
  "status": "MAINTENANCE",
  "notes": "Bus under maintenance"
}
```

### 5.2.10 Remove Bus from Schedule

```http
DELETE /api/company/schedules/{scheduleId}/buses/{busId}
```

**Description:** Remove bus from schedule

---

# 6. Ticket Management APIs

## 6.1 User Ticket APIs

**Base Path:** `/api/user`
**Authentication:** Required (USER role)

### 6.1.1 Book Ticket

```http
POST /api/user/tickets/book
```

**Description:** Book ticket and hold seat for 5 minutes pending payment

**Request Body:**

```json
{
  "scheduleId": 1,
  "seatId": 15,
  "passengerName": "Nguyễn Văn A",
  "passengerPhone": "0123456789",
  "passengerEmail": "passenger@example.com",
  "pickupLocation": "Bến xe Miền Đông",
  "dropoffLocation": "Bến xe Miền Tây"
}
```

### 6.1.2 Get Schedule Seats

```http
GET /api/user/schedules/{scheduleId}/seats
```

**Description:** Get seat layout and availability for a schedule

### 6.1.3 Lock Seat

```http
POST /api/user/seats/lock
```

**Description:** Lock seat for 10 minutes to prepare booking

**Request Body:**

```json
{
  "scheduleId": 1,
  "seatId": 15
}
```

### 6.1.4 Get Lock Countdown

```http
GET /api/user/seats/lock/{lockId}/countdown
```

**Description:** Get remaining time for seat lock

### 6.1.5 Unlock Seat

```http
POST /api/user/seats/unlock/{lockId}
```

**Description:** Cancel seat lock before expiration

### 6.1.6 Create Ticket from Lock

```http
POST /api/user/tickets/create-from-lock
```

**Description:** Create ticket from locked seat

**Request Body:**

```json
{
  "lockId": 123,
  "passengerName": "Nguyễn Văn A",
  "passengerPhone": "0123456789",
  "passengerEmail": "passenger@example.com"
}
```

### 6.1.7 Get User Tickets

```http
GET /api/user/tickets
```

**Description:** Get list of user's tickets

**Query Parameters:**

- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "createdAt"): Sort field
- `sortDir` (string, default: "desc"): Sort direction
- `status` (string, optional): Ticket status filter

### 6.1.8 Get Ticket Detail

```http
GET /api/user/tickets/{ticketId}
```

**Description:** Get detailed ticket information

### 6.1.9 Cancel Ticket

```http
POST /api/user/tickets/{ticketId}/cancel
```

**Description:** Cancel ticket (pending or confirmed)

## 6.2 Company Ticket APIs

**Base Path:** `/api/company`
**Authentication:** Required (COMPANY role)

### 6.2.1 Get Schedule Tickets

```http
GET /api/company/schedules/{scheduleId}/tickets
```

**Description:** Get all tickets for a schedule

**Query Parameters:**

- `page` (int, default: 0): Page number
- `size` (int, default: 20): Page size
- `sortBy` (string, default: "createdAt"): Sort field
- `sortDir` (string, default: "desc"): Sort direction
- `status` (string, optional): Ticket status filter

### 6.2.2 Get Schedule Seats (Company)

```http
GET /api/company/schedules/{scheduleId}/seats
```

**Description:** Get seat layout for company view

### 6.2.3 Book for Customer

```http
POST /api/company/tickets/book-for-customer
```

**Description:** Company books ticket on behalf of customer

**Request Body:**

```json
{
  "scheduleId": 1,
  "seatId": 15,
  "customerName": "Nguyễn Văn A",
  "customerPhone": "0123456789",
  "customerEmail": "customer@example.com",
  "paymentMethod": "CASH",
  "notes": "Booked at counter"
}
```

### 6.2.4 Get Company Tickets

```http
GET /api/company/tickets
```

**Description:** Get all tickets managed by company

**Query Parameters:**

- `page` (int, default: 0): Page number
- `size` (int, default: 20): Page size
- `sortBy` (string, default: "createdAt"): Sort field
- `sortDir` (string, default: "desc"): Sort direction
- `status` (string, optional): Ticket status filter
- `scheduleId` (int, optional): Schedule filter

### 6.2.5 Get Ticket Detail (Company)

```http
GET /api/company/tickets/{ticketId}
```

**Description:** Get ticket detail for company

### 6.2.6 Generate Tickets for Schedule

```http
POST /api/company/schedules/{scheduleId}/generate-tickets
```

**Description:** Generate tickets for all seats in schedule

---

# 7. Payment Management APIs

## 7.1 Public Payment APIs

**Base Path:** `/api/payment`
**Authentication:** Not required (Public access)

### 7.1.1 Get Payment Status

```http
GET /api/payment/status/{transactionId}
```

**Description:** Get payment status by transaction ID

### 7.1.2 MoMo Callback

```http
POST /api/payment/ipn/momo
```

**Description:** Receive payment notification from MoMo

### 7.1.3 SePay Callback

```http
POST /api/payment/ipn/sepay
```

**Description:** Receive payment notification from SePay

### 7.1.4 Test Payment Providers

```http
GET /api/payment/test/providers
```

**Description:** Test status of payment providers

### 7.1.5 Test Cleanup Expired Payments

```http
GET /api/payment/test/cleanup
```

**Description:** Manually trigger cleanup of expired payments

## 7.2 User Payment APIs

**Base Path:** `/api/user/payments`
**Authentication:** Required (USER role)

### 7.2.1 Create Payment

```http
POST /api/user/payments
```

**Description:** Create payment request and get QR code

**Request Body:**

```json
{
  "ticketId": 123,
  "amount": 500000,
  "paymentMethod": "MOMO",
  "returnUrl": "https://app.example.com/payment/success",
  "cancelUrl": "https://app.example.com/payment/cancel"
}
```

**Payment Methods:**

- `MOMO`: MoMo e-wallet
- `SEPAY`: SePay bank transfer
- `CASH`: Cash payment (for counter booking)

### 7.2.2 Get User Payments

```http
GET /api/user/payments
```

**Description:** Get list of user's payments

**Query Parameters:**

- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "createdAt"): Sort field
- `sortDir` (string, default: "desc"): Sort direction

### 7.2.3 Get Payment Detail

```http
GET /api/user/payments/{transactionId}
```

**Description:** Get detailed payment information

### 7.2.4 Cancel Payment

```http
POST /api/user/payments/{transactionId}/cancel
```

**Description:** Cancel pending payment request

---

# 8. Seat Management APIs

## 8.1 User Seat APIs

**Base Path:** `/api/user/seats`
**Authentication:** Required (USER, ADMIN, BUS_COMPANY roles)

### 8.1.1 Get Seats by Bus

```http
GET /api/user/seats/bus/{busId}
```

**Description:** Get seat layout of a bus (non-deleted seats only)

**Query Parameters:**

- `status` (enum, optional): Seat status filter
- `seatType` (enum, optional): Seat type filter
- `minPrice` (double, optional): Minimum price filter
- `maxPrice` (double, optional): Maximum price filter
- `seatNumber` (string, optional): Seat number filter
- Pagination and sorting parameters

**Seat Status:**

- `AVAILABLE`: Available for booking
- `BOOKED`: Already booked
- `MAINTENANCE`: Under maintenance
- `DELETED`: Soft deleted

**Seat Types:**

- `NORMAL`: Regular seat
- `VIP`: VIP seat with extra space
- `SLEEPER`: Sleeper bed

### 8.1.2 Get Seat Detail

```http
GET /api/user/seats/{seatId}
```

**Description:** Get detailed seat information

### 8.1.3 Get Available Seats

```http
GET /api/user/seats/available/bus/{busId}
```

**Description:** Get only available seats of a bus

**Query Parameters:**

- `seatType` (enum, optional): Seat type filter
- `minPrice` (double, optional): Minimum price filter
- `maxPrice` (double, optional): Maximum price filter
- Pagination and sorting parameters

## 8.2 Company Seat APIs

**Base Path:** `/api/bus-company/seats`
**Authentication:** Required (BUS_COMPANY role)

### 8.2.1 Create Seat

```http
POST /api/bus-company/seats
```

**Description:** Create new seat for company's bus

**Request Body:**

```json
{
  "busId": 1,
  "seatNumber": "A1",
  "seatType": "VIP",
  "priceForSeatType": 50000,
  "position": {
    "row": 1,
    "column": 1,
    "floor": 1
  },
  "descriptions": "VIP seat with extra legroom"
}
```

### 8.2.2 Get Company Seats

```http
GET /api/bus-company/seats
```

**Description:** Get all seats belonging to company (including deleted)

**Query Parameters:**

- `busId` (int, optional): Bus filter
- `status` (enum, optional): Status filter
- `seatType` (enum, optional): Seat type filter
- `minPrice` (double, optional): Minimum price filter
- `maxPrice` (double, optional): Maximum price filter
- `seatNumber` (string, optional): Seat number filter
- Pagination and sorting parameters

### 8.2.3 Get Seat Detail

```http
GET /api/bus-company/seats/{seatId}
```

**Description:** Get detailed seat information for company

### 8.2.4 Update Seat

```http
PUT /api/bus-company/seats/{seatId}
```

**Description:** Update seat information

**Request Body:**

```json
{
  "seatNumber": "A1-Updated",
  "seatType": "VIP",
  "descriptions": "Updated description",
  "position": {
    "row": 1,
    "column": 1,
    "floor": 1
  }
}
```

### 8.2.5 Update Seat Status

```http
PUT /api/bus-company/seats/{seatId}/status
```

**Description:** Change seat status

**Request Body:**

```json
{
  "status": "MAINTENANCE",
  "reason": "Seat needs repair"
}
```

### 8.2.6 Update Seat Price

```http
PUT /api/bus-company/seats/{seatId}/price
```

**Description:** Update seat pricing

**Request Body:**

```json
{
  "priceForSeatType": 60000,
  "reason": "Price adjustment for VIP seats"
}
```

### 8.2.7 Delete Seat

```http
DELETE /api/bus-company/seats/{seatId}
```

**Description:** Soft delete seat (set status to DELETED)

---

# 9. User Management APIs

## 9.1 User Profile APIs

**Base Path:** `/api/user/profile`
**Authentication:** Required (USER role)

### 9.1.1 Get User Profile

```http
GET /api/user/profile
```

**Description:** Get current user's profile information

**Response:**

```json
{
  "success": true,
  "message": "Lấy thông tin cá nhân thành công",
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "user@example.com",
    "phone": "0123456789",
    "dateOfBirth": "1990-01-01",
    "gender": "MALE",
    "address": "123 Main St, City",
    "avatar": "https://example.com/avatar.jpg",
    "status": "ACTIVE",
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-01T00:00:00"
  }
}
```

### 9.1.2 Update User Profile

```http
PUT /api/user/profile
```

**Description:** Update user profile information

**Request Body:**

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "0123456789",
  "dateOfBirth": "1990-01-01",
  "gender": "MALE",
  "address": "123 Main St, City",
  "avatar": "https://example.com/avatar.jpg"
}
```

### 9.1.3 Get Profile for Booking

```http
GET /api/user/profile/for-booking
```

**Description:** Get user profile to auto-fill booking form

## 9.2 Admin User Management APIs

**Base Path:** `/admin/users`
**Authentication:** Required (ADMIN role)

### 9.2.1 Get All Users

```http
GET /admin/users
```

**Description:** Get list of all users in system

**Query Parameters:**

- `status` (enum, optional): User status filter
- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "createdAt"): Sort field
- `sortDir` (string, default: "desc"): Sort direction

**User Status:**

- `ACTIVE`: Active user
- `INACTIVE`: Inactive user
- `BLOCKED`: Blocked user
- `DELETED`: Soft deleted user

### 9.2.2 Search Users

```http
GET /admin/users/search
```

**Description:** Search users by keyword

**Query Parameters:**

- `keyword` (string, required): Search keyword
- `status` (enum, optional): User status filter
- Pagination and sorting parameters

### 9.2.3 Get User Detail

```http
GET /admin/users/{id}
```

**Description:** Get detailed user information

### 9.2.4 Update User

```http
PUT /admin/users/{id}
```

**Description:** Update user information

**Request Body:**

```json
{
  "firstName": "Updated Name",
  "lastName": "Updated Last",
  "phone": "0987654321",
  "status": "ACTIVE",
  "roles": ["USER", "BUS_COMPANY"]
}
```

### 9.2.5 Block User

```http
PUT /admin/users/{id}/block
```

**Description:** Block user account

### 9.2.6 Unblock User

```http
PUT /admin/users/{id}/unblock
```

**Description:** Unblock user account

### 9.2.7 Delete User

```http
DELETE /admin/users/{id}
```

**Description:** Delete user (soft or hard delete)

**Request Body:**

```json
{
  "hardDelete": false,
  "reason": "User requested account deletion"
}
```

### 9.2.8 Get Deleted Users

```http
GET /admin/users/deleted
```

**Description:** Get list of soft-deleted users

### 9.2.9 Restore User

```http
POST /admin/users/deleted/{deletedUserId}/restore
```

**Description:** Restore soft-deleted user

---

# 10. Media Management APIs

## Base Path: `/media`

**Authentication:** Required (USER, ADMIN, BUS_COMPANY roles)

### 10.1 Upload File

```http
POST /media/upload
```

**Description:** Upload image file to Cloudinary

**Content-Type:** `multipart/form-data`

**Form Parameters:**

- `file` (file, required): Image file to upload
- `folder` (string, optional): Cloudinary folder path

**File Constraints:**

- Maximum size: 10MB
- Allowed types: Image files only (jpg, png, gif, etc.)
- Content-Type must start with "image/"

**Response:**

```json
{
  "success": true,
  "message": "Upload file thành công",
  "data": {
    "url": "https://res.cloudinary.com/demo/image/upload/v1234567890/sample.jpg",
    "publicId": "sample_abc123",
    "format": "jpg",
    "resourceType": "image",
    "bytes": 245760
  }
}
```

**Error Responses:**

- `400 BAD_REQUEST`: Empty file, file too large, or invalid file type
- `500 INTERNAL_SERVER_ERROR`: Upload failed

---

# Common Data Types and Enums

## Bus Types

- `GHE_NGOI`: Regular seats (45 seats)
- `GIUONG_NAM`: Sleeper beds (22 beds)
- `LIMOUSINE`: Luxury limousine (16-20 seats)

## Bus Status

- `ACTIVE`: Active and operational
- `INACTIVE`: Temporarily inactive
- `MAINTENANCE`: Under maintenance
- `DELETED`: Soft deleted

## Route Status

- `ACTIVE`: Active route
- `INACTIVE`: Temporarily inactive
- `DELETED`: Soft deleted

## Schedule Status

- `ACTIVE`: Active schedule
- `CANCELLED`: Cancelled schedule
- `COMPLETED`: Completed trip

## Ticket Status

- `PENDING`: Awaiting payment
- `CONFIRMED`: Payment confirmed
- `CANCELLED`: Cancelled ticket
- `USED`: Ticket used for travel

## Payment Status

- `PENDING`: Payment pending
- `SUCCESS`: Payment successful
- `FAILED`: Payment failed
- `CANCELLED`: Payment cancelled
- `EXPIRED`: Payment expired

## Seat Status

- `AVAILABLE`: Available for booking
- `BOOKED`: Already booked
- `MAINTENANCE`: Under maintenance
- `DELETED`: Soft deleted

## Seat Types

- `NORMAL`: Regular seat
- `VIP`: VIP seat with extra space
- `SLEEPER`: Sleeper bed

## User Status

- `ACTIVE`: Active user
- `INACTIVE`: Inactive user
- `BLOCKED`: Blocked user
- `DELETED`: Soft deleted

## User Roles

- `USER`: Regular customer
- `BUS_COMPANY`: Bus company operator
- `ADMIN`: System administrator

---

# Error Handling

## HTTP Status Codes

- `200 OK`: Successful operation
- `201 CREATED`: Resource created successfully
- `400 BAD_REQUEST`: Invalid request data
- `401 UNAUTHORIZED`: Authentication required
- `403 FORBIDDEN`: Insufficient permissions
- `404 NOT_FOUND`: Resource not found
- `500 INTERNAL_SERVER_ERROR`: Server error

## Error Response Format

```json
{
  "success": false,
  "message": "Error description in Vietnamese",
  "data": null
}
```

## Common Error Messages

- `"Email không tồn tại trong hệ thống"`: Email not found
- `"Mật khẩu không chính xác"`: Invalid password
- `"Tài khoản đã bị khóa"`: Account blocked
- `"Không có quyền truy cập"`: Access denied
- `"Dữ liệu không hợp lệ"`: Invalid data
- `"Lỗi hệ thống"`: System error

---

# Authentication & Authorization

## JWT Token Structure

```
Authorization: Bearer <jwt_token>
```

## Token Expiration

- Access Token: 24 hours
- Refresh Token: 7 days

## Role-Based Access Control

- **Public APIs**: No authentication required
- **User APIs**: Require USER role or higher
- **Company APIs**: Require BUS_COMPANY role
- **Admin APIs**: Require ADMIN role

## Cookie-Based Authentication

The system also supports cookie-based authentication:

- `accessToken`: HTTP-only cookie with access token
- `refreshToken`: HTTP-only cookie with refresh token

---

# Pagination

## Standard Pagination Parameters

- `page` (int, default: 0): Page number (0-based)
- `size` (int, default: 10): Page size
- `sortBy` (string): Field to sort by
- `sortDirection` (string): "asc" or "desc"

## Pagination Response Format

```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "ascending": false
    }
  },
  "totalElements": 100,
  "totalPages": 10,
  "first": true,
  "last": false,
  "numberOfElements": 10
}
```

---

# Rate Limiting

## Default Limits

- Public APIs: 100 requests per minute
- Authenticated APIs: 1000 requests per minute
- File Upload: 10 requests per minute

## Rate Limit Headers

```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1640995200
```

---

# API Versioning

Current API version: `v1`

All endpoints are prefixed with the version in the base URL:

```
https://api.busbook.com/v1/
```

---

This completes the comprehensive API documentation for the Bus Booking System with all 80+ endpoints across 10 modules, including detailed request/response examples, authentication requirements, and business rules.

---

## NEW APIS - Recently Added

### 9. Discount Code Management

#### 9.1 Create Discount Code (ADMIN)

- **POST** `/api/admin/discount-codes`
- **Auth**: Admin required
- **Description**: Create a new discount code
- **Request Body**:

```json
{
  "code": "SUMMER2024",
  "description": "Summer promotion discount",
  "discountType": "PERCENTAGE",
  "discountValue": 15.0,
  "minOrderAmount": 100000.0,
  "maxDiscountAmount": 50000.0,
  "usageLimit": 100,
  "validFrom": "2024-06-01T00:00:00",
  "validTo": "2024-08-31T23:59:59",
  "isActive": true
}
```

#### 9.2 Get All Discount Codes (ADMIN)

- **GET** `/api/admin/discount-codes`
- **Auth**: Admin required
- **Query Parameters**: `page`, `size`, `sortBy`, `sortDir`

#### 9.3 Get Discount Code by ID (ADMIN)

- **GET** `/api/admin/discount-codes/{id}`
- **Auth**: Admin required

#### 9.4 Update Discount Code (ADMIN)

- **PUT** `/api/admin/discount-codes/{id}`
- **Auth**: Admin required

#### 9.5 Delete Discount Code (ADMIN)

- **DELETE** `/api/admin/discount-codes/{id}`
- **Auth**: Admin required

#### 9.6 Validate Discount Code (USER)

- **POST** `/api/user/discount-codes/validate`
- **Auth**: User required
- **Request Body**:

```json
{
  "code": "SUMMER2024",
  "orderAmount": 200000.0
}
```

#### 9.7 Get Active Discount Codes (USER)

- **GET** `/api/user/discount-codes/active`
- **Auth**: User required

#### 9.8 Get Discount Usage Statistics (ADMIN)

- **GET** `/api/admin/discount-codes/{id}/usage`
- **Auth**: Admin required

### 10. Ticket Cancellation & Modification

#### 10.1 Cancel Confirmed Ticket with Refund (USER)

- **POST** `/api/user/tickets/cancel-confirmed`
- **Auth**: User required
- **Description**: Cancel a confirmed ticket and process refund according to cancellation policy
- **Request Body**:

```json
{
  "ticketId": 1,
  "cancellationReason": "Change of plans",
  "bankAccountNumber": "1234567890",
  "bankName": "Vietcombank",
  "bankAccountName": "Nguyen Van A"
}
```

#### 10.2 Modify Ticket (USER)

- **POST** `/api/user/tickets/modify`
- **Auth**: User required
- **Description**: Modify ticket schedule or seats with additional fees
- **Request Body**:

```json
{
  "ticketId": 1,
  "newScheduleId": 2,
  "newSeatNumbers": ["A1", "A2"],
  "modificationReason": "Change travel date"
}
```

### 11. Bus Review System

#### 11.1 Create Bus Review (USER)

- **POST** `/api/reviews`
- **Auth**: User required (must have used the bus)
- **Request Body**:

```json
{
  "busId": 1,
  "rating": 5,
  "comment": "Excellent service and comfortable seats",
  "serviceRating": 5,
  "comfortRating": 4,
  "punctualityRating": 5
}
```

#### 11.2 Get Reviews by Bus

- **GET** `/api/reviews/bus/{busId}`
- **Query Parameters**: `page`, `size`, `sortBy`, `sortDir`

#### 11.3 Get Bus Review Statistics

- **GET** `/api/reviews/bus/{busId}/stats`
- **Response**:

```json
{
  "busId": 1,
  "totalReviews": 150,
  "averageRating": 4.2,
  "ratingDistribution": {
    "5": 60,
    "4": 45,
    "3": 30,
    "2": 10,
    "1": 5
  }
}
```

#### 11.4 Get Reviews by User

- **GET** `/api/reviews/user/{userId}`

#### 11.5 Get Current User's Reviews (USER)

- **GET** `/api/reviews/my-reviews`
- **Auth**: User required

#### 11.6 Get User's Review for Specific Bus (USER)

- **GET** `/api/reviews/bus/{busId}/my-review`
- **Auth**: User required

#### 11.7 Update Review (USER)

- **PUT** `/api/reviews/{reviewId}`
- **Auth**: User required (owner only)

#### 11.8 Delete Review (USER/ADMIN)

- **DELETE** `/api/reviews/{reviewId}`
- **Auth**: User (owner) or Admin required

#### 11.9 Get Reviews by Company

- **GET** `/api/reviews/company/{companyId}`

#### 11.10 Filter Reviews by Rating

- **GET** `/api/reviews/filter`
- **Query Parameters**: `minRating`, `maxRating`, `page`, `size`

### 12. Admin Ticket Management

#### 12.1 Search All Tickets (ADMIN)

- **GET** `/api/admin/tickets/search`
- **Auth**: Admin required
- **Query Parameters**: `userId`, `scheduleId`, `status`, `fromDate`, `toDate`, `page`, `size`

#### 12.2 Get Ticket Detail (ADMIN)

- **GET** `/api/admin/tickets/{ticketId}`
- **Auth**: Admin required

#### 12.3 Update Ticket Status (ADMIN)

- **PUT** `/api/admin/tickets/{ticketId}/status`
- **Auth**: Admin required
- **Request Body**:

```json
{
  "status": "CONFIRMED",
  "notes": "Manual confirmation by admin"
}
```

#### 12.4 Cancel Ticket (ADMIN)

- **POST** `/api/admin/tickets/{ticketId}/cancel`
- **Auth**: Admin required

#### 12.5 Get Ticket Statistics (ADMIN)

- **GET** `/api/admin/tickets/statistics`
- **Auth**: Admin required

#### 12.6 Export Tickets (ADMIN)

- **GET** `/api/admin/tickets/export`
- **Auth**: Admin required
- **Query Parameters**: `format` (CSV/EXCEL), `fromDate`, `toDate`

### 13. Schedule Management Advanced

#### 13.1 Cancel Schedule (BUS_COMPANY)

- **POST** `/api/company/schedules/{scheduleId}/cancel`
- **Auth**: Bus Company required
- **Request Body**:

```json
{
  "cancellationReason": "Vehicle maintenance required",
  "processRefunds": true,
  "notifyPassengers": true
}
```

#### 13.2 Assign Bus to Schedule (BUS_COMPANY)

- **POST** `/api/company/schedules/{scheduleId}/assign-bus`
- **Auth**: Bus Company required
- **Request Body**:

```json
{
  "busId": 1,
  "notes": "Additional bus for high demand"
}
```

#### 13.3 Update Bus Status in Schedule (BUS_COMPANY)

- **PUT** `/api/company/schedules/{scheduleId}/buses/{busId}/status`
- **Auth**: Bus Company required
- **Request Body**:

```json
{
  "status": "INACTIVE",
  "notes": "Temporary maintenance"
}
```

#### 13.4 Remove Bus from Schedule (BUS_COMPANY)

- **DELETE** `/api/company/schedules/{scheduleId}/buses/{busId}`
- **Auth**: Bus Company required

### 14. Payment Management Advanced

#### 14.1 SePay Callback Handler (SYSTEM)

- **POST** `/api/admin/payments/sepay/callback`
- **Auth**: Admin required
- **Description**: Handle SePay payment callback notifications

#### 14.2 Process Refund (ADMIN)

- **POST** `/api/admin/payments/refund`
- **Auth**: Admin required
- **Request Body**:

```json
{
  "paymentId": 1,
  "refundAmount": 450000.0,
  "refundReason": "Schedule cancellation",
  "bankAccountNumber": "1234567890",
  "bankName": "Vietcombank",
  "bankAccountName": "Nguyen Van A"
}
```

#### 14.3 Get All Payments (ADMIN)

- **GET** `/api/admin/payments`
- **Auth**: Admin required
- **Query Parameters**: `page`, `size`, `sortBy`, `sortDir`

#### 14.4 Get Payments by Status (ADMIN)

- **GET** `/api/admin/payments/status/{status}`
- **Auth**: Admin required

#### 14.5 Get Payments by Date Range (ADMIN)

- **GET** `/api/admin/payments/date-range`
- **Auth**: Admin required
- **Query Parameters**: `fromDate`, `toDate`, `page`, `size`

#### 14.6 Generate Payment Report (ADMIN)

- **GET** `/api/admin/payments/report`
- **Auth**: Admin required
- **Query Parameters**: `fromDate`, `toDate`
- **Response**:

```json
{
  "reportDate": "2024-03-22T10:30:00",
  "totalPayments": 1250,
  "successfulPayments": 1180,
  "failedPayments": 45,
  "pendingPayments": 25,
  "refundedPayments": 15,
  "totalAmount": 125000000.0,
  "successfulAmount": 118000000.0,
  "refundedAmount": 1500000.0,
  "netRevenue": 116500000.0,
  "averagePaymentAmount": 100000.0,
  "successRate": 94.4,
  "paymentsByMethod": {
    "MOMO": 650,
    "SEPAY": 530
  },
  "amountByMethod": {
    "MOMO": 65000000.0,
    "SEPAY": 53000000.0
  }
}
```

#### 14.7 Get Pending Refunds (ADMIN)

- **GET** `/api/admin/payments/refunds/pending`
- **Auth**: Admin required

#### 14.8 Mark Refund as Completed (ADMIN)

- **POST** `/api/admin/payments/refunds/{refundId}/complete`
- **Auth**: Admin required

---

## API Response Status Codes

### Success Codes

- **200 OK**: Request successful
- **201 Created**: Resource created successfully
- **204 No Content**: Request successful, no content to return

### Error Codes

- **400 Bad Request**: Invalid request parameters or data
- **401 Unauthorized**: Authentication required or invalid token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **409 Conflict**: Resource conflict (e.g., duplicate data)
- **422 Unprocessable Entity**: Validation errors
- **500 Internal Server Error**: Server error

## Error Response Format

```json
{
  "success": false,
  "message": "Error description in Vietnamese",
  "errors": [
    {
      "field": "fieldName",
      "message": "Field-specific error message"
    }
  ]
}
```

## Rate Limiting

- **General APIs**: 100 requests per minute per IP
- **Payment APIs**: 10 requests per minute per user
- **Search APIs**: 50 requests per minute per IP

## Pagination

All list endpoints support pagination with the following parameters:

- `page`: Page number (0-based, default: 0)
- `size`: Page size (default: 20, max: 100)
- `sortBy`: Sort field (default varies by endpoint)
- `sortDir`: Sort direction (`asc` or `desc`, default: `desc`)

## Data Validation Rules

### Common Validations

- **Phone numbers**: Vietnamese format (+84xxxxxxxxx or 0xxxxxxxxx)
- **Email**: Valid email format
- **Dates**: ISO 8601 format (yyyy-MM-ddTHH:mm:ss)
- **Amounts**: Positive numbers, minimum 1,000 VND
- **Text fields**: Maximum lengths specified per field

### Business Rules

- **Ticket booking**: Must be at least 30 minutes before departure
- **Cancellation**: Must be at least 2 hours before departure for regular cancellation
- **Refunds**: Processed within 7 business days
- **Seat locks**: Expire after 10 minutes of inactivity
- **Payment timeout**: 5 minutes for ticket payment

## Security Considerations

- All sensitive endpoints require authentication
- Payment data is encrypted in transit and at rest
- User passwords are hashed using bcrypt
- JWT tokens expire after 24 hours
- API rate limiting prevents abuse
- Input validation prevents injection attacks

---

_Last updated: March 22, 2026_
_Total APIs: 80+ endpoints across 14 modules_

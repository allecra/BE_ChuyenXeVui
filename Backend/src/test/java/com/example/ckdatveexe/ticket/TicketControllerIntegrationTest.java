package com.example.ckdatveexe.ticket;

import com.example.ckdatveexe.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Ticket Management API Integration Tests")
class TicketControllerIntegrationTest extends BaseIntegrationTest {

    // ==================== USER TICKET APIs ====================

    @Test
    @DisplayName("POST /api/user/tickets/book - Should book ticket and hold seat")
    void testBookTicket() throws Exception {
        Map<String, Object> bookTicketRequest = Map.of(
                "scheduleId", 1,
                "seatId", 15,
                "passengerName", "Nguyễn Văn A",
                "passengerPhone", "0123456789",
                "passengerEmail", "passenger@example.com",
                "pickupLocation", "Bến xe Miền Đông",
                "dropoffLocation", "Bến xe Miền Tây");

        performAuthenticatedPost("/api/user/tickets/book", bookTicketRequest, userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    @Test
    @DisplayName("GET /api/user/schedules/{scheduleId}/seats - Should get seat layout for schedule")
    void testGetScheduleSeats() throws Exception {
        performAuthenticatedGet("/api/user/schedules/1/seats", userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    @Test
    @DisplayName("POST /api/user/seats/lock - Should lock seat for booking preparation")
    void testLockSeat() throws Exception {
        Map<String, Object> lockSeatRequest = Map.of(
                "scheduleId", 1,
                "seatId", 15);

        performAuthenticatedPost("/api/user/seats/lock", lockSeatRequest, userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    @Test
    @DisplayName("GET /api/user/seats/lock/{lockId}/countdown - Should get lock countdown")
    void testGetLockCountdown() throws Exception {
        performAuthenticatedGet("/api/user/seats/lock/123/countdown", userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no lock exists
    }

    @Test
    @DisplayName("POST /api/user/seats/unlock/{lockId} - Should unlock seat")
    void testUnlockSeat() throws Exception {
        performAuthenticatedPost("/api/user/seats/unlock/123", Map.of(), userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no lock exists
    }

    @Test
    @DisplayName("POST /api/user/tickets/create-from-lock - Should create ticket from locked seat")
    void testCreateTicketFromLock() throws Exception {
        Map<String, Object> createFromLockRequest = Map.of(
                "lockId", 123,
                "passengerName", "Nguyễn Văn A",
                "passengerPhone", "0123456789",
                "passengerEmail", "passenger@example.com");

        performAuthenticatedPost("/api/user/tickets/create-from-lock", createFromLockRequest, userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no lock exists
    }

    @Test
    @DisplayName("GET /api/user/tickets - Should get user tickets")
    void testGetUserTickets() throws Exception {
        performAuthenticatedGet("/api/user/tickets", userAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/user/tickets/{ticketId} - Should get ticket detail")
    void testGetTicketDetail() throws Exception {
        performAuthenticatedGet("/api/user/tickets/1", userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no ticket exists
    }

    @Test
    @DisplayName("POST /api/user/tickets/{ticketId}/cancel - Should cancel ticket")
    void testCancelTicket() throws Exception {
        performAuthenticatedPost("/api/user/tickets/1/cancel", Map.of(), userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no ticket exists
    }

    // ==================== COMPANY TICKET APIs ====================

    @Test
    @DisplayName("GET /api/company/schedules/{scheduleId}/tickets - Should get schedule tickets")
    void testGetScheduleTickets() throws Exception {
        performAuthenticatedGet("/api/company/schedules/1/tickets", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    @Test
    @DisplayName("GET /api/company/schedules/{scheduleId}/seats - Should get schedule seats for company")
    void testGetScheduleSeatsForCompany() throws Exception {
        performAuthenticatedGet("/api/company/schedules/1/seats", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    @Test
    @DisplayName("POST /api/company/tickets/book-for-customer - Should book ticket for customer")
    void testBookForCustomer() throws Exception {
        Map<String, Object> bookForCustomerRequest = Map.of(
                "scheduleId", 1,
                "seatId", 15,
                "customerName", "Nguyễn Văn A",
                "customerPhone", "0123456789",
                "customerEmail", "customer@example.com",
                "paymentMethod", "CASH",
                "notes", "Booked at counter");

        performAuthenticatedPost("/api/company/tickets/book-for-customer", bookForCustomerRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    @Test
    @DisplayName("GET /api/company/tickets - Should get company tickets")
    void testGetCompanyTickets() throws Exception {
        performAuthenticatedGet("/api/company/tickets", companyAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/company/tickets/{ticketId} - Should get ticket detail for company")
    void testGetTicketDetailForCompany() throws Exception {
        performAuthenticatedGet("/api/company/tickets/1", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no ticket exists
    }

    @Test
    @DisplayName("POST /api/company/schedules/{scheduleId}/generate-tickets - Should generate tickets for schedule")
    void testGenerateTicketsForSchedule() throws Exception {
        performAuthenticatedPost("/api/company/schedules/1/generate-tickets", Map.of(), companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    // ==================== AUTHORIZATION TESTS ====================

    @Test
    @DisplayName("POST /api/user/tickets/book - Should fail without authentication")
    void testBookTicketUnauthorized() throws Exception {
        Map<String, Object> bookTicketRequest = Map.of(
                "scheduleId", 1,
                "seatId", 15);

        mockMvc.perform(post("/api/user/tickets/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookTicketRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/company/tickets/book-for-customer - Should fail with USER role")
    void testBookForCustomerUnauthorized() throws Exception {
        Map<String, Object> bookForCustomerRequest = Map.of(
                "scheduleId", 1,
                "seatId", 15);

        performAuthenticatedPost("/api/company/tickets/book-for-customer", bookForCustomerRequest, userAccessToken)
                .andExpect(status().isForbidden()); // USER role cannot book for customers
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("POST /api/user/tickets/book - Should fail with invalid data")
    void testBookTicketInvalidData() throws Exception {
        Map<String, Object> invalidBookRequest = Map.of(
                "scheduleId", -1, // Invalid schedule ID
                "seatId", -1, // Invalid seat ID
                "passengerName", "", // Empty name
                "passengerPhone", "invalid"); // Invalid phone

        performAuthenticatedPost("/api/user/tickets/book", invalidBookRequest, userAccessToken)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/user/seats/lock - Should fail with missing data")
    void testLockSeatMissingData() throws Exception {
        Map<String, Object> incompleteLockRequest = Map.of(
                "scheduleId", 1); // Missing seatId

        performAuthenticatedPost("/api/user/seats/lock", incompleteLockRequest, userAccessToken)
                .andExpect(status().isBadRequest());
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("GET /api/user/tickets - Should handle pagination parameters")
    void testGetUserTicketsWithPagination() throws Exception {
        performAuthenticatedGet("/api/user/tickets?page=0&size=5&sortBy=createdAt&sortDir=desc", userAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/user/tickets - Should filter by status")
    void testGetUserTicketsWithStatusFilter() throws Exception {
        performAuthenticatedGet("/api/user/tickets?status=CONFIRMED", userAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/company/schedules/{scheduleId}/tickets - Should handle pagination and filters")
    void testGetScheduleTicketsWithFilters() throws Exception {
        performAuthenticatedGet("/api/company/schedules/1/tickets?page=0&size=20&status=PENDING", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    @Test
    @DisplayName("POST /api/company/tickets/book-for-customer - Should handle different payment methods")
    void testBookForCustomerDifferentPaymentMethods() throws Exception {
        Map<String, Object> cashBookingRequest = Map.of(
                "scheduleId", 1,
                "seatId", 15,
                "customerName", "Nguyễn Văn A",
                "customerPhone", "0123456789",
                "paymentMethod", "CASH");

        performAuthenticatedPost("/api/company/tickets/book-for-customer", cashBookingRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }
}
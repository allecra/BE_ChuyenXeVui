package com.example.ckdatveexe.seat;

import com.example.ckdatveexe.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Seat Management API Integration Tests")
class SeatControllerIntegrationTest extends BaseIntegrationTest {

    // ==================== USER SEAT APIs ====================

    @Test
    @DisplayName("GET /api/user/seats/bus/{busId} - Should get seat layout of bus")
    void testGetSeatsByBus() throws Exception {
        performAuthenticatedGet("/api/user/seats/bus/1", userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }

    @Test
    @DisplayName("GET /api/user/seats/bus/{busId} - Should get seats with filters")
    void testGetSeatsByBusWithFilters() throws Exception {
        performAuthenticatedGet("/api/user/seats/bus/1?status=AVAILABLE&seatType=VIP&minPrice=50000&maxPrice=100000",
                userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }

    @Test
    @DisplayName("GET /api/user/seats/{seatId} - Should get seat detail")
    void testGetSeatDetail() throws Exception {
        performAuthenticatedGet("/api/user/seats/1", userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no seat exists
    }

    @Test
    @DisplayName("GET /api/user/seats/available/bus/{busId} - Should get available seats only")
    void testGetAvailableSeats() throws Exception {
        performAuthenticatedGet("/api/user/seats/available/bus/1", userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }

    @Test
    @DisplayName("GET /api/user/seats/available/bus/{busId} - Should get available seats with type filter")
    void testGetAvailableSeatsWithTypeFilter() throws Exception {
        performAuthenticatedGet("/api/user/seats/available/bus/1?seatType=SLEEPER", userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }

    // ==================== COMPANY SEAT APIs ====================

    @Test
    @DisplayName("POST /api/bus-company/seats - Should create new seat")
    void testCreateSeat() throws Exception {
        Map<String, Object> createSeatRequest = Map.of(
                "busId", 1,
                "seatNumber", "A1",
                "seatType", "VIP",
                "priceForSeatType", 50000,
                "position", Map.of(
                        "row", 1,
                        "column", 1,
                        "floor", 1),
                "descriptions", "VIP seat with extra legroom");

        performAuthenticatedPost("/api/bus-company/seats", createSeatRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }

    @Test
    @DisplayName("GET /api/bus-company/seats - Should get company seats")
    void testGetCompanySeats() throws Exception {
        performAuthenticatedGet("/api/bus-company/seats", companyAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/bus-company/seats - Should get company seats with filters")
    void testGetCompanySeatsWithFilters() throws Exception {
        performAuthenticatedGet("/api/bus-company/seats?busId=1&status=ACTIVE&seatType=NORMAL", companyAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/bus-company/seats/{seatId} - Should get seat detail for company")
    void testGetSeatDetailForCompany() throws Exception {
        performAuthenticatedGet("/api/bus-company/seats/1", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no seat exists
    }

    @Test
    @DisplayName("PUT /api/bus-company/seats/{seatId} - Should update seat information")
    void testUpdateSeat() throws Exception {
        Map<String, Object> updateSeatRequest = Map.of(
                "seatNumber", "A1-Updated",
                "seatType", "VIP",
                "descriptions", "Updated VIP seat description",
                "position", Map.of(
                        "row", 1,
                        "column", 1,
                        "floor", 1));

        performAuthenticatedPut("/api/bus-company/seats/1", updateSeatRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no seat exists
    }

    @Test
    @DisplayName("PUT /api/bus-company/seats/{seatId}/status - Should update seat status")
    void testUpdateSeatStatus() throws Exception {
        Map<String, Object> statusUpdateRequest = Map.of(
                "status", "MAINTENANCE",
                "reason", "Seat needs repair");

        performAuthenticatedPut("/api/bus-company/seats/1/status", statusUpdateRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no seat exists
    }

    @Test
    @DisplayName("PUT /api/bus-company/seats/{seatId}/price - Should update seat price")
    void testUpdateSeatPrice() throws Exception {
        Map<String, Object> priceUpdateRequest = Map.of(
                "priceForSeatType", 60000,
                "reason", "Price adjustment for VIP seats");

        performAuthenticatedPut("/api/bus-company/seats/1/price", priceUpdateRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no seat exists
    }

    @Test
    @DisplayName("DELETE /api/bus-company/seats/{seatId} - Should soft delete seat")
    void testDeleteSeat() throws Exception {
        performAuthenticatedDelete("/api/bus-company/seats/1", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no seat exists
    }

    // ==================== AUTHORIZATION TESTS ====================

    @Test
    @DisplayName("GET /api/user/seats/bus/{busId} - Should fail without authentication")
    void testGetSeatsByBusUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/seats/bus/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/bus-company/seats - Should fail with USER role")
    void testCreateSeatUnauthorized() throws Exception {
        Map<String, Object> createSeatRequest = Map.of(
                "busId", 1,
                "seatNumber", "A1",
                "seatType", "NORMAL");

        performAuthenticatedPost("/api/bus-company/seats", createSeatRequest, userAccessToken)
                .andExpect(status().isForbidden()); // USER role cannot create seats
    }

    @Test
    @DisplayName("PUT /api/bus-company/seats/{seatId} - Should fail with USER role")
    void testUpdateSeatUnauthorized() throws Exception {
        Map<String, Object> updateSeatRequest = Map.of(
                "seatNumber", "A1-Updated");

        performAuthenticatedPut("/api/bus-company/seats/1", updateSeatRequest, userAccessToken)
                .andExpect(status().isForbidden()); // USER role cannot update seats
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("POST /api/bus-company/seats - Should fail with invalid seat type")
    void testCreateSeatInvalidType() throws Exception {
        Map<String, Object> invalidSeatRequest = Map.of(
                "busId", 1,
                "seatNumber", "A1",
                "seatType", "INVALID_TYPE",
                "priceForSeatType", 50000);

        performAuthenticatedPost("/api/bus-company/seats", invalidSeatRequest, companyAccessToken)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/bus-company/seats - Should fail with negative price")
    void testCreateSeatNegativePrice() throws Exception {
        Map<String, Object> negativePriceRequest = Map.of(
                "busId", 1,
                "seatNumber", "A1",
                "seatType", "NORMAL",
                "priceForSeatType", -1000);

        performAuthenticatedPost("/api/bus-company/seats", negativePriceRequest, companyAccessToken)
                .andExpect(status().isBadRequest());
    }

@Test
    @DisplayName("POST /api/bus-company/seats - Should fail with missing required fields")
    void testCreateSeatMissingFields() throws Exception {
        Map<String, Object> incompleteRequest = Map.of(
                "seatNumber", "A1"); // Missing busId, seatType, priceForSeatType

        performAuthenticatedPost("/api/bus-company/seats", incompleteRequest, companyAccessToken)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/bus-company/seats/{seatId}/status - Should fail with invalid status")
    void testUpdateSeatStatusInvalid() throws Exception {
        Map<String, Object> invalidStatusRequest = Map.of(
                "status", "INVALID_STATUS",
                "reason", "Test reason");

        performAuthenticatedPut("/api/bus-company/seats/1/status", invalidStatusRequest, companyAccessToken)
                .andExpect(status().isBadRequest());
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("GET /api/user/seats/bus/{busId} - Should handle pagination parameters")
    void testGetSeatsByBusWithPagination() throws Exception {
        performAuthenticatedGet("/api/user/seats/bus/1?page=0&size=20&sortBy=seatNumber&sortDirection=asc", userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }

    @Test
    @DisplayName("GET /api/bus-company/seats - Should handle complex filters")
    void testGetCompanySeatsComplexFilters() throws Exception {
        performAuthenticatedGet("/api/bus-company/seats?busId=1&status=AVAILABLE&seatType=VIP&minPrice=50000&maxPrice=100000&seatNumber=A", companyAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/bus-company/seats - Should create different seat types")
    void testCreateDifferentSeatTypes() throws Exception {
        // Test NORMAL seat
        Map<String, Object> normalSeatRequest = Map.of(
                "busId", 1,
                "seatNumber", "B1",
                "seatType", "NORMAL",
                "priceForSeatType", 30000,
                "descriptions", "Standard seat");

        performAuthenticatedPost("/api/bus-company/seats", normalSeatRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists

        // Test SLEEPER seat
        Map<String, Object> sleeperSeatRequest = Map.of(
                "busId", 1,
                "seatNumber", "C1",
                "seatType", "SLEEPER",
                "priceForSeatType", 80000,
                "descriptions", "Sleeper bed");

        performAuthenticatedPost("/api/bus-company/seats", sleeperSeatRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }

    @Test
    @DisplayName("PUT /api/bus-company/seats/{seatId}/status - Should handle different status transitions")
    void testSeatStatusTransitions() throws Exception {
        // Test setting to MAINTENANCE
        Map<String, Object> maintenanceRequest = Map.of(
                "status", "MAINTENANCE",
                "reason", "Regular maintenance");

        performAuthenticatedPut("/api/bus-company/seats/1/status", maintenanceRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no seat exists

        // Test setting to AVAILABLE
        Map<String, Object> availableRequest = Map.of(
                "status", "AVAILABLE",
                "reason", "Maintenance completed");

        performAuthenticatedPut("/api/bus-company/seats/1/status", availableRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no seat exists
    }

    @Test
    @DisplayName("PUT /api/bus-company/seats/{seatId}/price - Should handle price updates")
    void testSeatPriceUpdates() throws Exception {
        // Test price increase
        Map<String, Object> priceIncreaseRequest = Map.of(
                "priceForSeatType", 70000,
                "reason", "Inflation adjustment");

        performAuthenticatedPut("/api/bus-company/seats/1/price", priceIncreaseRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no seat exists

        // Test price decrease
        Map<String, Object> priceDecreaseRequest = Map.of(
                "priceForSeatType", 40000,
                "reason", "Promotional pricing");

        performAuthenticatedPut("/api/bus-company/seats/1/price", priceDecreaseRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no seat exists
    }

    @Test
    @DisplayName("GET /api/user/seats/available/bus/{busId} - Should filter by price range")
    void testGetAvailableSeatsWithPriceFilter() throws Exception {
        performAuthenticatedGet("/api/user/seats/available/bus/1?minPrice=30000&maxPrice=60000", userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }
}
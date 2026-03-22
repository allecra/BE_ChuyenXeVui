package com.example.ckdatveexe.schedule;

import com.example.ckdatveexe.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Schedule Management API Integration Tests")
class ScheduleControllerIntegrationTest extends BaseIntegrationTest {

    // ==================== USER SCHEDULE APIs (Public) ====================

    @Test
    @DisplayName("GET /api/schedules - Should get all active schedules (public)")
    void testGetAllActiveSchedules() throws Exception {
        mockMvc.perform(get("/api/schedules")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/schedules/{scheduleId} - Should get schedule detail (public)")
    void testGetScheduleDetail() throws Exception {
        mockMvc.perform(get("/api/schedules/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    @Test
    @DisplayName("GET /api/schedules/search - Should search schedules with filters (public)")
    void testSearchSchedules() throws Exception {
        mockMvc.perform(get("/api/schedules/search")
                .param("startStationId", "1")
                .param("endStationId", "2")
                .param("minPrice", "300000")
                .param("maxPrice", "800000")
                .param("busType", "GIUONG_NAM")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/schedules/{scheduleId}/buses - Should get active buses in schedule (public)")
    void testGetActiveBusesInSchedule() throws Exception {
        mockMvc.perform(get("/api/schedules/1/buses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()); // Expected since no schedule exists
    }

    // ==================== COMPANY SCHEDULE APIs ====================

    @Test
    @DisplayName("POST /api/company/schedules - Should create schedule for company")
    void testCreateScheduleForCompany() throws Exception {
        Map<String, Object> createScheduleRequest = Map.of(
                "routeId", 1,
                "busId", 1,
                "departureTime", "2026-03-15T08:00:00",
                "arrivalTime", "2026-03-15T20:00:00",
                "price", 500000,
                "availableSeats", 40,
                "status", "ACTIVE");

        performAuthenticatedPost("/api/company/schedules", createScheduleRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since route/bus don't exist
    }

    @Test
    @DisplayName("GET /api/company/schedules - Should get company schedules")
    void testGetCompanySchedules() throws Exception {
        performAuthenticatedGet("/api/company/schedules", companyAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/company/schedules/{scheduleId} - Should get schedule detail for company")
    void testGetScheduleDetailForCompany() throws Exception {
        performAuthenticatedGet("/api/company/schedules/1", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    @Test
    @DisplayName("PUT /api/company/schedules/{scheduleId} - Should update schedule")
    void testUpdateScheduleForCompany() throws Exception {
        Map<String, Object> updateScheduleRequest = Map.of(
                "departureTime", "2026-03-15T09:00:00",
                "arrivalTime", "2026-03-15T21:00:00",
                "price", 520000,
                "status", "ACTIVE");

        performAuthenticatedPut("/api/company/schedules/1", updateScheduleRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    @Test
    @DisplayName("GET /api/company/schedules/search - Should search company schedules")
    void testSearchCompanySchedules() throws Exception {
        performAuthenticatedGet("/api/company/schedules/search?routeId=1&status=ACTIVE", companyAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/company/schedules/{scheduleId} - Should cancel schedule")
    void testCancelSchedule() throws Exception {
        performAuthenticatedDelete("/api/company/schedules/1", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    // ==================== SCHEDULE BUS MANAGEMENT ====================

    @Test
    @DisplayName("POST /api/company/schedules/{scheduleId}/buses - Should assign buses to schedule")
    void testAssignBusesToSchedule() throws Exception {
        Map<String, Object> assignBusesRequest = Map.of(
                "busIds", List.of(1, 2, 3),
                "replaceAll", false,
                "scheduleBuses", List.of(
                        Map.of(
                                "busId", 1,
                                "status", "ACTIVE",
                                "notes", "Main bus for this schedule")));

        performAuthenticatedPost("/api/company/schedules/1/buses", assignBusesRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    @Test
    @DisplayName("GET /api/company/schedules/{scheduleId}/buses - Should get buses in schedule")
    void testGetBusesInSchedule() throws Exception {
        performAuthenticatedGet("/api/company/schedules/1/buses", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    @Test
    @DisplayName("PUT /api/company/schedules/{scheduleId}/buses/{busId}/status - Should update bus status in schedule")
    void testUpdateBusStatusInSchedule() throws Exception {
        Map<String, Object> statusUpdateRequest = Map.of(
                "status", "MAINTENANCE",
                "notes", "Bus under maintenance");

        performAuthenticatedPut("/api/company/schedules/1/buses/1/status", statusUpdateRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    @Test
    @DisplayName("DELETE /api/company/schedules/{scheduleId}/buses/{busId} - Should remove bus from schedule")
    void testRemoveBusFromSchedule() throws Exception {
        performAuthenticatedDelete("/api/company/schedules/1/buses/1", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no schedule exists
    }

    // ==================== AUTHORIZATION TESTS ====================

    @Test
    @DisplayName("POST /api/company/schedules - Should fail without proper role")
    void testCreateScheduleUnauthorized() throws Exception {
        Map<String, Object> createScheduleRequest = Map.of(
                "routeId", 1,
                "busId", 1,
                "departureTime", "2026-03-15T08:00:00");

        performAuthenticatedPost("/api/company/schedules", createScheduleRequest, userAccessToken)
                .andExpect(status().isForbidden()); // USER role cannot create schedules
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("POST /api/company/schedules - Should fail with invalid data")
    void testCreateScheduleInvalidData() throws Exception {
        Map<String, Object> invalidScheduleRequest = Map.of(
                "routeId", -1, // Invalid route ID
                "price", -1000 // Negative price
        );

        performAuthenticatedPost("/api/company/schedules", invalidScheduleRequest, companyAccessToken)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/schedules/search - Should handle invalid search parameters")
    void testSearchSchedulesInvalidParams() throws Exception {
        mockMvc.perform(get("/api/schedules/search")
                .param("minPrice", "invalid")
                .param("busType", "INVALID_TYPE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

@Test
    @DisplayName("PUT /api/company/schedules/{scheduleId}/buses/{busId}/status - Should fail with invalid status")
    void testUpdateBusStatusInvalidData() throws Exception {
        Map<String, Object> invalidStatusRequest = Map.of(
            "status", "INVALID_STATUS"
        );

        performAuthenticatedPut("/api/company/schedules/1/buses/1/status", invalidStatusRequest, companyAccessToken)
                .andExpect(status().isBadRequest());
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("GET /api/schedules/search - Should handle empty search results")
    void testSearchSchedulesNoResults() throws Exception {
        mockMvc.perform(get("/api/schedules/search")
                .param("startStationId", "999999") // Non-existent station
                .param("endStationId", "999998")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("GET /api/schedules/search - Should handle date range filters")
    void testSearchSchedulesWithDateRange() throws Exception {
        mockMvc.perform(get("/api/schedules/search")
                .param("departureDate", "2026-03-15T00:00:00")
                .param("timeFrom", "2026-03-15T06:00:00")
                .param("timeTo", "2026-03-15T18:00:00")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/company/schedules/{scheduleId}/buses - Should handle empty bus list")
    void testAssignEmptyBusesToSchedule() throws Exception {
        Map<String, Object> emptyBusesRequest = Map.of(
            "busIds", List.of(),
            "replaceAll", false,
            "scheduleBuses", List.of()
        );

        performAuthenticatedPost("/api/company/schedules/1/buses", emptyBusesRequest, companyAccessToken)
                .andExpect(status().isBadRequest()); // Should fail with empty bus list
    }
}
package com.example.ckdatveexe.station;

import com.example.ckdatveexe.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Station Management API Integration Tests")
class StationControllerIntegrationTest extends BaseIntegrationTest {

    // ==================== USER STATION APIs ====================

    @Test
    @DisplayName("GET /api/user/stations - Should get all stations for user")
    void testGetAllStationsForUser() throws Exception {
        performAuthenticatedGet("/api/user/stations", userAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/user/stations/{stationId} - Should get station detail for user")
    void testGetStationDetailForUser() throws Exception {
        performAuthenticatedGet("/api/user/stations/1", userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("GET /api/user/stations/{stationId}/buses - Should get buses at station for user")
    void testGetBusesAtStationForUser() throws Exception {
        performAuthenticatedGet("/api/user/stations/1/buses", userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("GET /api/user/stations/search - Should search stations")
    void testSearchStationsForUser() throws Exception {
        performAuthenticatedGet("/api/user/stations/search?keyword=test", userAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/user/stations/{stationId}/buses/search - Should search buses at station")
    void testSearchBusesAtStationForUser() throws Exception {
        performAuthenticatedGet("/api/user/stations/1/buses/search?keyword=test", userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    // ==================== COMPANY STATION APIs ====================

    @Test
    @DisplayName("POST /api/bus-company/stations - Should create station for company")
    void testCreateStationForCompany() throws Exception {
        Map<String, Object> createStationRequest = Map.of(
            "name", "Test Station",
            "location", "Hồ Chí Minh",
            "address", "123 Test Street, District 1, HCMC",
            "phone", "028-12345678",
            "descriptions", "Test station description"
        );

        performAuthenticatedPost("/api/bus-company/stations", createStationRequest, companyAccessToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Station"));
    }

    @Test
    @DisplayName("GET /api/bus-company/stations - Should get all stations for company")
    void testGetAllStationsForCompany() throws Exception {
        performAuthenticatedGet("/api/bus-company/stations", companyAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/bus-company/stations/{stationId} - Should get station detail for company")
    void testGetStationDetailForCompany() throws Exception {
        performAuthenticatedGet("/api/bus-company/stations/1", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("PUT /api/bus-company/stations/{stationId} - Should update station")
    void testUpdateStationForCompany() throws Exception {
        Map<String, Object> updateStationRequest = Map.of(
            "name", "Updated Station Name",
            "descriptions", "Updated description"
        );

        performAuthenticatedPut("/api/bus-company/stations/1", updateStationRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("POST /api/bus-company/stations/assign-buses - Should assign buses to station (basic)")
    void testAssignBusesToStationBasic() throws Exception {
        Map<String, Object> assignBusesRequest = Map.of(
            "stationId", 1,
            "busIds", List.of(1, 2, 3),
            "replaceAll", false
        );

        performAuthenticatedPost("/api/bus-company/stations/assign-buses", assignBusesRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("POST /api/bus-company/stations/assign-buses-detailed - Should assign buses with details")
    void testAssignBusesToStationDetailed() throws Exception {
        Map<String, Object> assignBusesRequest = Map.of(
            "stationId", 1,
            "replaceAll", false,
            "busStations", List.of(
                Map.of(
                    "busId", 1,
                    "stationId", 1,
                    "notes", "Main bus for this station",
                    "isActive", true
                )
            )
        );

        performAuthenticatedPost("/api/bus-company/stations/assign-buses-detailed", assignBusesRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("GET /api/bus-company/stations/{stationId}/bus-stations - Should get BusStation details")
    void testGetBusStationDetails() throws Exception {
        performAuthenticatedGet("/api/bus-company/stations/1/bus-stations", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("DELETE /api/bus-company/stations/{stationId}/buses - Should remove buses from station")
    void testRemoveBusesFromStation() throws Exception {
        List<Integer> busIds = List.of(1, 2, 3);

        performAuthenticatedDeleteWithBody("/api/bus-company/stations/1/buses", busIds, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("GET /api/bus-company/stations/{stationId}/buses - Should get buses at station for company")
    void testGetBusesAtStationForCompany() throws Exception {
        performAuthenticatedGet("/api/bus-company/stations/1/buses", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("GET /api/bus-company/stations/search - Should search stations for company")
    void testSearchStationsForCompany() throws Exception {
        performAuthenticatedGet("/api/bus-company/stations/search?keyword=test", companyAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/bus-company/stations/{stationId}/buses/search - Should search buses at station for company")
    void testSearchBusesAtStationForCompany() throws Exception {
        performAuthenticatedGet("/api/bus-company/stations/1/buses/search?keyword=test", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("DELETE /api/bus-company/stations/{stationId} - Should delete station")
    void testDeleteStationForCompany() throws Exception {
        Map<String, Object> deleteStationRequest = Map.of(
            "hardDelete", false
        );

        performAuthenticatedDeleteWithBody("/api/bus-company/stations/1", deleteStationRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("PUT /api/bus-company/stations/{stationId}/buses/status - Should update bus status at station")
    void testUpdateBusStatusAtStation() throws Exception {
        Map<String, Object> statusUpdateRequest = Map.of(
            "busId", 1,
            "isActive", false,
            "notes", "Bus under maintenance"
        );

        performAuthenticatedPut("/api/bus-company/stations/1/buses/status", statusUpdateRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("GET /api/bus-company/stations/debug/bus-station-data - Should debug BusStation data")
    void testDebugBusStationData() throws Exception {
        performAuthenticatedGet("/api/bus-company/stations/debug/bus-station-data", companyAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== ADMIN STATION APIs ====================

    @Test
    @DisplayName("POST /api/admin/stations - Should create station as admin")
    void testCreateStationForAdmin() throws Exception {
        Map<String, Object> createStationRequest = Map.of(
            "name", "Admin Test Station",
            "location", "Hà Nội",
            "address", "456 Admin Street, Hanoi",
            "phone", "024-87654321",
            "descriptions", "Admin created station"
        );

        performAuthenticatedPost("/api/admin/stations", createStationRequest, adminAccessToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/admin/stations - Should get all stations for admin")
    void testGetAllStationsForAdmin() throws Exception {
        performAuthenticatedGet("/api/admin/stations", adminAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/admin/stations/{stationId} - Should get station detail for admin")
    void testGetStationDetailForAdmin() throws Exception {
        performAuthenticatedGet("/api/admin/stations/1", adminAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("PUT /api/admin/stations/{stationId} - Should update station as admin")
    void testUpdateStationForAdmin() throws Exception {
        Map<String, Object> updateStationRequest = Map.of(
            "name", "Admin Updated Station",
            "status", "ACTIVE"
        );

        performAuthenticatedPut("/api/admin/stations/1", updateStationRequest, adminAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("POST /api/admin/stations/assign-buses - Should assign buses as admin")
    void testAssignBusesForAdmin() throws Exception {
        Map<String, Object> assignBusesRequest = Map.of(
            "stationId", 1,
            "busIds", List.of(1, 2),
            "replaceAll", true
        );

        performAuthenticatedPost("/api/admin/stations/assign-buses", assignBusesRequest, adminAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("DELETE /api/admin/stations/{stationId}/buses - Should remove buses as admin")
    void testRemoveBusesForAdmin() throws Exception {
        List<Integer> busIds = List.of(1, 2);

        performAuthenticatedDeleteWithBody("/api/admin/stations/1/buses", busIds, adminAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("GET /api/admin/stations/{stationId}/buses - Should get buses at station for admin")
    void testGetBusesAtStationForAdmin() throws Exception {
        performAuthenticatedGet("/api/admin/stations/1/buses", adminAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("GET /api/admin/stations/{stationId}/bus-stations - Should get BusStation details for admin")
    void testGetBusStationDetailsForAdmin() throws Exception {
        performAuthenticatedGet("/api/admin/stations/1/bus-stations", adminAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("DELETE /api/admin/stations/{stationId} - Should delete station as admin")
    void testDeleteStationForAdmin() throws Exception {
        Map<String, Object> deleteStationRequest = Map.of(
            "hardDelete", true
        );

        performAuthenticatedDeleteWithBody("/api/admin/stations/1", deleteStationRequest, adminAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    @Test
    @DisplayName("PUT /api/admin/stations/{stationId}/buses/status - Should update bus status as admin")
    void testUpdateBusStatusForAdmin() throws Exception {
        Map<String, Object> statusUpdateRequest = Map.of(
            "busId", 1,
            "isActive", true,
            "notes", "Admin approved bus operation"
        );

        performAuthenticatedPut("/api/admin/stations/1/buses/status", statusUpdateRequest, adminAccessToken)
                .andExpect(status().isNotFound()); // Expected since no station exists
    }

    // ==================== AUTHORIZATION TESTS ====================

    @Test
    @DisplayName("POST /api/bus-company/stations - Should fail without proper role")
    void testCreateStationUnauthorized() throws Exception {
        Map<String, Object> createStationRequest = Map.of(
            "name", "Unauthorized Station"
        );

        performAuthenticatedPost("/api/bus-company/stations", createStationRequest, userAccessToken)
                .andExpect(status().isForbidden()); // USER role cannot create stations
    }

    @Test
    @DisplayName("GET /api/admin/stations - Should fail without admin role")
    void testGetAdminStationsUnauthorized() throws Exception {
        performAuthenticatedGet("/api/admin/stations", userAccessToken)
                .andExpect(status().isForbidden()); // USER role cannot access admin endpoints
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("POST /api/bus-company/stations - Should fail with invalid data")
    void testCreateStationInvalidData() throws Exception {
        Map<String, Object> invalidStationRequest = Map.of(
            "name", "", // Empty name
            "phone", "invalid-phone" // Invalid phone format
        );

        performAuthenticatedPost("/api/bus-company/stations", invalidStationRequest, companyAccessToken)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/user/stations/search - Should fail with empty keyword")
    void testSearchStationsEmptyKeyword() throws Exception {
        performAuthenticatedGet("/api/user/stations/search?keyword=", userAccessToken)
                .andExpect(status().isBadRequest());
    }
}
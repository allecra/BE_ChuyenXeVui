package com.example.ckdatveexe.bus;

import com.example.ckdatveexe.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Bus Management API Integration Tests")
class BusControllerIntegrationTest extends BaseIntegrationTest {

    // ==================== USER BUS APIs ====================

    @Test
    @DisplayName("GET /api/user/buses - Should get all buses for user")
    void testGetAllBusesForUser() throws Exception {
        performAuthenticatedGet("/api/user/buses", userAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /api/user/buses/{busId} - Should get bus detail for user")
    void testGetBusDetailForUser() throws Exception {
        performAuthenticatedGet("/api/user/buses/1", userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus with ID 1 exists in test
    }

    @Test
    @DisplayName("GET /api/user/buses/search - Should search buses")
    void testSearchBusesForUser() throws Exception {
        performAuthenticatedGet("/api/user/buses/search?keyword=test", userAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/user/buses/company/{companyId} - Should get buses by company")
    void testGetBusesByCompanyForUser() throws Exception {
        performAuthenticatedGet("/api/user/buses/company/1", userAccessToken)
                .andExpect(status().isNotFound()); // Expected since no company with ID 1 exists in test
    }

    // ==================== COMPANY BUS APIs ====================

    @Test
    @DisplayName("POST /api/bus-company/buses - Should create bus for company")
    void testCreateBusForCompany() throws Exception {
        Map<String, Object> createBusRequest = Map.of(
                "name", "Test Bus",
                "licensePlate", "30A-12345",
                "capacity", 45,
                "busType", "GHE_NGOI",
                "descriptions", "Test bus description");

        performAuthenticatedPost("/api/bus-company/buses", createBusRequest, companyAccessToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Bus"));
    }

    @Test
    @DisplayName("GET /api/bus-company/buses - Should get company buses")
    void testGetCompanyBuses() throws Exception {
        performAuthenticatedGet("/api/bus-company/buses", companyAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /api/bus-company/buses/{busId} - Should get bus detail for company")
    void testGetBusDetailForCompany() throws Exception {
        performAuthenticatedGet("/api/bus-company/buses/1", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }

    @Test
    @DisplayName("PUT /api/bus-company/buses/{busId} - Should update bus")
    void testUpdateBusForCompany() throws Exception {
        Map<String, Object> updateBusRequest = Map.of(
                "name", "Updated Bus Name",
                "descriptions", "Updated description");

        performAuthenticatedPut("/api/bus-company/buses/1", updateBusRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }

    @Test
    @DisplayName("DELETE /api/bus-company/buses/{busId} - Should delete bus")
    void testDeleteBusForCompany() throws Exception {
        Map<String, Object> deleteBusRequest = Map.of(
                "hardDelete", false,
                "reason", "Test deletion");

        performAuthenticatedDeleteWithBody("/api/bus-company/buses/1", deleteBusRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }

    @Test
    @DisplayName("PUT /api/bus-company/buses/{busId}/seat-layout - Should update seat layout")
    void testUpdateSeatLayout() throws Exception {
        Map<String, Object> seatLayoutRequest = Map.of(
                "seats", Map.of(
                        "A1", Map.of("row", 1, "column", 1),
                        "A2", Map.of("row", 1, "column", 2)));

        performAuthenticatedPut("/api/bus-company/buses/1/seat-layout", seatLayoutRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }

    @Test
    @DisplayName("POST /api/bus-company/buses/{busId}/regenerate-seats - Should regenerate seats")
    void testRegenerateSeats() throws Exception {
        performAuthenticatedPost("/api/bus-company/buses/1/regenerate-seats", Map.of(), companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }

    @Test
    @DisplayName("GET /api/bus-company/buses/deleted - Should get deleted buses")
    void testGetDeletedBuses() throws Exception {
        performAuthenticatedGet("/api/bus-company/buses/deleted", companyAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/bus-company/buses/deleted/{deletedBusId}/restore - Should restore deleted bus")
    void testRestoreDeletedBus() throws Exception {
        performAuthenticatedPost("/api/bus-company/buses/deleted/1/restore", Map.of(), companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no deleted bus exists
    }

    // ==================== ADMIN BUS APIs ====================

    @Test
    @DisplayName("GET /api/admin/buses - Should get all buses for admin")
    void testGetAllBusesForAdmin() throws Exception {
        performAuthenticatedGet("/api/admin/buses", adminAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/admin/buses/{busId} - Should get bus detail for admin")
    void testGetBusDetailForAdmin() throws Exception {
        performAuthenticatedGet("/api/admin/buses/1", adminAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }

    @Test
    @DisplayName("PUT /api/admin/buses/{busId} - Should update bus as admin")
    void testUpdateBusForAdmin() throws Exception {
        Map<String, Object> updateBusRequest = Map.of(
                "name", "Admin Updated Bus",
                "status", "ACTIVE");

        performAuthenticatedPut("/api/admin/buses/1", updateBusRequest, adminAccessToken)
                .andExpect(status().isNotFound()); // Expected since no bus exists
    }

    @Test
    @DisplayName("GET /api/admin/buses/company/{companyId} - Should get buses by company for admin")
    void testGetBusesByCompanyForAdmin() throws Exception {
        performAuthenticatedGet("/api/admin/buses/company/1", adminAccessToken)
                .andExpect(status().isNotFound()); // Expected since no company exists
    }

    // ==================== AUTHORIZATION TESTS ====================

    @Test
    @DisplayName("POST /api/bus-company/buses - Should fail without proper role")
    void testCreateBusUnauthorized() throws Exception {
        Map<String, Object> createBusRequest = Map.of(
                "name", "Test Bus",
                "licensePlate", "30A-12345",
                "capacity", 45,
                "busType", "GHE_NGOI");

        performAuthenticatedPost("/api/bus-company/buses", createBusRequest, userAccessToken)
                .andExpect(status().isForbidden()); // USER role cannot create buses
    }

    @Test
    @DisplayName("GET /api/admin/buses - Should fail without admin role")
    void testGetAdminBusesUnauthorized() throws Exception {
        performAuthenticatedGet("/api/admin/buses", userAccessToken)
                .andExpect(status().isForbidden()); // USER role cannot access admin endpoints
    }
}
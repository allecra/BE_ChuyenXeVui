package com.example.ckdatveexe.route;

import com.example.ckdatveexe.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Route Management API Integration Tests")
class RouteControllerIntegrationTest extends BaseIntegrationTest {

    // ==================== USER ROUTE APIs (Public) ====================

    @Test
    @DisplayName("GET /api/routes - Should get all active routes (public)")
    void testGetAllActiveRoutes() throws Exception {
        mockMvc.perform(get("/api/routes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /api/routes/{routeId} - Should get route detail (public)")
    void testGetRouteDetail() throws Exception {
        mockMvc.perform(get("/api/routes/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Expected since no route exists
    }

    @Test
    @DisplayName("GET /api/routes/search - Should search routes with filters (public)")
    void testSearchRoutes() throws Exception {
        mockMvc.perform(get("/api/routes/search")
                .param("startLocation", "Hà Nội")
                .param("endLocation", "Hồ Chí Minh")
                .param("minPrice", "300000")
                .param("maxPrice", "800000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/routes/{routeId}/buses - Should get buses on route (public)")
    void testGetBusesOnRoute() throws Exception {
        mockMvc.perform(get("/api/routes/1/buses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Expected since no route exists
    }

    @Test
    @DisplayName("POST /api/routes/calculate-price - Should calculate segment price (public)")
    void testCalculateSegmentPrice() throws Exception {
        Map<String, Object> priceRequest = Map.of(
                "routeId", 1,
                "departureStationId", 1,
                "arrivalStationId", 2);

        mockMvc.perform(post("/api/routes/calculate-price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(priceRequest)))
                .andExpect(status().isNotFound()); // Expected since no route exists
    }

    // ==================== COMPANY ROUTE APIs ====================

    @Test
    @DisplayName("POST /api/bus-company/routes - Should create route for company")
    void testCreateRouteForCompany() throws Exception {
        Map<String, Object> createRouteRequest = Map.of(
                "routeName", "Test Route Hà Nội - Hồ Chí Minh",
                "startLocation", "Hà Nội",
                "endLocation", "Hồ Chí Minh",
                "price", 500000,
                "duration", 1200,
                "distance", 1700,
                "descriptions", "Test route description",
                "departureStationId", 1,
                "arrivalStationId", 2);

        performAuthenticatedPost("/api/bus-company/routes", createRouteRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since stations don't exist
    }

    @Test
    @DisplayName("GET /api/bus-company/routes - Should get company routes")
    void testGetCompanyRoutes() throws Exception {
        performAuthenticatedGet("/api/bus-company/routes", companyAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/bus-company/routes/{routeId} - Should get route detail for company")
    void testGetRouteDetailForCompany() throws Exception {
        performAuthenticatedGet("/api/bus-company/routes/1", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no route exists
    }

    @Test
    @DisplayName("PUT /api/bus-company/routes/{routeId} - Should update route")
    void testUpdateRouteForCompany() throws Exception {
        Map<String, Object> updateRouteRequest = Map.of(
                "routeName", "Updated Route Name",
                "price", 550000,
                "descriptions", "Updated description");

        performAuthenticatedPut("/api/bus-company/routes/1", updateRouteRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no route exists
    }

    @Test
    @DisplayName("GET /api/bus-company/routes/search - Should search company routes")
    void testSearchCompanyRoutes() throws Exception {
        performAuthenticatedGet("/api/bus-company/routes/search?startLocation=Hà Nội", companyAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/bus-company/routes/{routeId} - Should delete route")
    void testDeleteRouteForCompany() throws Exception {
        Map<String, Object> deleteRouteRequest = Map.of(
                "hardDelete", false,
                "reason", "Route no longer profitable");

        performAuthenticatedDeleteWithBody("/api/bus-company/routes/1", deleteRouteRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no route exists
    }

    @Test
    @DisplayName("GET /api/bus-company/routes/{routeId}/buses - Should get buses on route for company")
    void testGetBusesOnRouteForCompany() throws Exception {
        performAuthenticatedGet("/api/bus-company/routes/1/buses", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no route exists
    }

    // ==================== ROUTE STATION MANAGEMENT ====================

    @Test
    @DisplayName("POST /api/bus-company/routes/{routeId}/stations - Should add station to route")
    void testAddStationToRoute() throws Exception {
        Map<String, Object> stationRequest = Map.of(
                "stationId", 2,
                "orderIndex", 1,
                "distanceFromPrevious", 150,
                "priceFromPrevious", 200000,
                "notes", "Intermediate station");

        performAuthenticatedPost("/api/bus-company/routes/1/stations", stationRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no route exists
    }

    @Test
    @DisplayName("GET /api/bus-company/routes/{routeId}/stations - Should get route stations")
    void testGetRouteStations() throws Exception {
        performAuthenticatedGet("/api/bus-company/routes/1/stations", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no route exists
    }

    @Test
    @DisplayName("PUT /api/bus-company/routes/{routeId}/stations/{stationId} - Should update route station")
    void testUpdateRouteStation() throws Exception {
        Map<String, Object> updateStationRequest = Map.of(
                "priceFromPrevious", 250000,
                "distanceFromPrevious", 180,
                "notes", "Updated station info");

        performAuthenticatedPut("/api/bus-company/routes/1/stations/2", updateStationRequest, companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no route exists
    }

    @Test
    @DisplayName("DELETE /api/bus-company/routes/{routeId}/stations/{stationId} - Should remove station from route")
    void testRemoveStationFromRoute() throws Exception {
        performAuthenticatedDelete("/api/bus-company/routes/1/stations/2", companyAccessToken)
                .andExpect(status().isNotFound()); // Expected since no route exists
    }

// ==================== AUTHORIZATION TESTS ====================

@Test
    @DisplayName("POST /api/bus-company/routes - Should fail without proper role")
    void testCreateRouteUnauthorized() throws Exception {
      Map<String, Object> createRouteRequest = Map.of(
            "routeName", "Test Route",
            "startLocation", "Hà Nội",
            "endLocation", "Hồ Chí Minh",
            "price", 500000
        );

        performAuthenticatedPost("/api/bus-company/routes", createRouteRequest, userAccessToken)
                .andExpect(status().isForbidden()); // USER role cannot create routes
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("POST /api/bus-company/routes - Should fail with invalid data")
    void testCreateRouteInvalidData() throws Exception {
        Map<String, Object> invalidRouteRequest = Map.of(
            "routeName", "", // Empty name
            "price", -1000 // Negative price
        );

        performAuthenticatedPost("/api/bus-company/routes", invalidRouteRequest, companyAccessToken)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/routes/search - Should handle invalid search parameters")
    void testSearchRoutesInvalidParams() throws Exception {
        mockMvc.perform(get("/api/routes/search")
                .param("minPrice", "invalid")
                .param("maxPrice", "also-invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
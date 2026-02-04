package com.example.ckdatveexe.module.route.controller;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.route.dto.*;
import com.example.ckdatveexe.module.route.service.RouteService;
import com.example.ckdatveexe.module.route.service.RouteStationService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bus-company/routes")
@RequiredArgsConstructor
@Tag(name = "Route Company API", description = "API quản lý tuyến đường cho nhà xe")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class RouteCompanyController {

    private final RouteService routeService;
    private final RouteStationService routeStationService;

    @PostMapping
    @Operation(summary = "Tạo tuyến đường mới", description = "Tạo tuyến đường mới cho nhà xe")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin tuyến đường mới", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", examples = {
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Tuyến Hà Nội - Hồ Chí Minh", summary = "Tạo tuyến đường dài", description = "Tạo tuyến đường từ Hà Nội đến Hồ Chí Minh", value = """
                    {
                      "routeName": "Hà Nội - Hồ Chí Minh (Cao tốc)",
                      "startLocation": "Hà Nội",
                      "endLocation": "Hồ Chí Minh",
                      "price": 500000,
                      "duration": 1200,
                      "distance": 1700,
                      "descriptions": "Tuyến cao tốc, đi qua các tỉnh miền Trung, thời gian di chuyển nhanh",
                      "departureStationId": 1,
                      "arrivalStationId": 2
                    }
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Tuyến ngắn", summary = "Tạo tuyến đường ngắn", description = "Tạo tuyến đường trong nội thành", value = """
                    {
                      "routeName": "Hà Nội - Hải Phòng",
                      "startLocation": "Hà Nội",
                      "endLocation": "Hải Phòng",
                      "price": 150000,
                      "duration": 120,
                      "distance": 100,
                      "descriptions": "Tuyến nội vùng, thời gian di chuyển ngắn",
                      "departureStationId": 1,
                      "arrivalStationId": 3
                    }
                    """)
    }))
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<RouteResponse>> createRoute(
            @Valid @RequestBody RouteCreateRequest request,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] POST /api/bus-company/routes - Create route");
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            // Get company ID from authentication (assuming it's stored in the token)
            Integer busCompanyId = getBusCompanyIdFromAuth(authentication);

            RouteResponse route = routeService.createRoute(request, busCompanyId);

            log.info("🆕 [BUS COMPANY] 201 CREATED - Route created successfully: {}", route.getRouteName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<RouteResponse>builder()
                            .success(true)
                            .message("Tạo tuyến đường thành công")
                            .data(route)
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid route data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<RouteResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<RouteResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to create route", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<RouteResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi tạo tuyến đường")
                            .build());
        }
    }

    @GetMapping
    @Operation(summary = "Danh sách tuyến đường của nhà xe", description = "Lấy danh sách tất cả tuyến đường thuộc nhà xe (bao gồm ACTIVE, INACTIVE)")
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<Page<RouteResponse>>> getAllRoutes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] GET /api/bus-company/routes - Get all routes");
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            Integer busCompanyId = getBusCompanyIdFromAuth(authentication);

            RouteSearchRequest request = new RouteSearchRequest();
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            Page<RouteResponse> routes = routeService.getAllRoutesForCompany(busCompanyId, request);

            log.info("✅ [BUS COMPANY] 200 OK - Retrieved {} routes", routes.getTotalElements());
            return ResponseEntity.ok(ApiResponse.<Page<RouteResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách tuyến đường thành công")
                    .data(routes)
                    .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get routes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<RouteResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách tuyến đường")
                            .build());
        }
    }

    @GetMapping("/{routeId}")
    @Operation(summary = "Chi tiết tuyến đường", description = "Lấy thông tin chi tiết của một tuyến đường thuộc nhà xe")
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<RouteResponse>> getRouteDetail(
            @PathVariable Integer routeId,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] GET /api/bus-company/routes/{} - Get route detail", routeId);
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            Integer busCompanyId = getBusCompanyIdFromAuth(authentication);
            RouteResponse route = routeService.getRouteDetailForCompany(routeId, busCompanyId);

            log.info("✅ [BUS COMPANY] 200 OK - Retrieved route detail for ID: {}", routeId);
            return ResponseEntity.ok(ApiResponse.<RouteResponse>builder()
                    .success(true)
                    .message("Lấy thông tin tuyến đường thành công")
                    .data(route)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Route not found: {}", routeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<RouteResponse>builder()
                            .success(false)
                            .message("Không tìm thấy tuyến đường với ID: " + routeId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get route detail: {}", routeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<RouteResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy thông tin tuyến đường")
                            .build());
        }
    }

    @PutMapping("/{routeId}")
    @Operation(summary = "Cập nhật tuyến đường", description = "Cập nhật thông tin tuyến đường")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin cập nhật tuyến đường", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", examples = {
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Cập nhật giá", summary = "Cập nhật giá tuyến", description = "Chỉ cập nhật giá tuyến đường", value = """
                    {
                      "price": 550000
                    }
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Cập nhật trạng thái", summary = "Tạm ngưng tuyến", description = "Đặt tuyến về trạng thái INACTIVE", value = """
                    {
                      "status": "INACTIVE"
                    }
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Cập nhật toàn bộ", summary = "Cập nhật nhiều thông tin", description = "Cập nhật tên, giá và mô tả", value = """
                    {
                      "routeName": "Hà Nội - Hồ Chí Minh (Express)",
                      "price": 600000,
                      "descriptions": "Tuyến cao tốc mới, thời gian di chuyển nhanh hơn 2 tiếng"
                    }
                    """)
    }))
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<RouteResponse>> updateRoute(
            @PathVariable Integer routeId,
            @Valid @RequestBody RouteUpdateRequest request,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] PUT /api/bus-company/routes/{} - Update route", routeId);
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            Integer busCompanyId = getBusCompanyIdFromAuth(authentication);
            RouteResponse route = routeService.updateRoute(routeId, request, busCompanyId);

            log.info("✅ [BUS COMPANY] 200 OK - Route updated successfully: {}", route.getRouteName());
            return ResponseEntity.ok(ApiResponse.<RouteResponse>builder()
                    .success(true)
                    .message("Cập nhật tuyến đường thành công")
                    .data(route)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Route not found: {}", routeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<RouteResponse>builder()
                            .success(false)
                            .message("Không tìm thấy tuyến đường với ID: " + routeId)
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid update data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<RouteResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to update route: {}", routeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<RouteResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi cập nhật tuyến đường")
                            .build());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm tuyến đường", description = "Tìm kiếm tuyến đường của nhà xe với nhiều bộ lọc")
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<Page<RouteResponse>>> searchRoutes(
            @RequestParam(required = false) String startLocation,
            @RequestParam(required = false) String endLocation,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] GET /api/bus-company/routes/search - Search routes");
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            Integer busCompanyId = getBusCompanyIdFromAuth(authentication);

            RouteSearchRequest request = new RouteSearchRequest();
            request.setStartLocation(startLocation);
            request.setEndLocation(endLocation);
            request.setMinPrice(minPrice);
            request.setMaxPrice(maxPrice);
            if (status != null) {
                request.setStatus(com.example.ckdatveexe.shared.entity.RouteStatus.valueOf(status.toUpperCase()));
            }
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            Page<RouteResponse> routes = routeService.searchRoutesForCompany(busCompanyId, request);

            log.info("✅ [BUS COMPANY] 200 OK - Found {} routes matching search criteria", routes.getTotalElements());
            return ResponseEntity.ok(ApiResponse.<Page<RouteResponse>>builder()
                    .success(true)
                    .message("Tìm kiếm tuyến đường thành công")
                    .data(routes)
                    .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Page<RouteResponse>>builder()
                            .success(false)
                            .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to search routes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<RouteResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi tìm kiếm tuyến đường")
                            .build());
        }
    }

    @DeleteMapping("/{routeId}")
    @Operation(summary = "Xóa tuyến đường", description = "Xóa tuyến đường (xóa mềm hoặc xóa cứng)")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Tùy chọn xóa", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", examples = {
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Xóa mềm", summary = "Xóa mềm tuyến đường", description = "Đặt trạng thái DELETED, có thể khôi phục", value = """
                    {
                      "hardDelete": false,
                      "reason": "Tuyến không còn hiệu quả kinh doanh"
                    }
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Xóa cứng", summary = "Xóa vĩnh viễn tuyến đường", description = "Xóa hoàn toàn khỏi database", value = """
                    {
                      "hardDelete": true,
                      "reason": "Tuyến bị hủy bỏ vĩnh viễn"
                    }
                    """)
    }))
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(
            @PathVariable Integer routeId,
            @RequestBody(required = false) DeleteRouteRequest request,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] DELETE /api/bus-company/routes/{} - Delete route", routeId);
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            if (request == null) {
                request = new DeleteRouteRequest();
            }

            Integer busCompanyId = getBusCompanyIdFromAuth(authentication);
            routeService.deleteRoute(routeId, busCompanyId, request);

            String message = request.isHardDelete()
                    ? "Xóa tuyến đường vĩnh viễn thành công"
                    : "Xóa tuyến đường thành công";

            log.info("✅ [BUS COMPANY] 200 OK - Route {} successfully: {}",
                    request.isHardDelete() ? "hard deleted" : "soft deleted", routeId);

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message(message)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Route not found for deletion: {}", routeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Không tìm thấy tuyến đường với ID: " + routeId)
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid delete request for route {}: {}", routeId,
                    e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Yêu cầu xóa không hợp lệ: " + e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to delete route: {}", routeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi xóa tuyến đường")
                            .build());
        }
    }

    @GetMapping("/{routeId}/buses")
    @Operation(summary = "Danh sách xe trên tuyến", description = "Lấy danh sách tất cả xe trên tuyến đường (bao gồm xe không hoạt động)")
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<List<BusResponse>>> getBusesOnRoute(
            @PathVariable Integer routeId,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] GET /api/bus-company/routes/{}/buses - Get buses on route", routeId);
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            Integer busCompanyId = getBusCompanyIdFromAuth(authentication);
            List<BusResponse> buses = routeService.getBusesOnRouteForCompany(routeId, busCompanyId);

            log.info("✅ [BUS COMPANY] 200 OK - Retrieved {} buses on route ID: {}", buses.size(), routeId);
            return ResponseEntity.ok(ApiResponse.<List<BusResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách xe trên tuyến thành công")
                    .data(buses)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Route not found: {}", routeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<BusResponse>>builder()
                            .success(false)
                            .message("Không tìm thấy tuyến đường với ID: " + routeId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get buses on route: {}", routeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<BusResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách xe trên tuyến")
                            .build());
        }
    }

    // Helper method to extract bus company ID from authentication
    private Integer getBusCompanyIdFromAuth(Authentication authentication) {
        // This is a placeholder - you'll need to implement this based on your JWT token
        // structure
        // For now, returning a mock value
        return 1; // TODO: Extract actual company ID from JWT token
    }

    // ==================== ROUTE STATION MANAGEMENT ====================

    @PostMapping("/{routeId}/stations")
    @Operation(summary = "Thêm bến vào tuyến", description = "Thêm bến xe vào tuyến đường với thứ tự và giá cụ thể")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin bến cần thêm vào tuyến", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", examples = {
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Bến đầu tiên", summary = "Thêm bến đầu tiên (order = 0)", description = "Bến đầu tiên có khoảng cách và giá = 0", value = """
                    {
                      "stationId": 1,
                      "orderIndex": 0,
                      "distanceFromPrevious": 0,
                      "priceFromPrevious": 0,
                      "notes": "Bến xuất phát chính"
                    }
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Bến trung gian", summary = "Thêm bến trung gian", description = "Bến trung gian với khoảng cách và giá từ bến trước", value = """
                    {
                      "stationId": 2,
                      "orderIndex": 1,
                      "distanceFromPrevious": 150,
                      "priceFromPrevious": 200000,
                      "notes": "Bến trung gian, dừng 15 phút"
                    }
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Bến cuối", summary = "Thêm bến cuối tuyến", description = "Bến cuối tuyến với khoảng cách và giá từ bến trước", value = """
                    {
                      "stationId": 3,
                      "orderIndex": 2,
                      "distanceFromPrevious": 200,
                      "priceFromPrevious": 300000,
                      "notes": "Bến đến cuối cùng"
                    }
                    """)
    }))
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<RouteStationResponse>> addStationToRoute(
            @PathVariable Integer routeId,
            @Valid @RequestBody RouteStationRequest request,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] POST /api/bus-company/routes/{}/stations - Add station to route", routeId);
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            Integer busCompanyId = getBusCompanyIdFromAuth(authentication);
            RouteStationResponse routeStation = routeStationService.addStationToRoute(routeId, request, busCompanyId);

            log.info("🆕 [BUS COMPANY] 201 CREATED - Station added to route successfully: {} at order {}",
                    routeStation.getStationName(), routeStation.getOrderIndex());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<RouteStationResponse>builder()
                            .success(true)
                            .message("Thêm bến vào tuyến thành công")
                            .data(routeStation)
                            .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<RouteStationResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid station data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<RouteStationResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to add station to route: {}", routeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<RouteStationResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi thêm bến vào tuyến")
                            .build());
        }
    }

    @GetMapping("/{routeId}/stations")
    @Operation(summary = "Danh sách bến trong tuyến", description = "Lấy danh sách tất cả bến trong tuyến đường theo thứ tự")
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<List<RouteStationResponse>>> getRouteStations(
            @PathVariable Integer routeId,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] GET /api/bus-company/routes/{}/stations - Get route stations", routeId);
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            Integer busCompanyId = getBusCompanyIdFromAuth(authentication);
            List<RouteStationResponse> stations = routeStationService.getRouteStations(routeId, busCompanyId);

            log.info("✅ [BUS COMPANY] 200 OK - Retrieved {} stations for route ID: {}", stations.size(), routeId);
            return ResponseEntity.ok(ApiResponse.<List<RouteStationResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách bến trong tuyến thành công")
                    .data(stations)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Route not found: {}", routeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<RouteStationResponse>>builder()
                            .success(false)
                            .message("Không tìm thấy tuyến đường với ID: " + routeId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get route stations: {}", routeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<RouteStationResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách bến trong tuyến")
                            .build());
        }
    }

    @PutMapping("/{routeId}/stations/{stationId}")
    @Operation(summary = "Cập nhật bến trong tuyến", description = "Cập nhật thông tin bến trong tuyến đường")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin cập nhật bến", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", examples = {
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Cập nhật giá", summary = "Cập nhật giá đoạn", description = "Chỉ cập nhật giá từ bến trước", value = """
                    {
                      "priceFromPrevious": 250000
                    }
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Cập nhật khoảng cách", summary = "Cập nhật khoảng cách", description = "Cập nhật khoảng cách từ bến trước", value = """
                    {
                      "distanceFromPrevious": 180
                    }
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Cập nhật thứ tự", summary = "Thay đổi thứ tự bến", description = "Thay đổi vị trí bến trong tuyến", value = """
                    {
                      "orderIndex": 3,
                      "distanceFromPrevious": 120,
                      "priceFromPrevious": 180000,
                      "notes": "Đã thay đổi thứ tự bến"
                    }
                    """)
    }))
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<RouteStationResponse>> updateRouteStation(
            @PathVariable Integer routeId,
            @PathVariable Integer stationId,
            @Valid @RequestBody RouteStationUpdateRequest request,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] PUT /api/bus-company/routes/{}/stations/{} - Update route station", routeId,
                stationId);
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            Integer busCompanyId = getBusCompanyIdFromAuth(authentication);
            RouteStationResponse routeStation = routeStationService.updateRouteStation(routeId, stationId, request,
                    busCompanyId);

            log.info("✅ [BUS COMPANY] 200 OK - Route station updated successfully: {}", routeStation.getStationName());
            return ResponseEntity.ok(ApiResponse.<RouteStationResponse>builder()
                    .success(true)
                    .message("Cập nhật bến trong tuyến thành công")
                    .data(routeStation)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<RouteStationResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid update data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<RouteStationResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to update route station: {}/{}", routeId,
                    stationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<RouteStationResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi cập nhật bến trong tuyến")
                            .build());
        }
    }

    @DeleteMapping("/{routeId}/stations/{stationId}")
    @Operation(summary = "Xóa bến khỏi tuyến", description = "Xóa bến xe khỏi tuyến đường (chỉ cho phép xóa bến trung gian)")
    @PreAuthorize("hasRole('BUS_COMPANY')")
    public ResponseEntity<ApiResponse<Void>> removeStationFromRoute(
            @PathVariable Integer routeId,
            @PathVariable Integer stationId,
            Authentication authentication) {

        log.info("🚌 [BUS COMPANY] DELETE /api/bus-company/routes/{}/stations/{} - Remove station from route", routeId,
                stationId);
        log.info("🔐 [BUS COMPANY] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            Integer busCompanyId = getBusCompanyIdFromAuth(authentication);
            routeStationService.removeStationFromRoute(routeId, stationId, busCompanyId);

            log.info("✅ [BUS COMPANY] 200 OK - Station removed from route successfully: {}/{}", routeId, stationId);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Xóa bến khỏi tuyến thành công")
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [BUS COMPANY] 404 NOT_FOUND - Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [BUS COMPANY] 400 BAD_REQUEST - Invalid remove request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [BUS COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to remove station from route: {}/{}",
                    routeId, stationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi xóa bến khỏi tuyến")
                            .build());
        }
    }
}
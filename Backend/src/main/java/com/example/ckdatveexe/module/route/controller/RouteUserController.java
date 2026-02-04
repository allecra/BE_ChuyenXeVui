package com.example.ckdatveexe.module.route.controller;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.route.dto.RouteResponse;
import com.example.ckdatveexe.module.route.dto.RouteSearchRequest;
import com.example.ckdatveexe.module.route.dto.SegmentPriceRequest;
import com.example.ckdatveexe.module.route.dto.SegmentPriceResponse;
import com.example.ckdatveexe.module.route.service.RouteService;
import com.example.ckdatveexe.module.route.service.RouteStationService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Tag(name = "Route User API", description = "API xem tuyến đường cho người dùng")
@SecurityRequirements() // No authentication required
@Slf4j
public class RouteUserController {

    private final RouteService routeService;
    private final RouteStationService routeStationService;

    @GetMapping
    @Operation(summary = "Danh sách tuyến đường", description = "Lấy danh sách tất cả tuyến đường đang hoạt động")
    public ResponseEntity<ApiResponse<Page<RouteResponse>>> getAllActiveRoutes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("👤 [USER] GET /api/routes - Get all active routes");

        try {
            RouteSearchRequest request = new RouteSearchRequest();
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            Page<RouteResponse> routes = routeService.getAllActiveRoutesForUser(request);

            log.info("✅ [USER] 200 OK - Retrieved {} active routes", routes.getTotalElements());
            return ResponseEntity.ok(ApiResponse.<Page<RouteResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách tuyến đường thành công")
                    .data(routes)
                    .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get active routes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<RouteResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách tuyến đường")
                            .build());
        }
    }

    @GetMapping("/{routeId}")
    @Operation(summary = "Chi tiết tuyến đường", description = "Lấy thông tin chi tiết của một tuyến đường")
    public ResponseEntity<ApiResponse<RouteResponse>> getRouteDetail(@PathVariable Integer routeId) {

        log.info("👤 [USER] GET /api/routes/{} - Get route detail", routeId);

        try {
            RouteResponse route = routeService.getRouteDetailForUser(routeId);

            log.info("✅ [USER] 200 OK - Retrieved route detail for ID: {}", routeId);
            return ResponseEntity.ok(ApiResponse.<RouteResponse>builder()
                    .success(true)
                    .message("Lấy thông tin tuyến đường thành công")
                    .data(route)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [USER] 404 NOT_FOUND - Route not found: {}", routeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<RouteResponse>builder()
                            .success(false)
                            .message("Không tìm thấy tuyến đường với ID: " + routeId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get route detail: {}", routeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<RouteResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy thông tin tuyến đường")
                            .build());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm tuyến đường", description = "Tìm kiếm tuyến đường với nhiều bộ lọc")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Các tham số tìm kiếm (tất cả đều tùy chọn)", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", examples = {
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Tìm theo điểm đi và đến", summary = "Tìm tuyến từ Hà Nội đến Hồ Chí Minh", value = """
                    ?startLocation=Hà Nội&endLocation=Hồ Chí Minh&page=0&size=10&sortBy=price&sortDirection=asc
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Tìm theo khoảng giá", summary = "Tìm tuyến có giá từ 300k đến 800k", value = """
                    ?minPrice=300000&maxPrice=800000&page=0&size=10&sortBy=price&sortDirection=asc
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Tìm theo nhà xe", summary = "Tìm tuyến của nhà xe Phương Trang", value = """
                    ?busCompanyName=Phương Trang&page=0&size=10&sortBy=createdAt&sortDirection=desc
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Tìm theo thời gian", summary = "Tìm tuyến xuất phát ngày 10/02/2026 từ 6h đến 18h", value = """
                    ?departureDate=2026-02-10T00:00:00&departureTimeFrom=2026-02-10T06:00:00&departureTimeTo=2026-02-10T18:00:00&page=0&size=10
                    """)
    }))
    public ResponseEntity<ApiResponse<Page<RouteResponse>>> searchRoutes(
            @RequestParam(required = false) String startLocation,
            @RequestParam(required = false) String endLocation,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer busCompanyId,
            @RequestParam(required = false) String busCompanyName,
            @RequestParam(required = false) String departureDate,
            @RequestParam(required = false) String departureTimeFrom,
            @RequestParam(required = false) String departureTimeTo,
            @RequestParam(required = false) String arrivalDate,
            @RequestParam(required = false) String arrivalTimeFrom,
            @RequestParam(required = false) String arrivalTimeTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        log.info("👤 [USER] GET /api/routes/search - Search routes with filters");

        try {
            RouteSearchRequest request = new RouteSearchRequest();
            request.setStartLocation(startLocation);
            request.setEndLocation(endLocation);
            request.setMinPrice(minPrice);
            request.setMaxPrice(maxPrice);
            request.setBusCompanyId(busCompanyId);
            request.setBusCompanyName(busCompanyName);

            // Parse date strings if provided
            if (departureDate != null) {
                request.setDepartureDate(java.time.LocalDateTime.parse(departureDate));
            }
            if (departureTimeFrom != null) {
                request.setDepartureTimeFrom(java.time.LocalDateTime.parse(departureTimeFrom));
            }
            if (departureTimeTo != null) {
                request.setDepartureTimeTo(java.time.LocalDateTime.parse(departureTimeTo));
            }
            if (arrivalDate != null) {
                request.setArrivalDate(java.time.LocalDateTime.parse(arrivalDate));
            }
            if (arrivalTimeFrom != null) {
                request.setArrivalTimeFrom(java.time.LocalDateTime.parse(arrivalTimeFrom));
            }
            if (arrivalTimeTo != null) {
                request.setArrivalTimeTo(java.time.LocalDateTime.parse(arrivalTimeTo));
            }

            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            Page<RouteResponse> routes = routeService.searchRoutesForUser(request);

            log.info("✅ [USER] 200 OK - Found {} routes matching search criteria", routes.getTotalElements());
            return ResponseEntity.ok(ApiResponse.<Page<RouteResponse>>builder()
                    .success(true)
                    .message("Tìm kiếm tuyến đường thành công")
                    .data(routes)
                    .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [USER] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Page<RouteResponse>>builder()
                            .success(false)
                            .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to search routes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<RouteResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi tìm kiếm tuyến đường")
                            .build());
        }
    }

    @GetMapping("/{routeId}/buses")
    @Operation(summary = "Danh sách xe trên tuyến", description = "Lấy danh sách xe đang hoạt động trên tuyến đường")
    public ResponseEntity<ApiResponse<List<BusResponse>>> getBusesOnRoute(@PathVariable Integer routeId) {

        log.info("👤 [USER] GET /api/routes/{}/buses - Get buses on route", routeId);

        try {
            List<BusResponse> buses = routeService.getBusesOnRouteForUser(routeId);

            log.info("✅ [USER] 200 OK - Retrieved {} buses on route ID: {}", buses.size(), routeId);
            return ResponseEntity.ok(ApiResponse.<List<BusResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách xe trên tuyến thành công")
                    .data(buses)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [USER] 404 NOT_FOUND - Route not found: {}", routeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<BusResponse>>builder()
                            .success(false)
                            .message("Không tìm thấy tuyến đường với ID: " + routeId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get buses on route: {}", routeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<BusResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách xe trên tuyến")
                            .build());
        }
    }

    @PostMapping("/calculate-price")
    @Operation(summary = "Tính giá vé theo đoạn", description = "Tính giá vé từ bến đi đến bến đến trên một tuyến cụ thể")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin tính giá vé theo đoạn", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", examples = {
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Tính giá đoạn ngắn", summary = "Tính giá từ bến đầu đến bến trung gian", description = "Tính giá vé cho đoạn đường ngắn", value = """
                    {
                      "routeId": 1,
                      "departureStationId": 1,
                      "arrivalStationId": 2
                    }
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Tính giá toàn tuyến", summary = "Tính giá từ bến đầu đến bến cuối", description = "Tính giá vé cho toàn bộ tuyến đường", value = """
                    {
                      "routeId": 1,
                      "departureStationId": 1,
                      "arrivalStationId": 3
                    }
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Tính giá đoạn giữa", summary = "Tính giá từ bến trung gian đến bến cuối", description = "Tính giá vé cho đoạn cuối tuyến", value = """
                    {
                      "routeId": 1,
                      "departureStationId": 2,
                      "arrivalStationId": 3
                    }
                    """)
    }))
    public ResponseEntity<ApiResponse<SegmentPriceResponse>> calculateSegmentPrice(
            @Valid @RequestBody SegmentPriceRequest request) {

        log.info("👤 [USER] POST /api/routes/calculate-price - Calculate segment price");

        try {
            SegmentPriceResponse segmentPrice = routeStationService.calculateSegmentPrice(request);

            log.info("✅ [USER] 200 OK - Calculated segment price: {}km, {}VND",
                    segmentPrice.getTotalDistance(), segmentPrice.getTotalPrice());
            return ResponseEntity.ok(ApiResponse.<SegmentPriceResponse>builder()
                    .success(true)
                    .message("Tính giá vé theo đoạn thành công")
                    .data(segmentPrice)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [USER] 404 NOT_FOUND - Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<SegmentPriceResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [USER] 400 BAD_REQUEST - Invalid price calculation request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<SegmentPriceResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to calculate segment price", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<SegmentPriceResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi tính giá vé theo đoạn")
                            .build());
        }
    }
}
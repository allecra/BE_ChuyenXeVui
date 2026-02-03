package com.example.ckdatveexe.module.station.controller;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.station.dto.StationResponse;
import com.example.ckdatveexe.module.station.dto.StationSearchRequest;
import com.example.ckdatveexe.module.station.service.StationService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/user/stations")
@RequiredArgsConstructor
@Tag(name = "Station User API", description = "API quản lý bến xe cho người dùng")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class StationUserController {

    private final StationService stationService;

    @GetMapping
    @Operation(summary = "Xem tất cả bến xe", description = "Lấy danh sách tất cả bến xe với thông tin cơ bản")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
    public ResponseEntity<ApiResponse<Page<StationResponse>>> getAllStations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            Authentication authentication) {

        log.info("👤 [USER] GET /api/user/stations - Get all stations");
        log.info("🔐 [USER] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            StationSearchRequest request = new StationSearchRequest();
            request.setKeyword(keyword);
            request.setLocation(location);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            Page<StationResponse> stations = stationService.getAllStationsForUser(request);

            log.info("✅ [USER] 200 OK - Retrieved {} stations", stations.getTotalElements());
            return ResponseEntity.ok(ApiResponse.<Page<StationResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách bến xe thành công")
                    .data(stations)
                    .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [USER] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Page<StationResponse>>builder()
                            .success(false)
                            .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get stations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<StationResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách bến xe")
                            .build());
        }
    }

    @GetMapping("/{stationId}")
    @Operation(summary = "Xem chi tiết bến xe", description = "Lấy thông tin chi tiết của một bến xe")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
    public ResponseEntity<ApiResponse<StationResponse>> getStationDetail(
            @PathVariable Integer stationId,
            Authentication authentication) {

        log.info("👤 [USER] GET /api/user/stations/{} - Get station detail", stationId);
        log.info("🔐 [USER] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            StationResponse station = stationService.getStationDetailForUser(stationId);

            log.info("✅ [USER] 200 OK - Retrieved station detail for ID: {}", stationId);
            return ResponseEntity.ok(ApiResponse.<StationResponse>builder()
                    .success(true)
                    .message("Lấy thông tin bến xe thành công")
                    .data(station)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [USER] 404 NOT_FOUND - Station not found: {}", stationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<StationResponse>builder()
                            .success(false)
                            .message("Không tìm thấy bến xe với ID: " + stationId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get station detail: {}", stationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<StationResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy thông tin bến xe")
                            .build());
        }
    }

    @GetMapping("/{stationId}/buses")
    @Operation(summary = "Xem danh sách xe thuộc bến", description = "Lấy danh sách xe đang hoạt động tại bến xe")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
    public ResponseEntity<ApiResponse<List<BusResponse>>> getBusesAtStation(
            @PathVariable Integer stationId,
            Authentication authentication) {

        log.info("👤 [USER] GET /api/user/stations/{}/buses - Get buses at station", stationId);
        log.info("🔐 [USER] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            List<BusResponse> buses = stationService.getBusesAtStationForUser(stationId);

            log.info("✅ [USER] 200 OK - Retrieved {} buses at station ID: {}", buses.size(), stationId);
            return ResponseEntity.ok(ApiResponse.<List<BusResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách xe tại bến thành công")
                    .data(buses)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [USER] 404 NOT_FOUND - Station not found: {}", stationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<BusResponse>>builder()
                            .success(false)
                            .message("Không tìm thấy bến xe với ID: " + stationId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get buses at station: {}", stationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<BusResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách xe tại bến")
                            .build());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm bến xe", description = "Tìm kiếm bến xe theo từ khóa và địa điểm")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
    public ResponseEntity<ApiResponse<Page<StationResponse>>> searchStations(
            @RequestParam String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            Authentication authentication) {

        log.info("👤 [USER] GET /api/user/stations/search - Search stations with keyword: {}", keyword);
        log.info("🔐 [USER] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                log.error("❌ [USER] 400 BAD_REQUEST - Empty search keyword");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Page<StationResponse>>builder()
                                .success(false)
                                .message("Từ khóa tìm kiếm không được để trống")
                                .build());
            }

            StationSearchRequest request = new StationSearchRequest();
            request.setKeyword(keyword);
            request.setLocation(location);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            Page<StationResponse> stations = stationService.getAllStationsForUser(request);

            log.info("✅ [USER] 200 OK - Found {} stations matching keyword: {}", stations.getTotalElements(), keyword);
            return ResponseEntity.ok(ApiResponse.<Page<StationResponse>>builder()
                    .success(true)
                    .message("Tìm kiếm bến xe thành công")
                    .data(stations)
                    .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [USER] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Page<StationResponse>>builder()
                            .success(false)
                            .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to search stations with keyword: {}", keyword, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<StationResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi tìm kiếm bến xe")
                            .build());
        }
    }

    @GetMapping("/{stationId}/buses/search")
    @Operation(summary = "Tìm kiếm xe trong bến", description = "Tìm kiếm xe theo từ khóa tại một bến xe cụ thể")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')")
    public ResponseEntity<ApiResponse<List<BusResponse>>> searchBusesAtStation(
            @PathVariable Integer stationId,
            @RequestParam String keyword,
            Authentication authentication) {

        log.info("👤 [USER] GET /api/user/stations/{}/buses/search - Search buses at station with keyword: {}",
                stationId, keyword);
        log.info("🔐 [USER] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            List<BusResponse> buses = stationService.searchBusesAtStationForUser(stationId, keyword);

            log.info("✅ [USER] 200 OK - Found {} buses at station {} matching keyword: {}",
                    buses.size(), stationId, keyword);
            return ResponseEntity.ok(ApiResponse.<List<BusResponse>>builder()
                    .success(true)
                    .message("Tìm kiếm xe tại bến thành công")
                    .data(buses)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [USER] 404 NOT_FOUND - Station not found: {}", stationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<BusResponse>>builder()
                            .success(false)
                            .message("Không tìm thấy bến xe với ID: " + stationId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to search buses at station: {}", stationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<BusResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi tìm kiếm xe tại bến")
                            .build());
        }
    }
}
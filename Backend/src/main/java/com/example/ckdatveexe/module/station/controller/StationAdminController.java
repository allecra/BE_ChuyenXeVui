package com.example.ckdatveexe.module.station.controller;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.station.dto.*;
import com.example.ckdatveexe.module.station.service.StationService;
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
@RequestMapping("/api/admin/stations")
@RequiredArgsConstructor
@Tag(name = "Station Admin API", description = "API quản lý bến xe cho Admin")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class StationAdminController {

    private final StationService stationService;

    @PostMapping
    @Operation(summary = "Tạo bến xe mới (Admin)", description = "Admin tạo bến xe mới trong hệ thống")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StationResponse>> createStation(
            @Valid @RequestBody StationCreateRequest request,
            Authentication authentication) {

        log.info("👑 [ADMIN] POST /api/admin/stations - Create station");
        log.info("🔐 [ADMIN] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            StationResponse station = stationService.createStation(request);

            log.info("🆕 [ADMIN] 201 CREATED - Station created successfully: {}", station.getName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<StationResponse>builder()
                            .success(true)
                            .message("Tạo bến xe thành công")
                            .data(station)
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [ADMIN] 400 BAD_REQUEST - Invalid station data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<StationResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to create station", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<StationResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi tạo bến xe")
                            .build());
        }
    }

    @GetMapping
    @Operation(summary = "Danh sách tất cả bến xe (Admin)", description = "Admin lấy danh sách tất cả bến xe với thông tin đầy đủ")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<StationResponse>>> getAllStations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            Authentication authentication) {

        log.info("👑 [ADMIN] GET /api/admin/stations - Get all stations");
        log.info("🔐 [ADMIN] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            StationSearchRequest request = new StationSearchRequest();
            request.setKeyword(keyword);
            request.setLocation(location);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            Page<StationResponse> stations = stationService.getAllStationsForCompany(request);

            log.info("✅ [ADMIN] 200 OK - Retrieved {} stations", stations.getTotalElements());
            return ResponseEntity.ok(ApiResponse.<Page<StationResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách bến xe thành công")
                    .data(stations)
                    .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [ADMIN] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Page<StationResponse>>builder()
                            .success(false)
                            .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to get stations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<StationResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách bến xe")
                            .build());
        }
    }

    @GetMapping("/{stationId}")
    @Operation(summary = "Chi tiết bến xe (Admin)", description = "Admin lấy thông tin chi tiết của một bến xe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StationResponse>> getStationDetail(
            @PathVariable Integer stationId,
            Authentication authentication) {

        log.info("👑 [ADMIN] GET /api/admin/stations/{} - Get station detail", stationId);
        log.info("🔐 [ADMIN] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            StationResponse station = stationService.getStationDetailForCompany(stationId);

            log.info("✅ [ADMIN] 200 OK - Retrieved station detail for ID: {}", stationId);
            return ResponseEntity.ok(ApiResponse.<StationResponse>builder()
                    .success(true)
                    .message("Lấy thông tin bến xe thành công")
                    .data(station)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [ADMIN] 404 NOT_FOUND - Station not found: {}", stationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<StationResponse>builder()
                            .success(false)
                            .message("Không tìm thấy bến xe với ID: " + stationId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to get station detail: {}", stationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<StationResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy thông tin bến xe")
                            .build());
        }
    }

    @PutMapping("/{stationId}")
    @Operation(summary = "Cập nhật bến xe (Admin)", description = "Admin cập nhật thông tin bến xe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StationResponse>> updateStation(
            @PathVariable Integer stationId,
            @Valid @RequestBody StationUpdateRequest request,
            Authentication authentication) {

        log.info("👑 [ADMIN] PUT /api/admin/stations/{} - Update station", stationId);
        log.info("🔐 [ADMIN] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            StationResponse station = stationService.updateStation(stationId, request);

            log.info("✅ [ADMIN] 200 OK - Station updated successfully: {}", station.getName());
            return ResponseEntity.ok(ApiResponse.<StationResponse>builder()
                    .success(true)
                    .message("Cập nhật bến xe thành công")
                    .data(station)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [ADMIN] 404 NOT_FOUND - Station not found: {}", stationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<StationResponse>builder()
                            .success(false)
                            .message("Không tìm thấy bến xe với ID: " + stationId)
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [ADMIN] 400 BAD_REQUEST - Invalid update data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<StationResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to update station: {}", stationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<StationResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi cập nhật bến xe")
                            .build());
        }
    }

    @PostMapping("/assign-buses")
    @Operation(summary = "Gắn xe cho bến (Admin)", description = "Admin gắn danh sách xe vào bến xe - hỗ trợ cả mode cơ bản và chi tiết")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StationResponse>> assignBusesToStation(
            @Valid @RequestBody UnifiedAssignBusRequest request,
            Authentication authentication) {

        log.info("👑 [ADMIN] POST /api/admin/stations/assign-buses - Assign buses to station");
        log.info("🔐 [ADMIN] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            StationResponse station = stationService.assignBusesToStationUnified(request);

            String mode = request.isBasicMode() ? "cơ bản" : "chi tiết";
            int busCount = request.isBasicMode() ? request.getBusIds().size() : request.getBusStations().size();

            log.info("✅ [ADMIN] 200 OK - Assigned {} buses to station using {} mode: {}",
                    busCount, mode, station.getName());
            return ResponseEntity.ok(ApiResponse.<StationResponse>builder()
                    .success(true)
                    .message("Gắn xe vào bến thành công (mode " + mode + ")")
                    .data(station)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [ADMIN] 404 NOT_FOUND - Station or buses not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<StationResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [ADMIN] 400 BAD_REQUEST - Invalid assign request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<StationResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to assign buses to station", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<StationResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi gắn xe vào bến")
                            .build());
        }
    }

    @DeleteMapping("/{stationId}/buses")
    @Operation(summary = "Gỡ xe khỏi bến (Admin)", description = "Admin gỡ danh sách xe khỏi bến xe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StationResponse>> removeBusesFromStation(
            @PathVariable Integer stationId,
            @RequestBody List<Integer> busIds,
            Authentication authentication) {

        log.info("👑 [ADMIN] DELETE /api/admin/stations/{}/buses - Remove buses from station", stationId);
        log.info("🔐 [ADMIN] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            StationResponse station = stationService.removeBusesFromStation(stationId, busIds);

            log.info("✅ [ADMIN] 200 OK - Removed {} buses from station: {}", busIds.size(), station.getName());
            return ResponseEntity.ok(ApiResponse.<StationResponse>builder()
                    .success(true)
                    .message("Gỡ xe khỏi bến thành công")
                    .data(station)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [ADMIN] 404 NOT_FOUND - Station not found: {}", stationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<StationResponse>builder()
                            .success(false)
                            .message("Không tìm thấy bến xe với ID: " + stationId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to remove buses from station: {}", stationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<StationResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi gỡ xe khỏi bến")
                            .build());
        }
    }

    @GetMapping("/{stationId}/buses")
    @Operation(summary = "Danh sách xe tại bến (Admin)", description = "Admin lấy danh sách tất cả xe tại bến (bao gồm xe không hoạt động)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BusResponse>>> getBusesAtStation(
            @PathVariable Integer stationId,
            Authentication authentication) {

        log.info("👑 [ADMIN] GET /api/admin/stations/{}/buses - Get buses at station", stationId);
        log.info("🔐 [ADMIN] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            List<BusResponse> buses = stationService.getBusesAtStationForCompany(stationId);

            log.info("✅ [ADMIN] 200 OK - Retrieved {} buses at station ID: {}", buses.size(), stationId);
            return ResponseEntity.ok(ApiResponse.<List<BusResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách xe tại bến thành công")
                    .data(buses)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [ADMIN] 404 NOT_FOUND - Station not found: {}", stationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<BusResponse>>builder()
                            .success(false)
                            .message("Không tìm thấy bến xe với ID: " + stationId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to get buses at station: {}", stationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<BusResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách xe tại bến")
                            .build());
        }
    }

    @GetMapping("/{stationId}/bus-stations")
    @Operation(summary = "Chi tiết BusStation tại bến (Admin)", description = "Admin lấy thông tin chi tiết về các BusStation tại một bến xe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BusStationResponse>>> getBusStationDetails(
            @PathVariable Integer stationId,
            Authentication authentication) {

        log.info("👑 [ADMIN] GET /api/admin/stations/{}/bus-stations - Get detailed BusStation info", stationId);
        log.info("🔐 [ADMIN] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            List<BusStationResponse> busStations = stationService.getBusStationDetailsForCompany(stationId);

            log.info("✅ [ADMIN] 200 OK - Retrieved {} detailed BusStation records for station ID: {}",
                    busStations.size(), stationId);
            return ResponseEntity.ok(ApiResponse.<List<BusStationResponse>>builder()
                    .success(true)
                    .message("Lấy thông tin chi tiết BusStation thành công")
                    .data(busStations)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [ADMIN] 404 NOT_FOUND - Station not found: {}", stationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<BusStationResponse>>builder()
                            .success(false)
                            .message("Không tìm thấy bến xe với ID: " + stationId)
                            .build());

        } catch (Exception e) {
            log.error("💥 [ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to get detailed BusStation info for station: {}",
                    stationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<BusStationResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy thông tin chi tiết BusStation")
                            .build());
        }
    }

    @DeleteMapping("/{stationId}")
    @Operation(summary = "Xóa bến xe (Admin)", description = "Admin xóa bến xe (xóa cứng hoặc mềm)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStation(
            @PathVariable Integer stationId,
            @RequestBody(required = false) DeleteStationRequest request,
            Authentication authentication) {

        log.info("👑 [ADMIN] DELETE /api/admin/stations/{} - Delete station", stationId);
        log.info("🔐 [ADMIN] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            if (request == null) {
                request = new DeleteStationRequest();
            }

            stationService.deleteStation(stationId, request.isHardDelete());

            String message = request.isHardDelete()
                    ? "Xóa bến xe vĩnh viễn thành công"
                    : "Xóa bến xe thành công";

            log.info("✅ [ADMIN] 200 OK - Station {} successfully: {}",
                    request.isHardDelete() ? "hard deleted" : "soft deleted", stationId);

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message(message)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [ADMIN] 404 NOT_FOUND - Station not found for deletion: {}", stationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Không tìm thấy bến xe với ID: " + stationId)
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [ADMIN] 400 BAD_REQUEST - Invalid delete request for station {}: {}", stationId,
                    e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Yêu cầu xóa không hợp lệ: " + e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to delete station: {}", stationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi xóa bến xe")
                            .build());
        }
    }

    @PutMapping("/{stationId}/buses/status")
    @Operation(summary = "Cập nhật trạng thái xe trong bến (Admin)", description = "Admin cập nhật trạng thái hoạt động và ghi chú của xe tại bến xe")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin cập nhật trạng thái xe", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", examples = {
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Tạm ngưng xe", summary = "Admin tạm ngưng hoạt động của xe tại bến", description = "Admin đặt xe về trạng thái không hoạt động với ghi chú", value = """
                    {
                      "busId": 3,
                      "isActive": false,
                      "notes": "Admin tạm ngưng xe do vi phạm quy định an toàn"
                    }
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Kích hoạt xe", summary = "Admin kích hoạt lại xe tại bến", description = "Admin đặt xe về trạng thái hoạt động", value = """
                    {
                      "busId": 4,
                      "isActive": true,
                      "notes": "Admin cho phép xe hoạt động trở lại sau kiểm tra"
                    }
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Cập nhật ghi chú quản lý", summary = "Admin cập nhật ghi chú quản lý", description = "Admin cập nhật ghi chú theo dõi", value = """
                    {
                      "busId": 5,
                      "isActive": true,
                      "notes": "Xe đã qua kiểm tra định kỳ của cơ quan quản lý"
                    }
                    """)
    }))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusStationResponse>> updateBusStationStatus(
            @PathVariable Integer stationId,
            @Valid @RequestBody BusStationStatusUpdateRequest request,
            Authentication authentication) {

        log.info("👑 [ADMIN] PUT /api/admin/stations/{}/buses/status - Update bus status at station", stationId);
        log.info("🔐 [ADMIN] User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        try {
            BusStationResponse busStation = stationService.updateBusStationStatus(stationId, request);

            log.info("✅ [ADMIN] 200 OK - Updated bus status: Bus {} at Station {}, Active: {}",
                    request.getBusId(), stationId, request.getIsActive());
            return ResponseEntity.ok(ApiResponse.<BusStationResponse>builder()
                    .success(true)
                    .message("Cập nhật trạng thái xe trong bến thành công")
                    .data(busStation)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [ADMIN] 404 NOT_FOUND - Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<BusStationResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [ADMIN] 400 BAD_REQUEST - Invalid status update request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<BusStationResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [ADMIN] 500 INTERNAL_SERVER_ERROR - Failed to update bus status at station: {}",
                    stationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<BusStationResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi cập nhật trạng thái xe trong bến")
                            .build());
        }
    }
}
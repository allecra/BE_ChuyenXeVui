package com.example.ckdatveexe.module.schedule.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.schedule.dto.*;
import com.example.ckdatveexe.module.schedule.service.ScheduleService;
import com.example.ckdatveexe.module.schedule.service.ScheduleBusService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule Company API", description = "API quản lý lịch trình và xe cho nhà xe")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('COMPANY')")
@Slf4j
public class ScheduleCompanyController {

        private final ScheduleService scheduleService;
        private final ScheduleBusService scheduleBusService;

        // ==================== SCHEDULE MANAGEMENT ENDPOINTS ====================

        @PostMapping
        @Operation(summary = "Tạo lịch trình mới", description = "Tạo lịch trình mới cho tuyến đường")
        public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(
                        @Valid @RequestBody ScheduleCreateRequest request,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("🚌 [COMPANY] POST /api/company/schedules - Create new schedule for route {} with bus {}",
                                request.getRouteId(), request.getBusId());

                try {
                        Integer busCompanyId = userDetails.getUser().getBusCompany().getId();
                        ScheduleResponse schedule = scheduleService.createSchedule(request, busCompanyId);

                        log.info("✅ [COMPANY] 201 CREATED - Created schedule with ID: {}", schedule.getId());
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.<ScheduleResponse>builder()
                                                        .success(true)
                                                        .message("Tạo lịch trình thành công")
                                                        .data(schedule)
                                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [COMPANY] 404 NOT_FOUND - Resource not found: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<ScheduleResponse>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.warn("❌ [COMPANY] 400 BAD_REQUEST - Invalid request: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<ScheduleResponse>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to create schedule", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<ScheduleResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi tạo lịch trình")
                                                        .build());
                }
        }

        @GetMapping
        @Operation(summary = "Danh sách lịch trình", description = "Lấy danh sách tất cả lịch trình của nhà xe")
        public ResponseEntity<ApiResponse<Page<ScheduleResponse>>> getAllSchedules(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "departureTime") String sortBy,
                        @RequestParam(defaultValue = "asc") String sortDirection,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("🚌 [COMPANY] GET /api/company/schedules - Get all schedules for company");

                try {
                        Integer busCompanyId = userDetails.getUser().getBusCompany().getId();

                        ScheduleSearchRequest request = new ScheduleSearchRequest();
                        request.setPage(page);
                        request.setSize(size);
                        request.setSortBy(sortBy);
                        request.setSortDirection(sortDirection);

                        Page<ScheduleResponse> schedules = scheduleService.getAllSchedulesForCompany(busCompanyId,
                                        request);

                        log.info("✅ [COMPANY] 200 OK - Retrieved {} schedules", schedules.getTotalElements());
                        return ResponseEntity.ok(ApiResponse.<Page<ScheduleResponse>>builder()
                                        .success(true)
                                        .message("Lấy danh sách lịch trình thành công")
                                        .data(schedules)
                                        .build());

                } catch (Exception e) {
                        log.error("💥 [COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get schedules", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<ScheduleResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy danh sách lịch trình")
                                                        .build());
                }
        }

        @GetMapping("/{scheduleId}")
        @Operation(summary = "Chi tiết lịch trình", description = "Lấy thông tin chi tiết của một lịch trình")
        public ResponseEntity<ApiResponse<ScheduleResponse>> getScheduleDetail(
                        @PathVariable Integer scheduleId,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("🚌 [COMPANY] GET /api/company/schedules/{} - Get schedule detail", scheduleId);

                try {
                        Integer busCompanyId = userDetails.getUser().getBusCompany().getId();
                        ScheduleResponse schedule = scheduleService.getScheduleDetailForCompany(scheduleId,
                                        busCompanyId);

                        log.info("✅ [COMPANY] 200 OK - Retrieved schedule detail for ID: {}", scheduleId);
                        return ResponseEntity.ok(ApiResponse.<ScheduleResponse>builder()
                                        .success(true)
                                        .message("Lấy thông tin lịch trình thành công")
                                        .data(schedule)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [COMPANY] 404 NOT_FOUND - Schedule not found: {}", scheduleId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<ScheduleResponse>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy lịch trình với ID: " + scheduleId)
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get schedule detail: {}",
                                        scheduleId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<ScheduleResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy thông tin lịch trình")
                                                        .build());
                }
        }

        @PutMapping("/{scheduleId}")
        @Operation(summary = "Cập nhật lịch trình", description = "Cập nhật thông tin lịch trình")
        public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
                        @PathVariable Integer scheduleId,
                        @Valid @RequestBody ScheduleUpdateRequest request,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("🚌 [COMPANY] PUT /api/company/schedules/{} - Update schedule", scheduleId);

                try {
                        Integer busCompanyId = userDetails.getUser().getBusCompany().getId();
                        ScheduleResponse schedule = scheduleService.updateSchedule(scheduleId, request, busCompanyId);

                        log.info("✅ [COMPANY] 200 OK - Updated schedule with ID: {}", scheduleId);
                        return ResponseEntity.ok(ApiResponse.<ScheduleResponse>builder()
                                        .success(true)
                                        .message("Cập nhật lịch trình thành công")
                                        .data(schedule)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [COMPANY] 404 NOT_FOUND - Schedule not found: {}", scheduleId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<ScheduleResponse>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy lịch trình với ID: " + scheduleId)
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.warn("❌ [COMPANY] 400 BAD_REQUEST - Invalid update request: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<ScheduleResponse>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to update schedule: {}", scheduleId,
                                        e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<ScheduleResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi cập nhật lịch trình")
                                                        .build());
                }
        }

        @GetMapping("/search")
        @Operation(summary = "Tìm kiếm lịch trình", description = "Tìm kiếm lịch trình với nhiều bộ lọc")
        public ResponseEntity<ApiResponse<Page<ScheduleResponse>>> searchSchedules(
                        @RequestParam(required = false) Integer routeId,
                        @RequestParam(required = false) Integer busId,
                        @RequestParam(required = false) Integer startStationId,
                        @RequestParam(required = false) Integer endStationId,
                        @RequestParam(required = false) String departureTimeFrom,
                        @RequestParam(required = false) String departureTimeTo,
                        @RequestParam(required = false) String arrivalTimeFrom,
                        @RequestParam(required = false) String arrivalTimeTo,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) Double minPrice,
                        @RequestParam(required = false) Double maxPrice,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "departureTime") String sortBy,
                        @RequestParam(defaultValue = "asc") String sortDirection,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("🚌 [COMPANY] GET /api/company/schedules/search - Search schedules with filters");

                try {
                        Integer busCompanyId = userDetails.getUser().getBusCompany().getId();

                        ScheduleSearchRequest request = new ScheduleSearchRequest();
                        request.setRouteId(routeId);
                        request.setBusId(busId);
                        request.setStartStationId(startStationId);
                        request.setEndStationId(endStationId);
                        request.setMinPrice(minPrice);
                        request.setMaxPrice(maxPrice);

                        // Parse date strings if provided
                        if (departureTimeFrom != null) {
                                request.setDepartureTimeFrom(java.time.LocalDateTime.parse(departureTimeFrom));
                        }
                        if (departureTimeTo != null) {
                                request.setDepartureTimeTo(java.time.LocalDateTime.parse(departureTimeTo));
                        }
                        if (arrivalTimeFrom != null) {
                                request.setArrivalTimeFrom(java.time.LocalDateTime.parse(arrivalTimeFrom));
                        }
                        if (arrivalTimeTo != null) {
                                request.setArrivalTimeTo(java.time.LocalDateTime.parse(arrivalTimeTo));
                        }
                        if (status != null) {
                                request.setStatus(com.example.ckdatveexe.shared.entity.ScheduleStatus
                                                .valueOf(status.toUpperCase()));
                        }

                        request.setPage(page);
                        request.setSize(size);
                        request.setSortBy(sortBy);
                        request.setSortDirection(sortDirection);

                        Page<ScheduleResponse> schedules = scheduleService.searchSchedulesForCompany(busCompanyId,
                                        request);

                        log.info("✅ [COMPANY] 200 OK - Found {} schedules matching search criteria",
                                        schedules.getTotalElements());
                        return ResponseEntity.ok(ApiResponse.<Page<ScheduleResponse>>builder()
                                        .success(true)
                                        .message("Tìm kiếm lịch trình thành công")
                                        .data(schedules)
                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [COMPANY] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Page<ScheduleResponse>>builder()
                                                        .success(false)
                                                        .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to search schedules", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Page<ScheduleResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi tìm kiếm lịch trình")
                                                        .build());
                }
        }

        @DeleteMapping("/{scheduleId}")
        @Operation(summary = "Hủy lịch trình", description = "Hủy lịch trình (cập nhật trạng thái thành CANCELLED)")
        public ResponseEntity<ApiResponse<Void>> cancelSchedule(
                        @PathVariable Integer scheduleId,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("🚌 [COMPANY] DELETE /api/company/schedules/{} - Cancel schedule", scheduleId);

                try {
                        Integer busCompanyId = userDetails.getUser().getBusCompany().getId();
                        scheduleService.cancelSchedule(scheduleId, busCompanyId);

                        log.info("✅ [COMPANY] 200 OK - Cancelled schedule with ID: {}", scheduleId);
                        return ResponseEntity.ok(ApiResponse.<Void>builder()
                                        .success(true)
                                        .message("Hủy lịch trình thành công")
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("� [COMPANY] 404 NOT_FOUND - Schedule not found: {}", scheduleId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy lịch trình với ID: " + scheduleId)
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.warn("❌ [COMPANY] 400 BAD_REQUEST - Cannot cancel schedule: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to cancel schedule: {}", scheduleId,
                                        e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi hủy lịch trình")
                                                        .build());
                }
        }

        // ==================== BUS MANAGEMENT ENDPOINTS ====================

        @PostMapping("/{scheduleId}/buses")
        @Operation(summary = "Gán xe vào lịch trình", description = "Gán một hoặc nhiều xe vào lịch trình")
        public ResponseEntity<ApiResponse<List<ScheduleBusResponse>>> assignBusesToSchedule(
                        @PathVariable Integer scheduleId,
                        @Valid @RequestBody ScheduleBusRequest request,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("🚌 [COMPANY] POST /api/company/schedules/{}/buses - Assign buses to schedule", scheduleId);

                try {
                        Integer busCompanyId = userDetails.getUser().getBusCompany().getId();
                        List<ScheduleBusResponse> scheduleBuses = scheduleBusService.assignBusesToSchedule(
                                        scheduleId, request, busCompanyId);

                        log.info("✅ [COMPANY] 201 CREATED - Assigned {} buses to schedule {}",
                                        scheduleBuses.size(), scheduleId);
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.<List<ScheduleBusResponse>>builder()
                                                        .success(true)
                                                        .message("Gán xe vào lịch trình thành công")
                                                        .data(scheduleBuses)
                                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [COMPANY] 404 NOT_FOUND - Resource not found: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<List<ScheduleBusResponse>>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.warn("❌ [COMPANY] 400 BAD_REQUEST - Invalid request: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<List<ScheduleBusResponse>>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("[COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to assign buses to schedule", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<List<ScheduleBusResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi gán xe vào lịch trình")
                                                        .build());
                }
        }

        @GetMapping("/{scheduleId}/buses")
        @Operation(summary = "Danh sách xe trong lịch trình", description = "Lấy danh sách tất cả xe trong lịch trình")
        public ResponseEntity<ApiResponse<List<ScheduleBusResponse>>> getBusesInSchedule(
                        @PathVariable Integer scheduleId,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("[COMPANY] GET /api/company/schedules/{}/buses - Get buses in schedule", scheduleId);

                try {
                        Integer busCompanyId = userDetails.getUser().getBusCompany().getId();
                        List<ScheduleBusResponse> scheduleBuses = scheduleBusService.getBusesInSchedule(scheduleId,
                                        busCompanyId);

                        log.info("✅ [COMPANY] 200 OK - Retrieved {} buses for schedule {}", scheduleBuses.size(),
                                        scheduleId);
                        return ResponseEntity.ok(ApiResponse.<List<ScheduleBusResponse>>builder()
                                        .success(true)
                                        .message("Lấy danh sách xe trong lịch trình thành công")
                                        .data(scheduleBuses)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("[COMPANY] 404 NOT_FOUND - Schedule not found: {}", scheduleId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<List<ScheduleBusResponse>>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy lịch trình với ID: " + scheduleId)
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to get buses in schedule: {}",
                                        scheduleId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<List<ScheduleBusResponse>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi lấy danh sách xe trong lịch trình")
                                                        .build());
                }
        }

        @PutMapping("/{scheduleId}/buses/{busId}/status")
        @Operation(summary = "Cập nhật trạng thái xe", description = "Cập nhật trạng thái xe trong lịch trình")
        public ResponseEntity<ApiResponse<ScheduleBusResponse>> updateBusStatusInSchedule(
                        @PathVariable Integer scheduleId,
                        @PathVariable Integer busId,
                        @Valid @RequestBody ScheduleBusStatusUpdateRequest request,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("🚌 [COMPANY] PUT /api/company/schedules/{}/buses/{}/status - Update bus status",
                                scheduleId, busId);

                try {
                        Integer busCompanyId = userDetails.getUser().getBusCompany().getId();
                        ScheduleBusResponse scheduleBus = scheduleBusService.updateBusStatusInSchedule(
                                        scheduleId, busId, request, busCompanyId);

                        log.info("✅ [COMPANY] 200 OK - Updated bus {} status in schedule {} to {}",
                                        busId, scheduleId, request.getStatus());
                        return ResponseEntity.ok(ApiResponse.<ScheduleBusResponse>builder()
                                        .success(true)
                                        .message("Cập nhật trạng thái xe thành công")
                                        .data(scheduleBus)
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [COMPANY] 404 NOT_FOUND - Resource not found: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<ScheduleBusResponse>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to update bus status: {}/{}",
                                        scheduleId, busId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<ScheduleBusResponse>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi cập nhật trạng thái xe")
                                                        .build());
                }
        }

        @DeleteMapping("/{scheduleId}/buses/{busId}")
        @Operation(summary = "Gỡ xe khỏi lịch trình", description = "Gỡ xe khỏi lịch trình")
        public ResponseEntity<ApiResponse<Void>> removeBusFromSchedule(
                        @PathVariable Integer scheduleId,
                        @PathVariable Integer busId,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("🚌 [COMPANY] DELETE /api/company/schedules/{}/buses/{} - Remove bus from schedule",
                                scheduleId, busId);

                try {
                        Integer busCompanyId = userDetails.getUser().getBusCompany().getId();
                        scheduleBusService.removeBusFromSchedule(scheduleId, busId, busCompanyId);

                        log.info("✅ [COMPANY] 200 OK - Removed bus {} from schedule {}", busId, scheduleId);
                        return ResponseEntity.ok(ApiResponse.<Void>builder()
                                        .success(true)
                                        .message("Gỡ xe khỏi lịch trình thành công")
                                        .build());

                } catch (ResourceNotFoundException e) {
                        log.warn("🔍 [COMPANY] 404 NOT_FOUND - Resource not found: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.warn("❌ [COMPANY] 400 BAD_REQUEST - Cannot remove bus: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error(" [COMPANY] 500 INTERNAL_SERVER_ERROR - Failed to remove bus from schedule: {}/{}",
                                        scheduleId, busId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi gỡ xe khỏi lịch trình")
                                                        .build());
                }
        }
}
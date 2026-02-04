package com.example.ckdatveexe.module.schedule.controller;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.schedule.dto.ScheduleResponse;
import com.example.ckdatveexe.module.schedule.dto.ScheduleSearchRequest;
import com.example.ckdatveexe.module.schedule.dto.ScheduleBusResponse;
import com.example.ckdatveexe.module.schedule.service.ScheduleService;
import com.example.ckdatveexe.module.schedule.service.ScheduleBusService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule User API", description = "API xem lịch trình cho người dùng")
@SecurityRequirements() // No authentication required
@Slf4j
public class ScheduleUserController {

    private final ScheduleService scheduleService;
    private final ScheduleBusService scheduleBusService;

    @GetMapping
    @Operation(summary = "Danh sách lịch trình", description = "Lấy danh sách tất cả lịch trình đang hoạt động")
    public ResponseEntity<ApiResponse<Page<ScheduleResponse>>> getAllActiveSchedules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "departureTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        log.info("👤 [USER] GET /api/schedules - Get all active schedules");

        try {
            ScheduleSearchRequest request = new ScheduleSearchRequest();
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            Page<ScheduleResponse> schedules = scheduleService.getAllActiveSchedulesForUser(request);

            log.info("✅ [USER] 200 OK - Retrieved {} active schedules", schedules.getTotalElements());
            return ResponseEntity.ok(ApiResponse.<Page<ScheduleResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách lịch trình thành công")
                    .data(schedules)
                    .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get active schedules", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<ScheduleResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách lịch trình")
                            .build());
        }
    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "Chi tiết lịch trình", description = "Lấy thông tin chi tiết của một lịch trình")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getScheduleDetail(@PathVariable Integer scheduleId) {

        log.info("👤 [USER] GET /api/schedules/{} - Get schedule detail", scheduleId);

        try {
            ScheduleResponse schedule = scheduleService.getScheduleDetailForUser(scheduleId);

            log.info("✅ [USER] 200 OK - Retrieved schedule detail for ID: {}", scheduleId);
            return ResponseEntity.ok(ApiResponse.<ScheduleResponse>builder()
                    .success(true)
                    .message("Lấy thông tin lịch trình thành công")
                    .data(schedule)
                    .build());

        } catch (ResourceNotFoundException e) {
            log.warn("🔍 [USER] 404 NOT_FOUND - Schedule not found: {}", scheduleId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<ScheduleResponse>builder()
                            .success(false)
                            .message("Không tìm thấy lịch trình với ID: " + scheduleId)
                            .build());

        } catch (IllegalArgumentException e) {
            log.warn("❌ [USER] 400 BAD_REQUEST - Invalid schedule access: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<ScheduleResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get schedule detail: {}", scheduleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<ScheduleResponse>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy thông tin lịch trình")
                            .build());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm lịch trình", description = "Tìm kiếm lịch trình với nhiều bộ lọc")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Các tham số tìm kiếm (tất cả đều tùy chọn)", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", examples = {
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Tìm theo bến đi và đến", summary = "Tìm lịch trình từ bến 1 đến bến 2", value = """
                    ?startStationId=1&endStationId=2&page=0&size=10&sortBy=departureTime&sortDirection=asc
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Tìm theo ngày và giờ", summary = "Tìm lịch trình ngày 10/02/2026 từ 6h đến 18h", value = """
                    ?departureDate=2026-02-10T00:00:00&timeFrom=2026-02-10T06:00:00&timeTo=2026-02-10T18:00:00&page=0&size=10
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Tìm theo khoảng giá", summary = "Tìm lịch trình có giá từ 300k đến 800k", value = """
                    ?minPrice=300000&maxPrice=800000&page=0&size=10&sortBy=price&sortDirection=asc
                    """),
            @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Tìm theo nhà xe và loại xe", summary = "Tìm lịch trình của nhà xe 1 với xe giường nằm", value = """
                    ?busCompanyId=1&busType=GIUONG_NAM&minSeats=5&page=0&size=10
                    """)
    }))
    public ResponseEntity<ApiResponse<Page<ScheduleResponse>>> searchSchedules(
            @RequestParam(required = false) Integer startStationId,
            @RequestParam(required = false) Integer endStationId,
            @RequestParam(required = false) String departureDate,
            @RequestParam(required = false) String timeFrom,
            @RequestParam(required = false) String timeTo,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer busCompanyId,
            @RequestParam(required = false) String busType,
            @RequestParam(required = false) Integer minSeats,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "departureTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        log.info("👤 [USER] GET /api/schedules/search - Search schedules with filters");

        try {
            ScheduleSearchRequest request = new ScheduleSearchRequest();
            request.setStartStationId(startStationId);
            request.setEndStationId(endStationId);
            request.setMinPrice(minPrice);
            request.setMaxPrice(maxPrice);
            request.setBusCompanyId(busCompanyId);
            request.setMinSeats(minSeats);

            // Parse date strings if provided
            if (departureDate != null) {
                request.setDepartureDate(java.time.LocalDateTime.parse(departureDate));
            }
            if (timeFrom != null) {
                request.setTimeFrom(java.time.LocalDateTime.parse(timeFrom));
            }
            if (timeTo != null) {
                request.setTimeTo(java.time.LocalDateTime.parse(timeTo));
            }
            if (busType != null) {
                request.setBusType(com.example.ckdatveexe.shared.entity.BusType.valueOf(busType.toUpperCase()));
            }

            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            Page<ScheduleResponse> schedules = scheduleService.searchSchedulesForUser(request);

            log.info("✅ [USER] 200 OK - Found {} schedules matching search criteria", schedules.getTotalElements());
            return ResponseEntity.ok(ApiResponse.<Page<ScheduleResponse>>builder()
                    .success(true)
                    .message("Tìm kiếm lịch trình thành công")
                    .data(schedules)
                    .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ [USER] 400 BAD_REQUEST - Invalid search parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Page<ScheduleResponse>>builder()
                            .success(false)
                            .message("Tham số tìm kiếm không hợp lệ: " + e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to search schedules", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<ScheduleResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi tìm kiếm lịch trình")
                            .build());
        }
    }

    @GetMapping("/{scheduleId}/buses")
    @Operation(summary = "Danh sách xe trong lịch trình", description = "Lấy danh sách xe đang hoạt động trong lịch trình")
    public ResponseEntity<ApiResponse<List<ScheduleBusResponse>>> getActiveBusesInSchedule(
            @PathVariable Integer scheduleId) {

        log.info("👤 [USER] GET /api/schedules/{}/buses - Get active buses in schedule", scheduleId);

        try {
            List<ScheduleBusResponse> buses = scheduleBusService.getActiveBusesInSchedule(scheduleId);

            log.info("✅ [USER] 200 OK - Retrieved {} active buses for schedule {}", buses.size(), scheduleId);
            return ResponseEntity.ok(ApiResponse.<List<ScheduleBusResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách xe trong lịch trình thành công")
                    .data(buses)
                    .build());

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Failed to get buses in schedule: {}", scheduleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ScheduleBusResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách xe trong lịch trình")
                            .build());
        }
    }
}
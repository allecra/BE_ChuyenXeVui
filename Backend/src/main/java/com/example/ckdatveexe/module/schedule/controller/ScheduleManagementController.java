package com.example.ckdatveexe.module.schedule.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.module.auth.dto.ApiResponse;
import com.example.ckdatveexe.module.schedule.dto.*;
import com.example.ckdatveexe.module.schedule.service.ScheduleManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company/schedule-management")
@RequiredArgsConstructor
@Tag(name = "Schedule Management", description = "APIs for advanced schedule management by bus companies")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('BUS_COMPANY')")
@Slf4j
public class ScheduleManagementController {

    private final ScheduleManagementService scheduleManagementService;

    @PostMapping("/{scheduleId}/cancel")
    @Operation(summary = "Cancel schedule", description = "Cancel a schedule and handle passenger refunds and notifications")
    public ResponseEntity<ApiResponse> cancelSchedule(
            @Parameter(description = "Schedule ID") @PathVariable Integer scheduleId,
            @Valid @RequestBody ScheduleCancelRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            ScheduleResponse response = scheduleManagementService.cancelSchedule(
                    scheduleId, request, userDetails.getId());

            return ResponseEntity.ok(ApiResponse.success("Hủy lịch trình thành công", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error cancelling schedule", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể hủy lịch trình"));
        }
    }

    @PostMapping("/{scheduleId}/assign-bus")
    @Operation(summary = "Assign bus to schedule", description = "Assign a bus to a schedule")
    public ResponseEntity<ApiResponse> assignBusToSchedule(
            @Parameter(description = "Schedule ID") @PathVariable Integer scheduleId,
            @Valid @RequestBody AssignBusToScheduleRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            ScheduleResponse response = scheduleManagementService.assignBusToSchedule(
                    scheduleId, request, userDetails.getId());

            return ResponseEntity.ok(ApiResponse.success("Gán xe cho lịch trình thành công", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error assigning bus to schedule", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể gán xe cho lịch trình"));
        }
    }

    @PutMapping("/{scheduleId}/buses/{busId}/status")
    @Operation(summary = "Update bus status in schedule", description = "Update the status of a bus in a specific schedule")
    public ResponseEntity<ApiResponse> updateBusStatusInSchedule(
            @Parameter(description = "Schedule ID") @PathVariable Integer scheduleId,
            @Parameter(description = "Bus ID") @PathVariable Integer busId,
            @Valid @RequestBody UpdateBusStatusRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            ScheduleResponse response = scheduleManagementService.updateBusStatusInSchedule(
                    scheduleId, busId, request, userDetails.getId());

            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái xe thành công", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error updating bus status in schedule", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể cập nhật trạng thái xe"));
        }
    }

    @DeleteMapping("/{scheduleId}/buses/{busId}")
    @Operation(summary = "Remove bus from schedule", description = "Remove a bus from a schedule (only if no confirmed tickets)")
    public ResponseEntity<ApiResponse> removeBusFromSchedule(
            @Parameter(description = "Schedule ID") @PathVariable Integer scheduleId,
            @Parameter(description = "Bus ID") @PathVariable Integer busId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            ScheduleResponse response = scheduleManagementService.removeBusFromSchedule(
                    scheduleId, busId, userDetails.getId());

            return ResponseEntity.ok(ApiResponse.success("Gỡ xe khỏi lịch trình thành công", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error removing bus from schedule", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể gỡ xe khỏi lịch trình"));
        }
    }
}
package com.example.ckdatveexe.module.ticket.controller;

import com.example.ckdatveexe.module.auth.dto.ApiResponse;
import com.example.ckdatveexe.module.ticket.dto.*;
import com.example.ckdatveexe.module.ticket.service.AdminTicketService;
import com.example.ckdatveexe.shared.entity.TicketStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
@Tag(name = "Admin Ticket Management", description = "APIs for admin ticket management and reporting")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class TicketAdminController {

    private final AdminTicketService adminTicketService;

    @PostMapping("/search")
    @Operation(summary = "Search tickets", description = "Advanced search for tickets with multiple filters")
    public ResponseEntity<ApiResponse> searchTickets(
            @RequestBody AdminTicketSearchRequest searchRequest,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TicketResponse> response = adminTicketService.searchTickets(searchRequest, pageable);
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm vé thành công", response));
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "Get ticket detail", description = "Get detailed information of a specific ticket")
    public ResponseEntity<ApiResponse> getTicketDetail(
            @Parameter(description = "Ticket ID") @PathVariable Integer ticketId) {

        TicketResponse response = adminTicketService.getTicketDetail(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin vé thành công", response));
    }

    @PutMapping("/{ticketId}/status")
    @Operation(summary = "Update ticket status", description = "Update ticket status with reason")
    public ResponseEntity<ApiResponse> updateTicketStatus(
            @Parameter(description = "Ticket ID") @PathVariable Integer ticketId,
            @Parameter(description = "New status") @RequestParam TicketStatus status,
            @Parameter(description = "Reason for status change") @RequestParam(required = false) String reason) {

        try {
            TicketResponse response = adminTicketService.updateTicketStatus(ticketId, status, reason);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái vé thành công", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error updating ticket status", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể cập nhật trạng thái vé"));
        }
    }

    @GetMapping("/reports")
    @Operation(summary = "Generate ticket report", description = "Generate comprehensive ticket statistics and revenue report")
    public ResponseEntity<ApiResponse> generateTicketReport(
            @Parameter(description = "From date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "To date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        TicketReportResponse response = adminTicketService.generateTicketReport(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("Tạo báo cáo vé thành công", response));
    }

    @GetMapping("/expired")
    @Operation(summary = "Get expired tickets", description = "Get list of all expired tickets")
    public ResponseEntity<ApiResponse> getExpiredTickets() {

        List<TicketResponse> response = adminTicketService.getExpiredTickets();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách vé hết hạn thành công", response));
    }

    @PostMapping("/cleanup-expired")
    @Operation(summary = "Cleanup expired tickets", description = "Clean up expired tickets and release associated seats")
    public ResponseEntity<ApiResponse> cleanupExpiredTickets() {

        try {
            adminTicketService.cleanupExpiredTickets();
            return ResponseEntity.ok(ApiResponse.success("Dọn dẹp vé hết hạn thành công"));

        } catch (Exception e) {
            log.error("Error cleaning up expired tickets", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể dọn dẹp vé hết hạn"));
        }
    }
}
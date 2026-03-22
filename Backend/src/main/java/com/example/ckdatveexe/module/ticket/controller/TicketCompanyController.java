package com.example.ckdatveexe.module.ticket.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.module.ticket.dto.*;
import com.example.ckdatveexe.module.ticket.service.TicketService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@Tag(name = "Ticket Company API", description = "API quản lý vé cho nhà xe")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class TicketCompanyController {

    // TODO: Inject services when implemented
    // private final TicketService ticketService;

    private final TicketService ticketService;

    @GetMapping("/schedules/{scheduleId}/tickets")
    @Operation(summary = "Danh sách vé theo chuyến", description = "Lấy tất cả vé của một chuyến xe")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getScheduleTickets(
            @PathVariable Integer scheduleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status) {

        log.info("📋 [COMPANY] GET /api/company/schedules/{}/tickets - Get tickets by company {} (status: {})",
                scheduleId, userDetails.getId(), status);

        try {
            Pageable pageable = PageRequest.of(page, size,
                    Sort.Direction.fromString(sortDir), sortBy);

            // TODO: Implement service call
            // List<TicketResponse> tickets = ticketService.getScheduleTickets(scheduleId,
            // userDetails.getId(), status, pageable);

            // Mock response for now
            List<TicketResponse> mockResponse = List.of();

            log.info("✅ [COMPANY] 200 OK - Retrieved {} tickets for schedule: {}", mockResponse.size(), scheduleId);
            return ResponseEntity.ok(ApiResponse.<List<TicketResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách vé thành công")
                    .data(mockResponse)
                    .build());

        } catch (Exception e) {
            log.error("💥 [COMPANY] 500 INTERNAL_SERVER_ERROR - Error getting schedule tickets: {}", scheduleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<TicketResponse>>builder()
                            .success(false)
                            .message("Không thể lấy danh sách vé")
                            .build());
        }
    }

    @GetMapping("/schedules/{scheduleId}/seats")
    @Operation(summary = "Sơ đồ ghế của chuyến", description = "Xem sơ đồ ghế và trạng thái cho nhà xe")
    public ResponseEntity<ApiResponse<ScheduleSeatsResponse>> getScheduleSeats(
            @PathVariable Integer scheduleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("🎫 [COMPANY] GET /api/company/schedules/{}/seats - Get seat layout by company {}",
                scheduleId, userDetails.getId());

        try {
            // TODO: Implement service call
            // ScheduleSeatsResponse seats =
            // ticketService.getScheduleSeatsForCompany(scheduleId, userDetails.getId());

            // Mock response for now
            ScheduleSeatsResponse mockResponse = new ScheduleSeatsResponse();
            mockResponse.setScheduleId(scheduleId);

            log.info("✅ [COMPANY] 200 OK - Retrieved seat layout for schedule: {}", scheduleId);
            return ResponseEntity.ok(ApiResponse.<ScheduleSeatsResponse>builder()
                    .success(true)
                    .message("Lấy sơ đồ ghế thành công")
                    .data(mockResponse)
                    .build());

        } catch (Exception e) {
            log.error("💥 [COMPANY] 500 INTERNAL_SERVER_ERROR - Error getting seat layout: {}", scheduleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<ScheduleSeatsResponse>builder()
                            .success(false)
                            .message("Không thể lấy sơ đồ ghế")
                            .build());
        }
    }

    @PostMapping("/tickets/book-for-customer")
    @Operation(summary = "Đặt vé hộ khách", description = "Nhà xe đặt vé thay cho khách hàng")
    public ResponseEntity<ApiResponse<TicketResponse>> bookForCustomer(
            @Valid @RequestBody BookForCustomerRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info(
                "🎫 [COMPANY] POST /api/company/tickets/book-for-customer - Book for customer by company {} (schedule: {}, seat: {})",
                userDetails.getId(), request.getScheduleId(), request.getSeatId());

        try {
            // TODO: Implement service call
            // TicketResponse ticket = ticketService.bookForCustomer(request,
            // userDetails.getId());

            // Mock response for now
            TicketResponse mockResponse = new TicketResponse();
            mockResponse.setTicketId(1);
            mockResponse.setTicketCode("TK20260311002");
            mockResponse.setStatus("CONFIRMED");
            mockResponse.setPaymentRequired(false);

            log.info("✅ [COMPANY] 201 CREATED - Booked ticket for customer: {}", mockResponse.getTicketCode());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<TicketResponse>builder()
                            .success(true)
                            .message("Đặt vé hộ khách thành công")
                            .data(mockResponse)
                            .build());

        } catch (Exception e) {
            log.error("💥 [COMPANY] 400 BAD_REQUEST - Error booking for customer", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<TicketResponse>builder()
                            .success(false)
                            .message("Không thể đặt vé hộ khách")
                            .build());
        }
    }

    @GetMapping("/tickets")
    @Operation(summary = "Tất cả vé của công ty", description = "Lấy danh sách tất cả vé do công ty quản lý")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getCompanyTickets(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer scheduleId) {

        log.info("📋 [COMPANY] GET /api/company/tickets - Get all tickets by company {} (status: {}, schedule: {})",
                userDetails.getId(), status, scheduleId);

        try {
            Pageable pageable = PageRequest.of(page, size,
                    Sort.Direction.fromString(sortDir), sortBy);

            // TODO: Implement service call
            // List<TicketResponse> tickets =
            // ticketService.getCompanyTickets(userDetails.getId(), status, scheduleId,
            // pageable);

            // Mock response for now
            List<TicketResponse> mockResponse = List.of();

            log.info("✅ [COMPANY] 200 OK - Retrieved {} tickets for company: {}", mockResponse.size(),
                    userDetails.getId());
            return ResponseEntity.ok(ApiResponse.<List<TicketResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách vé thành công")
                    .data(mockResponse)
                    .build());

        } catch (Exception e) {
            log.error("💥 [COMPANY] 500 INTERNAL_SERVER_ERROR - Error getting company tickets", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<TicketResponse>>builder()
                            .success(false)
                            .message("Không thể lấy danh sách vé")
                            .build());
        }
    }

    @GetMapping("/tickets/{ticketId}")
    @Operation(summary = "Chi tiết vé", description = "Xem chi tiết vé của công ty")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketDetail(
            @PathVariable Integer ticketId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("🔍 [COMPANY] GET /api/company/tickets/{} - Get ticket detail by company {}",
                ticketId, userDetails.getId());

        try {
            // TODO: Implement service call
            // TicketResponse ticket = ticketService.getCompanyTicketDetail(ticketId,
            // userDetails.getId());

            // Mock response for now
            TicketResponse mockResponse = new TicketResponse();
            mockResponse.setTicketId(ticketId);

            log.info("✅ [COMPANY] 200 OK - Retrieved ticket detail: {}", ticketId);
            return ResponseEntity.ok(ApiResponse.<TicketResponse>builder()
                    .success(true)
                    .message("Lấy chi tiết vé thành công")
                    .data(mockResponse)
                    .build());

        } catch (Exception e) {
            log.error("💥 [COMPANY] 404 NOT_FOUND - Ticket not found: {}", ticketId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<TicketResponse>builder()
                            .success(false)
                            .message("Không tìm thấy vé")
                            .build());
        }
    }

    @PostMapping("/schedules/{scheduleId}/generate-tickets")
    @Operation(summary = "Phát hành vé cho chuyến", description = "Tạo vé cho tất cả ghế của chuyến xe")
    public ResponseEntity<ApiResponse<GenerateTicketsResponse>> generateTickets(
            @PathVariable Integer scheduleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("🎫 [COMPANY] POST /api/company/schedules/{}/generate-tickets - Generate tickets by company {}",
                scheduleId, userDetails.getId());

        try {
            // TODO: Implement service call
            // GenerateTicketsResponse result =
            // ticketService.generateTicketsForSchedule(scheduleId, userDetails.getId());

            // Mock response for now
            GenerateTicketsResponse mockResponse = new GenerateTicketsResponse();
            mockResponse.setScheduleId(scheduleId);
            mockResponse.setTotalSeats(40);
            mockResponse.setTicketsGenerated(40);
            mockResponse.setAvailableTickets(40);

            log.info("✅ [COMPANY] 201 CREATED - Generated {} tickets for schedule: {}",
                    mockResponse.getTicketsGenerated(), scheduleId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<GenerateTicketsResponse>builder()
                            .success(true)
                            .message("Phát hành vé thành công")
                            .data(mockResponse)
                            .build());

        } catch (Exception e) {
            log.error("💥 [COMPANY] 400 BAD_REQUEST - Error generating tickets for schedule: {}", scheduleId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<GenerateTicketsResponse>builder()
                            .success(false)
                            .message("Không thể phát hành vé")
                            .build());
        }
    }
}
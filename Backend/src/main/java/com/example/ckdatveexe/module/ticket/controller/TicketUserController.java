package com.example.ckdatveexe.module.ticket.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.module.ticket.dto.*;
import com.example.ckdatveexe.module.ticket.service.SeatLockService;
import com.example.ckdatveexe.module.ticket.service.TicketService;
import com.example.ckdatveexe.module.ticket.service.TicketCancellationService;
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
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "Ticket User API", description = "API đặt vé cho người dùng")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class TicketUserController {

        private final TicketService ticketService;
        private final SeatLockService seatLockService;
        private final TicketCancellationService ticketCancellationService;

        @PostMapping("/tickets/book")
        @Operation(summary = "Đặt vé", description = "Đặt vé và giữ ghế trong 5 phút chờ thanh toán")
        public ResponseEntity<ApiResponse<BookTicketResponse>> bookTicket(
                        @Valid @RequestBody BookTicketRequest request,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("🎫 [USER] POST /api/user/tickets/book - Book ticket for user {} (schedule: {}, seat: {})",
                                userDetails.getId(), request.getScheduleId(), request.getSeatId());

                try {
                        BookTicketResponse ticket = ticketService.bookTicket(request, userDetails.getId());

                        log.info("✅ [USER] 201 CREATED - Ticket booked successfully: {}", ticket.getTicketCode());
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.<BookTicketResponse>builder()
                                                        .success(true)
                                                        .message("Đặt vé thành công")
                                                        .data(ticket)
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("💥 [USER] 400 BAD_REQUEST - Invalid booking request: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<BookTicketResponse>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Error booking ticket", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<BookTicketResponse>builder()
                                                        .success(false)
                                                        .message("Không thể đặt vé")
                                                        .build());
                }
        }

        @GetMapping("/schedules/{scheduleId}/seats")
        @Operation(summary = "Xem sơ đồ ghế của chuyến", description = "Lấy danh sách ghế và trạng thái của một chuyến xe")
        public ResponseEntity<ApiResponse<ScheduleSeatsResponse>> getScheduleSeats(
                        @PathVariable Integer scheduleId) {

                log.info("🎫 [USER] GET /api/user/schedules/{}/seats - Get seat layout", scheduleId);

                try {
                        ScheduleSeatsResponse seats = ticketService.getScheduleSeats(scheduleId);

                        log.info("✅ [USER] 200 OK - Retrieved seat layout for schedule: {}", scheduleId);
                        return ResponseEntity.ok(ApiResponse.<ScheduleSeatsResponse>builder()
                                        .success(true)
                                        .message("Lấy sơ đồ ghế thành công")
                                        .data(seats)
                                        .build());

                } catch (Exception e) {
                        log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Error getting seat layout: {}", scheduleId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<ScheduleSeatsResponse>builder()
                                                        .success(false)
                                                        .message("Không thể lấy sơ đồ ghế")
                                                        .build());
                }
        }

        @PostMapping("/seats/lock")
        @Operation(summary = "Lock ghế", description = "Giữ ghế trong 10 phút để chuẩn bị đặt vé")
        public ResponseEntity<ApiResponse<SeatLockResponse>> lockSeat(
                        @Valid @RequestBody SeatLockRequest request,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("🔒 [USER] POST /api/user/seats/lock - Lock seat {} for schedule {} by user {}",
                                request.getSeatId(), request.getScheduleId(), userDetails.getId());

                try {
                        SeatLockResponse lockResponse = seatLockService.lockSeat(request, userDetails.getId());

                        log.info("✅ [USER] 201 CREATED - Seat locked successfully: lockId={}",
                                        lockResponse.getLockId());
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.<SeatLockResponse>builder()
                                                        .success(true)
                                                        .message("Giữ ghế thành công")
                                                        .data(lockResponse)
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("💥 [USER] 400 BAD_REQUEST - Invalid lock request: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<SeatLockResponse>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Error locking seat", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<SeatLockResponse>builder()
                                                        .success(false)
                                                        .message("Không thể giữ ghế")
                                                        .build());
                }
        }

        @GetMapping("/seats/lock/{lockId}/countdown")
        @Operation(summary = "Kiểm tra thời gian còn lại", description = "Lấy thời gian countdown của ghế đang được lock")
        public ResponseEntity<ApiResponse<SeatLockResponse>> getLockCountdown(
                        @PathVariable Integer lockId,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("⏰ [USER] GET /api/user/seats/lock/{}/countdown - Get countdown for user {}",
                                lockId, userDetails.getId());

                try {
                        SeatLockResponse countdown = seatLockService.getLockCountdown(lockId, userDetails.getId());

                        log.info("✅ [USER] 200 OK - Retrieved countdown for lock: {}", lockId);
                        return ResponseEntity.ok(ApiResponse.<SeatLockResponse>builder()
                                        .success(true)
                                        .message("Lấy thời gian còn lại thành công")
                                        .data(countdown)
                                        .build());

                } catch (Exception e) {
                        log.error("💥 [USER] 404 NOT_FOUND - Lock not found: {}", lockId, e);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<SeatLockResponse>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy thông tin giữ ghế")
                                                        .build());
                }
        }

        @PostMapping("/seats/unlock/{lockId}")
        @Operation(summary = "Hủy lock ghế", description = "Hủy việc giữ ghế trước khi hết hạn")
        public ResponseEntity<ApiResponse<Void>> unlockSeat(
                        @PathVariable Integer lockId,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("🔓 [USER] POST /api/user/seats/unlock/{} - Unlock seat by user {}",
                                lockId, userDetails.getId());

                try {
                        seatLockService.unlockSeat(lockId, userDetails.getId());

                        log.info("✅ [USER] 200 OK - Seat unlocked successfully: {}", lockId);
                        return ResponseEntity.ok(ApiResponse.<Void>builder()
                                        .success(true)
                                        .message("Hủy giữ ghế thành công")
                                        .build());

                } catch (Exception e) {
                        log.error("💥 [USER] 400 BAD_REQUEST - Error unlocking seat: {}", lockId, e);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Không thể hủy giữ ghế")
                                                        .build());
                }
        }

        @PostMapping("/tickets/create-from-lock")
        @Operation(summary = "Tạo vé từ lock", description = "Tạo vé từ ghế đã được lock")
        public ResponseEntity<ApiResponse<TicketResponse>> createTicketFromLock(
                        @Valid @RequestBody CreateTicketRequest request,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("🎫 [USER] POST /api/user/tickets/create-from-lock - Create ticket from lock {} by user {}",
                                request.getLockId(), userDetails.getId());

                try {
                        TicketResponse ticket = ticketService.createTicketFromLock(request, userDetails.getId());

                        log.info("✅ [USER] 201 CREATED - Ticket created successfully: {}", ticket.getTicketCode());
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.<TicketResponse>builder()
                                                        .success(true)
                                                        .message("Tạo vé thành công")
                                                        .data(ticket)
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [USER] 400 BAD_REQUEST - Error creating ticket from lock", e);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<TicketResponse>builder()
                                                        .success(false)
                                                        .message("Không thể tạo vé")
                                                        .build());
                }
        }

        @GetMapping("/tickets")
        @Operation(summary = "Danh sách vé của user", description = "Lấy danh sách vé đã đặt")
        public ResponseEntity<ApiResponse<List<TicketResponse>>> getUserTickets(
                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir,
                        @RequestParam(required = false) String status) {

                log.info("📋 [USER] GET /api/user/tickets - Get tickets for user {} (page: {}, size: {}, status: {})",
                                userDetails.getId(), page, size, status);

                try {
                        Pageable pageable = PageRequest.of(page, size,
                                        Sort.Direction.fromString(sortDir), sortBy);

                        List<TicketResponse> tickets = ticketService.getUserTickets(userDetails.getId(), status,
                                        pageable);

                        log.info("✅ [USER] 200 OK - Retrieved {} tickets for user: {}", tickets.size(),
                                        userDetails.getId());
                        return ResponseEntity.ok(ApiResponse.<List<TicketResponse>>builder()
                                        .success(true)
                                        .message("Lấy danh sách vé thành công")
                                        .data(tickets)
                                        .build());

                } catch (Exception e) {
                        log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Error getting user tickets", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<List<TicketResponse>>builder()
                                                        .success(false)
                                                        .message("Không thể lấy danh sách vé")
                                                        .build());
                }
        }

        @GetMapping("/tickets/{ticketId}")
        @Operation(summary = "Chi tiết vé", description = "Lấy thông tin chi tiết của một vé")
        public ResponseEntity<ApiResponse<TicketResponse>> getTicketDetail(
                        @PathVariable Integer ticketId,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("🔍 [USER] GET /api/user/tickets/{} - Get ticket detail for user {}",
                                ticketId, userDetails.getId());

                try {
                        TicketResponse ticket = ticketService.getTicketDetail(ticketId, userDetails.getId());

                        log.info("✅ [USER] 200 OK - Retrieved ticket detail: {}", ticketId);
                        return ResponseEntity.ok(ApiResponse.<TicketResponse>builder()
                                        .success(true)
                                        .message("Lấy chi tiết vé thành công")
                                        .data(ticket)
                                        .build());

                } catch (Exception e) {
                        log.error("💥 [USER] 404 NOT_FOUND - Ticket not found: {}", ticketId, e);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.<TicketResponse>builder()
                                                        .success(false)
                                                        .message("Không tìm thấy vé")
                                                        .build());
                }
        }

        @PostMapping("/tickets/{ticketId}/cancel")
        @Operation(summary = "Hủy vé", description = "Hủy vé trước giờ khởi hành hoặc hủy vé đang chờ thanh toán")
        public ResponseEntity<ApiResponse<TicketResponse>> cancelTicket(
                        @PathVariable Integer ticketId,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                log.info("❌ [USER] POST /api/user/tickets/{}/cancel - Cancel ticket by user {}",
                                ticketId, userDetails.getId());

                try {
                        // Try to cancel pending ticket first
                        try {
                                TicketResponse cancelledTicket = ticketService.cancelPendingTicket(ticketId,
                                                userDetails.getId());
                                log.info("✅ [USER] 200 OK - Pending ticket cancelled successfully: {}", ticketId);
                                return ResponseEntity.ok(ApiResponse.<TicketResponse>builder()
                                                .success(true)
                                                .message("Hủy vé chờ thanh toán thành công")
                                                .data(cancelledTicket)
                                                .build());
                        } catch (IllegalArgumentException e) {
                                // If not pending, try regular cancellation
                                TicketResponse cancelledTicket = ticketService.cancelTicket(ticketId,
                                                userDetails.getId());
                                log.info("✅ [USER] 200 OK - Ticket cancelled successfully: {}", ticketId);
                                return ResponseEntity.ok(ApiResponse.<TicketResponse>builder()
                                                .success(true)
                                                .message("Hủy vé thành công")
                                                .data(cancelledTicket)
                                                .build());
                        }

                } catch (Exception e) {
                        log.error("💥 [USER] 400 BAD_REQUEST - Error cancelling ticket: {}", ticketId, e);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<TicketResponse>builder()
                                                        .success(false)
                                                        .message(e.getMessage())
                                                        .build());
                }
        }

        @PostMapping("/tickets/cancel-confirmed")
        @Operation(summary = "Cancel confirmed ticket with refund", description = "Cancel a confirmed ticket and process refund according to cancellation policy", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<TicketCancellationResponse>> cancelConfirmedTicket(
                        @Valid @RequestBody TicketCancellationRequest request,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                TicketCancellationResponse response = ticketCancellationService.cancelTicket(request,
                                userDetails.getId());
                return ResponseEntity.ok(ApiResponse.success("Hủy vé thành công", response));
        }

        @PostMapping("/tickets/modify")
        @Operation(summary = "Modify ticket", description = "Modify ticket schedule or seats with additional fees", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<TicketModificationResponse>> modifyTicket(
                        @Valid @RequestBody TicketModificationRequest request,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                TicketModificationResponse response = ticketCancellationService.modifyTicket(request,
                                userDetails.getId());
                return ResponseEntity.ok(ApiResponse.success("Đổi vé thành công", response));
        }
}
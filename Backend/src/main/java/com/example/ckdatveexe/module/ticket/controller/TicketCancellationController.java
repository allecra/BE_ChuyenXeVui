package com.example.ckdatveexe.module.ticket.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.module.auth.dto.ApiResponse;
import com.example.ckdatveexe.module.ticket.dto.*;
import com.example.ckdatveexe.module.ticket.service.TicketCancellationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket Cancellation & Modification", description = "APIs for ticket cancellation and modification")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class TicketCancellationController {

    private final TicketCancellationService ticketCancellationService;

    @PostMapping("/cancel-confirmed")
    @Operation(summary = "Cancel confirmed ticket with refund", description = "Cancel a confirmed ticket and process refund according to cancellation policy")
    public ResponseEntity<ApiResponse> cancelConfirmedTicket(
            @Valid @RequestBody TicketCancellationRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("❌ [USER] POST /api/user/tickets/cancel-confirmed - Cancel ticket {} by user {}",
                request.getTicketId(), userDetails.getId());

        try {
            TicketCancellationResponse response = ticketCancellationService.cancelTicket(request, userDetails.getId());

            log.info("✅ [USER] 200 OK - Ticket cancelled successfully: {}", request.getTicketId());
            return ResponseEntity.ok(ApiResponse.success("Hủy vé thành công", response));

        } catch (IllegalArgumentException e) {
            log.error("💥 [USER] 400 BAD_REQUEST - Invalid cancellation request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Error cancelling ticket", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể hủy vé"));
        }
    }

    @PostMapping("/modify")
    @Operation(summary = "Modify ticket", description = "Modify ticket schedule or seats with additional fees")
    public ResponseEntity<ApiResponse> modifyTicket(
            @Valid @RequestBody TicketModificationRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("🔄 [USER] POST /api/user/tickets/modify - Modify ticket {} by user {}",
                request.getTicketId(), userDetails.getId());

        try {
            TicketModificationResponse response = ticketCancellationService.modifyTicket(request, userDetails.getId());

            log.info("✅ [USER] 200 OK - Ticket modified successfully: {}", request.getTicketId());
            return ResponseEntity.ok(ApiResponse.success("Đổi vé thành công", response));

        } catch (IllegalArgumentException e) {
            log.error("💥 [USER] 400 BAD_REQUEST - Invalid modification request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("💥 [USER] 500 INTERNAL_SERVER_ERROR - Error modifying ticket", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể đổi vé"));
        }
    }
}
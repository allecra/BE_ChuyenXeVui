package com.example.ckdatveexe.module.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response khi đặt vé thành công")
public class BookTicketResponse {

    @Schema(description = "ID vé", example = "1")
    private Integer ticketId;

    @Schema(description = "Mã vé", example = "TK20260317001")
    private String ticketCode;

    @Schema(description = "ID lịch trình", example = "1")
    private Integer scheduleId;

    @Schema(description = "Số ghế", example = "A5")
    private String seatNumber;

    @Schema(description = "Loại ghế", example = "VIP")
    private String seatType;

    @Schema(description = "Giá vé", example = "350000")
    private Double price;

    @Schema(description = "Trạng thái vé", example = "PENDING")
    private String status;

    @Schema(description = "Thông tin hành khách")
    private PassengerInfo passengerInfo;

    @Schema(description = "Thời gian tạo", example = "2026-03-17T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Hạn thanh toán", example = "2026-03-17T10:05:00")
    private LocalDateTime paymentDeadline;

    @Schema(description = "Số giây còn lại để thanh toán", example = "300")
    private Long remainingSeconds;

    @Schema(description = "Cần thanh toán", example = "true")
    private Boolean paymentRequired;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassengerInfo {
        @Schema(description = "Họ tên", example = "Nguyễn Văn A")
        private String fullName;

        @Schema(description = "Số điện thoại", example = "0123456789")
        private String phoneNumber;

        @Schema(description = "Email", example = "nguyenvana@example.com")
        private String email;

        @Schema(description = "CMND/CCCD", example = "123456789")
        private String idCard;
    }

    public static BookTicketResponse fromEntity(com.example.ckdatveexe.shared.entity.Ticket ticket) {
        BookTicketResponse response = new BookTicketResponse();
        response.setTicketId(ticket.getId());
        response.setTicketCode(ticket.getTicketCode());
        response.setScheduleId(ticket.getSchedule().getId());
        response.setSeatNumber(ticket.getSeat().getSeatNumber());
        response.setSeatType(ticket.getSeat().getSeatType().name());
        response.setPrice(ticket.getPrice());
        response.setStatus(ticket.getStatus().name());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setPaymentDeadline(ticket.getPaymentDeadline());
        response.setPaymentRequired(ticket.getStatus() == com.example.ckdatveexe.shared.entity.TicketStatus.PENDING);

        // Calculate remaining seconds
        if (ticket.getPaymentDeadline() != null) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (ticket.getPaymentDeadline().isAfter(now)) {
                response.setRemainingSeconds(
                        java.time.temporal.ChronoUnit.SECONDS.between(now, ticket.getPaymentDeadline()));
            } else {
                response.setRemainingSeconds(0L);
            }
        }

        // Set passenger info
        PassengerInfo passengerInfo = new PassengerInfo();
        passengerInfo.setFullName(ticket.getPassengerName());
        passengerInfo.setPhoneNumber(ticket.getPassengerPhone());
        passengerInfo.setEmail(ticket.getPassengerEmail());
        passengerInfo.setIdCard(ticket.getPassengerIdCard());
        response.setPassengerInfo(passengerInfo);

        return response;
    }
}
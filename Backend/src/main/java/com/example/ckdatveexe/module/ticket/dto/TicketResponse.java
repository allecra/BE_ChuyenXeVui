package com.example.ckdatveexe.module.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin vé")
public class TicketResponse {

    @Schema(description = "ID vé", example = "1")
    private Integer ticketId;

    @Schema(description = "Mã vé", example = "TK20260311001")
    private String ticketCode;

    @Schema(description = "ID lịch trình", example = "1")
    private Integer scheduleId;

    @Schema(description = "Thông tin chuyến xe")
    private TripInfo tripInfo;

    @Schema(description = "Số ghế", example = "A1")
    private String seatNumber;

    @Schema(description = "Loại ghế", example = "VIP")
    private String seatType;

    @Schema(description = "Giá vé", example = "350000")
    private Double price;

    @Schema(description = "Giá gốc", example = "350000")
    private Double originalPrice;

    @Schema(description = "Số tiền giảm giá", example = "0")
    private Double discountAmount;

    @Schema(description = "Trạng thái vé", example = "PENDING")
    private String status;

    @Schema(description = "Thông tin hành khách")
    private PassengerInfo passengerInfo;

    @Schema(description = "Cần thanh toán", example = "true")
    private Boolean paymentRequired;

    @Schema(description = "Hạn thanh toán", example = "2026-03-11T08:15:00")
    private LocalDateTime paymentDeadline;

    @Schema(description = "Thời gian tạo", example = "2026-03-11T08:05:00")
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripInfo {
        @Schema(description = "Điểm đi", example = "Hà Nội")
        private String departureStation;

        @Schema(description = "Điểm đến", example = "Hồ Chí Minh")
        private String arrivalStation;

        @Schema(description = "Thời gian khởi hành", example = "2026-03-11T08:00:00")
        private LocalDateTime departureTime;

        @Schema(description = "Thời gian đến", example = "2026-03-11T20:00:00")
        private LocalDateTime arrivalTime;

        @Schema(description = "Biển số xe", example = "29B-12345")
        private String busNumber;
    }

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

    public static TicketResponse fromEntity(com.example.ckdatveexe.shared.entity.Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setTicketId(ticket.getId());
        response.setTicketCode(ticket.getTicketCode());
        response.setScheduleId(ticket.getSchedule().getId());
        response.setSeatNumber(ticket.getSeat().getSeatNumber());
        response.setSeatType(ticket.getSeat().getSeatType().name());
        response.setPrice(ticket.getPrice());
        response.setOriginalPrice(ticket.getOriginalPrice());
        response.setDiscountAmount(ticket.getDiscountAmount());
        response.setStatus(ticket.getStatus().name());
        response.setPaymentDeadline(ticket.getPaymentDeadline());
        response.setCreatedAt(ticket.getCreatedAt());

        // Set payment required flag
        response.setPaymentRequired(ticket.getStatus() == com.example.ckdatveexe.shared.entity.TicketStatus.PENDING);

        // Set trip info
        TripInfo tripInfo = new TripInfo();
        tripInfo.setDepartureTime(ticket.getDepartureTime());
        tripInfo.setArrivalTime(ticket.getArrivalTime());

        // Get station names from schedule
        if (ticket.getSchedule().getRoute() != null) {
            tripInfo.setDepartureStation(ticket.getSchedule().getRoute().getDepartureStation().getName());
            tripInfo.setArrivalStation(ticket.getSchedule().getRoute().getArrivalStation().getName());
        }

        // Get bus number from seat's bus
        if (ticket.getSeat().getBus() != null) {
            tripInfo.setBusNumber(ticket.getSeat().getBus().getLicensePlate());
        }

        response.setTripInfo(tripInfo);

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
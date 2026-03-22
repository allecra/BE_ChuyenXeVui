package com.example.ckdatveexe.module.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request đặt vé - tạo booking tạm thời")
public class BookTicketRequest {

    @NotNull(message = "Schedule ID không được để trống")
    @Schema(description = "ID lịch trình", example = "1", required = true)
    private Integer scheduleId;

    @NotNull(message = "Seat ID không được để trống")
    @Schema(description = "ID ghế", example = "5", required = true)
    private Integer seatId;

    @Valid
    @Schema(description = "Thông tin hành khách (tùy chọn - nếu không có sẽ lấy từ thông tin tài khoản)", required = false)
    private PassengerInfo passengerInfo;

    @Schema(description = "Session ID của user", example = "user_session_123")
    private String sessionId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassengerInfo {
        @Schema(description = "Họ tên hành khách", example = "Nguyễn Văn A")
        private String fullName;

        @Schema(description = "Số điện thoại", example = "0123456789")
        private String phoneNumber;

        @Schema(description = "Email", example = "nguyenvana@example.com")
        private String email;

        @Schema(description = "CMND/CCCD", example = "123456789")
        private String idCard;
    }
}
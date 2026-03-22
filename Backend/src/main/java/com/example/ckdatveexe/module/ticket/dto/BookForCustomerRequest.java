package com.example.ckdatveexe.module.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request đặt vé hộ khách hàng (Company)")
public class BookForCustomerRequest {

    @NotNull(message = "ID lịch trình không được để trống")
    @Schema(description = "ID lịch trình", example = "1")
    private Integer scheduleId;

    @NotNull(message = "ID ghế không được để trống")
    @Schema(description = "ID ghế", example = "10")
    private Integer seatId;

    @Schema(description = "Thông tin khách hàng")
    private CustomerInfo customerInfo;

    @Schema(description = "Phương thức thanh toán", example = "CASH", allowableValues = { "CASH", "CARD", "TRANSFER" })
    private String paymentMethod = "CASH";

    @Schema(description = "Ghi chú", example = "Khách đặt tại quầy")
    private String notes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        @NotBlank(message = "Họ tên không được để trống")
        @Schema(description = "Họ tên khách hàng", example = "Trần Thị B")
        private String fullName;

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
        @Schema(description = "Số điện thoại", example = "0987654321")
        private String phoneNumber;

        @Email(message = "Email không hợp lệ")
        @Schema(description = "Email", example = "tranthib@example.com")
        private String email;

        @Schema(description = "CMND/CCCD", example = "987654321")
        private String idCard;
    }
}
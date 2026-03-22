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
@Schema(description = "Request tạo vé từ lock")
public class CreateTicketRequest {

    @NotNull(message = "ID lock không được để trống")
    @Schema(description = "ID của seat lock", example = "1")
    private Integer lockId;

    @Schema(description = "Thông tin hành khách")
    private PassengerInfo passengerInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassengerInfo {
        @NotBlank(message = "Họ tên không được để trống")
        @Schema(description = "Họ tên hành khách", example = "Nguyễn Văn A")
        private String fullName;

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
        @Schema(description = "Số điện thoại", example = "0123456789")
        private String phoneNumber;

        @Email(message = "Email không hợp lệ")
        @Schema(description = "Email", example = "nguyenvana@example.com")
        private String email;

        @Schema(description = "CMND/CCCD", example = "123456789")
        private String idCard;
    }
}
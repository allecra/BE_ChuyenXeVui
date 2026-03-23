package com.example.ckdatveexe.module.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @JsonProperty("currentPassword")
    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    private String currentPassword;

    @JsonProperty("newPassword")
    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, max = 50, message = "Mật khẩu phải có từ 6-50 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "Mật khẩu phải chứa ít nhất 1 chữ thường, 1 chữ hoa và 1 số")
    private String newPassword;

    @JsonProperty("confirmPassword")
    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
}
package com.example.ckdatveexe.module.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String firstName;

    @NotBlank(message = "Họ không được để trống")
    @Size(max = 100, message = "Họ không được vượt quá 100 ký tự")
    private String lastName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @Size(max = 11, message = "Số điện thoại không được vượt quá 11 ký tự")
    private String phone;
}

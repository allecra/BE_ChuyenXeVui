package com.example.ckdatveexe.module.buscompany.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusCompanyRegistrationRequest {
    @NotBlank(message = "Tên công ty không được để trống")
    @Size(max = 255, message = "Tên công ty không được vượt quá 255 ký tự")
    private String companyName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    private String image;

    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    private String descriptions;

    private String businessLicense; // Giấy phép kinh doanh
    private String address;
}
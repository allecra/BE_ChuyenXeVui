package com.example.ckdatveexe.module.buscompany.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @JsonProperty("company_name")
    private String companyName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("image")
    private String image;

    @JsonProperty("descriptions")
    private String descriptions;

    @JsonProperty("business_license")
    private String businessLicense;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    @JsonProperty("address")
    private String address;
}
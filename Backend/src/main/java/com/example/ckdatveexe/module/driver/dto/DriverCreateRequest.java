package com.example.ckdatveexe.module.driver.dto;

import com.example.ckdatveexe.shared.entity.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverCreateRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @NotBlank(message = "Số bằng lái không được để trống")
    @Size(max = 50, message = "Số bằng lái không được vượt quá 50 ký tự")
    private String licenseNumber;

    @Size(max = 20, message = "Loại bằng lái không được vượt quá 20 ký tự")
    private String licenseType;

    @Future(message = "Ngày hết hạn bằng lái phải là ngày trong tương lai")
    private LocalDate licenseExpiryDate;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    private String address;

    private DriverStatus status = DriverStatus.ACTIVE;

    private String notes;

    private Integer currentBusId;
}
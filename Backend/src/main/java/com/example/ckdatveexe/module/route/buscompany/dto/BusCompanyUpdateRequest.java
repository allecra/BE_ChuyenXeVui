package com.example.ckdatveexe.module.route.buscompany.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusCompanyUpdateRequest {
    @Size(max = 255, message = "Tên công ty không được vượt quá 255 ký tự")
    private String companyName;

    private String image;

    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    private String descriptions;
}
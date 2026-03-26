package com.example.ckdatveexe.module.buscompany.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusCompanyUpdateRequest {
    @Size(max = 255, message = "Tên công ty không được vượt quá 255 ký tự")
    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("image")
    private String image;

    @JsonProperty("descriptions")
    private String descriptions;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    @JsonProperty("address")
    private String address;
}
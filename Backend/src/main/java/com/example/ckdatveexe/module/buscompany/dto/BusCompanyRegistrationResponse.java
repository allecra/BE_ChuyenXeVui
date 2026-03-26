package com.example.ckdatveexe.module.buscompany.dto;

import com.example.ckdatveexe.shared.entity.RegistrationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusCompanyRegistrationResponse {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("image")
    private String image;

    @JsonProperty("descriptions")
    private String descriptions;

    @JsonProperty("business_license")
    private String businessLicense;

    @JsonProperty("address")
    private String address;

    @JsonProperty("status")
    private RegistrationStatus status;

    @JsonProperty("admin_notes")
    private String adminNotes;

    @JsonProperty("approved_by")
    private Integer approvedBy;

    @JsonProperty("approved_at")
    private LocalDateTime approvedAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
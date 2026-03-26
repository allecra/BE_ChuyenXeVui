package com.example.ckdatveexe.module.buscompany.dto;

import com.example.ckdatveexe.shared.entity.RegistrationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {
    @NotNull(message = "Trạng thái không được để trống")
    @JsonProperty("status")
    private RegistrationStatus status;

    @JsonProperty("admin_notes")
    private String adminNotes;
}
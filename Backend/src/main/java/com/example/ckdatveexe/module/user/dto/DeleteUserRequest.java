package com.example.ckdatveexe.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserRequest {

    private boolean hardDelete = false; // Default to soft delete

    @NotBlank(message = "Lý do xóa không được để trống")
    private String reason;

    private String notes;
}
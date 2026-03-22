package com.example.ckdatveexe.module.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScheduleCancelRequest {

    @NotBlank(message = "Lý do hủy lịch trình không được để trống")
    private String cancellationReason;

    private Boolean notifyPassengers = true;

    private Boolean processRefunds = true;
}
package com.example.ckdatveexe.module.schedule.dto;

import com.example.ckdatveexe.shared.entity.ScheduleBusStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateBusStatusRequest {

    @NotNull(message = "Trạng thái xe buýt không được để trống")
    private ScheduleBusStatus status;

    private String notes;
}
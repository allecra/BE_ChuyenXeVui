package com.example.ckdatveexe.module.schedule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignBusToScheduleRequest {

    @NotNull(message = "ID xe buýt không được để trống")
    private Integer busId;

    private String notes;
}
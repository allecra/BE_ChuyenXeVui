package com.example.ckdatveexe.module.driver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverAssignBusRequest {

    @NotNull(message = "ID xe buýt không được để trống")
    private Integer busId;

    private String notes;
}
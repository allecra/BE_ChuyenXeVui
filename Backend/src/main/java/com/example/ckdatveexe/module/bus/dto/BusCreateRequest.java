package com.example.ckdatveexe.module.bus.dto;

import com.example.ckdatveexe.shared.entity.BusType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusCreateRequest {

    @NotBlank(message = "Tên xe không được để trống")
    private String name;

    private String descriptions;

    @NotBlank(message = "Biển số xe không được để trống")
    private String licensePlate;

    @NotNull(message = "Sức chứa không được để trống")
    @Positive(message = "Sức chứa phải lớn hơn 0")
    private Integer capacity;

    @NotNull(message = "Loại xe không được để trống")
    private BusType busType;
}
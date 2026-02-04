package com.example.ckdatveexe.module.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để gán xe vào lịch trình")
public class ScheduleBusRequest {

    @NotEmpty(message = "Danh sách ID xe không được để trống")
    @Schema(description = "Danh sách ID xe cần gán vào lịch trình", example = "[1, 2, 3]")
    private List<Integer> busIds;
}
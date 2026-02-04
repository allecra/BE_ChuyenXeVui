package com.example.ckdatveexe.module.schedule.dto;

import com.example.ckdatveexe.shared.entity.ScheduleBusStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để cập nhật trạng thái xe trong lịch trình")
public class ScheduleBusStatusUpdateRequest {

    @NotNull(message = "Trạng thái không được để trống")
    @Schema(description = "Trạng thái mới của xe trong lịch trình", example = "INACTIVE")
    private ScheduleBusStatus status;
}
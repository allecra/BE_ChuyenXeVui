package com.example.ckdatveexe.module.station.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để cập nhật trạng thái xe trong bến")
public class BusStationStatusUpdateRequest {

    @NotNull(message = "ID xe không được để trống")
    @Schema(description = "ID của xe cần cập nhật trạng thái", example = "1")
    private Integer busId;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    @Schema(description = "Trạng thái hoạt động của xe tại bến (true = hoạt động, false = tạm ngưng)", example = "true")
    private Boolean isActive;

    @Schema(description = "Ghi chú về trạng thái xe tại bến", example = "Xe đang bảo trì định kỳ")
    private String notes;
}
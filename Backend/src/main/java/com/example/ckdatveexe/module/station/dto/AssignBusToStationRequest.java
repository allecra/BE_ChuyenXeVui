package com.example.ckdatveexe.module.station.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request gắn xe vào bến")
public class AssignBusToStationRequest {

    @NotNull(message = "ID bến xe không được để trống")
    @Schema(description = "ID bến xe", example = "1", required = true)
    private Integer stationId;

    @NotEmpty(message = "Danh sách xe không được để trống")
    @Schema(description = "Danh sách ID xe cần gắn vào bến", example = "[1, 2, 3]", required = true)
    private List<Integer> busIds;

    @Schema(description = "Có thay thế toàn bộ danh sách xe không (true = thay thế, false = thêm vào)", example = "false", defaultValue = "false")
    private boolean replaceAll = false;
}
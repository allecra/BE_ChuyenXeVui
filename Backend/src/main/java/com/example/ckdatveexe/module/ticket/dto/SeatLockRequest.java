package com.example.ckdatveexe.module.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để lock ghế")
public class SeatLockRequest {

    @NotNull(message = "ID lịch trình không được để trống")
    @Schema(description = "ID lịch trình", example = "1")
    private Integer scheduleId;

    @NotNull(message = "ID ghế không được để trống")
    @Schema(description = "ID ghế", example = "5")
    private Integer seatId;

    @Schema(description = "Session ID của user", example = "user_session_123")
    private String sessionId;
}
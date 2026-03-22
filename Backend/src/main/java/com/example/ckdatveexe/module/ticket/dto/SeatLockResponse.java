package com.example.ckdatveexe.module.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response khi lock ghế thành công")
public class SeatLockResponse {

    @Schema(description = "ID của lock", example = "1")
    private Integer lockId;

    @Schema(description = "ID ghế", example = "5")
    private Integer seatId;

    @Schema(description = "Số ghế", example = "A5")
    private String seatNumber;

    @Schema(description = "ID lịch trình", example = "1")
    private Integer scheduleId;

    @Schema(description = "Thời gian lock", example = "2026-03-11T08:05:00")
    private LocalDateTime lockedAt;

    @Schema(description = "Thời gian hết hạn", example = "2026-03-11T08:15:00")
    private LocalDateTime expiresAt;

    @Schema(description = "Số giây còn lại", example = "600")
    private Long remainingSeconds;

    @Schema(description = "Giá vé", example = "350000")
    private Double price;

    @Schema(description = "Trạng thái lock", example = "ACTIVE")
    private String status;

    public static SeatLockResponse fromEntity(com.example.ckdatveexe.shared.entity.SeatLock seatLock) {
        SeatLockResponse response = new SeatLockResponse();
        response.setLockId(seatLock.getId());
        response.setSeatId(seatLock.getSeat().getId());
        response.setSeatNumber(seatLock.getSeat().getSeatNumber());
        response.setScheduleId(seatLock.getSchedule().getId());
        response.setLockedAt(seatLock.getLockedAt());
        response.setExpiresAt(seatLock.getExpiresAt());
        response.setPrice(seatLock.getSeat().getPriceForSeatType());
        response.setStatus(seatLock.getStatus().name());

        // Calculate remaining seconds
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (seatLock.getExpiresAt().isAfter(now)) {
            response.setRemainingSeconds(java.time.temporal.ChronoUnit.SECONDS.between(now, seatLock.getExpiresAt()));
        } else {
            response.setRemainingSeconds(0L);
        }

        return response;
    }
}
package com.example.ckdatveexe.module.ticket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TicketModificationRequest {

    @NotNull(message = "ID vé không được để trống")
    private Integer ticketId;

    private Integer newScheduleId;

    private List<String> newSeatNumbers;

    private LocalDateTime newDepartureTime;

    private String modificationReason;
}
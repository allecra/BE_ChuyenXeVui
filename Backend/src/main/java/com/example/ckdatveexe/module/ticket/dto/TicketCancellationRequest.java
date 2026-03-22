package com.example.ckdatveexe.module.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketCancellationRequest {

    @NotNull(message = "ID vé không được để trống")
    private Integer ticketId;

    @NotBlank(message = "Lý do hủy vé không được để trống")
    private String cancellationReason;

    private String bankAccountNumber;

    private String bankAccountName;

    private String bankName;
}
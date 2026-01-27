package com.example.ckdatveexe.module.bus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteBusRequest {
    private String deletionReason;
    private boolean hardDelete = false; // false = soft delete, true = hard delete
}
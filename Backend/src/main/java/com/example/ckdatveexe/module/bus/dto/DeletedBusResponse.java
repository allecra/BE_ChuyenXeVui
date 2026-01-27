package com.example.ckdatveexe.module.bus.dto;

import com.example.ckdatveexe.shared.entity.BusType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeletedBusResponse {
    private Integer id;
    private Integer originalBusId;
    private String name;
    private String descriptions;
    private String licensePlate;
    private Integer capacity;
    private Integer companyId;
    private String companyName;
    private BusType busType;
    private LocalDateTime originalCreatedAt;
    private LocalDateTime originalUpdatedAt;
    private LocalDateTime deletedAt;
    private Integer deletedBy;
    private String deletionReason;
}
package com.example.ckdatveexe.module.bus.dto;

import com.example.ckdatveexe.shared.entity.BusStatus;
import com.example.ckdatveexe.shared.entity.BusType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusSearchRequest {
    private String keyword;
    private BusType busType;
    private BusStatus status;
    private Integer companyId;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}
package com.example.ckdatveexe.module.route.buscompany.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusCompanyResponse {
    private Integer id;
    private String companyName;
    private String image;
    private String descriptions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
package com.example.ckdatveexe.module.bus.dto;

import com.example.ckdatveexe.shared.entity.SeatStatus;
import com.example.ckdatveexe.shared.entity.SeatType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    private Integer id;
    private String seatNumber;
    private SeatType seatType;
    private SeatStatus status;
    private Double priceForSeatType;
    private Integer rowNumber;
    private Integer columnNumber;
}
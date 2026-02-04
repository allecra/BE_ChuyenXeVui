package com.example.ckdatveexe.module.seat.dto;

import com.example.ckdatveexe.shared.entity.SeatStatus;
import com.example.ckdatveexe.shared.entity.SeatType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatSearchRequest {

    private Integer busId;
    private SeatStatus status;
    private SeatType seatType;
    private Double minPrice;
    private Double maxPrice;
    private String seatNumber; // Tìm kiếm theo số ghế

    // Pagination
    private int page = 0;
    private int size = 10;
    private String sortBy = "seatNumber";
    private String sortDirection = "asc";
}
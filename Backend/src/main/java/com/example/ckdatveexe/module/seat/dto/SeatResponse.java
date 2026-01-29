package com.example.ckdatveexe.module.seat.dto;

import com.example.ckdatveexe.shared.entity.SeatStatus;
import com.example.ckdatveexe.shared.entity.SeatType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private Integer busId;
    private String busName;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor for USER response (limited info)
    public static SeatResponse forUser(Integer id, String seatNumber, SeatType seatType,
            SeatStatus status, Double priceForSeatType,
            Integer rowNumber, Integer columnNumber) {
        SeatResponse response = new SeatResponse();
        response.setId(id);
        response.setSeatNumber(seatNumber);
        response.setSeatType(seatType);
        response.setStatus(status);
        response.setPriceForSeatType(priceForSeatType);
        response.setRowNumber(rowNumber);
        response.setColumnNumber(columnNumber);
        return response;
    }
}
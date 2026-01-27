package com.example.ckdatveexe.module.bus.dto;

import com.example.ckdatveexe.shared.entity.BusStatus;
import com.example.ckdatveexe.shared.entity.BusType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusResponse {
    private Integer id;
    private String name;
    private String descriptions;
    private String licensePlate;
    private Integer capacity;
    private BusType busType;
    private BusStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thông tin nhà xe (cho admin)
    private Integer companyId;
    private String companyName;

    // Thông tin ghế
    private Integer totalSeats;
    private Integer availableSeats;
    private List<SeatResponse> seats;

    // Thông tin hình ảnh
    private List<String> images;

    // Thông tin đánh giá (cho user)
    private Double averageRating;
    private Integer totalReviews;
}
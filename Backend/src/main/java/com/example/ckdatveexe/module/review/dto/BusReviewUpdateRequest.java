package com.example.ckdatveexe.module.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BusReviewUpdateRequest {

    @Min(value = 1, message = "Điểm đánh giá phải từ 1-5")
    @Max(value = 5, message = "Điểm đánh giá phải từ 1-5")
    private Integer rating;

    @Size(min = 10, max = 1000, message = "Nội dung đánh giá phải từ 10-1000 ký tự")
    private String review;
}
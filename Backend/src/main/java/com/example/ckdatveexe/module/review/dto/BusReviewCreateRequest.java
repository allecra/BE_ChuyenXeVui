package com.example.ckdatveexe.module.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BusReviewCreateRequest {

    @NotNull(message = "ID xe buýt không được để trống")
    private Integer busId;

    @NotNull(message = "Điểm đánh giá không được để trống")
    @Min(value = 1, message = "Điểm đánh giá phải từ 1-5")
    @Max(value = 5, message = "Điểm đánh giá phải từ 1-5")
    private Integer rating;

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    @Size(min = 10, max = 1000, message = "Nội dung đánh giá phải từ 10-1000 ký tự")
    private String review;
}
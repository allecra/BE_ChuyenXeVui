package com.example.ckdatveexe.module.review.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BusReviewResponse {
    private Integer id;
    private Integer busId;
    private String busLicensePlate;
    private String busCompanyName;
    private Integer rating;
    private String review;
    private String userName;
    private String userEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean canEdit; // Can current user edit this review
    private Boolean canDelete; // Can current user delete this review
}
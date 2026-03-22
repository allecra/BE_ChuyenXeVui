package com.example.ckdatveexe.module.review.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class BusReviewStatsResponse {
    private Integer busId;
    private String busLicensePlate;
    private Integer totalReviews;
    private BigDecimal averageRating;
    private Map<Integer, Integer> ratingDistribution; // Rating (1-5) -> Count
    private Integer fiveStarCount;
    private Integer fourStarCount;
    private Integer threeStarCount;
    private Integer twoStarCount;
    private Integer oneStarCount;
    private BigDecimal fiveStarPercentage;
    private BigDecimal fourStarPercentage;
    private BigDecimal threeStarPercentage;
    private BigDecimal twoStarPercentage;
    private BigDecimal oneStarPercentage;
}
package com.example.ckdatveexe.module.buscompany.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusCompanyResponse {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("image")
    private String image;

    @JsonProperty("descriptions")
    private String descriptions;

    @JsonProperty("total_buses")
    private Integer totalBuses;

    @JsonProperty("average_rating")
    private Double averageRating;

    @JsonProperty("total_reviews")
    private Integer totalReviews;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
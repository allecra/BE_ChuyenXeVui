package com.example.ckdatveexe.module.banner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BannerCreateRequest {

    @NotBlank(message = "bannerUrl không được để trống")
    @JsonProperty("bannerUrl")
    private String bannerUrl;

    @NotBlank(message = "position không được để trống")
    @JsonProperty("position")
    private String position;
}
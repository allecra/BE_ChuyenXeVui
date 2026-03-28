package com.example.ckdatveexe.module.banner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BannerUpdateRequest {

    @JsonProperty("bannerUrl")
    private String bannerUrl;

    @JsonProperty("position")
    private String position;

    @JsonProperty("status")
    private String status;
}
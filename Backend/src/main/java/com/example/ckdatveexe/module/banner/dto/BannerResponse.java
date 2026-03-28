package com.example.ckdatveexe.module.banner.dto;

import com.example.ckdatveexe.shared.entity.BannerStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BannerResponse {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("bannerUrl")
    private String bannerUrl;

    @JsonProperty("position")
    private String position;

    @JsonProperty("status")
    private BannerStatus status;
}
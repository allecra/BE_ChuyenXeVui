package com.example.ckdatveexe.module.station.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request để tạo bến xe mới")
public class StationCreateRequest {

    @NotBlank(message = "Tên bến xe không được để trống")
    @Size(max = 255, message = "Tên bến xe không được vượt quá 255 ký tự")
    @Schema(description = "Tên bến xe", example = "Bến xe Miền Đông", required = true)
    private String name;

    @Schema(description = "URL hình ảnh bến xe", example = "https://example.com/station.jpg")
    private String image;

    @Schema(description = "URL hình nền bến xe", example = "https://example.com/wallpaper.jpg")
    private String wallpaper;

    @Schema(description = "Mô tả chi tiết về bến xe", example = "Bến xe lớn nhất khu vực miền Đông")
    private String descriptions;

    @NotBlank(message = "Địa chỉ bến xe không được để trống")
    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    @Schema(description = "Địa chỉ bến xe", example = "292 Đinh Bộ Lĩnh, Phường 26, Bình Thạnh, TP.HCM", required = true)
    private String location;
}
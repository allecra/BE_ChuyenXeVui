package com.example.ckdatveexe.module.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để tạo tuyến đường mới")
public class RouteCreateRequest {

    @NotBlank(message = "Tên tuyến không được để trống")
    @Size(max = 255, message = "Tên tuyến không được vượt quá 255 ký tự")
    @Schema(description = "Tên tuyến đường", example = "Hà Nội - Hồ Chí Minh")
    private String routeName;

    @NotBlank(message = "Điểm đầu không được để trống")
    @Size(max = 255, message = "Điểm đầu không được vượt quá 255 ký tự")
    @Schema(description = "Điểm xuất phát", example = "Hà Nội")
    private String startLocation;

    @NotBlank(message = "Điểm cuối không được để trống")
    @Size(max = 255, message = "Điểm cuối không được vượt quá 255 ký tự")
    @Schema(description = "Điểm đến", example = "Hồ Chí Minh")
    private String endLocation;

    @NotNull(message = "Giá tuyến không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá tuyến phải lớn hơn 0")
    @Schema(description = "Giá tuyến (VND)", example = "500000")
    private Double price;

    @NotNull(message = "Thời gian di chuyển không được để trống")
    @Min(value = 1, message = "Thời gian di chuyển phải lớn hơn 0 phút")
    @Schema(description = "Thời gian di chuyển (phút)", example = "1200")
    private Integer duration;

    @NotNull(message = "Khoảng cách không được để trống")
    @Min(value = 1, message = "Khoảng cách phải lớn hơn 0 km")
    @Schema(description = "Khoảng cách (km)", example = "1700")
    private Integer distance;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    @Schema(description = "Mô tả tuyến đường", example = "Tuyến cao tốc, đi qua các tỉnh miền Trung")
    private String descriptions;

    @NotNull(message = "ID bến xuất phát không được để trống")
    @Schema(description = "ID bến xe xuất phát", example = "1")
    private Integer departureStationId;

    @NotNull(message = "ID bến đến không được để trống")
    @Schema(description = "ID bến xe đến", example = "2")
    private Integer arrivalStationId;
}
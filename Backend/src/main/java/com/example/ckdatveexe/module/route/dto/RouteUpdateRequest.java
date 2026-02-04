package com.example.ckdatveexe.module.route.dto;

import com.example.ckdatveexe.shared.entity.RouteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để cập nhật thông tin tuyến đường")
public class RouteUpdateRequest {

    @Size(max = 255, message = "Tên tuyến không được vượt quá 255 ký tự")
    @Schema(description = "Tên tuyến đường", example = "Hà Nội - Hồ Chí Minh (Cao tốc)")
    private String routeName;

    @Size(max = 255, message = "Điểm đầu không được vượt quá 255 ký tự")
    @Schema(description = "Điểm xuất phát", example = "Hà Nội")
    private String startLocation;

    @Size(max = 255, message = "Điểm cuối không được vượt quá 255 ký tự")
    @Schema(description = "Điểm đến", example = "Hồ Chí Minh")
    private String endLocation;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá tuyến phải lớn hơn 0")
    @Schema(description = "Giá tuyến (VND)", example = "550000")
    private Double price;

    @Min(value = 1, message = "Thời gian di chuyển phải lớn hơn 0 phút")
    @Schema(description = "Thời gian di chuyển (phút)", example = "1100")
    private Integer duration;

    @Min(value = 1, message = "Khoảng cách phải lớn hơn 0 km")
    @Schema(description = "Khoảng cách (km)", example = "1650")
    private Integer distance;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    @Schema(description = "Mô tả tuyến đường", example = "Tuyến cao tốc mới, thời gian di chuyển nhanh hơn")
    private String descriptions;

    @Schema(description = "Trạng thái tuyến đường", example = "ACTIVE")
    private RouteStatus status;

    @Schema(description = "ID bến xe xuất phát", example = "1")
    private Integer departureStationId;

    @Schema(description = "ID bến xe đến", example = "2")
    private Integer arrivalStationId;
}
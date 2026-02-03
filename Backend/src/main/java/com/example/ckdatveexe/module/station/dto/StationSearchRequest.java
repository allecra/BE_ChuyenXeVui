package com.example.ckdatveexe.module.station.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request tìm kiếm bến xe")
public class StationSearchRequest {

    @Schema(description = "Từ khóa tìm kiếm (tên hoặc địa chỉ)", example = "Miền Đông")
    private String keyword;

    @Schema(description = "Địa chỉ cụ thể", example = "TP.HCM")
    private String location;

    @Schema(description = "Số trang (bắt đầu từ 0)", example = "0", defaultValue = "0")
    private int page = 0;

    @Schema(description = "Số lượng bản ghi mỗi trang", example = "10", defaultValue = "10")
    private int size = 10;

    @Schema(description = "Trường sắp xếp", example = "name", defaultValue = "name")
    private String sortBy = "name";

    @Schema(description = "Hướng sắp xếp (asc/desc)", example = "asc", defaultValue = "asc")
    private String sortDirection = "asc";
}
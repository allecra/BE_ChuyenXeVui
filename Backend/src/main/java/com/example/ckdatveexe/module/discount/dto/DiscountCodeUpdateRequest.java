package com.example.ckdatveexe.module.discount.dto;

import com.example.ckdatveexe.shared.entity.DiscountCodeStatus;
import com.example.ckdatveexe.shared.entity.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DiscountCodeUpdateRequest {

    @Size(max = 100, message = "Tên mã giảm giá không được quá 100 ký tự")
    private String name;

    private String description;

    private DiscountType type;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm giá phải lớn hơn 0")
    private BigDecimal value;

    @DecimalMin(value = "0.0", message = "Số tiền giảm tối đa phải >= 0")
    private BigDecimal maxDiscountAmount;

    @DecimalMin(value = "0.0", message = "Số tiền đơn hàng tối thiểu phải >= 0")
    private BigDecimal minOrderAmount;

    @Min(value = 0, message = "Giới hạn sử dụng phải >= 0")
    private Integer usageLimit;

    @Min(value = 1, message = "Giới hạn sử dụng mỗi user phải >= 1")
    private Integer usageLimitPerUser;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private DiscountCodeStatus status;

    private List<Integer> applicableRoutes;

    private List<Integer> applicableBusCompanies;

    private Boolean isFirstTimeUserOnly;

    @AssertTrue(message = "Ngày kết thúc phải sau ngày bắt đầu")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }
}
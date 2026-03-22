package com.example.ckdatveexe.module.discount.dto;

import com.example.ckdatveexe.shared.entity.DiscountScope;
import com.example.ckdatveexe.shared.entity.DiscountStatus;
import com.example.ckdatveexe.shared.entity.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCodeCreateRequest {

    @NotBlank(message = "Mã giảm giá không được để trống")
    @Size(max = 50, message = "Mã giảm giá không được vượt quá 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Mã giảm giá chỉ được chứa chữ hoa, số, dấu gạch dưới và gạch ngang")
    private String code;

    @NotBlank(message = "Tên mã giảm giá không được để trống")
    @Size(max = 200, message = "Tên mã giảm giá không được vượt quá 200 ký tự")
    private String name;

    private String description;

    @NotNull(message = "Loại giảm giá không được để trống")
    private DiscountType discountType;

    @NotNull(message = "Giá trị giảm giá không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm giá phải lớn hơn 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0", message = "Số tiền giảm tối đa phải lớn hơn hoặc bằng 0")
    private BigDecimal maxDiscountAmount;

    @DecimalMin(value = "0", message = "Số tiền đơn hàng tối thiểu phải lớn hơn hoặc bằng 0")
    private BigDecimal minOrderAmount;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Future(message = "Ngày bắt đầu phải là ngày trong tương lai")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;

    @NotNull(message = "Giới hạn sử dụng không được để trống")
    @Min(value = 1, message = "Giới hạn sử dụng phải lớn hơn 0")
    private Integer usageLimit;

    @Min(value = 1, message = "Giới hạn sử dụng mỗi người phải lớn hơn 0")
    private Integer usageLimitPerUser = 1;

    private DiscountStatus status = DiscountStatus.ACTIVE;

    @NotNull(message = "Phạm vi áp dụng không được để trống")
    private DiscountScope scope;

    private Integer busCompanyId;

    private Integer routeId;

    private String terms;

    // Custom validation
    @AssertTrue(message = "Ngày kết thúc phải sau ngày bắt đầu")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return endDate.isAfter(startDate);
    }

    @AssertTrue(message = "Giá trị giảm giá theo phần trăm không được vượt quá 100%")
    public boolean isValidPercentageDiscount() {
        if (discountType == null || discountValue == null) {
            return true; // Let @NotNull handle null validation
        }
        if (discountType == DiscountType.PERCENTAGE) {
            return discountValue.compareTo(BigDecimal.valueOf(100)) <= 0;
        }
        return true;
    }
}
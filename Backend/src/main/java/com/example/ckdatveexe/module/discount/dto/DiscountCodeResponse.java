package com.example.ckdatveexe.module.discount.dto;

import com.example.ckdatveexe.shared.entity.DiscountCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCodeResponse {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer usageLimitPerUser;
    private String status;
    private String scope;
    private String terms;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Company info
    private Integer busCompanyId;
    private String busCompanyName;

    // Route info
    private Integer routeId;
    private String routeName;

    // Creator info
    private Integer createdById;
    private String createdByName;

    // Calculated fields
    private boolean isActive;
    private boolean isExpired;
    private Integer remainingUsage;

    public static DiscountCodeResponse fromEntity(DiscountCode discountCode) {
        DiscountCodeResponse response = new DiscountCodeResponse();
        response.setId(discountCode.getId());
        response.setCode(discountCode.getCode());
        response.setName(discountCode.getName());
        response.setDescription(discountCode.getDescription());
        response.setDiscountType(discountCode.getDiscountType().name());
        response.setDiscountValue(discountCode.getDiscountValue());
        response.setMaxDiscountAmount(discountCode.getMaxDiscountAmount());
        response.setMinOrderAmount(discountCode.getMinOrderAmount());
        response.setStartDate(discountCode.getStartDate());
        response.setEndDate(discountCode.getEndDate());
        response.setUsageLimit(discountCode.getUsageLimit());
        response.setUsedCount(discountCode.getUsedCount());
        response.setUsageLimitPerUser(discountCode.getUsageLimitPerUser());
        response.setStatus(discountCode.getStatus().name());
        response.setScope(discountCode.getScope().name());
        response.setTerms(discountCode.getTerms());
        response.setCreatedAt(discountCode.getCreatedAt());
        response.setUpdatedAt(discountCode.getUpdatedAt());

        // Set company info if exists
        if (discountCode.getBusCompany() != null) {
            response.setBusCompanyId(discountCode.getBusCompany().getId());
            response.setBusCompanyName(discountCode.getBusCompany().getCompanyName());
        }

        // Set route info if exists
        if (discountCode.getRoute() != null) {
            response.setRouteId(discountCode.getRoute().getId());
            response.setRouteName(discountCode.getRoute().getDepartureLocation() + " - " +
                    discountCode.getRoute().getArrivalLocation());
        }

        // Set creator info if exists
        if (discountCode.getCreatedBy() != null) {
            response.setCreatedById(discountCode.getCreatedBy().getId());
            response.setCreatedByName(discountCode.getCreatedBy().getFirstName() + " " +
                    discountCode.getCreatedBy().getLastName());
        }

        // Set calculated fields
        response.setActive(discountCode.isActive());
        response.setExpired(LocalDateTime.now().isAfter(discountCode.getEndDate()));
        response.setRemainingUsage(discountCode.getUsageLimit() - discountCode.getUsedCount());

        return response;
    }
}
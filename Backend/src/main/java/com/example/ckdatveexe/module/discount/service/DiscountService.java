package com.example.ckdatveexe.module.discount.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.discount.dto.*;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {

    private final DiscountCodeRepository discountCodeRepository;
    private final DiscountUsageRepository discountUsageRepository;
    private final BusCompanyRepository busCompanyRepository;
    private final RouteRepository routeRepository;
    private final UserRepository userRepository;

    // ===== USER METHODS =====

    public List<DiscountCodeResponse> getActiveDiscounts(Integer companyId) {
        log.info("🎫 Getting active discounts for company: {}", companyId);

        LocalDateTime now = LocalDateTime.now();
        List<DiscountCode> discounts;

        if (companyId != null) {
            discounts = discountCodeRepository.findActiveDiscountsForCompany(now, companyId);
        } else {
            discounts = discountCodeRepository.findActiveDiscounts(now);
        }

        log.info("✅ Found {} active discounts", discounts.size());
        return discounts.stream()
                .map(DiscountCodeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public DiscountCodeResponse getDiscountByCode(String code) {
        log.info("🎫 Getting discount by code: {}", code);

        DiscountCode discount = discountCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không tồn tại"));

        log.info("✅ Discount found: {}", discount.getName());
        return DiscountCodeResponse.fromEntity(discount);
    }

    @Transactional
    public ApplyDiscountResponse applyDiscount(ApplyDiscountRequest request, Integer userId) {
        log.info("🎫 Applying discount code: {} for user: {}", request.getDiscountCode(), userId);

        try {
            // 1. Find discount code
            DiscountCode discount = discountCodeRepository.findByCode(request.getDiscountCode())
                    .orElse(null);

            if (discount == null) {
                return ApplyDiscountResponse.error("Mã giảm giá không tồn tại");
            }

            // 2. Check if discount is active
            if (!discount.isActive()) {
                return ApplyDiscountResponse.error("Mã giảm giá đã hết hạn hoặc không còn hiệu lực");
            }

            // 3. Check minimum order amount
            if (discount.getMinOrderAmount() != null &&
                    request.getOrderAmount().compareTo(discount.getMinOrderAmount()) < 0) {
                return ApplyDiscountResponse.error(
                        String.format("Đơn hàng tối thiểu %s để sử dụng mã này",
                                discount.getMinOrderAmount()));
            }

            // 4. Check scope restrictions
            if (discount.getScope() == DiscountScope.COMPANY &&
                    (request.getCompanyId() == null
                            || !discount.getBusCompany().getId().equals(request.getCompanyId()))) {
                return ApplyDiscountResponse.error("Mã giảm giá chỉ áp dụng cho nhà xe cụ thể");
            }

            if (discount.getScope() == DiscountScope.ROUTE &&
                    (request.getRouteId() == null || !discount.getRoute().getId().equals(request.getRouteId()))) {
                return ApplyDiscountResponse.error("Mã giảm giá chỉ áp dụng cho tuyến đường cụ thể");
            }

            // 5. Check user usage limit
            long userUsageCount = discountUsageRepository.countByUserIdAndDiscountCodeId(userId, discount.getId());
            if (userUsageCount >= discount.getUsageLimitPerUser()) {
                return ApplyDiscountResponse.error("Bạn đã sử dụng hết lượt cho mã giảm giá này");
            }

            // 6. Calculate discount amount
            BigDecimal discountAmount = discount.calculateDiscount(request.getOrderAmount());

            log.info("✅ Discount applied successfully: {} VND", discountAmount);
            return ApplyDiscountResponse.success(
                    discount.getId(),
                    discount.getCode(),
                    discount.getName(),
                    request.getOrderAmount(),
                    discountAmount,
                    discount.getDiscountType().name(),
                    discount.getDiscountValue());

        } catch (Exception e) {
            log.error("💥 Error applying discount: {}", e.getMessage());
            return ApplyDiscountResponse.error("Có lỗi xảy ra khi áp dụng mã giảm giá");
        }
    }

    @Transactional
    public void recordDiscountUsage(Integer discountCodeId, Integer userId, Integer ticketId,
            BigDecimal orderAmount, BigDecimal discountAmount) {
        log.info("📝 Recording discount usage: discountId={}, userId={}, ticketId={}",
                discountCodeId, userId, ticketId);

        DiscountCode discount = discountCodeRepository.findById(discountCodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không tồn tại"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // Create usage record
        DiscountUsage usage = new DiscountUsage();
        usage.setDiscountCode(discount);
        usage.setUser(user);
        usage.setOrderAmount(orderAmount);
        usage.setDiscountAmount(discountAmount);

        if (ticketId != null) {
            // Set ticket reference if available
            // usage.setTicket(ticketRepository.findById(ticketId).orElse(null));
        }

        discountUsageRepository.save(usage);

        // Update discount used count
        discount.setUsedCount(discount.getUsedCount() + 1);

        // Check if discount should be marked as used up
        if (discount.getUsedCount() >= discount.getUsageLimit()) {
            discount.setStatus(DiscountStatus.USED_UP);
        }

        discountCodeRepository.save(discount);

        log.info("✅ Discount usage recorded successfully");
    }

    // ===== COMPANY & ADMIN METHODS =====

    public Page<DiscountCodeResponse> getDiscounts(DiscountStatus status, DiscountScope scope,
            String keyword, Integer companyId, Pageable pageable) {
        log.info("🎫 Getting discounts - Status: {}, Scope: {}, Keyword: {}, Company: {}",
                status, scope, keyword, companyId);

        Page<DiscountCode> discounts;

        if (keyword != null && !keyword.trim().isEmpty()) {
            if (status != null) {
                discounts = discountCodeRepository.searchByKeywordAndStatus(keyword.trim(), status, pageable);
            } else {
                discounts = discountCodeRepository.searchByKeyword(keyword.trim(), pageable);
            }
        } else if (companyId != null) {
            if (status != null) {
                discounts = discountCodeRepository.findByBusCompanyIdAndStatus(companyId, status, pageable);
            } else {
                discounts = discountCodeRepository.findByBusCompanyId(companyId, pageable);
            }
        } else if (status != null) {
            discounts = discountCodeRepository.findByStatus(status, pageable);
        } else if (scope != null) {
            discounts = discountCodeRepository.findByScope(scope, pageable);
        } else {
            discounts = discountCodeRepository.findAll(pageable);
        }

        log.info("✅ Retrieved {} discounts", discounts.getTotalElements());
        return discounts.map(DiscountCodeResponse::fromEntity);
    }

    public DiscountCodeResponse getDiscountById(Integer id) {
        log.info("🎫 Getting discount by ID: {}", id);

        DiscountCode discount = discountCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không tồn tại"));

        log.info("✅ Discount retrieved successfully: {}", discount.getName());
        return DiscountCodeResponse.fromEntity(discount);
    }

    @Transactional
    public DiscountCodeResponse createDiscount(DiscountCodeCreateRequest request, Integer createdById) {
        log.info("🎫 Creating new discount: {}", request.getName());

        // Validate unique code
        if (discountCodeRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Mã giảm giá đã tồn tại trong hệ thống");
        }

        // Create discount entity
        DiscountCode discount = new DiscountCode();
        discount.setCode(request.getCode());
        discount.setName(request.getName());
        discount.setDescription(request.getDescription());
        discount.setDiscountType(request.getDiscountType());
        discount.setDiscountValue(request.getDiscountValue());
        discount.setMaxDiscountAmount(request.getMaxDiscountAmount());
        discount.setMinOrderAmount(request.getMinOrderAmount());
        discount.setStartDate(request.getStartDate());
        discount.setEndDate(request.getEndDate());
        discount.setUsageLimit(request.getUsageLimit());
        discount.setUsageLimitPerUser(request.getUsageLimitPerUser());
        discount.setStatus(request.getStatus());
        discount.setScope(request.getScope());
        discount.setTerms(request.getTerms());

        // Set creator
        User creator = userRepository.findById(createdById)
                .orElseThrow(() -> new ResourceNotFoundException("Người tạo không tồn tại"));
        discount.setCreatedBy(creator);

        // Set company if scope is COMPANY
        if (request.getScope() == DiscountScope.COMPANY && request.getBusCompanyId() != null) {
            BusCompany company = busCompanyRepository.findById(request.getBusCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nhà xe không tồn tại"));
            discount.setBusCompany(company);
        }

        // Set route if scope is ROUTE
        if (request.getScope() == DiscountScope.ROUTE && request.getRouteId() != null) {
            Route route = routeRepository.findById(request.getRouteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tuyến đường không tồn tại"));
            discount.setRoute(route);
        }

        discount = discountCodeRepository.save(discount);

        log.info("✅ Discount created successfully: {} (ID: {})", discount.getName(), discount.getId());
        return DiscountCodeResponse.fromEntity(discount);
    }

    @Transactional
    public DiscountCodeResponse updateDiscount(Integer id, DiscountCodeCreateRequest request) {
        log.info("📝 Updating discount: {}", id);

        DiscountCode discount = discountCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không tồn tại"));

        // Validate unique code
        if (!discount.getCode().equals(request.getCode())) {
            if (discountCodeRepository.existsByCodeAndIdNot(request.getCode(), id)) {
                throw new IllegalArgumentException("Mã giảm giá đã tồn tại trong hệ thống");
            }
        }

        // Update discount information
        discount.setCode(request.getCode());
        discount.setName(request.getName());
        discount.setDescription(request.getDescription());
        discount.setDiscountType(request.getDiscountType());
        discount.setDiscountValue(request.getDiscountValue());
        discount.setMaxDiscountAmount(request.getMaxDiscountAmount());
        discount.setMinOrderAmount(request.getMinOrderAmount());
        discount.setStartDate(request.getStartDate());
        discount.setEndDate(request.getEndDate());
        discount.setUsageLimit(request.getUsageLimit());
        discount.setUsageLimitPerUser(request.getUsageLimitPerUser());
        discount.setStatus(request.getStatus());
        discount.setScope(request.getScope());
        discount.setTerms(request.getTerms());

        // Update company if scope is COMPANY
        if (request.getScope() == DiscountScope.COMPANY && request.getBusCompanyId() != null) {
            BusCompany company = busCompanyRepository.findById(request.getBusCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nhà xe không tồn tại"));
            discount.setBusCompany(company);
        } else {
            discount.setBusCompany(null);
        }

        // Update route if scope is ROUTE
        if (request.getScope() == DiscountScope.ROUTE && request.getRouteId() != null) {
            Route route = routeRepository.findById(request.getRouteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tuyến đường không tồn tại"));
            discount.setRoute(route);
        } else {
            discount.setRoute(null);
        }

        discount = discountCodeRepository.save(discount);

        log.info("✅ Discount updated successfully: {}", discount.getName());
        return DiscountCodeResponse.fromEntity(discount);
    }

    @Transactional
    public void deleteDiscount(Integer id) {
        log.info("🗑️ Deleting discount: {}", id);

        DiscountCode discount = discountCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không tồn tại"));

        // Check if discount has been used
        long usageCount = discountUsageRepository.getTotalUsageCountByDiscountCodeId(id);
        if (usageCount > 0) {
            throw new IllegalArgumentException("Không thể xóa mã giảm giá đã được sử dụng");
        }

        discountCodeRepository.delete(discount);

        log.info("✅ Discount deleted successfully: {}", discount.getName());
    }

    @Transactional
    public DiscountCodeResponse updateDiscountStatus(Integer id, DiscountStatus status) {
        log.info("📝 Updating discount status: {} to {}", id, status);

        DiscountCode discount = discountCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không tồn tại"));

        discount.setStatus(status);
        discount = discountCodeRepository.save(discount);

        log.info("✅ Discount status updated successfully: {}", discount.getName());
        return DiscountCodeResponse.fromEntity(discount);
    }

    public List<DiscountCodeResponse> getExpiringDiscounts() {
        log.info("⚠️ Getting expiring discounts");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysFromNow = now.plusDays(7);

        List<DiscountCode> discounts = discountCodeRepository.findExpiringDiscounts(now, sevenDaysFromNow);

        log.info("✅ Found {} expiring discounts", discounts.size());
        return discounts.stream()
                .map(DiscountCodeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public long getDiscountCountByStatus(DiscountStatus status) {
        if (status != null) {
            return discountCodeRepository.countByStatus(status);
        } else {
            return discountCodeRepository.count();
        }
    }

    public long getDiscountCountByScope(DiscountScope scope) {
        return discountCodeRepository.countByScope(scope);
    }
}
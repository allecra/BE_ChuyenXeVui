package com.example.ckdatveexe.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "discount_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType type = DiscountType.PERCENTAGE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit = 0;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @Column(name = "usage_limit_per_user", nullable = false)
    private Integer usageLimitPerUser = 1;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountCodeStatus status = DiscountCodeStatus.ACTIVE;

    @Column(name = "applicable_routes", columnDefinition = "JSON")
    private String applicableRoutes;

    @Column(name = "applicable_bus_companies", columnDefinition = "JSON")
    private String applicableBusCompanies;

    @Column(name = "is_first_time_user_only", nullable = false)
    private Boolean isFirstTimeUserOnly = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "discountCode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    @OneToMany(mappedBy = "discountCode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiscountUsage> discountUsages;
}
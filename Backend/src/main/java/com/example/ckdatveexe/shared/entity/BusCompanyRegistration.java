package com.example.ckdatveexe.shared.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bus_company_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusCompanyRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Integer id;

    @Column(name = "company_name", nullable = false, length = 255)
    @JsonProperty("company_name")
    private String companyName;

    @Column(nullable = false, length = 255)
    @JsonProperty("email")
    private String email;

    @Column(name = "phone_number", nullable = false, length = 20)
    @JsonProperty("phone_number")
    private String phoneNumber;

    @Column(length = 500)
    @JsonProperty("image")
    private String image;

    @Column(columnDefinition = "LONGTEXT")
    @JsonProperty("descriptions")
    private String descriptions;

    @Column(name = "business_license", length = 100)
    @JsonProperty("business_license")
    private String businessLicense;

    @Column(length = 500)
    @JsonProperty("address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @JsonProperty("status")
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    @JsonProperty("admin_notes")
    private String adminNotes;

    @Column(name = "approved_by")
    @JsonProperty("approved_by")
    private Integer approvedBy; // ID của admin xác thực

    @Column(name = "approved_at")
    @JsonProperty("approved_at")
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
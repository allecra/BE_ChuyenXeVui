package com.example.ckdatveexe.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "deleted_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "original_user_id", nullable = false)
    private Integer originalUserId;

    @Column(nullable = false)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column
    private String phone;  // không bắt buộc not null

    @Column(nullable = false)
    private String role;  // lưu String: "ROLE_USER" hoặc "ROLE_USER,ROLE_ADMIN"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "original_created_at")
    private LocalDateTime originalCreatedAt;

    @Column(name = "original_updated_at")
    private LocalDateTime originalUpdatedAt;

    @CreationTimestamp
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Integer deletedBy;

    @Column(name = "deletion_reason", columnDefinition = "TEXT")
    private String deletionReason;
}
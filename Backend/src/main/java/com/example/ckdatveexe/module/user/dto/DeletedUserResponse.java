package com.example.ckdatveexe.module.user.dto;

import com.example.ckdatveexe.shared.entity.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeletedUserResponse {
    private Integer id;
    private Integer originalUserId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private UserStatus status;
    private LocalDateTime originalCreatedAt;
    private LocalDateTime originalUpdatedAt;
    private LocalDateTime deletedAt;
    private Integer deletedBy;
    private String deletionReason;
}
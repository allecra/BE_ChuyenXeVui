package com.example.ckdatveexe.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletedUserResponse {
    private Integer id;
    private Integer originalUserId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String idCard;
    private String busCompanyName;
    private String deleteReason;
    private String deleteNotes;
    private String deletedByAdminName;
    private Integer deletedByAdminId;
    private LocalDateTime deletedAt;
    private LocalDateTime originalCreatedAt;
}
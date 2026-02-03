package com.example.ckdatveexe.module.user.dto;

import com.example.ckdatveexe.shared.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserStatus status;
}
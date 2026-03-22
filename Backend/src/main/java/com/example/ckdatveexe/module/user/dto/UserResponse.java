package com.example.ckdatveexe.module.user.dto;

import com.example.ckdatveexe.shared.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String idCard;
    private String status;
    private String busCompanyName;
    private Integer busCompanyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFirstName() + " " + user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setIdCard(user.getIdCard());
        response.setStatus(user.getStatus().name());

        if (user.getBusCompany() != null) {
            response.setBusCompanyName(user.getBusCompany().getCompanyName());
            response.setBusCompanyId(user.getBusCompany().getId());
        }

        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
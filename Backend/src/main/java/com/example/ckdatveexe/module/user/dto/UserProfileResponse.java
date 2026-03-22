package com.example.ckdatveexe.module.user.dto;

import com.example.ckdatveexe.shared.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Integer id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String idCard;
    private String status;

    public static UserProfileResponse fromEntity(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFirstName() + " " + user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setIdCard(user.getIdCard());
        response.setStatus(user.getStatus().name());
        return response;
    }
}
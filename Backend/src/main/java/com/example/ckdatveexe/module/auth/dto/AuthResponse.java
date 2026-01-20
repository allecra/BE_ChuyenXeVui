package com.example.ckdatveexe.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserInfo user;
    private String loginStatus = "SUCCESS";
    private LocalDateTime loginTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Integer id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private Set<String> roles;
        private String status;
        private LocalDateTime lastLogin;
    }

    public AuthResponse(String accessToken, String refreshToken, UserInfo user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.loginTime = LocalDateTime.now();
    }
}
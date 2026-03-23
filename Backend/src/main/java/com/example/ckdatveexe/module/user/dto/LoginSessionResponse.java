package com.example.ckdatveexe.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginSessionResponse {
    private String sessionId;
    private String deviceInfo;
    private String ipAddress;
    private String location;
    private LocalDateTime loginTime;
    private LocalDateTime lastActivity;
    private Boolean isActive;
    private String browserInfo;
    private String operatingSystem;

    public static LoginSessionResponse createMockSession(String sessionId, String deviceInfo, String ipAddress) {
        return LoginSessionResponse.builder()
                .sessionId(sessionId)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .location("Hà Nội, Việt Nam") // Mock location
                .loginTime(LocalDateTime.now().minusHours(2))
                .lastActivity(LocalDateTime.now().minusMinutes(5))
                .isActive(true)
                .browserInfo("Chrome 120.0.0.0")
                .operatingSystem("Windows 11")
                .build();
    }
}
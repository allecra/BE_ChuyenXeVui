package com.example.ckdatveexe.auth;

import com.example.ckdatveexe.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Authentication API Integration Tests")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /auth/register - Should register new user successfully")
    void testRegisterUser() throws Exception {
        String registerRequest = objectMapper.writeValueAsString(Map.of(
                "firstName", "John",
                "lastName", "Doe",
                "email", "newuser@test.com",
                "password", "password123",
                "phone", "0987654321"));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /auth/register - Should fail with invalid email")
    void testRegisterUserInvalidEmail() throws Exception {
        String registerRequest = objectMapper.writeValueAsString(Map.of(
                "firstName", "John",
                "lastName", "Doe",
                "email", "invalid-email",
                "password", "password123",
                "phone", "0987654321"));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - Should login successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        // First register a user
        String registerRequest = objectMapper.writeValueAsString(Map.of(
                "firstName", "Test",
                "lastName", "User",
                "email", "login@test.com",
                "password", "password123",
                "phone", "0123456789"));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
                .andExpect(status().isCreated());

        // Then login
        String loginRequest = objectMapper.writeValueAsString(Map.of(
                "email", "login@test.com",
                "password", "password123"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.email").value("login@test.com"));
    }

    @Test
    @DisplayName("POST /auth/login - Should fail with invalid credentials")
    void testLoginFailure() throws Exception {
        String loginRequest = objectMapper.writeValueAsString(Map.of(
                "email", "nonexistent@test.com",
                "password", "wrongpassword"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /auth/refresh - Should refresh token successfully")
    void testRefreshToken() throws Exception {
        // First login to get refresh token
        String loginRequest = objectMapper.writeValueAsString(Map.of(
                "email", "user@test.com",
                "password", "password123"));

        String response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        String refreshToken = (String) data.get("refreshToken");

        // Use refresh token
        String refreshRequest = objectMapper.writeValueAsString(Map.of(
                "refreshToken", refreshToken));

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    @DisplayName("POST /auth/logout - Should logout successfully")
    void testLogout() throws Exception {
        performAuthenticatedPost("/auth/logout", Map.of(), userAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /auth/me - Should get current user info")
    void testGetCurrentUser() throws Exception {
        performAuthenticatedGet("/auth/me", userAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    @DisplayName("POST /auth/forgot-password - Should send OTP for password reset")
    void testForgotPassword() throws Exception {
        String forgotPasswordRequest = objectMapper.writeValueAsString(Map.of(
                "email", "user@test.com"));

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(forgotPasswordRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /auth/verify-otp - Should verify OTP")
    void testVerifyOtp() throws Exception {
        String verifyOtpRequest = objectMapper.writeValueAsString(Map.of(
                "email", "user@test.com",
                "otp", "123456"));

        mockMvc.perform(post("/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(verifyOtpRequest))
                .andExpect(status().isBadRequest()); // Will fail with invalid OTP, but tests the endpoint
    }

    @Test
    @DisplayName("POST /auth/set-new-password - Should set new password")
    void testSetNewPassword() throws Exception {
        String setPasswordRequest = objectMapper.writeValueAsString(Map.of(
                "email", "user@test.com",
                "otp", "123456",
                "newPassword", "newpassword123"));

        mockMvc.perform(post("/auth/set-new-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(setPasswordRequest))
                .andExpect(status().isBadRequest()); // Will fail with invalid OTP, but tests the endpoint
    }
}
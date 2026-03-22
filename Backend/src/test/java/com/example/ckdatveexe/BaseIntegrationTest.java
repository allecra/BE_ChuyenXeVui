package com.example.ckdatveexe;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String accessToken;
    protected String userAccessToken;
    protected String companyAccessToken;
    protected String adminAccessToken;

    @BeforeEach
    void setUp() throws Exception {
        // Setup test data and authentication tokens
        setupTestAuthentication();
    }

    protected void setupTestAuthentication() throws Exception {
        // Create test users and get tokens
        userAccessToken = createUserAndGetToken("thienkt179@gmail.com", "MatKhau@123", "USER");
        companyAccessToken = createUserAndGetToken("company@test.com", "password123", "BUS_COMPANY");
        adminAccessToken = createUserAndGetToken("admin@test.com", "password123", "ADMIN");
    }

    protected String createUserAndGetToken(String email, String password, String role) throws Exception {
        // Register user
        String registerRequest = objectMapper.writeValueAsString(Map.of(
                "firstName", "Test",
                "lastName", "User",
                "email", email,
                "password", password,
                "phone", "0123456789"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
                .andExpect(status().isCreated());

        // Login and get token
        String loginRequest = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", password));

        ResultActions result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists());

        String response = result.andReturn().getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        return (String) data.get("accessToken");
    }

    protected ResultActions performAuthenticatedGet(String url, String token) throws Exception {
        return mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
    }

    protected ResultActions performAuthenticatedPost(String url, Object requestBody, String token) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    protected ResultActions performAuthenticatedPut(String url, Object requestBody, String token) throws Exception {
        return mockMvc.perform(put(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    protected ResultActions performAuthenticatedDelete(String url, String token) throws Exception {
        return mockMvc.perform(delete(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
    }

    protected ResultActions performAuthenticatedDeleteWithBody(String url, Object requestBody, String token)
            throws Exception {
        return mockMvc.perform(delete(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }
}
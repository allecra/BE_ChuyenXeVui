package com.example.ckdatveexe.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        String path = request.getRequestURI();

        // ✅ BỎ QUA PUBLIC API
        if (path.startsWith("/api/user/")
                || path.startsWith("/auth/")
                || path.startsWith("/public/")
                || path.startsWith("/api-docs/")
                || path.startsWith("/api/bus-company/registration/")
                || path.startsWith("/swagger-ui/")) {

            // 👉 KHÔNG trả 401
            return;
        }

        log.error("🚫 Unauthorized: {}", path);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        body.put("status", 401);
        body.put("error", "Unauthorized");
        body.put("message", "JWT token không hợp lệ hoặc thiếu");
        body.put("path", path);

        new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writeValue(response.getOutputStream(), body);
    }
}
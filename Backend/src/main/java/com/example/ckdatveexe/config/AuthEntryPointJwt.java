package com.example.ckdatveexe.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        log.error("🚫 [AUTH ENTRY POINT] 401 Unauthorized - {} {}", method, requestPath);
        log.error("🚫 [AUTH ENTRY POINT] Reason: {}", authException.getMessage());
        log.error("🚫 [AUTH ENTRY POINT] Exception type: {}", authException.getClass().getSimpleName());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 401);
        body.put("error", "Unauthorized");
        body.put("message", "🔐 Yêu cầu phải có JWT token hợp lệ để truy cập API này");
        body.put("path", requestPath);
        body.put("method", method);
        body.put("details", "Vui lòng đăng nhập và sử dụng token trong header: Authorization: Bearer <your-token>");

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);

        log.info("📤 [AUTH ENTRY POINT] Sent 401 response to client");
    }
}
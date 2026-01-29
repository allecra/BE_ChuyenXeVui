package com.example.ckdatveexe.module.auth.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.module.auth.dto.*;
import com.example.ckdatveexe.module.auth.service.AuthService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "Các API quản lý đăng nhập, đăng xuất và xác thực người dùng")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Xác thực người dùng và trả về access token và refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        log.info("🔐 [AUTH] POST /auth/login - Login attempt for email: {}", loginRequest.getEmail());

        try {
            AuthResponse authResponse = authService.authenticateUser(loginRequest);

            // Cookie access token
            Cookie accessTokenCookie = new Cookie("accessToken", authResponse.getAccessToken());
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false); // true khi deploy HTTPS
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(24 * 60 * 60); // 24h
            response.addCookie(accessTokenCookie);

            // Cookie refresh token
            Cookie refreshTokenCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
            response.addCookie(refreshTokenCookie);

            log.info("✅ [AUTH] 200 OK - Login successful for email: {}", loginRequest.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.<AuthResponse>builder()
                            .success(true)
                            .message("Đăng nhập thành công")
                            .data(authResponse)
                            .build());
        } catch (RuntimeException e) {
            log.error("❌ [AUTH] 400 BAD_REQUEST - Login failed for email: {} - Exception: {} - Message: {}",
                    loginRequest.getEmail(), e.getClass().getSimpleName(), e.getMessage());

            String errorMessage;
            switch (e.getMessage()) {
                case "EMAIL_NOT_FOUND":
                    errorMessage = "Email không tồn tại trong hệ thống";
                    break;
                case "INVALID_PASSWORD":
                    errorMessage = "Mật khẩu không chính xác";
                    break;
                case "ACCOUNT_INACTIVE":
                    errorMessage = "Tài khoản đã bị khóa hoặc chưa được kích hoạt";
                    break;
                case "USER_NO_ROLES":
                    errorMessage = "Tài khoản chưa được phân quyền. Vui lòng liên hệ admin";
                    break;
                case "AUTHENTICATION_ERROR":
                    errorMessage = "Lỗi xác thực. Vui lòng thử lại";
                    break;
                default:
                    errorMessage = "Lỗi không xác định: " + e.getMessage();
            }

            return ResponseEntity.badRequest()
                    .body(ApiResponse.<AuthResponse>builder()
                            .success(false)
                            .message(errorMessage)
                            .build());
        } catch (Exception e) {
            log.error("💥 [AUTH] 500 INTERNAL_SERVER_ERROR - Unexpected error during login for email: {}",
                    loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<AuthResponse>builder()
                            .success(false)
                            .message("Đã xảy ra lỗi. Vui lòng thử lại sau")
                            .build());
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký", description = "Tạo tài khoản người dùng mới")
    public ResponseEntity<ApiResponse<Object>> registerUser(
            @Valid @RequestBody RegisterRequest signUpRequest) {

        log.info("📝 [AUTH] POST /auth/register - Registration attempt for email: {}", signUpRequest.getEmail());

        try {
            ApiResponse<Object> response = authService.registerUser(signUpRequest);
            log.info("✅ [AUTH] 201 CREATED - Registration successful for email: {}", signUpRequest.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("❌ [AUTH] 400 BAD_REQUEST - Registration failed for email: {}", signUpRequest.getEmail(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Object>builder()
                            .success(false)
                            .message("Đăng ký thất bại: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token", description = "Tạo access token mới bằng refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenFromCookie,
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletResponse response) {

        log.info("🔄 [AUTH] POST /auth/refresh - Token refresh attempt");

        try {
            String refreshToken = refreshTokenFromCookie;
            if (refreshToken == null && request != null) {
                refreshToken = request.getRefreshToken();
            }

            if (refreshToken == null) {
                log.warn("❌ [AUTH] 400 BAD_REQUEST - Missing refresh token");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<AuthResponse>builder()
                                .success(false)
                                .message("Thiếu refresh token")
                                .build());
            }

            RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
            refreshRequest.setRefreshToken(refreshToken);

            AuthResponse authResponse = authService.refreshToken(refreshRequest);

            Cookie accessTokenCookie = new Cookie("accessToken", authResponse.getAccessToken());
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(24 * 60 * 60);
            response.addCookie(accessTokenCookie);

            log.info("✅ [AUTH] 200 OK - Token refresh successful");
            return ResponseEntity.ok(
                    ApiResponse.<AuthResponse>builder()
                            .success(true)
                            .message("Làm mới token thành công")
                            .data(authResponse)
                            .build());
        } catch (Exception e) {
            log.error("❌ [AUTH] 400 BAD_REQUEST - Token refresh failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<AuthResponse>builder()
                            .success(false)
                            .message("Refresh token không hợp lệ hoặc đã hết hạn")
                            .build());
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất", description = "Đăng xuất người dùng và vô hiệu hóa token")
    public ResponseEntity<ApiResponse<Object>> logoutUser(
            Authentication authentication,
            HttpServletResponse response) {

        log.info("🚪 [AUTH] POST /auth/logout - Logout attempt");

        try {
            if (authentication != null) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                authService.logout(userDetails.getEmail());
                log.info("👤 [AUTH] User logged out: {}", userDetails.getEmail());
            }

            Cookie accessTokenCookie = new Cookie("accessToken", null);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(0);
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refreshToken", null);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);
            response.addCookie(refreshTokenCookie);

            log.info("✅ [AUTH] 200 OK - Logout successful");
            return ResponseEntity.ok(
                    ApiResponse.<Object>builder()
                            .success(true)
                            .message("Đăng xuất thành công")
                            .build());
        } catch (Exception e) {
            log.error("💥 [AUTH] 500 INTERNAL_SERVER_ERROR - Logout failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Object>builder()
                            .success(false)
                            .message("Đăng xuất thất bại")
                            .build());
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Quên mật khẩu", description = "Gửi mã xác thực (OTP) đặt lại mật khẩu qua email")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        log.info("🔑 [AUTH] POST /auth/forgot-password - Forgot password request for email: {}", request.getEmail());

        try {
            ApiResponse<Object> response = authService.forgotPassword(request);
            log.info("✅ [AUTH] 200 OK - Forgot password email sent to: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ [AUTH] 400 BAD_REQUEST - Forgot password failed for email: {}", request.getEmail(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Object>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Xác thực mã OTP", description = "Xác thực mã OTP được gửi qua email để đặt lại mật khẩu")
    public ResponseEntity<ApiResponse<Object>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        log.info("🔢 [AUTH] POST /auth/verify-otp - OTP verification attempt");

        try {
            ApiResponse<Object> response = authService.verifyOtp(request);
            log.info("✅ [AUTH] 200 OK - OTP verification successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ [AUTH] 400 BAD_REQUEST - OTP verification failed for OTP: {} - Error: {}",
                    request.getOtp(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Object>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    @PostMapping("/set-new-password")
    @Operation(summary = "Đặt mật khẩu mới", description = "Đặt mật khẩu mới sau khi xác thực OTP thành công")
    public ResponseEntity<ApiResponse<Object>> setNewPassword(
            @Valid @RequestBody NewPasswordRequest request) {

        log.info("🔐 [AUTH] POST /auth/set-new-password - Set new password attempt");

        try {
            ApiResponse<Object> response = authService.setNewPassword(request);
            log.info("✅ [AUTH] 200 OK - New password set successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ [AUTH] 400 BAD_REQUEST - Set new password failed for OTP: {} - Error: {}",
                    request.getOtp(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Object>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Thông tin người dùng hiện tại", description = "Lấy thông tin người dùng đang đăng nhập")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> getCurrentUser(Authentication authentication) {

        log.info("👤 [AUTH] GET /auth/me - Get current user info");

        try {
            if (authentication == null) {
                log.warn("❌ [AUTH] 401 UNAUTHORIZED - User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.<AuthResponse.UserInfo>builder()
                                .success(false)
                                .message("Người dùng chưa đăng nhập")
                                .build());
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                    userDetails.getId(),
                    userDetails.getFirstName(),
                    userDetails.getLastName(),
                    userDetails.getEmail(),
                    userDetails.getPhone(),
                    userDetails.getAuthorities().stream()
                            .map(auth -> auth.getAuthority())
                            .collect(java.util.stream.Collectors.toSet()),
                    "ACTIVE",
                    java.time.LocalDateTime.now());

            log.info("✅ [AUTH] 200 OK - User info retrieved for: {}", userDetails.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.<AuthResponse.UserInfo>builder()
                            .success(true)
                            .message("Lấy thông tin người dùng thành công")
                            .data(userInfo)
                            .build());
        } catch (Exception e) {
            log.error("💥 [AUTH] 500 INTERNAL_SERVER_ERROR - Failed to get user info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<AuthResponse.UserInfo>builder()
                            .success(false)
                            .message("Lỗi khi lấy thông tin người dùng")
                            .build());
        }
    }
}

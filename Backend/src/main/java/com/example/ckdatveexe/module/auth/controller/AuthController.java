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
    public ResponseEntity<ApiResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

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

            return ResponseEntity.ok(
                    ApiResponse.success("Đăng nhập thành công", authResponse));
        } catch (RuntimeException e) {
            log.error("Đăng nhập thất bại với email: {} - Exception type: {} - Message: {}",
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
                    .body(ApiResponse.error(errorMessage));
        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi đăng nhập: {}", loginRequest.getEmail(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Đã xảy ra lỗi. Vui lòng thử lại sau"));
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký", description = "Tạo tài khoản người dùng mới")
    public ResponseEntity<ApiResponse> registerUser(
            @Valid @RequestBody RegisterRequest signUpRequest) {

        try {
            ApiResponse response = authService.registerUser(signUpRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Đăng ký thất bại với email: {}", signUpRequest.getEmail(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Đăng ký thất bại: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token", description = "Tạo access token mới bằng refresh token")
    public ResponseEntity<ApiResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenFromCookie,
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletResponse response) {

        try {
            String refreshToken = refreshTokenFromCookie;
            if (refreshToken == null && request != null) {
                refreshToken = request.getRefreshToken();
            }

            if (refreshToken == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Thiếu refresh token"));
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

            return ResponseEntity.ok(
                    ApiResponse.success("Làm mới token thành công", authResponse));
        } catch (Exception e) {
            log.error("Làm mới token thất bại", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Refresh token không hợp lệ hoặc đã hết hạn"));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất", description = "Đăng xuất người dùng và vô hiệu hóa token")
    public ResponseEntity<ApiResponse> logoutUser(
            Authentication authentication,
            HttpServletResponse response) {

        try {
            if (authentication != null) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                authService.logout(userDetails.getEmail());
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

            return ResponseEntity.ok(
                    ApiResponse.success("Đăng xuất thành công"));
        } catch (Exception e) {
            log.error("Đăng xuất thất bại", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Đăng xuất thất bại"));
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Quên mật khẩu", description = "Gửi mã xác thực (OTP) đặt lại mật khẩu qua email")
    public ResponseEntity<ApiResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        try {
            ApiResponse response = authService.forgotPassword(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Quên mật khẩu thất bại với email: {}", request.getEmail(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Xác thực mã OTP", description = "Xác thực mã OTP được gửi qua email để đặt lại mật khẩu")
    public ResponseEntity<ApiResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        try {
            ApiResponse response = authService.verifyOtp(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Xác thực OTP thất bại với OTP: {} - Lỗi: {}", request.getOtp(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/set-new-password")
    @Operation(summary = "Đặt mật khẩu mới", description = "Đặt mật khẩu mới sau khi xác thực OTP thành công")
    public ResponseEntity<ApiResponse> setNewPassword(
            @Valid @RequestBody NewPasswordRequest request) {

        try {
            ApiResponse response = authService.setNewPassword(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Đặt mật khẩu mới thất bại với OTP: {} - Lỗi: {}", request.getOtp(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Thông tin người dùng hiện tại", description = "Lấy thông tin người dùng đang đăng nhập")
    public ResponseEntity<ApiResponse> getCurrentUser(Authentication authentication) {

        try {
            if (authentication == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Người dùng chưa đăng nhập"));
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

            return ResponseEntity.ok(
                    ApiResponse.success("Lấy thông tin người dùng thành công", userInfo));
        } catch (Exception e) {
            log.error("Không thể lấy thông tin người dùng", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy thông tin người dùng"));
        }
    }
}

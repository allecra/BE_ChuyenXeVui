package com.example.ckdatveexe.module.auth.service;

import com.example.ckdatveexe.config.JwtUtils;
import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.module.auth.dto.*;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        try {
            log.info("Attempting to authenticate user with email: {}", loginRequest.getEmail());

            // Kiểm tra user có tồn tại không
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> {
                        log.warn("User not found with email: {}", loginRequest.getEmail());
                        RuntimeException ex = new RuntimeException("EMAIL_NOT_FOUND");
                        log.warn("Throwing exception: {}", ex.getMessage());
                        return ex;
                    });

            log.info("User found: {}, Status: {}, Roles: {}", user.getEmail(), user.getStatus(),
                    user.getRoles() != null ? user.getRoles().size() : "null");

            // Kiểm tra trạng thái user
            if (user.getStatus() != UserStatus.ACTIVE) {
                log.warn("User account is not active: {}", loginRequest.getEmail());
                throw new RuntimeException("ACCOUNT_INACTIVE");
            }

            // Kiểm tra mật khẩu
            if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.warn("Invalid password for user: {}", loginRequest.getEmail());
                RuntimeException ex = new RuntimeException("INVALID_PASSWORD");
                log.warn("Throwing password exception: {}", ex.getMessage());
                throw ex;
            }

            // Kiểm tra roles
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                log.error("User has no roles assigned: {}", loginRequest.getEmail());
                throw new RuntimeException("USER_NO_ROLES");
            }

            // Nếu tất cả đều OK, tạo authentication token trực tiếp
            log.info("Creating UserDetailsImpl for user: {}", loginRequest.getEmail());
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.toSet());

            // Tạo refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                    userDetails.getId(),
                    userDetails.getFirstName(),
                    userDetails.getLastName(),
                    userDetails.getEmail(),
                    userDetails.getPhone(),
                    roles,
                    user.getStatus().name(),
                    LocalDateTime.now());

            log.info("Authentication successful for user: {}", loginRequest.getEmail());
            return new AuthResponse(jwt, refreshToken.getToken(), userInfo);

        } catch (RuntimeException e) {
            log.error("Authentication failed for email: {} - {}", loginRequest.getEmail(), e.getMessage());
            throw e; // Re-throw để controller xử lý
        } catch (Exception e) {
            log.error("Unexpected error during authentication for email: {}", loginRequest.getEmail(), e);
            throw new RuntimeException("AUTHENTICATION_ERROR");
        }
    }

    @Transactional
    public ApiResponse registerUser(RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ApiResponse.error("Email đã được sử dụng!");
        }

        // Tạo tài khoản người dùng mới
        User user = new User();
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setEmail(signUpRequest.getEmail());
        user.setPhone(signUpRequest.getPhone());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        // Gán role mặc định là ROLE_USER
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByRoleName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy ROLE_USER. Vui lòng kiểm tra database."));
        roles.add(userRole);

        user.setRoles(roles);
        userRepository.save(user);

        log.info("User registered successfully: {} with role: {} (ID: {})", 
                signUpRequest.getEmail(), userRole.getRoleName(), userRole.getId());
        return ApiResponse.success("Đăng ký tài khoản thành công!");
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getEmail());

                    Set<String> roles = user.getRoles().stream()
                            .map(role -> role.getRoleName().name())
                            .collect(Collectors.toSet());

                    AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                            user.getId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getEmail(),
                            user.getPhone(),
                            roles,
                            user.getStatus().name(),
                            LocalDateTime.now());

                    return new AuthResponse(token, requestRefreshToken, userInfo);
                })
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ!"));
    }

    @Transactional
    public ApiResponse logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        refreshTokenService.deleteByUser(user);
        return ApiResponse.success("Đăng xuất thành công!");
    }

    @Transactional
    public ApiResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailAndStatus(request.getEmail(), UserStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + request.getEmail()));

        // Xóa các yêu cầu reset password cũ
        passwordResetRepository.deleteByEmail(request.getEmail());

        // Tạo OTP 6 chữ số
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Tạo bản ghi reset password
        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setEmail(request.getEmail());
        passwordReset.setOtp(otp);
        passwordReset.setExpiresAt(LocalDateTime.now().plusMinutes(15)); // Hết hạn sau 15 phút

        passwordResetRepository.save(passwordReset);

        // Gửi email
        try {
            emailService.sendPasswordResetEmail(request.getEmail(), otp);
            return ApiResponse.success("Mã OTP đặt lại mật khẩu đã được gửi đến email của bạn");
        } catch (Exception e) {
            log.error("Failed to send password reset email", e);
            return ApiResponse.error("Không thể gửi email. Vui lòng thử lại sau.");
        }
    }

    @Transactional
    public ApiResponse verifyOtp(VerifyOtpRequest request) {
        // Tìm bản ghi reset password chỉ bằng OTP
        PasswordReset passwordReset = passwordResetRepository.findAll().stream()
                .filter(pr -> pr.getOtp().equals(request.getOtp()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mã OTP không hợp lệ"));

        // Kiểm tra OTP có hết hạn không
        if (passwordReset.getExpiresAt().isBefore(LocalDateTime.now())) {
            passwordResetRepository.delete(passwordReset);
            throw new RuntimeException("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới");
        }

        return ApiResponse.success("Mã OTP hợp lệ. Bạn có thể đặt mật khẩu mới");
    }

    @Transactional
    public ApiResponse setNewPassword(NewPasswordRequest request) {
        // Tìm bản ghi reset password chỉ bằng OTP
        PasswordReset passwordReset = passwordResetRepository.findAll().stream()
                .filter(pr -> pr.getOtp().equals(request.getOtp()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mã OTP không hợp lệ"));

        // Kiểm tra OTP có hết hạn không
        if (passwordReset.getExpiresAt().isBefore(LocalDateTime.now())) {
            passwordResetRepository.delete(passwordReset);
            throw new RuntimeException("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới");
        }

        // Tìm user bằng email từ password reset record
        User user = userRepository.findByEmailAndStatus(passwordReset.getEmail(), UserStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Xóa bản ghi reset password
        passwordResetRepository.delete(passwordReset);

        // Xóa tất cả refresh token (buộc đăng nhập lại)
        refreshTokenService.deleteByUser(user);

        return ApiResponse.success("Đặt mật khẩu mới thành công!");
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenService.deleteExpiredTokens();
        passwordResetRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
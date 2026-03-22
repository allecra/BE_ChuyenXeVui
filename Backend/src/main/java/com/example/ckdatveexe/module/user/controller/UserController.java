package com.example.ckdatveexe.module.user.controller;

import com.example.ckdatveexe.module.user.dto.UpdateUserProfileRequest;
import com.example.ckdatveexe.module.user.dto.UserProfileResponse;
import com.example.ckdatveexe.module.user.service.UserService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profile", description = "APIs quản lý thông tin cá nhân người dùng")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy thông tin cá nhân", description = "Lấy thông tin cá nhân của người dùng đang đăng nhập")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("👤 [GET] /api/user/profile - Email: {}", email);

            UserProfileResponse profile = userService.getUserProfileByEmail(email);

            log.info("✅ [GET] /api/user/profile - Success for email: {}", email);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thông tin cá nhân thành công",
                    profile));
        } catch (Exception e) {
            log.error("💥 [GET] /api/user/profile - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Cập nhật thông tin cá nhân", description = "Cập nhật thông tin cá nhân của người dùng")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            @Valid @RequestBody UpdateUserProfileRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("📝 [PUT] /api/user/profile - Email: {} updating profile", email);

            UserProfileResponse updatedProfile = userService.updateUserProfileByEmail(email, request);

            log.info("✅ [PUT] /api/user/profile - Success for email: {}", email);
            return ResponseEntity.ok(ApiResponse.success(
                    "Cập nhật thông tin cá nhân thành công",
                    updatedProfile));
        } catch (Exception e) {
            log.error("💥 [PUT] /api/user/profile - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/for-booking")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy thông tin để điền form đặt vé", description = "Lấy thông tin cá nhân để tự động điền vào form đặt vé")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfileForBooking(Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("🎫 [GET] /api/user/profile/for-booking - Email: {}", email);

            UserProfileResponse profile = userService.getUserProfileByEmail(email);

            log.info("✅ [GET] /api/user/profile/for-booking - Success for email: {}", email);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thông tin để đặt vé thành công",
                    profile));
        } catch (Exception e) {
            log.error("💥 [GET] /api/user/profile/for-booking - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
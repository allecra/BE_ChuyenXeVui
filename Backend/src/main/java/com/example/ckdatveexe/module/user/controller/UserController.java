package com.example.ckdatveexe.module.user.controller;

import com.example.ckdatveexe.module.user.dto.*;
import com.example.ckdatveexe.module.user.service.UserService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profile", description = "APIs quản lý thông tin cá nhân người dùng")
public class UserController {

    private final UserService userService;

    @GetMapping("/health")
    @Operation(summary = "Health check for user profile")
    public ResponseEntity<String> healthCheck() {
        System.out.println("🔍 [HEALTH] UserController health check called!");
        log.info("🔍 [HEALTH] UserController health check called!");
        return ResponseEntity.ok().body("UserController is working!");
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy thông tin cá nhân", description = "Lấy thông tin cá nhân của người dùng đang đăng nhập")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("👤 [GET] /api/profile - Email: {}", email);

            UserProfileResponse profile = userService.getUserProfileByEmail(email);

            log.info("✅ [GET] /api/profile - Success for email: {}", email);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thông tin cá nhân thành công",
                    profile));
        } catch (Exception e) {
            log.error("💥 [GET] /api/profile - Error: {}", e.getMessage());
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
            log.info("📝 [PUT] /api/profile - Email: {} updating profile", email);

            // Validate that at least one field is provided for update
            if ((request.getFirstName() == null || request.getFirstName().trim().isEmpty()) &&
                    (request.getLastName() == null || request.getLastName().trim().isEmpty()) &&
                    (request.getEmail() == null || request.getEmail().trim().isEmpty()) &&
                    (request.getPhone() == null || request.getPhone().trim().isEmpty()) &&
                    (request.getIdCard() == null || request.getIdCard().trim().isEmpty())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Vui lòng cung cấp ít nhất một thông tin để cập nhật"));
            }

            UserProfileResponse updatedProfile = userService.updateUserProfileByEmail(email, request);

            log.info("✅ [PUT] /api/profile - Success for email: {}", email);
            return ResponseEntity.ok(ApiResponse.success(
                    "Cập nhật thông tin cá nhân thành công",
                    updatedProfile));
        } catch (Exception e) {
            log.error("💥 [PUT] /api/profile - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/for-booking")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy thông tin để điền form đặt vé", description = "Lấy thông tin cá nhân để tự động điền vào form đặt vé")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfileForBooking(Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("🎫 [GET] /api/profile/for-booking - Email: {}", email);

            UserProfileResponse profile = userService.getUserProfileByEmail(email);

            log.info("✅ [GET] /api/profile/for-booking - Success for email: {}", email);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy thông tin để đặt vé thành công",
                    profile));
        } catch (Exception e) {
            log.error("💥 [GET] /api/profile/for-booking - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/booking-history")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lịch sử đặt vé", description = "Lấy lịch sử đặt vé của người dùng")
    public ResponseEntity<ApiResponse<Page<BookingHistoryResponse>>> getBookingHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            String email = authentication.getName();
            log.info("🎫 [GET] /api/profile/booking-history - Email: {}", email);

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<BookingHistoryResponse> bookingHistory = userService.getBookingHistory(email, pageable);

            log.info("✅ [GET] /api/profile/booking-history - Success for email: {}", email);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy lịch sử đặt vé thành công",
                    bookingHistory));
        } catch (Exception e) {
            log.error("💥 [GET] /api/profile/booking-history - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/payment-history")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lịch sử thanh toán", description = "Lấy lịch sử thanh toán của người dùng")
    public ResponseEntity<ApiResponse<Page<PaymentHistoryResponse>>> getPaymentHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            String email = authentication.getName();
            log.info("💳 [GET] /api/profile/payment-history - Email: {}", email);

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<PaymentHistoryResponse> paymentHistory = userService.getPaymentHistory(email, pageable);

            log.info("✅ [GET] /api/profile/payment-history - Success for email: {}", email);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy lịch sử thanh toán thành công",
                    paymentHistory));
        } catch (Exception e) {
            log.error("💥 [GET] /api/profile/payment-history - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/login-sessions")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Quản lý đăng nhập", description = "Lấy danh sách phiên đăng nhập của người dùng")
    public ResponseEntity<ApiResponse<List<LoginSessionResponse>>> getLoginSessions(Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("🔐 [GET] /api/profile/login-sessions - Email: {}", email);

            List<LoginSessionResponse> sessions = userService.getLoginSessions(email);

            log.info("✅ [GET] /api/profile/login-sessions - Success for email: {}", email);
            return ResponseEntity.ok(ApiResponse.success(
                    "Lấy danh sách phiên đăng nhập thành công",
                    sessions));
        } catch (Exception e) {
            log.error("💥 [GET] /api/profile/login-sessions - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Đổi mật khẩu", description = "Thay đổi mật khẩu và gửi email thông báo")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("🔒 [POST] /api/profile/change-password - Email: {}", email);
            log.info("🔍 Request data - currentPassword: {}, newPassword: {}, confirmPassword: {}",
                    request.getCurrentPassword() != null ? "***PROVIDED***" : "NULL",
                    request.getNewPassword() != null ? "***PROVIDED***" : "NULL",
                    request.getConfirmPassword() != null ? "***PROVIDED***" : "NULL");

            // Manual validation since we removed @Valid
            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Mật khẩu hiện tại không được để trống"));
            }
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Mật khẩu mới không được để trống"));
            }
            if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Xác nhận mật khẩu không được để trống"));
            }
            if (request.getNewPassword().length() < 6 || request.getNewPassword().length() > 50) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Mật khẩu phải có từ 6-50 ký tự"));
            }
            if (!request.getNewPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Mật khẩu phải chứa ít nhất 1 chữ thường, 1 chữ hoa và 1 số"));
            }

            ApiResponse<Void> result = userService.changePassword(email, request);

            log.info("✅ [POST] /api/profile/change-password - Success for email: {}", email);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("💥 [POST] /api/profile/change-password - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
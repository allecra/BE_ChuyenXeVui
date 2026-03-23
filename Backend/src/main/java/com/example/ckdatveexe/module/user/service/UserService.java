package com.example.ckdatveexe.module.user.service;

import com.example.ckdatveexe.module.auth.service.EmailService;
import com.example.ckdatveexe.module.user.dto.*;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final DeletedUserRepository deletedUserRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserProfileResponse getUserProfile(Integer userId) {
        log.info("👤 Getting user profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        log.info("✅ User profile retrieved successfully for user: {}", userId);
        return UserProfileResponse.fromEntity(user);
    }

    public UserProfileResponse getUserProfileByEmail(String email) {
        log.info("👤 Getting user profile for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        log.info("✅ User profile retrieved successfully for email: {}", email);
        return UserProfileResponse.fromEntity(user);
    }

    public UserProfileResponse updateUserProfileByEmail(String email, UpdateUserProfileRequest request) {
        log.info("📝 Updating user profile for email: {}", email);
        log.info("🔍 Request idCard value: '{}', length: {}",
                request.getIdCard(),
                request.getIdCard() != null ? request.getIdCard().length() : "null");
        log.info("🔍 Request data - firstName: {}, lastName: {}, email: {}, phone: {}, idCard: {}",
                request.getFirstName(), request.getLastName(), request.getEmail(),
                request.getPhone(), request.getIdCard());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        log.info("🔍 Current user data - firstName: {}, lastName: {}, email: {}, phone: {}, idCard: {}",
                user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getPhone(), user.getIdCard());

        // Validate email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty() &&
                !user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmailAndIdNot(request.getEmail(), user.getId())) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi người dùng khác");
        }

        // Validate ID card uniqueness if provided and changed
        if (request.getIdCard() != null && !request.getIdCard().trim().isEmpty() &&
                !request.getIdCard().equals(user.getIdCard()) &&
                userRepository.existsByIdCardAndIdNot(request.getIdCard(), user.getId())) {
            throw new IllegalArgumentException("Số CMND/CCCD đã được sử dụng bởi người dùng khác");
        }

        // Update user fields only if they are provided and not empty
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            user.setFirstName(request.getFirstName().trim());
            log.info("✅ Updated firstName to: {}", request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            user.setLastName(request.getLastName().trim());
            log.info("✅ Updated lastName to: {}", request.getLastName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user.setEmail(request.getEmail().trim());
            log.info("✅ Updated email to: {}", request.getEmail().trim());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone().trim());
            log.info("✅ Updated phone to: {}", request.getPhone().trim());
        }
        if (request.getIdCard() != null && !request.getIdCard().trim().isEmpty()) {
            user.setIdCard(request.getIdCard().trim());
            log.info("✅ Updated idCard to: {}", request.getIdCard().trim());
        } else {
            log.info("🔍 IdCard not updated - request.getIdCard(): {}, isEmpty: {}",
                    request.getIdCard(),
                    request.getIdCard() != null ? request.getIdCard().trim().isEmpty() : "null");
        }

        log.info("🔍 Before save - firstName: {}, lastName: {}, email: {}, phone: {}, idCard: {}",
                user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getPhone(), user.getIdCard());

        User updatedUser = userRepository.save(user);

        log.info("🔍 After save - firstName: {}, lastName: {}, email: {}, phone: {}, idCard: {}",
                updatedUser.getFirstName(), updatedUser.getLastName(), updatedUser.getEmail(),
                updatedUser.getPhone(), updatedUser.getIdCard());

        log.info("✅ User profile updated successfully for email: {}", email);

        return UserProfileResponse.fromEntity(updatedUser);
    }

    // ===== NEW PROFILE APIs =====

    public Page<BookingHistoryResponse> getBookingHistory(String email, Pageable pageable) {
        log.info("🎫 Getting booking history for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        Page<Ticket> tickets = ticketRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        log.info("✅ Found {} tickets for user: {}", tickets.getTotalElements(), email);
        return tickets.map(BookingHistoryResponse::fromEntity);
    }

    public Page<PaymentHistoryResponse> getPaymentHistory(String email, Pageable pageable) {
        log.info("💳 Getting payment history for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        Page<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        log.info("✅ Found {} payments for user: {}", payments.getTotalElements(), email);
        return payments.map(PaymentHistoryResponse::fromEntity);
    }

    public List<LoginSessionResponse> getLoginSessions(String email) {
        log.info("🔐 Getting login sessions for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // Mock data for now - in real implementation, you would store session info
        List<LoginSessionResponse> sessions = Arrays.asList(
                LoginSessionResponse.createMockSession("sess_001", "Chrome on Windows", "192.168.1.100"),
                LoginSessionResponse.createMockSession("sess_002", "Mobile App on Android", "192.168.1.101"),
                LoginSessionResponse.createMockSession("sess_003", "Safari on MacOS", "192.168.1.102"));

        log.info("✅ Found {} active sessions for user: {}", sessions.size(), email);
        return sessions;
    }

    @Transactional
    public ApiResponse<Void> changePassword(String email, ChangePasswordRequest request) {
        log.info("🔒 Changing password for email: {}", email);

        // Validate confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Send email notification
        try {
            emailService.sendPasswordChangeNotification(user.getEmail(),
                    user.getFirstName() + " " + user.getLastName());
            log.info("📧 Password change notification sent to: {}", email);
        } catch (Exception e) {
            log.warn("⚠️ Failed to send password change notification: {}", e.getMessage());
        }

        log.info("✅ Password changed successfully for user: {}", email);
        return ApiResponse.success("Đổi mật khẩu thành công. Email thông báo đã được gửi.");
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Integer userId, UpdateUserProfileRequest request) {
        log.info("📝 Updating user profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // Check if email is already taken by another user
        if (!user.getEmail().equals(request.getEmail())) {
            boolean emailExists = userRepository.existsByEmailAndIdNot(request.getEmail(), userId);
            if (emailExists) {
                throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác");
            }
        }

        // Check if idCard is already taken by another user (if provided)
        if (request.getIdCard() != null && !request.getIdCard().isEmpty()) {
            if (user.getIdCard() == null || !user.getIdCard().equals(request.getIdCard())) {
                boolean idCardExists = userRepository.existsByIdCardAndIdNot(request.getIdCard(), userId);
                if (idCardExists) {
                    throw new IllegalArgumentException("CMND/CCCD đã được sử dụng bởi tài khoản khác");
                }
            }
        }

        // Update user information
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setIdCard(request.getIdCard());

        userRepository.save(user);

        log.info("✅ User profile updated successfully for user: {}", userId);
        return UserProfileResponse.fromEntity(user);
    }

    public User getUserEntityById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
    }

    // ===== ADMIN METHODS =====

    public Page<UserResponse> getUsers(UserStatus status, Pageable pageable) {
        log.info("👥 Getting users list - Status: {}, Page: {}", status, pageable.getPageNumber());

        Page<User> users;
        if (status != null) {
            users = userRepository.findByStatus(status, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        log.info("✅ Retrieved {} users", users.getTotalElements());
        return users.map(UserResponse::fromEntity);
    }

    public Page<UserResponse> searchUsers(String keyword, UserStatus status, Pageable pageable) {
        log.info("🔍 Searching users - Keyword: {}, Status: {}", keyword, status);

        Page<User> users;
        if (status != null) {
            users = userRepository.searchByKeywordAndStatus(keyword, status, pageable);
        } else {
            users = userRepository.searchByKeyword(keyword, pageable);
        }

        log.info("✅ Found {} users matching search criteria", users.getTotalElements());
        return users.map(UserResponse::fromEntity);
    }

    public UserResponse getUserById(Integer id) {
        log.info("👤 Getting user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        log.info("✅ User retrieved successfully: {}", user.getEmail());
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateUser(Integer id, UpdateUserRequest request) {
        log.info("📝 Admin updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // Check email uniqueness
        if (!user.getEmail().equals(request.getEmail())) {
            boolean emailExists = userRepository.existsByEmailAndIdNot(request.getEmail(), id);
            if (emailExists) {
                throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác");
            }
        }

        // Check idCard uniqueness
        if (request.getIdCard() != null && !request.getIdCard().isEmpty()) {
            if (user.getIdCard() == null || !user.getIdCard().equals(request.getIdCard())) {
                boolean idCardExists = userRepository.existsByIdCardAndIdNot(request.getIdCard(), id);
                if (idCardExists) {
                    throw new IllegalArgumentException("CMND/CCCD đã được sử dụng bởi tài khoản khác");
                }
            }
        }

        // Update user information
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setIdCard(request.getIdCard());

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        // TODO: Handle busCompanyId if needed

        userRepository.save(user);

        log.info("✅ User updated successfully by admin: {}", id);
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public void blockUser(Integer id) {
        log.info("🔒 Blocking user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new IllegalArgumentException("Người dùng đã bị khóa trước đó");
        }

        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);

        log.info("✅ User blocked successfully: {}", id);
    }

    @Transactional
    public void unblockUser(Integer id) {
        log.info("🔓 Unblocking user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        if (user.getStatus() != UserStatus.BLOCKED) {
            throw new IllegalArgumentException("Người dùng không ở trạng thái bị khóa");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("✅ User unblocked successfully: {}", id);
    }

    @Transactional
    public void deleteUser(Integer adminId, Integer userId, DeleteUserRequest request) {
        log.info("🗑️ Deleting user: {} by admin: {} (Hard: {})", userId, adminId, request.isHardDelete());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin không tồn tại"));

        if (request.isHardDelete()) {
            // Hard delete - remove completely
            userRepository.delete(user);
            log.info("✅ User hard deleted successfully: {}", userId);
        } else {
            // Soft delete - move to deleted_users table
            DeletedUser deletedUser = DeletedUser.fromUser(
                    user,
                    adminId,
                    admin.getFirstName() + " " + admin.getLastName(),
                    request.getReason(),
                    request.getNotes());

            deletedUserRepository.save(deletedUser);
            userRepository.delete(user);

            log.info("✅ User soft deleted successfully: {}", userId);
        }
    }

    public Page<DeletedUserResponse> getDeletedUsers(int page, int size, String sortBy, String sortDir) {
        log.info("🗑️ Getting deleted users list - Page: {}, Size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DeletedUser> deletedUsers = deletedUserRepository.findAllByOrderByDeletedAtDesc(pageable);

        log.info("✅ Retrieved {} deleted users", deletedUsers.getTotalElements());
        return deletedUsers.map(this::mapToDeletedUserResponse);
    }

    @Transactional
    public UserResponse restoreUser(Integer deletedUserId) {
        log.info("♻️ Restoring deleted user: {}", deletedUserId);

        DeletedUser deletedUser = deletedUserRepository.findById(deletedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Bản ghi người dùng đã xóa không tồn tại"));

        // Check if user with same email already exists
        boolean emailExists = userRepository.existsByEmail(deletedUser.getEmail());
        if (emailExists) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác, không thể khôi phục");
        }

        // Check if user with same idCard already exists (if idCard is not null)
        if (deletedUser.getIdCard() != null && !deletedUser.getIdCard().isEmpty()) {
            boolean idCardExists = userRepository.existsByIdCard(deletedUser.getIdCard());
            if (idCardExists) {
                throw new IllegalArgumentException("CMND/CCCD đã được sử dụng bởi tài khoản khác, không thể khôi phục");
            }
        }

        // Restore user
        User restoredUser = new User();
        restoredUser.setFirstName(deletedUser.getFirstName());
        restoredUser.setLastName(deletedUser.getLastName());
        restoredUser.setEmail(deletedUser.getEmail());
        restoredUser.setPhone(deletedUser.getPhone());
        restoredUser.setIdCard(deletedUser.getIdCard());
        restoredUser.setStatus(UserStatus.ACTIVE);
        // TODO: Set default password or require password reset
        restoredUser.setPassword("$2a$10$defaultPasswordHash"); // Placeholder

        userRepository.save(restoredUser);
        deletedUserRepository.delete(deletedUser);

        log.info("✅ User restored successfully: {}", restoredUser.getId());
        return UserResponse.fromEntity(restoredUser);
    }

    private DeletedUserResponse mapToDeletedUserResponse(DeletedUser deletedUser) {
        DeletedUserResponse response = new DeletedUserResponse();
        response.setId(deletedUser.getId());
        response.setOriginalUserId(deletedUser.getOriginalUserId());
        response.setFirstName(deletedUser.getFirstName());
        response.setLastName(deletedUser.getLastName());
        response.setFullName(deletedUser.getFirstName() + " " + deletedUser.getLastName());
        response.setEmail(deletedUser.getEmail());
        response.setPhone(deletedUser.getPhone());
        response.setIdCard(deletedUser.getIdCard());
        response.setBusCompanyName(deletedUser.getBusCompanyName());
        response.setDeleteReason(deletedUser.getDeleteReason());
        response.setDeleteNotes(deletedUser.getDeleteNotes());
        response.setDeletedByAdminName(deletedUser.getDeletedByAdminName());
        response.setDeletedByAdminId(deletedUser.getDeletedByAdminId());
        response.setDeletedAt(deletedUser.getDeletedAt());
        response.setOriginalCreatedAt(deletedUser.getOriginalCreatedAt());
        return response;
    }
}
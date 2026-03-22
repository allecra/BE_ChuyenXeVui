package com.example.ckdatveexe.module.user.service;

import com.example.ckdatveexe.module.user.dto.*;
import com.example.ckdatveexe.shared.entity.DeletedUser;
import com.example.ckdatveexe.shared.entity.User;
import com.example.ckdatveexe.shared.entity.UserStatus;
import com.example.ckdatveexe.shared.repository.DeletedUserRepository;
import com.example.ckdatveexe.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final DeletedUserRepository deletedUserRepository;

    public UserProfileResponse getUserProfile(Integer userId) {
        log.info("👤 Getting user profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        log.info("✅ User profile retrieved successfully for user: {}", userId);
        return UserProfileResponse.fromEntity(user);
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
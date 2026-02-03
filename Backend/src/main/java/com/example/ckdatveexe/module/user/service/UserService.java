package com.example.ckdatveexe.module.user.service;

import com.example.ckdatveexe.module.user.dto.*;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final DeletedUserRepository deletedUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ===================== MAPPER =====================

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus()
        );
    }

    private DeletedUserResponse toDeletedUserResponse(DeletedUser du) {
        DeletedUserResponse res = new DeletedUserResponse();
        res.setId(du.getId());
        res.setOriginalUserId(du.getOriginalUserId());
        res.setEmail(du.getEmail());
        res.setFirstName(du.getFirstName());
        res.setLastName(du.getLastName());
        res.setPhone(du.getPhone());
        res.setRole(du.getRole());
        res.setStatus(du.getStatus());
        res.setOriginalCreatedAt(du.getOriginalCreatedAt());
        res.setOriginalUpdatedAt(du.getOriginalUpdatedAt());
        res.setDeletedAt(du.getDeletedAt());
        res.setDeletedBy(du.getDeletedBy());
        res.setDeletionReason(du.getDeletionReason());
        return res;
    }

    // ===================== READ =====================

    public Page<UserResponse> getAllUsers(String keyword, UserStatus status, Pageable pageable) {
        Page<User> page;

        if (keyword != null && status != null) {
            page = userRepository.searchByKeywordAndStatus(keyword, status, pageable);
        } else if (keyword != null) {
            page = userRepository.searchByKeyword(keyword, pageable);
        } else if (status != null) {
            page = userRepository.findByStatus(status, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }

        return page.map(this::toResponse);
    }

    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        return toResponse(user);
    }

    public Page<UserResponse> getUsers(UserStatus status, Pageable pageable) {
        Page<User> page = (status == null)
                ? userRepository.findAll(pageable)
                : userRepository.findByStatus(status, pageable);

        return page.map(this::toResponse);
    }

    public Page<UserResponse> searchUsers(String keyword, UserStatus status, Pageable pageable) {
        Page<User> page = (status == null)
                ? userRepository.searchByKeyword(keyword, pageable)
                : userRepository.searchByKeywordAndStatus(keyword, status, pageable);

        return page.map(this::toResponse);
    }

    // ===================== DELETED LIST =====================

    public Page<DeletedUserResponse> getDeletedUsers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DeletedUser> deletedPage = deletedUserRepository.findAll(pageable);

        return deletedPage.map(this::toDeletedUserResponse);
    }

    // ===================== UPDATE INFO =====================

    public UserResponse updateUser(Integer id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());

        if (request.getLastName() != null)
            user.setLastName(request.getLastName());

        if (request.getPhone() != null)
            user.setPhone(request.getPhone());

        return toResponse(userRepository.save(user));
    }

    // ===================== STATUS =====================

    public void updateUserStatus(Integer id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        user.setStatus(status);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Integer deletedBy, Integer userId, DeleteUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (user.getId().equals(deletedBy)) {
            throw new IllegalArgumentException("Không thể tự xóa chính mình");
        }

        if (request.isHardDelete()) {
            userRepository.delete(user);
            log.info("Hard delete user id={} by admin {}", userId, deletedBy);
            return;
        }

        DeletedUser du = new DeletedUser();
        du.setOriginalUserId(user.getId());
        du.setEmail(user.getEmail());
        du.setFirstName(user.getFirstName());
        du.setLastName(user.getLastName());
        du.setPhone(user.getPhone());

        String rolesStr = user.getRoles().stream()
                .map(r -> r.getRoleName().name())
                .collect(Collectors.joining(", "));
        du.setRole(rolesStr.isEmpty() ? "ROLE_USER" : rolesStr);

        du.setStatus(user.getStatus());
        du.setOriginalCreatedAt(user.getCreatedAt());
        du.setOriginalUpdatedAt(user.getUpdatedAt());
        du.setDeletedBy(deletedBy);
        du.setDeletionReason(request.getDeletionReason() != null ? request.getDeletionReason() : "Xóa bởi admin");

        DeletedUser savedDu = deletedUserRepository.save(du);
        log.info("Soft delete user id={}, saved to deleted_users id={}", userId, savedDu.getId());

        userRepository.delete(user);
    }

    @Transactional
    public UserResponse restoreUser(Integer deletedUserId) {
        log.info("Bắt đầu khôi phục deletedUserId={}", deletedUserId);

        DeletedUser du = deletedUserRepository.findById(deletedUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi đã xóa với id=" + deletedUserId));

        log.info("Tìm thấy deleted user: originalUserId={}, email={}", du.getOriginalUserId(), du.getEmail());

        if (userRepository.existsByEmail(du.getEmail())) {
            throw new IllegalStateException("Email đã tồn tại, không thể khôi phục");
        }

        User restored = new User();
        restored.setFirstName(du.getFirstName());
        restored.setLastName(du.getLastName());
        restored.setEmail(du.getEmail());
        restored.setPhone(du.getPhone());
        restored.setStatus(UserStatus.ACTIVE);

        String[] roleNames = du.getRole().split(",\\s*");
        Set<Role> roles = new HashSet<>();
        for (String roleStr : roleNames) {
            if (!roleStr.isBlank()) {
                try {
                    RoleName roleName = RoleName.valueOf(roleStr.trim());
                    Role role = roleRepository.findByRoleName(roleName)
                            .orElse(null);
                    if (role != null) {
                        roles.add(role);
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid role name in deleted user: {}", roleStr);
                }
            }
        }
        if (roles.isEmpty()) {
            Role defaultRole = roleRepository.findByRoleName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Role ROLE_USER không tồn tại"));
            roles.add(defaultRole);
        }
        restored.setRoles(roles);

        String randomPassword = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
        String encodedPassword = passwordEncoder.encode(randomPassword);
        restored.setPassword(encodedPassword);

        log.info("User {} restored with temporary password: {}", du.getEmail(), randomPassword);
        // TODO: Gửi email reset password ở đây

        User saved = userRepository.save(restored);
        deletedUserRepository.delete(du);

        log.info("Khôi phục hoàn tất: new user id={}, original id={}", saved.getId(), du.getOriginalUserId());

        return toResponse(saved);
    }

    public void blockUser(Integer id) {
        updateUserStatus(id, UserStatus.BLOCKED);
    }

    public void unblockUser(Integer id) {
        updateUserStatus(id, UserStatus.ACTIVE);
    }
}
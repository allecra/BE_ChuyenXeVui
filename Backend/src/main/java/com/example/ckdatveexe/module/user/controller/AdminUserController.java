package com.example.ckdatveexe.module.user.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.module.user.dto.*;
import com.example.ckdatveexe.module.user.service.UserService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "User Admin", description = "API quản lý tài khoản người dùng cho Admin")
public class AdminUserController {

    private final UserService userService;

    // GET list
    @GetMapping
    @Operation(summary = "Lấy danh sách user")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> users = userService.getUsers(status, pageable);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách user thành công", users));
    }

    // GET search
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm user")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> users = userService.searchUsers(keyword, status, pageable);

        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm user thành công", users));
    }

    // GET detail
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết user")
    public ResponseEntity<ApiResponse<UserResponse>> getUserDetail(@PathVariable Integer id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy user thành công", user));
    }

    // PUT update
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật user")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Integer id,
            @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật user thành công", user));
    }

    // PUT block
    @PutMapping("/{id}/block")
    @Operation(summary = "Khóa user")
    public ResponseEntity<ApiResponse<Void>> blockUser(@PathVariable Integer id) {
        userService.blockUser(id);
        return ResponseEntity.ok(ApiResponse.success("Khóa user thành công"));
    }

    // PUT unblock
    @PutMapping("/{id}/unblock")
    @Operation(summary = "Mở khóa user")
    public ResponseEntity<ApiResponse<Void>> unblockUser(@PathVariable Integer id) {
        userService.unblockUser(id);
        return ResponseEntity.ok(ApiResponse.success("Mở khóa user thành công"));
    }

    // DELETE user
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa người dùng (soft/hard delete)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Integer id,
            @RequestBody DeleteUserRequest request,
            Authentication authentication) {

        UserDetailsImpl admin = (UserDetailsImpl) authentication.getPrincipal();
        Integer adminId = admin.getUser().getId();

        userService.deleteUser(adminId, id, request);

        String msg = request.isHardDelete()
                ? "Xóa vĩnh viễn thành công"
                : "Xóa mềm thành công (lưu lịch sử)";

        return ResponseEntity.ok(ApiResponse.success(msg));
    }

    // GET deleted list (mới thêm)
    @GetMapping("/deleted")
    @Operation(summary = "Danh sách người dùng đã xóa mềm")
    public ResponseEntity<ApiResponse<Page<DeletedUserResponse>>> getDeletedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deletedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<DeletedUserResponse> deleted = userService.getDeletedUsers(page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách user đã xóa thành công", deleted));
    }

    // POST restore (đổi từ PUT sang POST)
    @PostMapping("/deleted/{deletedUserId}/restore")
    @Operation(summary = "Khôi phục người dùng đã xóa mềm (POST đồng bộ với Bus)")
    public ResponseEntity<ApiResponse<UserResponse>> restoreUser(@PathVariable Integer deletedUserId) {
        UserResponse restored = userService.restoreUser(deletedUserId);
        return ResponseEntity.ok(ApiResponse.success("Khôi phục user thành công", restored));
    }
}
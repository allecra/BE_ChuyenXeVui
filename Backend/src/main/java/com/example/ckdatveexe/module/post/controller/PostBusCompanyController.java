package com.example.ckdatveexe.module.post.controller;

import com.example.ckdatveexe.module.post.dto.PostCreateRequest;
import com.example.ckdatveexe.module.post.dto.PostUpdateRequest;
import com.example.ckdatveexe.module.post.service.PostService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bus/posts")
@RequiredArgsConstructor
@Tag(name = "Post Bus API", description = "Nhà xe quản lý bài viết")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('BUS_COMPANY')")
public class PostBusCompanyController {

    private final PostService postService;

    // ================= CREATE =================
    @PostMapping
    @Operation(summary = "Nhà xe tạo bài viết (PENDING)")
    public ResponseEntity<ApiResponse> create(
            @RequestBody PostCreateRequest request,
            Authentication auth) {

        String role = auth.getAuthorities().iterator().next().getAuthority();

        return ResponseEntity.ok(
                ApiResponse.success("Tạo bài viết thành công",
                        postService.create(request, auth.getName(), role))
        );
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    @Operation(summary = "Nhà xe sửa bài (chỉ PENDING/REJECTED)")
    public ResponseEntity<ApiResponse> update(
            @PathVariable Integer id,
            @RequestBody PostUpdateRequest request,
            Authentication auth) {

        return ResponseEntity.ok(
                ApiResponse.success("Sửa bài thành công",
                        postService.updateByBus(id, request, auth.getName()))
        );
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @Operation(summary = "Nhà xe xóa bài (chỉ PENDING)")
    public ResponseEntity<ApiResponse> delete(
            @PathVariable Integer id,
            Authentication auth) {

        postService.deleteByBus(id, auth.getName());

        return ResponseEntity.ok(
                ApiResponse.success("Xóa bài thành công")
        );
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(
                ApiResponse.success("Danh sách bài của bạn",
                        postService.getMyPosts(auth.getName(), pageable))
        );
    }
}
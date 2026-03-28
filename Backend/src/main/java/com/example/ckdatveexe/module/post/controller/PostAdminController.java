package com.example.ckdatveexe.module.post.controller;

import com.example.ckdatveexe.module.post.dto.*;
import com.example.ckdatveexe.module.post.service.PostService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import com.example.ckdatveexe.shared.entity.PostStatus;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/posts")
@RequiredArgsConstructor
@Tag(name = "Post Admin API", description = "Quản lý bài viết")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class PostAdminController {

    private final PostService postService;

    @GetMapping
    @Operation(summary = "Danh sách bài viết")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAll(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(
                ApiResponse.success("OK",
                        postService.getAllForAdmin(keyword, status, pageable))
        );
    }

    @PostMapping
    @Operation(summary = "Thêm bài viết")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createPost(
            @RequestBody PostCreateRequest request,
            Authentication auth) {

        return ResponseEntity.ok(
                ApiResponse.success("Created",
                        postService.create(request, auth.getName()))
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Sửa bài viết")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updatePost(
            @PathVariable Integer id,
            @RequestBody PostUpdateRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Updated",
                        postService.update(id, request))
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa bài viết")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deletePost(@PathVariable Integer id) {
        postService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Deleted"));
    }
}
package com.example.ckdatveexe.module.post.controller;

import com.example.ckdatveexe.module.post.dto.PostResponse;
import com.example.ckdatveexe.module.post.service.PostService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Post Public API", description = "API hiển thị bài viết cho người dùng")
@Slf4j
public class PostController {

    private final PostService postService;

    // ================= GET LIST =================
    @GetMapping
    @Operation(
            summary = "Lấy danh sách bài viết đã đăng",
            description = "Frontend dùng để hiển thị danh sách bài viết (chỉ PUBLISHED)"
    )
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("🌍 GET /posts - Get published posts");

        try {
            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by("createdAt").descending() // 🔥 nên dùng cái này
            );

            Page<PostResponse> posts = postService.getPublished(pageable);

            log.info("✅ Retrieved {} posts", posts.getTotalElements());

            return ResponseEntity.ok(
                    ApiResponse.<Page<PostResponse>>builder()
                            .success(true)
                            .message("Lấy danh sách bài viết thành công")
                            .data(posts)
                            .build()
            );

        } catch (Exception e) {
            log.error("💥 Error getting posts", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<PostResponse>>builder()
                            .success(false)
                            .message("Lỗi hệ thống khi lấy danh sách bài viết")
                            .build());
        }
    }

    // ================= GET DETAIL =================
    @GetMapping("/{id}")
    @Operation(
            summary = "Xem chi tiết bài viết",
            description = "Lấy chi tiết 1 bài viết (chỉ khi status = PUBLISHED)"
    )
    public ResponseEntity<ApiResponse<PostResponse>> getDetail(@PathVariable Integer id) {

        log.info("🌍 GET /posts/{} - Get post detail", id);

        try {
            PostResponse post = postService.getPublishedById(id);

            return ResponseEntity.ok(
                    ApiResponse.<PostResponse>builder()
                            .success(true)
                            .message("Lấy chi tiết bài viết thành công")
                            .data(post)
                            .build()
            );

        } catch (Exception e) {
            log.error("💥 Error getting post detail: {}", id, e);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<PostResponse>builder()
                            .success(false)
                            .message("Không tìm thấy bài viết")
                            .build());
        }
    }
}
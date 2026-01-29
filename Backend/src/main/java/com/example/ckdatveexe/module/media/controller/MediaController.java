package com.example.ckdatveexe.module.media.controller;

import com.example.ckdatveexe.module.media.service.CloudinaryService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Media", description = "Upload media to Cloudinary")
@SecurityRequirement(name = "Bearer Authentication") // Yêu cầu authentication
public class MediaController {

        private final CloudinaryService cloudinaryService;

        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Upload image", description = "Upload file to Cloudinary and return its URL")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_COMPANY')") // Yêu cầu role
        public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFile(
                        @RequestParam("file") MultipartFile file,
                        @RequestParam(value = "folder", required = false) String folder) {

                log.info("📁 [MEDIA] POST /media/upload - Upload file");
                log.info("📁 [MEDIA] File name: {}, Size: {} bytes, Content type: {}",
                                file.getOriginalFilename(), file.getSize(), file.getContentType());

                try {
                        // Validate file
                        if (file.isEmpty()) {
                                log.error("❌ [MEDIA] 400 BAD_REQUEST - Empty file uploaded");
                                return ResponseEntity.badRequest()
                                                .body(ApiResponse.<Map<String, Object>>builder()
                                                                .success(false)
                                                                .message("File không được để trống")
                                                                .build());
                        }

                        // Check file size (10MB limit)
                        if (file.getSize() > 10 * 1024 * 1024) {
                                log.error("❌ [MEDIA] 400 BAD_REQUEST - File too large: {} bytes", file.getSize());
                                return ResponseEntity.badRequest()
                                                .body(ApiResponse.<Map<String, Object>>builder()
                                                                .success(false)
                                                                .message("File quá lớn. Kích thước tối đa là 10MB")
                                                                .build());
                        }

                        // Check file type
                        String contentType = file.getContentType();
                        if (contentType == null || !contentType.startsWith("image/")) {
                                log.error("❌ [MEDIA] 400 BAD_REQUEST - Invalid file type: {}", contentType);
                                return ResponseEntity.badRequest()
                                                .body(ApiResponse.<Map<String, Object>>builder()
                                                                .success(false)
                                                                .message("Chỉ chấp nhận file hình ảnh (jpg, png, gif, etc.)")
                                                                .build());
                        }

                        Map<String, Object> uploadResult = cloudinaryService.upload(file, folder);

                        Map<String, Object> data = new HashMap<>();
                        data.put("url", uploadResult.get("secure_url"));
                        data.put("publicId", uploadResult.get("public_id"));
                        data.put("format", uploadResult.get("format"));
                        data.put("resourceType", uploadResult.get("resource_type"));
                        data.put("bytes", uploadResult.get("bytes"));

                        log.info("✅ [MEDIA] 201 CREATED - File uploaded successfully with publicId: {}",
                                        uploadResult.get("public_id"));
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.<Map<String, Object>>builder()
                                                        .success(true)
                                                        .message("Upload file thành công")
                                                        .data(data)
                                                        .build());

                } catch (IllegalArgumentException e) {
                        log.error("❌ [MEDIA] 400 BAD_REQUEST - Invalid upload parameters: {}", e.getMessage());
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<Map<String, Object>>builder()
                                                        .success(false)
                                                        .message("Tham số upload không hợp lệ: " + e.getMessage())
                                                        .build());

                } catch (Exception e) {
                        log.error("💥 [MEDIA] 500 INTERNAL_SERVER_ERROR - Failed to upload file", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Map<String, Object>>builder()
                                                        .success(false)
                                                        .message("Lỗi hệ thống khi upload file")
                                                        .build());
                }
        }
}

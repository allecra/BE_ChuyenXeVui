package com.example.ckdatveexe.module.media.controller;

import com.example.ckdatveexe.module.media.service.CloudinaryService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class MediaController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image", description = "Upload file to Cloudinary and return its URL")
    public ResponseEntity<ApiResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {

        Map<String, Object> uploadResult = cloudinaryService.upload(file, folder);

        Map<String, Object> data = new HashMap<>();
        data.put("url", uploadResult.get("secure_url"));
        data.put("publicId", uploadResult.get("public_id"));
        data.put("format", uploadResult.get("format"));
        data.put("resourceType", uploadResult.get("resource_type"));
        data.put("bytes", uploadResult.get("bytes"));

        log.info("Uploaded file to Cloudinary with publicId={}", uploadResult.get("public_id"));

        return ResponseEntity.ok(ApiResponse.success("Upload successfully", data));
    }
}


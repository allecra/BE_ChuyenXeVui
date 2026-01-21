package com.example.ckdatveexe.module.media.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @SuppressWarnings("unchecked")
    public Map<String, Object> upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String targetFolder = (folder == null || folder.isBlank()) ? "uploads" : folder;
        try {
            Map<?, ?> uploadResult = cloudinary.uploader()
                    .upload(file.getBytes(), ObjectUtils.asMap(
                            "folder", targetFolder,
                            "resource_type", "auto"));
            Map<String, Object> typedResult = new HashMap<>((Map<String, Object>) uploadResult);
            return typedResult;
        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new RuntimeException("Upload image failed");
        }
    }
}


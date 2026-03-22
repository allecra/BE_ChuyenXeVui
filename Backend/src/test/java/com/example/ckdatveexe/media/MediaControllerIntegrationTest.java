package com.example.ckdatveexe.media;

import com.example.ckdatveexe.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Media Management API Integration Tests")
class MediaControllerIntegrationTest extends BaseIntegrationTest {

    // ==================== MEDIA UPLOAD APIs ====================

    @Test
    @DisplayName("POST /media/upload - Should upload image file successfully")
    void testUploadImageFile() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "fake image content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(imageFile)
                .header("Authorization", "Bearer " + userAccessToken)
                .param("folder", "avatars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").exists())
                .andExpect(jsonPath("$.data.publicId").exists())
                .andExpect(jsonPath("$.data.format").value("jpg"));
    }

    @Test
    @DisplayName("POST /media/upload - Should upload PNG image")
    void testUploadPngImage() throws Exception {
        MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                "fake png content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(pngFile)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.format").value("png"));
    }

    @Test
    @DisplayName("POST /media/upload - Should upload GIF image")
    void testUploadGifImage() throws Exception {
        MockMultipartFile gifFile = new MockMultipartFile(
                "file",
                "test-animation.gif",
                "image/gif",
                "fake gif content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(gifFile)
                .header("Authorization", "Bearer " + userAccessToken)
                .param("folder", "animations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.format").value("gif"));
    }

    @Test
    @DisplayName("POST /media/upload - Should upload to specific folder")
    void testUploadToSpecificFolder() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "bus-photo.jpg",
                "image/jpeg",
                "fake bus photo content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(imageFile)
                .header("Authorization", "Bearer " + companyAccessToken)
                .param("folder", "buses/photos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").exists());
    }

    @Test
    @DisplayName("POST /media/upload - Should upload without folder parameter")
    void testUploadWithoutFolder() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "general-image.jpg",
                "image/jpeg",
                "fake general image content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(imageFile)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").exists());
    }

    // ==================== AUTHORIZATION TESTS ====================

    @Test
    @DisplayName("POST /media/upload - Should fail without authentication")
    void testUploadFileUnauthorized() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "fake image content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(imageFile))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /media/upload - Should work with USER role")
    void testUploadFileWithUserRole() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "user-avatar.jpg",
                "image/jpeg",
                "fake avatar content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(imageFile)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /media/upload - Should work with BUS_COMPANY role")
    void testUploadFileWithCompanyRole() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "company-logo.jpg",
                "image/jpeg",
                "fake logo content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(imageFile)
                .header("Authorization", "Bearer " + companyAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /media/upload - Should work with ADMIN role")
    void testUploadFileWithAdminRole() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "admin-image.jpg",
                "image/jpeg",
                "fake admin image content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(imageFile)
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("POST /media/upload - Should fail with empty file")
    void testUploadEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]);

        mockMvc.perform(multipart("/media/upload")
                .file(emptyFile)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /media/upload - Should fail with non-image file")
    void testUploadNonImageFile() throws Exception {
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "This is a text document".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(textFile)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /media/upload - Should fail with PDF file")
    void testUploadPdfFile() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "fake pdf content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(pdfFile)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /media/upload - Should fail with video file")
    void testUploadVideoFile() throws Exception {
        MockMultipartFile videoFile = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "fake video content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(videoFile)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /media/upload - Should fail with large file (>10MB)")
    void testUploadLargeFile() throws Exception {
        // Create a file larger than 10MB (10 * 1024 * 1024 + 1 bytes)
        byte[] largeContent = new byte[10 * 1024 * 1024 + 1];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-image.jpg",
                "image/jpeg",
                largeContent);

        mockMvc.perform(multipart("/media/upload")
                .file(largeFile)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /media/upload - Should fail without file parameter")
    void testUploadWithoutFile() throws Exception {
        mockMvc.perform(multipart("/media/upload")
                .header("Authorization", "Bearer " + userAccessToken)
                .param("folder", "test"))
                .andExpect(status().isBadRequest());
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("POST /media/upload - Should handle file at size limit (10MB)")
    void testUploadFileAtSizeLimit() throws Exception {
        // Create a file exactly 10MB (10 * 1024 * 1024 bytes)
        byte[] maxSizeContent = new byte[10 * 1024 * 1024];
        MockMultipartFile maxSizeFile = new MockMultipartFile(
                "file",
                "max-size-image.jpg",
                "image/jpeg",
                maxSizeContent);

        mockMvc.perform(multipart("/media/upload")
                .file(maxSizeFile)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bytes").value(10 * 1024 * 1024));
    }

    @Test
    @DisplayName("POST /media/upload - Should handle special characters in filename")
    void testUploadFileWithSpecialCharacters() throws Exception {
        MockMultipartFile specialFile = new MockMultipartFile(
                "file",
                "test-image-with-special-chars-@#$%^&().jpg",
                "image/jpeg",
                "fake image content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(specialFile)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /media/upload - Should handle Unicode filename")
    void testUploadFileWithUnicodeFilename() throws Exception {
        MockMultipartFile unicodeFile = new MockMultipartFile(
                "file",
                "ảnh-xe-buýt-việt-nam.jpg",
                "image/jpeg",
                "fake vietnamese image content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(unicodeFile)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /media/upload - Should handle nested folder paths")
    void testUploadToNestedFolder() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "nested-image.jpg",
                "image/jpeg",
                "fake nested image content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(imageFile)
                .header("Authorization", "Bearer " + companyAccessToken)
                .param("folder", "company/buses/interior/photos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /media/upload - Should handle different image formats")
    void testUploadDifferentImageFormats() throws Exception {
        // Test WEBP format
        MockMultipartFile webpFile = new MockMultipartFile(
                "file",
                "test-image.webp",
                "image/webp",
                "fake webp content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(webpFile)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Test BMP format
        MockMultipartFile bmpFile = new MockMultipartFile(
                "file",
                "test-image.bmp",
                "image/bmp",
                "fake bmp content".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(bmpFile)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /media/upload - Should return proper response structure")
    void testUploadResponseStructure() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "response-test.jpg",
                "image/jpeg",
                "fake image for response test".getBytes());

        mockMvc.perform(multipart("/media/upload")
                .file(imageFile)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.url").exists())
                .andExpect(jsonPath("$.data.publicId").exists())
                .andExpect(jsonPath("$.data.format").exists())
                .andExpect(jsonPath("$.data.resourceType").value("image"))
                .andExpect(jsonPath("$.data.bytes").exists());
    }
}
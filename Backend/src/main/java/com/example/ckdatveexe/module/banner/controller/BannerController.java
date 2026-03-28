package com.example.ckdatveexe.module.banner.controller;

import com.example.ckdatveexe.module.banner.dto.BannerResponse;
import com.example.ckdatveexe.module.banner.service.BannerService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
@Tag(name = "Banner Public", description = "API lấy banner để hiển thị cho người dùng")
public class BannerController {

    private final BannerService bannerService;

    /**
     * Lấy tất cả banner đang hoạt động (ACTIVE)
     * Dùng cho trang chủ, sidebar, popup...
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách banner đang hoạt động",
            description = "API này dùng cho frontend để hiển thị banner trên website")
    public ResponseEntity<ApiResponse> getActiveBanners() {
        // Lấy tối đa 50 banner để tránh load quá nhiều
        List<BannerResponse> banners = bannerService.getActiveBannersForFrontend();

        return ResponseEntity.ok(
                ApiResponse.success("Lấy banner thành công", banners)
        );
    }

    /**
     * Lấy banner theo vị trí cụ thể (rất hữu ích)
     * Ví dụ: HOME_TOP, HOME_BOTTOM, SIDEBAR, DETAIL_PAGE...
     */
    @GetMapping("/position/{position}")
    @Operation(summary = "Lấy banner theo vị trí cụ thể")
    public ResponseEntity<ApiResponse> getActiveBannersByPosition(
            @PathVariable String position) {

        List<BannerResponse> banners = bannerService.getActiveBannersByPosition(position);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy banner theo vị trí thành công", banners)
        );
    }
}
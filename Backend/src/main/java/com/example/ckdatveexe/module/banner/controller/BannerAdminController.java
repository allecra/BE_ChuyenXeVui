package com.example.ckdatveexe.module.banner.controller;

import com.example.ckdatveexe.module.banner.dto.*;
import com.example.ckdatveexe.module.banner.service.BannerService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/banners")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
@Tag(name = "Banner Admin", description = "API quản lý banner (quảng cáo/ưu đãi)")
public class BannerAdminController {

    private final BannerService bannerService;

    @GetMapping
    @Operation(summary = "Lấy danh sách banner đang quản lý (ACTIVE + INACTIVE)")
    public ResponseEntity<ApiResponse> getAll(
            @RequestParam(required = false) String position,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách banner thành công",
                        bannerService.getAllForAdmin(position, pageable))
        );
    }

    @GetMapping("/trash")
    @Operation(summary = "Xem thùng rác - Banner đã xóa mềm (DELETED)")
    public ResponseEntity<ApiResponse> getTrash(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách thùng rác thành công",
                        bannerService.getTrashBanners(pageable))
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết banner")
    public ResponseEntity<ApiResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.success("Lấy banner thành công", bannerService.getById(id))
        );
    }

    @PostMapping
    @Operation(summary = "Tạo banner mới")
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody BannerCreateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Tạo banner thành công", bannerService.create(request))
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật banner (chỉ ACTIVE/INACTIVE)")
    public ResponseEntity<ApiResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody BannerUpdateRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật banner thành công", bannerService.update(id, request))
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm banner (chuyển sang DELETED)")
    public ResponseEntity<ApiResponse> softDelete(@PathVariable Integer id) {
        bannerService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa mềm banner thành công"));
    }

    @PutMapping("/{id}/restore")
    @Operation(summary = "Khôi phục banner từ thùng rác")
    public ResponseEntity<ApiResponse> restore(@PathVariable Integer id) {
        bannerService.restore(id);
        return ResponseEntity.ok(ApiResponse.success("Khôi phục banner thành công"));
    }

    @DeleteMapping("/{id}/hard")
    @Operation(summary = "Xóa banner vĩnh viễn")
    public ResponseEntity<ApiResponse> hardDelete(@PathVariable Integer id) {
        bannerService.hardDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa vĩnh viễn thành công"));
    }
}
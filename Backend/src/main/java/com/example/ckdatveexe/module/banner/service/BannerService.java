package com.example.ckdatveexe.module.banner.service;

import com.example.ckdatveexe.module.banner.dto.*;
import com.example.ckdatveexe.shared.entity.Banner;
import com.example.ckdatveexe.shared.entity.BannerStatus;
import com.example.ckdatveexe.shared.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    private BannerResponse toResponse(Banner banner) {
        return new BannerResponse(
                banner.getId(),
                banner.getBannerUrl(),
                banner.getPosition(),
                banner.getStatus()
        );
    }

    // Danh sách chính Admin: ACTIVE + INACTIVE
    public Page<BannerResponse> getAllForAdmin(String position, Pageable pageable) {
        List<BannerStatus> visibleStatuses = List.of(BannerStatus.ACTIVE, BannerStatus.INACTIVE);

        if (position != null && !position.isEmpty()) {
            return bannerRepository.findByPositionContainingIgnoreCaseAndStatusIn(
                    position, visibleStatuses, pageable).map(this::toResponse);
        }
        return bannerRepository.findByStatusIn(visibleStatuses, pageable).map(this::toResponse);
    }

    // Thùng rác: Chỉ DELETED
    public Page<BannerResponse> getTrashBanners(Pageable pageable) {
        return bannerRepository.findByStatus(BannerStatus.DELETED, pageable)
                .map(this::toResponse);
    }

    // Tạo mới - Mặc định ACTIVE
    public BannerResponse create(BannerCreateRequest request) {
        Banner banner = new Banner();
        banner.setBannerUrl(request.getBannerUrl().trim());
        banner.setPosition(request.getPosition().trim());
        banner.setStatus(BannerStatus.ACTIVE);

        return toResponse(bannerRepository.save(banner));
    }

    // Update - Chỉ cho phép ACTIVE hoặc INACTIVE
    public BannerResponse update(Integer id, BannerUpdateRequest request) {
        Banner banner = findBannerById(id);

        if (request.getBannerUrl() != null && !request.getBannerUrl().trim().isEmpty()) {
            banner.setBannerUrl(request.getBannerUrl().trim());
        }
        if (request.getPosition() != null && !request.getPosition().trim().isEmpty()) {
            banner.setPosition(request.getPosition().trim());
        }
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            try {
                BannerStatus newStatus = BannerStatus.valueOf(request.getStatus().trim().toUpperCase());
                if (newStatus == BannerStatus.DELETED) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Không được cập nhật status thành DELETED. Vui lòng dùng chức năng Xóa mềm.");
                }
                banner.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status không hợp lệ (ACTIVE hoặc INACTIVE)");
            }
        }

        return toResponse(bannerRepository.save(banner));
    }

    public BannerResponse getById(Integer id) {
        return toResponse(findBannerById(id));
    }

    // Xóa mềm
    public void softDelete(Integer id) {
        Banner banner = findBannerById(id);
        if (banner.getStatus() == BannerStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Banner đã bị xóa mềm rồi");
        }
        banner.setStatus(BannerStatus.DELETED);
        bannerRepository.save(banner);
    }

    // Khôi phục
    public void restore(Integer id) {
        Banner banner = findBannerById(id);
        if (banner.getStatus() != BannerStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể khôi phục banner đã xóa mềm");
        }
        banner.setStatus(BannerStatus.ACTIVE);
        bannerRepository.save(banner);
    }

    // Xóa hẳn
    public void hardDelete(Integer id) {
        Banner banner = findBannerById(id);
        if (banner.getStatus() != BannerStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể xóa hẳn banner đã xóa mềm");
        }
        bannerRepository.deleteById(id);
    }

    private Banner findBannerById(Integer id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Banner không tồn tại"));
    }

    // Dành cho frontend: Chỉ lấy ACTIVE và có thể lọc theo vị trí
    public List<BannerResponse> getActiveBannersForFrontend() {
        // Lấy tối đa 50 banner ACTIVE, sắp xếp theo id giảm dần
        Pageable pageable = PageRequest.of(0, 50, Sort.by("id").descending());

        return bannerRepository.findByStatus(BannerStatus.ACTIVE, pageable)
                .map(this::toResponse)
                .getContent();
    }

    /**
     * Lấy banner ACTIVE theo vị trí cụ thể
     * Rất hữu ích cho frontend (ví dụ: banner đầu trang, banner chân trang...)
     */
    public List<BannerResponse> getActiveBannersByPosition(String position) {
        if (position == null || position.trim().isEmpty()) {
            return getActiveBannersForFrontend();
        }

        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").descending());

        return bannerRepository.findByPositionContainingIgnoreCaseAndStatus(
                        position.trim(), BannerStatus.ACTIVE, pageable)
                .map(this::toResponse)
                .getContent();
    }
}
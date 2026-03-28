package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.Banner;
import com.example.ckdatveexe.shared.entity.BannerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Integer> {

    Page<Banner> findByPositionContainingIgnoreCaseAndStatus(
            String position, BannerStatus status, Pageable pageable);
    Page<Banner> findByStatusIn(List<BannerStatus> statuses, Pageable pageable);

    Page<Banner> findByPositionContainingIgnoreCaseAndStatusIn(
            String position, List<BannerStatus> statuses, Pageable pageable);

    Page<Banner> findByStatus(BannerStatus status, Pageable pageable);
}
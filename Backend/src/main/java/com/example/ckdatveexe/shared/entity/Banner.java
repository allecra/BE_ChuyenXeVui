package com.example.ckdatveexe.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "banners")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "banner_url", nullable = false)
    private String bannerUrl;

    @Column(nullable = false, length = 100)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BannerStatus status = BannerStatus.ACTIVE;
}
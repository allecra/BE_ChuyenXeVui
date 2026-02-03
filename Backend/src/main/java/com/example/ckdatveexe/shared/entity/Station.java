package com.example.ckdatveexe.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "stations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String image;

    private String wallpaper;

    @Column(columnDefinition = "LONGTEXT")
    private String descriptions;

    @Column(nullable = false)
    private String location;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "departureStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Route> departureRoutes;

    @OneToMany(mappedBy = "arrivalStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Route> arrivalRoutes;

    @ManyToMany(mappedBy = "stations", fetch = FetchType.LAZY)
    private Set<Bus> buses = new HashSet<>();

    // BusStation relationships
    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BusStation> busStations = new ArrayList<>();

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BusHistory> busHistories;
}
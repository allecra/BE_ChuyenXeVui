package com.example.ckdatveexe.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "route_stations", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "route_id", "station_id" }),
        @UniqueConstraint(columnNames = { "route_id", "order_index" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "distance_from_previous", nullable = false)
    private Integer distanceFromPrevious = 0; // km from previous station

    @Column(name = "price_from_previous", nullable = false)
    private Double priceFromPrevious = 0.0; // price from previous station

    @Column(name = "notes")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors for convenience
    public RouteStation(Route route, Station station, Integer orderIndex) {
        this.route = route;
        this.station = station;
        this.orderIndex = orderIndex;
        this.distanceFromPrevious = 0;
        this.priceFromPrevious = 0.0;
    }

    public RouteStation(Route route, Station station, Integer orderIndex,
            Integer distanceFromPrevious, Double priceFromPrevious) {
        this.route = route;
        this.station = station;
        this.orderIndex = orderIndex;
        this.distanceFromPrevious = distanceFromPrevious;
        this.priceFromPrevious = priceFromPrevious;
    }
}
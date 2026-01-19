package com.example.ckdatveexe.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bus_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "actual_departure_time")
    private LocalDateTime actualDepartureTime;

    @Column(name = "actual_arrival_time")
    private LocalDateTime actualArrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HistoryStatus status = HistoryStatus.SCHEDULED;

    @Column(name = "total_passengers", nullable = false)
    private Integer totalPassengers = 0;

    @Column(name = "total_revenue", nullable = false)
    private Double totalRevenue = 0.0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;
}
package com.example.ckdatveexe.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_buses", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "schedule_id", "bus_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleBus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "schedule_id", nullable = false)
    private Integer scheduleId;

    @Column(name = "bus_id", nullable = false)
    private Integer busId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleBusStatus status = ScheduleBusStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", insertable = false, updatable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", insertable = false, updatable = false)
    private Bus bus;
}
package com.example.ckdatveexe.shared.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //
    private Integer id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String thumbnail;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    private String createdBy;

    private LocalDateTime createdAt;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "reject_reason")
    private String rejectReason;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
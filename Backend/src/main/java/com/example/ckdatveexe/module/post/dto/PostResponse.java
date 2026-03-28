package com.example.ckdatveexe.module.post.dto;

import com.example.ckdatveexe.shared.entity.PostStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PostResponse {

    private Integer id;
    private String title;
    private String content;
    private String thumbnail;
    private PostStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
}
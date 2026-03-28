package com.example.ckdatveexe.module.post.dto;

import lombok.Data;

@Data
public class PostUpdateRequest {

    private String title;
    private String content;
    private String thumbnail;
    private String status;
}
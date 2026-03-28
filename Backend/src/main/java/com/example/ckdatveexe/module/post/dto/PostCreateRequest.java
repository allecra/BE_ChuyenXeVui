package com.example.ckdatveexe.module.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostCreateRequest {

    @NotBlank(message = "title không được để trống")
    private String title;

    @NotBlank(message = "content không được để trống")
    private String content;

    @NotBlank(message = "thumbnail không được để trống")
    private String thumbnail;
}
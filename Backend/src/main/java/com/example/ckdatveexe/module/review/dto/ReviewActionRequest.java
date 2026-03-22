package com.example.ckdatveexe.module.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewActionRequest {

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String adminNotes;
}
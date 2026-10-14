package com.smarteng.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateArticleRequest(
        @NotBlank(message = "Tiêu đề bài đọc không được để trống")
        String title,
        String topic,
        String level,
        @NotBlank(message = "Nội dung bài đọc không được để trống")
        String content,
        String audioUrl,
        Integer durationSeconds,
        String sourceName
) {}

package com.smarteng.dto.response;

import com.smarteng.model.Article;

public record ArticleResponse(
        Long id,
        String title,
        String topic,
        String level,
        String content,
        String audioUrl,
        Integer durationSeconds,
        String sourceName,
        boolean isBookmarked
) {
    public static ArticleResponse fromEntity(Article a) {
        if (a == null) return null;
        return new ArticleResponse(
                a.getId(),
                a.getTitle(),
                a.getTopic(),
                a.getLevel(),
                a.getContent(),
                a.getAudioUrl(),
                a.getDurationSeconds(),
                a.getSourceName(),
                a.isBookmarked()
        );
    }
}

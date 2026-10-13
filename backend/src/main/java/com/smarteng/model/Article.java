package com.smarteng.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String topic;

    @Column(length = 20)
    private String level; // A2, B1, B2, C1

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String audioUrl;

    private Integer durationSeconds;

    private String sourceName; // e.g. VOA Learning English, BBC

    @Builder.Default
    private boolean isBookmarked = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

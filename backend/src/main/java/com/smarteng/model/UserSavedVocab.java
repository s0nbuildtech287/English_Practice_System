package com.smarteng.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_saved_vocab")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSavedVocab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String word;

    private String ipa;

    @Column(columnDefinition = "TEXT")
    private String meaningVi;

    @Column(columnDefinition = "TEXT")
    private String meaningEn;

    private String audioUrl;

    @Column(columnDefinition = "TEXT")
    private String contextSentence;

    @Builder.Default
    private int repetitionLevel = 0; // 0: new, 1: 1 day, 2: 3 days, 3: 7 days, 4: 14 days, 5: 30 days

    @Builder.Default
    private LocalDate nextReviewDate = LocalDate.now();

    @Builder.Default
    private LocalDateTime savedAt = LocalDateTime.now();
}

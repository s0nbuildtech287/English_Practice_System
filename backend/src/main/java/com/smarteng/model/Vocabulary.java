package com.smarteng.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vocabularies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String word;

    private String ipa;

    private String partOfSpeech;

    @Column(columnDefinition = "TEXT")
    private String meaningVi;

    @Column(columnDefinition = "TEXT")
    private String meaningEn;

    private String audioUrl;

    @Column(columnDefinition = "TEXT")
    private String exampleSentence;

    private String topic;

    private String cefrLevel;
}


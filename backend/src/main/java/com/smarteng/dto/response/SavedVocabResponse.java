package com.smarteng.dto.response;

import com.smarteng.model.UserSavedVocab;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SavedVocabResponse(
        Long id,
        String word,
        String ipa,
        String meaningVi,
        String meaningEn,
        String audioUrl,
        String contextSentence,
        int repetitionLevel,
        LocalDate nextReviewDate,
        LocalDateTime savedAt
) {
    public static SavedVocabResponse fromEntity(UserSavedVocab u) {
        if (u == null) return null;
        return new SavedVocabResponse(
                u.getId(),
                u.getWord(),
                u.getIpa(),
                u.getMeaningVi(),
                u.getMeaningEn(),
                u.getAudioUrl(),
                u.getContextSentence(),
                u.getRepetitionLevel(),
                u.getNextReviewDate(),
                u.getSavedAt()
        );
    }
}

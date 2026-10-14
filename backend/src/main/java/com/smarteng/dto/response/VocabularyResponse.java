package com.smarteng.dto.response;

import com.smarteng.model.Vocabulary;

public record VocabularyResponse(
        Long id,
        String word,
        String ipa,
        String partOfSpeech,
        String meaningVi,
        String meaningEn,
        String audioUrl,
        String exampleSentence
) {
    public static VocabularyResponse fromEntity(Vocabulary v) {
        if (v == null) return null;
        return new VocabularyResponse(
                v.getId(),
                v.getWord(),
                v.getIpa(),
                v.getPartOfSpeech(),
                v.getMeaningVi(),
                v.getMeaningEn(),
                v.getAudioUrl(),
                v.getExampleSentence()
        );
    }
}

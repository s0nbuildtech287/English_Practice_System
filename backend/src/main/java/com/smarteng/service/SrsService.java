package com.smarteng.service;

import com.smarteng.model.UserSavedVocab;
import com.smarteng.repository.UserSavedVocabRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SrsService {

    private final UserSavedVocabRepository userSavedVocabRepository;

    private static final int[] INTERVALS_DAYS = {1, 2, 4, 7, 14, 30};

    public UserSavedVocab saveWord(String word, String ipa, String meaningVi, String meaningEn, String audioUrl, String contextSentence) {
        return userSavedVocabRepository.findByWordIgnoreCase(word)
                .map(existing -> {
                    existing.setContextSentence(contextSentence);
                    if (meaningVi != null && !meaningVi.isEmpty()) existing.setMeaningVi(meaningVi);
                    return userSavedVocabRepository.save(existing);
                })
                .orElseGet(() -> {
                    UserSavedVocab newVocab = UserSavedVocab.builder()
                            .word(word)
                            .ipa(ipa)
                            .meaningVi(meaningVi)
                            .meaningEn(meaningEn)
                            .audioUrl(audioUrl)
                            .contextSentence(contextSentence)
                            .repetitionLevel(0)
                            .nextReviewDate(LocalDate.now().plusDays(1))
                            .build();
                    return userSavedVocabRepository.save(newVocab);
                });
    }

    public List<UserSavedVocab> getAllSavedVocab() {
        return userSavedVocabRepository.findAllByOrderBySavedAtDesc();
    }

    public List<UserSavedVocab> getWordsDueForReview() {
        return userSavedVocabRepository.findByNextReviewDateLessThanEqual(LocalDate.now());
    }

    public UserSavedVocab reviewWord(Long id, boolean remembered) {
        return userSavedVocabRepository.findById(id).map(vocab -> {
            int currentLevel = vocab.getRepetitionLevel();
            if (remembered) {
                int nextLevel = Math.min(currentLevel + 1, INTERVALS_DAYS.length - 1);
                vocab.setRepetitionLevel(nextLevel);
                vocab.setNextReviewDate(LocalDate.now().plusDays(INTERVALS_DAYS[nextLevel]));
            } else {
                vocab.setRepetitionLevel(0);
                vocab.setNextReviewDate(LocalDate.now().plusDays(INTERVALS_DAYS[0]));
            }
            return userSavedVocabRepository.save(vocab);
        }).orElseThrow(() -> new IllegalArgumentException("Vocabulary item not found with id: " + id));
    }
}

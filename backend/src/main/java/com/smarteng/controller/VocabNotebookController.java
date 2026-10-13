package com.smarteng.controller;

import com.smarteng.model.UserSavedVocab;
import com.smarteng.service.SrsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vocab-notebook")
@RequiredArgsConstructor
@Tag(name = "Vocabulary Notebook", description = "Personal Flashcards & Spaced Repetition")
public class VocabNotebookController {

    private final SrsService srsService;

    @GetMapping
    @Operation(summary = "Get all saved vocabulary in the personal notebook")
    public ResponseEntity<List<UserSavedVocab>> getAllSavedVocab() {
        return ResponseEntity.ok(srsService.getAllSavedVocab());
    }

    @GetMapping("/due")
    @Operation(summary = "Get vocabulary cards due for review today")
    public ResponseEntity<List<UserSavedVocab>> getDueCards() {
        return ResponseEntity.ok(srsService.getWordsDueForReview());
    }

    @PostMapping("/save")
    @Operation(summary = "Save a new word from reading context")
    public ResponseEntity<UserSavedVocab> saveWord(@RequestBody SaveWordRequest request) {
        return ResponseEntity.ok(srsService.saveWord(
                request.getWord(),
                request.getIpa(),
                request.getMeaningVi(),
                request.getMeaningEn(),
                request.getAudioUrl(),
                request.getContextSentence()
        ));
    }

    @PostMapping("/review/{id}")
    @Operation(summary = "Submit review result for a card (remembered: true/false)")
    public ResponseEntity<UserSavedVocab> reviewCard(
            @PathVariable Long id,
            @RequestParam boolean remembered) {
        return ResponseEntity.ok(srsService.reviewWord(id, remembered));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveWordRequest {
        private String word;
        private String ipa;
        private String meaningVi;
        private String meaningEn;
        private String audioUrl;
        private String contextSentence;
    }
}

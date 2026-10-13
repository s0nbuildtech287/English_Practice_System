package com.smarteng.controller;

import com.smarteng.model.Vocabulary;
import com.smarteng.service.DictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
@Tag(name = "Dictionary", description = "Instant word lookup with IPA, audio and definitions")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping("/lookup")
    @Operation(summary = "Lookup a word definition, phonetic IPA and audio pronunciation")
    public ResponseEntity<Vocabulary> lookupWord(@RequestParam String word) {
        Vocabulary vocab = dictionaryService.lookupWord(word);
        if (vocab == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vocab);
    }
}

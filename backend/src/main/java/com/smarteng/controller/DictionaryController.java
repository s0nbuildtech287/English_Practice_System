package com.smarteng.controller;

import com.smarteng.dto.response.ApiResponse;
import com.smarteng.dto.response.VocabularyResponse;
import com.smarteng.exception.ResourceNotFoundException;
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
@Tag(name = "Dictionary", description = "Tra cứu từ vựng đa tầng tức thì (FreeDict + Datamuse + TTS Audio)")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping("/lookup")
    @Operation(summary = "Tra cứu từ vựng, phiên âm IPA, từ loại và phát âm chuẩn")
    public ResponseEntity<ApiResponse<VocabularyResponse>> lookupWord(@RequestParam String word) {
        Vocabulary vocab = dictionaryService.lookupWord(word);
        if (vocab == null) {
            throw new ResourceNotFoundException("Từ vựng", "word", word);
        }
        return ResponseEntity.ok(ApiResponse.ok(VocabularyResponse.fromEntity(vocab)));
    }
}

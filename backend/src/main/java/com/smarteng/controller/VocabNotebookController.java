package com.smarteng.controller;

import com.smarteng.dto.request.SaveWordRequest;
import com.smarteng.dto.response.ApiResponse;
import com.smarteng.dto.response.SavedVocabResponse;
import com.smarteng.model.UserSavedVocab;
import com.smarteng.service.SrsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vocab-notebook")
@RequiredArgsConstructor
@Tag(name = "Vocabulary Notebook", description = "Sổ tay từ vựng cá nhân & Thẻ ghi nhớ 3D Flashcards SRS")
public class VocabNotebookController {

    private final SrsService srsService;

    @GetMapping
    @Operation(summary = "Lấy toàn bộ từ vựng đã lưu trong sổ tay cá nhân")
    public ResponseEntity<ApiResponse<List<SavedVocabResponse>>> getAllSavedVocab() {
        List<SavedVocabResponse> list = srsService.getAllSavedVocab()
                .stream()
                .map(SavedVocabResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/due")
    @Operation(summary = "Lấy các thẻ từ vựng đến hạn cần ôn tập hôm nay theo thuật toán SRS")
    public ResponseEntity<ApiResponse<List<SavedVocabResponse>>> getDueCards() {
        List<SavedVocabResponse> list = srsService.getWordsDueForReview()
                .stream()
                .map(SavedVocabResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/save")
    @Operation(summary = "Lưu từ mới vào sổ tay từ ngữ cảnh bài đọc")
    public ResponseEntity<ApiResponse<SavedVocabResponse>> saveWord(@Valid @RequestBody SaveWordRequest request) {
        UserSavedVocab saved = srsService.saveWord(
                request.getWord(),
                request.getIpa(),
                request.getMeaningVi(),
                request.getMeaningEn(),
                request.getAudioUrl(),
                request.getContextSentence()
        );
        return ResponseEntity.ok(ApiResponse.ok("Lưu từ thành công", SavedVocabResponse.fromEntity(saved)));
    }

    @PostMapping("/review/{id}")
    @Operation(summary = "Gửi kết quả ôn tập thẻ (remembered: true/false)")
    public ResponseEntity<ApiResponse<SavedVocabResponse>> reviewCard(
            @PathVariable Long id,
            @RequestParam boolean remembered) {
        UserSavedVocab updated = srsService.reviewWord(id, remembered);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật tiến độ ôn tập thành công", SavedVocabResponse.fromEntity(updated)));
    }
}

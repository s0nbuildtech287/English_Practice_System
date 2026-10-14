package com.smarteng.controller;

import com.smarteng.dto.response.ApiResponse;
import com.smarteng.model.Vocabulary;
import com.smarteng.service.VocabularyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
@Tag(name = "Vocabulary Topic Library", description = "Kho từ vựng theo chủ đề CEFR A1-C2, IELTS, TOEIC, IT & Y Tế")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping("/topics")
    @Operation(summary = "Lấy danh sách các chủ đề từ vựng có sẵn kèm số lượng từ")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopics() {
        return ResponseEntity.ok(ApiResponse.ok(vocabularyService.getTopicsSummary()));
    }

    @GetMapping("/topics/{topicCode}")
    @Operation(summary = "Lấy danh sách từ vựng chuẩn chỉnh theo mã chủ đề")
    public ResponseEntity<ApiResponse<List<Vocabulary>>> getWordsByTopic(@PathVariable String topicCode) {
        return ResponseEntity.ok(ApiResponse.ok(vocabularyService.getWordsByTopic(topicCode)));
    }

    @PostMapping("/topics/{topicCode}/crawl-more")
    @Operation(summary = "Cào nạp thêm từ vựng tự động từ Datamuse & Dictionary theo chủ đề")
    public ResponseEntity<ApiResponse<List<Vocabulary>>> crawlMoreWords(@PathVariable String topicCode) {
        List<Vocabulary> added = vocabularyService.crawlMoreWordsForTopic(topicCode);
        return ResponseEntity.ok(ApiResponse.ok("Đã nạp thêm thành công " + added.size() + " từ vựng mới vào chủ đề", added));
    }
}

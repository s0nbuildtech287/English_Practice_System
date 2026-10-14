package com.smarteng.controller;

import com.smarteng.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private final GeminiService geminiService;

    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askAi(@RequestBody Map<String, String> payload) {
        String query = payload.getOrDefault("query", "");
        String context = payload.getOrDefault("context", "");
        String answer = geminiService.explainInContext(query, context);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "answer", answer
        ));
    }

    @PostMapping("/quiz")
    public ResponseEntity<Map<String, Object>> generateQuiz(@RequestBody Map<String, String> payload) {
        String title = payload.getOrDefault("title", "");
        String content = payload.getOrDefault("content", "");
        String quizJson = geminiService.generateQuizForArticle(title, content);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "quiz", quizJson
        ));
    }
}

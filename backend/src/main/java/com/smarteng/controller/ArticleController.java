package com.smarteng.controller;

import com.smarteng.dto.request.CreateArticleRequest;
import com.smarteng.dto.response.ApiResponse;
import com.smarteng.dto.response.ArticleResponse;
import com.smarteng.exception.ResourceNotFoundException;
import com.smarteng.model.Article;
import com.smarteng.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "Articles", description = "Quản lý bài đọc & nghe chuẩn khung CEFR (A1 -> C2)")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    @Operation(summary = "Lấy danh sách bài đọc theo cấp độ (level) và chủ đề (topic)")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getArticles(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String topic) {
        List<ArticleResponse> list = articleService.getAllArticles(level, topic)
                .stream()
                .map(ArticleResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết một bài đọc theo ID")
    public ResponseEntity<ApiResponse<ArticleResponse>> getArticleById(@PathVariable Long id) {
        Article article = articleService.getArticleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài đọc", "id", id));
        return ResponseEntity.ok(ApiResponse.ok(ArticleResponse.fromEntity(article)));
    }

    @GetMapping("/bookmarked")
    @Operation(summary = "Lấy danh sách các bài đọc đã lưu vào Bookmark")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getBookmarkedArticles() {
        List<ArticleResponse> list = articleService.getBookmarkedArticles()
                .stream()
                .map(ArticleResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/{id}/toggle-bookmark")
    @Operation(summary = "Bật/Tắt trạng thái Bookmark của bài đọc")
    public ResponseEntity<ApiResponse<ArticleResponse>> toggleBookmark(@PathVariable Long id) {
        Article updated = articleService.toggleBookmark(id);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật bookmark thành công", ArticleResponse.fromEntity(updated)));
    }

    @PostMapping
    @Operation(summary = "Tạo một bài đọc mới cá nhân")
    public ResponseEntity<ApiResponse<ArticleResponse>> createArticle(@Valid @RequestBody CreateArticleRequest req) {
        Article article = Article.builder()
                .title(req.title())
                .topic(req.topic() != null ? req.topic() : "General")
                .level(req.level() != null ? req.level() : "B1")
                .content(req.content())
                .audioUrl(req.audioUrl())
                .durationSeconds(req.durationSeconds() != null ? req.durationSeconds() : 60)
                .sourceName(req.sourceName() != null ? req.sourceName() : "Custom User Article")
                .build();
        Article saved = articleService.saveArticle(article);
        return ResponseEntity.ok(ApiResponse.ok("Tạo bài đọc thành công", ArticleResponse.fromEntity(saved)));
    }
}

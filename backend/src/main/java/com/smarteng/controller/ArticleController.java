package com.smarteng.controller;

import com.smarteng.model.Article;
import com.smarteng.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "Articles", description = "Reading & Listening articles management")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    @Operation(summary = "Get all articles with optional filtering by level and topic")
    public ResponseEntity<List<Article>> getArticles(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String topic) {
        return ResponseEntity.ok(articleService.getAllArticles(level, topic));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get article by ID")
    public ResponseEntity<Article> getArticleById(@PathVariable Long id) {
        return articleService.getArticleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/bookmarked")
    @Operation(summary = "Get all bookmarked articles")
    public ResponseEntity<List<Article>> getBookmarkedArticles() {
        return ResponseEntity.ok(articleService.getBookmarkedArticles());
    }

    @PostMapping("/{id}/toggle-bookmark")
    @Operation(summary = "Toggle bookmark status for an article")
    public ResponseEntity<Article> toggleBookmark(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.toggleBookmark(id));
    }

    @PostMapping
    @Operation(summary = "Create custom article for reading")
    public ResponseEntity<Article> createArticle(@RequestBody Article article) {
        return ResponseEntity.ok(articleService.saveArticle(article));
    }
}

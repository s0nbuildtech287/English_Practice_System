package com.smarteng.repository;

import com.smarteng.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByLevelIgnoreCase(String level);
    List<Article> findByTopicIgnoreCase(String topic);
    List<Article> findByLevelIgnoreCaseAndTopicIgnoreCase(String level, String topic);
    List<Article> findByIsBookmarkedTrue();
}

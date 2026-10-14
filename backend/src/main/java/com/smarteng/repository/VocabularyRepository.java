package com.smarteng.repository;

import com.smarteng.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {
    Optional<Vocabulary> findByWordIgnoreCase(String word);
    List<Vocabulary> findByTopicIgnoreCase(String topic);
    List<Vocabulary> findByTopicIgnoreCaseOrderByIdDesc(String topic);
    List<Vocabulary> findByCefrLevelIgnoreCase(String cefrLevel);
}


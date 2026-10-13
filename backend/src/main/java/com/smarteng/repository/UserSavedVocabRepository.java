package com.smarteng.repository;

import com.smarteng.model.UserSavedVocab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSavedVocabRepository extends JpaRepository<UserSavedVocab, Long> {
    Optional<UserSavedVocab> findByWordIgnoreCase(String word);
    List<UserSavedVocab> findByNextReviewDateLessThanEqual(LocalDate date);
    List<UserSavedVocab> findAllByOrderBySavedAtDesc();
}

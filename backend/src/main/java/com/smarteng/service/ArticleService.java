package com.smarteng.service;

import com.smarteng.model.Article;
import com.smarteng.repository.ArticleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {

    private final ArticleRepository articleRepository;

    public List<Article> getAllArticles(String level, String topic) {
        if (level != null && !level.isEmpty() && topic != null && !topic.isEmpty()) {
            return articleRepository.findByLevelIgnoreCaseAndTopicIgnoreCase(level, topic);
        } else if (level != null && !level.isEmpty()) {
            return articleRepository.findByLevelIgnoreCase(level);
        } else if (topic != null && !topic.isEmpty()) {
            return articleRepository.findByTopicIgnoreCase(topic);
        }
        return articleRepository.findAll();
    }

    public Optional<Article> getArticleById(Long id) {
        return articleRepository.findById(id);
    }

    public Article saveArticle(Article article) {
        return articleRepository.save(article);
    }

    public Article toggleBookmark(Long id) {
        return articleRepository.findById(id).map(a -> {
            a.setBookmarked(!a.isBookmarked());
            return articleRepository.save(a);
        }).orElseThrow(() -> new IllegalArgumentException("Article not found: " + id));
    }

    public List<Article> getBookmarkedArticles() {
        return articleRepository.findByIsBookmarkedTrue();
    }

    @PostConstruct
    public void seedInitialArticles() {
        List<Article> existing = articleRepository.findAll();
        if (existing.size() >= 6) {
            for (Article a : existing) {
                if (a.getAudioUrl() == null || a.getAudioUrl().contains("voanews.com") || a.getAudioUrl().contains("localhost:4000")) {
                    a.setAudioUrl("/audio/article" + a.getId() + ".mp3");
                    articleRepository.save(a);
                }
            }
            return;
        }

        // If existing has less than 6 articles, clean and seed full set of CEFR levels A1-C2
        articleRepository.deleteAll();

        log.info("Seeding full suite of CEFR articles (A1 to C2) with local neural audio...");

        // Article 1: Level B1 (Intermediate)
        Article article1 = Article.builder()
                .title("Artificial Intelligence Helps Doctors Discover New Medicines")
                .topic("Technology")
                .level("B1")
                .sourceName("BBC Learning English")
                .audioUrl("/audio/article1.mp3")
                .durationSeconds(120)
                .isBookmarked(true)
                .content("Scientists are using artificial intelligence to discover new antibiotics and medicines faster than ever before. " +
                        "Traditional methods of developing medicine take many years and cost millions of dollars. " +
                        "Researchers train computer models on thousands of chemical compounds. " +
                        "The artificial intelligence system then predicts which molecules are most likely to stop dangerous bacteria. " +
                        "In recent tests, researchers identified powerful new compounds that can fight drug-resistant infections. " +
                        "Experts say this technology will revolutionize healthcare and save countless lives in the coming decades.")
                .build();

        // Article 2: Level A2 (Elementary)
        Article article2 = Article.builder()
                .title("Why Walking Every Day Is Good for Your Brain")
                .topic("Health")
                .level("A2")
                .sourceName("VOA Learning English")
                .audioUrl("/audio/article2.mp3")
                .durationSeconds(110)
                .isBookmarked(false)
                .content("Walking is one of the easiest and most effective forms of physical exercise. " +
                        "Doctors recommend taking at least thirty minutes of brisk walking every day. " +
                        "When you walk, blood flow to your brain increases significantly. " +
                        "This helps improve memory, concentration, and emotional well-being. " +
                        "People who walk regularly report lower levels of stress and anxiety. " +
                        "You do not need special equipment to start walking. " +
                        "Just wear comfortable shoes and take a stroll in your neighborhood or a nearby park.")
                .build();

        // Article 3: Level B2 (Upper-Intermediate)
        Article article3 = Article.builder()
                .title("Global Efforts to Protect Coral Reefs from Ocean Warming")
                .topic("Environment")
                .level("B2")
                .sourceName("National Geographic")
                .audioUrl("/audio/article3.mp3")
                .durationSeconds(135)
                .isBookmarked(false)
                .content("Coral reefs represent some of the most diverse and fragile marine ecosystems on Earth. " +
                        "Rising ocean temperatures have caused widespread coral bleaching events across tropical regions. " +
                        "When seawater becomes unusually warm, corals expel the colorful algae living inside their tissues. " +
                        "Without this algae, the coral turns completely white and becomes vulnerable to disease and starvation. " +
                        "Marine biologists around the world are developing heat-resilient corals in specialized laboratories. " +
                        "These resilient species are then transplanted back into endangered ocean habitats to restore marine biodiversity.")
                .build();

        // Article 4: Level A1 (Beginner)
        Article article4 = Article.builder()
                .title("My Daily Morning Routine in London")
                .topic("Daily Life")
                .level("A1")
                .sourceName("Easy English")
                .audioUrl("/audio/article4.mp3")
                .durationSeconds(95)
                .isBookmarked(false)
                .content("My name is Peter and I live in London. Every morning, I wake up at seven o'clock. " +
                        "I drink a warm cup of tea and eat fresh bread with butter. At eight o'clock, I take the red bus to my school. " +
                        "I love meeting my friends in the park during lunchtime. The city is very big, but the people are friendly and welcoming. " +
                        "In the evening, I cook a simple dinner with my family and read a short story before going to sleep.")
                .build();

        // Article 5: Level C1 (Advanced)
        Article article5 = Article.builder()
                .title("The Psychology of Cognitive Biases in High-Stakes Decision Making")
                .topic("Psychology")
                .level("C1")
                .sourceName("Harvard Review")
                .audioUrl("/audio/article5.mp3")
                .durationSeconds(145)
                .isBookmarked(false)
                .content("Human cognition is frequently susceptible to systematic errors known as cognitive biases. " +
                        "In high-stakes environments, such as medical diagnostics or macroeconomic policy formulation, these subconscious distortions can lead to catastrophic consequences. " +
                        "Confirmation bias causes analysts to disproportionately favor information confirming their preexisting hypotheses while dismissing contrary empirical evidence. " +
                        "Furthermore, the availability heuristic leads individuals to overestimate the probability of events that are readily recalled. " +
                        "Mitigating these psychological blind spots demands structured analytical frameworks and continuous peer review.")
                .build();

        // Article 6: Level C2 (Mastery)
        Article article6 = Article.builder()
                .title("The Epistemological Nuances of Linguistic Evolution and Algorithmic Determinism")
                .topic("Philosophy & AI")
                .level("C2")
                .sourceName("Oxford Academic")
                .audioUrl("/audio/article6.mp3")
                .durationSeconds(155)
                .isBookmarked(false)
                .content("The dialectical interplay between human linguistic vernacular and algorithmic synthesis presents profound epistemological dilemmas. " +
                        "As natural language processing models increasingly generate persuasive prose, they simultaneously homogenize idiosyncratic linguistic traits. " +
                        "This algorithmic curation risks subverting the organic cadence of human communication, establishing an implicit orthodoxy of syntax and semantics. " +
                        "Consequently, linguists and philosophers grapple with whether synthetic rhetoric will inevitably impoverish semantic nuance or serendipitously catalyze novel modes of philosophical discourse.")
                .build();

        articleRepository.saveAll(List.of(article4, article2, article1, article3, article5, article6));
        log.info("Seeded 6 CEFR articles (A1 to C2) successfully with local audio!");
    }
}

package com.smarteng.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarteng.model.Vocabulary;
import com.smarteng.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DictionaryService {

    private final VocabularyRepository vocabularyRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Vocabulary lookupWord(String rawWord) {
        if (rawWord == null || rawWord.trim().isEmpty()) {
            return null;
        }

        String cleanWord = rawWord.trim().toLowerCase().replaceAll("[^a-zA-Z\\-]", "");
        if (cleanWord.isEmpty()) {
            return null;
        }

        // 1. Check local DB cache (only return if valid definition)
        Optional<Vocabulary> cached = vocabularyRepository.findByWordIgnoreCase(cleanWord);
        if (cached.isPresent()) {
            Vocabulary v = cached.get();
            if (v.getMeaningEn() != null && !v.getMeaningEn().isEmpty() && !v.getMeaningEn().contains("No definition found")) {
                return v;
            }
        }

        // 2. Tier 1: Try Free Dictionary API
        Vocabulary tier1Result = tryFreeDictionaryApi(cleanWord);
        if (tier1Result != null) {
            return vocabularyRepository.save(tier1Result);
        }

        // 3. Tier 2: Try Datamuse API (high reliability, never blocks)
        Vocabulary tier2Result = tryDatamuseApi(cleanWord);
        if (tier2Result != null) {
            return vocabularyRepository.save(tier2Result);
        }

        // 4. Final placeholder without caching into DB
        return Vocabulary.builder()
                .word(cleanWord)
                .ipa("")
                .partOfSpeech("")
                .meaningEn("Không tìm thấy định nghĩa trực tuyến. Vui lòng thử từ khác.")
                .meaningVi("")
                .audioUrl("https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=en&q=" + cleanWord)
                .exampleSentence("")
                .build();
    }

    private Vocabulary tryFreeDictionaryApi(String cleanWord) {
        try {
            String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + cleanWord;
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray() && root.size() > 0) {
                    JsonNode entry = root.get(0);

                    String word = entry.has("word") ? entry.get("word").asText() : cleanWord;
                    String ipa = entry.has("phonetic") ? entry.get("phonetic").asText() : "";
                    String audioUrl = "";

                    if (entry.has("phonetics") && entry.get("phonetics").isArray()) {
                        for (JsonNode p : entry.get("phonetics")) {
                            if (ipa.isEmpty() && p.has("text") && !p.get("text").asText().isEmpty()) {
                                ipa = p.get("text").asText();
                            }
                            if (audioUrl.isEmpty() && p.has("audio") && !p.get("audio").asText().isEmpty()) {
                                audioUrl = p.get("audio").asText();
                            }
                        }
                    }

                    String partOfSpeech = "";
                    String definition = "";
                    String example = "";

                    if (entry.has("meanings") && entry.get("meanings").isArray() && entry.get("meanings").size() > 0) {
                        JsonNode firstMeaning = entry.get("meanings").get(0);
                        if (firstMeaning.has("partOfSpeech")) {
                            partOfSpeech = firstMeaning.get("partOfSpeech").asText();
                        }
                        if (firstMeaning.has("definitions") && firstMeaning.get("definitions").isArray() && firstMeaning.get("definitions").size() > 0) {
                            JsonNode def0 = firstMeaning.get("definitions").get(0);
                            if (def0.has("definition")) {
                                definition = def0.get("definition").asText();
                            }
                            if (def0.has("example")) {
                                example = def0.get("example").asText();
                            }
                        }
                    }

                    if (audioUrl.isEmpty()) {
                        audioUrl = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=en&q=" + cleanWord;
                    }

                    if (!definition.isEmpty()) {
                        return Vocabulary.builder()
                                .word(word)
                                .ipa(ipa)
                                .partOfSpeech(partOfSpeech)
                                .meaningEn(definition)
                                .meaningVi("")
                                .audioUrl(audioUrl)
                                .exampleSentence(example)
                                .build();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Free Dictionary API lookup failed for {}: {}", cleanWord, e.getMessage());
        }
        return null;
    }

    private Vocabulary tryDatamuseApi(String cleanWord) {
        try {
            String url = "https://api.datamuse.com/words?sp=" + cleanWord + "&md=dp";
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray() && root.size() > 0) {
                    JsonNode first = root.get(0);
                    if (first.has("defs") && first.get("defs").isArray() && first.get("defs").size() > 0) {
                        String rawDef = first.get("defs").get(0).asText();
                        String partOfSpeech = "";
                        String definition = rawDef;

                        if (rawDef.contains("\t")) {
                            String[] parts = rawDef.split("\t", 2);
                            String posCode = parts[0].trim();
                            if (posCode.equals("v")) partOfSpeech = "verb";
                            else if (posCode.equals("n")) partOfSpeech = "noun";
                            else if (posCode.equals("adj")) partOfSpeech = "adjective";
                            else if (posCode.equals("adv")) partOfSpeech = "adverb";
                            else partOfSpeech = posCode;

                            definition = parts[1].trim();
                        }

                        String audioUrl = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=en&q=" + cleanWord;

                        return Vocabulary.builder()
                                .word(cleanWord)
                                .ipa("/" + cleanWord + "/")
                                .partOfSpeech(partOfSpeech)
                                .meaningEn(definition)
                                .meaningVi("")
                                .audioUrl(audioUrl)
                                .exampleSentence("")
                                .build();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Datamuse lookup failed for {}: {}", cleanWord, e.getMessage());
        }
        return null;
    }
}

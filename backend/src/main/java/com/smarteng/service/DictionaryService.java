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
    private final RestTemplate restTemplate = createFastRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static RestTemplate createFastRestTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2500);
        factory.setReadTimeout(3000);
        return new RestTemplate(factory);
    }

    public Vocabulary lookupWord(String rawWord) {
        if (rawWord == null || rawWord.trim().isEmpty()) {
            return null;
        }

        String cleanWord = rawWord.trim().toLowerCase().replaceAll("[^a-zA-Z\\-]", "");
        if (cleanWord.isEmpty()) {
            return null;
        }

        // 1. Check local DB cache (populate meaningVi if missing, encoded, or placeholder)
        Optional<Vocabulary> cached = vocabularyRepository.findByWordIgnoreCase(cleanWord);
        if (cached.isPresent()) {
            Vocabulary v = cached.get();
            if (v.getMeaningEn() != null && !v.getMeaningEn().isEmpty() && !v.getMeaningEn().contains("No definition found")) {
                String curVi = v.getMeaningVi();
                if (curVi == null || curVi.trim().isEmpty() || curVi.contains("%") || curVi.contains("chuẩn xác") || curVi.startsWith("Để được hưởng")) {
                    String translated = translateToVietnamese(cleanWord, v.getMeaningEn(), v.getPartOfSpeech());
                    if (translated != null && !translated.trim().isEmpty()) {
                        v.setMeaningVi(translated);
                        v = vocabularyRepository.save(v);
                    }
                }
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

        // 4. Final fallback with live Vietnamese translation
        String fallbackVi = translateToVietnamese(cleanWord, null, "term");
        return Vocabulary.builder()
                .word(cleanWord)
                .ipa("/" + cleanWord + "/")
                .partOfSpeech("term")
                .meaningEn("Academic English vocabulary term: " + cleanWord)
                .meaningVi(fallbackVi.isEmpty() ? cleanWord : fallbackVi)
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
                        String meaningVi = translateToVietnamese(word, definition, partOfSpeech);
                        return Vocabulary.builder()
                                .word(word)
                                .ipa(ipa)
                                .partOfSpeech(partOfSpeech)
                                .meaningEn(definition)
                                .meaningVi(meaningVi)
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
                        String meaningVi = translateToVietnamese(cleanWord, definition, partOfSpeech);

                        return Vocabulary.builder()
                                .word(cleanWord)
                                .ipa("/" + cleanWord + "/")
                                .partOfSpeech(partOfSpeech)
                                .meaningEn(definition)
                                .meaningVi(meaningVi)
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

    private String translateToVietnamese(String word, String definition, String targetPos) {
        if (word == null || word.trim().isEmpty()) return "";
        try {
            String encoded = java.net.URLEncoder.encode(word.trim(), java.nio.charset.StandardCharsets.UTF_8);
            java.net.URI uri = java.net.URI.create("https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=vi&dt=t&dt=bd&q=" + encoded);
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());

                // 1. Try bilingual dictionary matching target partOfSpeech
                if (root.size() > 1 && root.get(1).isArray()) {
                    String normPos = targetPos != null ? targetPos.toLowerCase().trim() : "";
                    StringBuilder matched = new StringBuilder();
                    StringBuilder fallback = new StringBuilder();

                    for (JsonNode posBlock : root.get(1)) {
                        if (posBlock.isArray() && posBlock.size() >= 2) {
                            String blockPos = posBlock.get(0).asText();
                            JsonNode meaningsArr = posBlock.get(1);
                            if (meaningsArr.isArray() && meaningsArr.size() > 0) {
                                StringBuilder sb = new StringBuilder();
                                int count = 0;
                                for (JsonNode m : meaningsArr) {
                                    if (count > 0) sb.append(", ");
                                    sb.append(m.asText());
                                    count++;
                                    if (count >= 3) break;
                                }

                                if (!normPos.isEmpty() && (normPos.startsWith(blockPos) || blockPos.startsWith(normPos))) {
                                    matched.append(sb);
                                    break;
                                } else {
                                    if (fallback.length() > 0) fallback.append("; ");
                                    fallback.append("(").append(blockPos).append(") ").append(sb);
                                }
                            }
                        }
                    }

                    if (matched.length() > 0) {
                        return cleanVi(matched.toString());
                    } else if (fallback.length() > 0) {
                        return cleanVi(fallback.toString());
                    }
                }

                // 2. Direct translation from root[0]
                if (root.isArray() && root.size() > 0 && root.get(0).isArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode part : root.get(0)) {
                        if (part.isArray() && part.size() > 0 && part.get(0).isTextual()) {
                            sb.append(part.get(0).asText());
                        }
                    }
                    String res = sb.toString().trim();
                    if (!res.isEmpty()) return cleanVi(res);
                }
            }
        } catch (Exception e) {
            log.warn("Bilingual translation failed for [{}]: {}", word, e.getMessage());
        }

        // 3. Fallback: translate the definition
        if (definition != null && !definition.trim().isEmpty()) {
            try {
                String encodedDef = java.net.URLEncoder.encode(definition.trim(), java.nio.charset.StandardCharsets.UTF_8);
                java.net.URI uriDef = java.net.URI.create("https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=vi&dt=t&q=" + encodedDef);
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(uriDef, HttpMethod.GET, entity, String.class);
                if (response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    if (root.isArray() && root.size() > 0 && root.get(0).isArray()) {
                        StringBuilder sb = new StringBuilder();
                        for (JsonNode part : root.get(0)) {
                            if (part.isArray() && part.size() > 0 && part.get(0).isTextual()) {
                                sb.append(part.get(0).asText());
                            }
                        }
                        return cleanVi(sb.toString().trim());
                    }
                }
            } catch (Exception e) {
                log.warn("Definition translation fallback failed: {}", e.getMessage());
            }
        }
        return "";
    }

    private String cleanVi(String text) {
        if (text == null) return "";
        try {
            if (text.contains("%")) {
                text = java.net.URLDecoder.decode(text, java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}
        return text.trim();
    }
}

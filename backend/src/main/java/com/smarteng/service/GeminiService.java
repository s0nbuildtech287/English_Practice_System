package com.smarteng.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.1-flash-lite}")
    private String defaultModel;

    @jakarta.annotation.PostConstruct
    public void initApiKey() {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.startsWith("${")) {
            apiKey = System.getenv("GEMINI_API_KEY");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            for (String p : List.of(".env", "../.env", "../../.env")) {
                java.io.File f = new java.io.File(p);
                if (f.exists()) {
                    try {
                        for (String line : java.nio.file.Files.readAllLines(f.toPath())) {
                            if (line.startsWith("GEMINI_API_KEY=")) {
                                apiKey = line.substring("GEMINI_API_KEY=".length()).trim();
                                break;
                            }
                        }
                    } catch (Exception ignored) {}
                }
                if (apiKey != null && !apiKey.isEmpty()) break;
            }
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final List<String> fallbackModels = List.of(
            "gemini-flash-lite-latest",
            "gemini-3.5-flash-lite",
            "gemma-4-31b-it",
            "gemini-flash-latest",
            "gemini-3.1-flash-lite"
    );

    /**
     * Send a prompt to Google Gemini AI with automatic fallback across supported model versions.
     */
    public String callGemini(String prompt) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return null;
        }

        for (String model : fallbackModels) {
            try {
                String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey.trim();

                Map<String, Object> body = Map.of(
                        "contents", List.of(
                                Map.of("parts", List.of(Map.of("text", prompt)))
                        )
                );

                String jsonBody = objectMapper.writeValueAsString(body);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(20))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode candidates = root.path("candidates");
                    if (candidates.isArray() && !candidates.isEmpty()) {
                        JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
                        if (!textNode.isMissingNode()) {
                            log.info("Gemini AI answered successfully using model [{}]", model);
                            return textNode.asText().trim();
                        }
                    }
                } else {
                    log.warn("Gemini model [{}] returned status {}: {}", model, response.statusCode(), response.body());
                }
            } catch (Exception e) {
                log.warn("Error calling Gemini model [{}]: {}", model, e.getMessage());
            }
        }

        return null;
    }

    /**
     * Explain grammar, sentence structure, or vocabulary in context, or act as an interactive academic tutor.
     */
    public String explainInContext(String query, String passageContext) {
        String prompt;
        if (passageContext != null && !passageContext.trim().isEmpty()) {
            prompt = "Bạn là chuyên gia ngôn ngữ tiếng Anh học thuật.\n" +
                    "Ngữ cảnh bài đọc: \"" + passageContext.trim() + "\"\n" +
                    "Người học thắc mắc: \"" + query.trim() + "\"\n\n" +
                    "Hãy phân tích chi tiết, dễ hiểu bằng tiếng Việt:\n" +
                    "1. Ý nghĩa chính xác trong ngữ cảnh trên.\n" +
                    "2. Cấu trúc ngữ pháp hoặc từ loại liên quan.\n" +
                    "3. 2 ví dụ đặt câu ứng dụng thực tế theo chuẩn CEFR.";
        } else {
            prompt = "Bạn là Cố vấn Học thuật Tiếng Anh (Academic English Tutor & Advisor) thông minh và chu đáo.\n" +
                    "Người học vừa nhắn: \"" + query.trim() + "\"\n\n" +
                    "Hãy phản hồi một cách tự nhiên, thông minh, đúng trọng tâm bằng tiếng Việt (hoặc tiếng Anh nếu người học hỏi bằng tiếng Anh):\n" +
                    "- Nếu người học chào hỏi (ví dụ: 'alo', 'chào bạn', 'hi'), hãy chào lại thân thiện, tự nhiên và hướng dẫn ngắn gọn các cách bạn có thể hỗ trợ (sửa lỗi ngữ pháp, nâng cấp từ vựng C1/C2, viết bài luận mẫu, giải thích thành ngữ, luyện viết IELTS/TOEFL...).\n" +
                    "- Nếu người học yêu cầu viết bài hoặc đồng ý viết (ví dụ: 'viết đi', 'ok', 'viết cho tôi một đoạn...'), hãy viết ngay một đoạn văn tiếng Anh học thuật mẫu mực (CEFR C1 / IELTS 7.5+), kèm bản dịch tiếng Việt và phân tích các cụm từ vựng/cấu trúc đắt giá.\n" +
                    "- Nếu người học đặt câu hỏi hoặc gửi văn bản, hãy phân tích chi tiết, chỉ ra lỗi ngữ pháp và đưa ra giải pháp cải thiện tối ưu.";
        }

        String aiResult = callGemini(prompt);
        if (aiResult != null && !aiResult.trim().isEmpty()) {
            return aiResult;
        }

        // Smart linguistic fallback if Google Gemini experiences temporary server 503
        if (passageContext != null && !passageContext.trim().isEmpty()) {
            return "✨ [Phân Tích Ngữ Nghĩa & Ngữ Pháp]:\n" +
                    "• Thuật ngữ/Từ vựng: \"" + query + "\"\n" +
                    "• Chức năng: Đóng vai trò thành phần ngữ pháp trọng tâm trong câu.\n" +
                    "• Ví dụ ứng dụng: Practice using \"" + query + "\" in your daily academic studies.";
        } else {
            return "Xin chào! Tôi đã nhận được yêu cầu của bạn về \"" + query + "\". " +
                    "Tôi có thể hỗ trợ bạn sửa lỗi ngữ pháp, nâng cấp từ vựng lên chuẩn C1/C2, hoặc viết bài luận học thuật mẫu. Bạn hãy chia sẻ cụ thể nội dung cần luyện tập nhé!";
        }
    }

    /**
     * Generate reading comprehension quiz for a newly ingested passage.
     */
    public String generateQuizForArticle(String title, String content) {
        String prompt = "Dựa trên bài đọc tiếng Anh sau:\n" +
                "Tiêu đề: " + title + "\n" +
                "Nội dung: " + content + "\n\n" +
                "Hãy tạo 1 câu hỏi trắc nghiệm đọc hiểu (bằng tiếng Việt) để kiểm tra độ hiểu bài.\n" +
                "Định dạng JSON như sau:\n" +
                "{\n" +
                "  \"question\": \"Câu hỏi...\",\n" +
                "  \"options\": [\"Đáp án A\", \"Đáp án B\", \"Đáp án C\"],\n" +
                "  \"correctIndex\": 0,\n" +
                "  \"explanation\": \"Giải thích vì sao đúng bám sát chi tiết trong bài...\"\n" +
                "}\n" +
                "Chỉ trả về JSON thuần túy, không kèm giải thích thừa.";
        return callGemini(prompt);
    }
}

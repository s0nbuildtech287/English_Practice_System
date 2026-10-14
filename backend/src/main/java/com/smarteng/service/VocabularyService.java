package com.smarteng.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarteng.model.Vocabulary;
import com.smarteng.repository.VocabularyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final DictionaryService dictionaryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class TopicMeta {
        public String code;
        public String title;
        public String description;
        public String level;
        public String badgeColor;

        public TopicMeta(String code, String title, String description, String level, String badgeColor) {
            this.code = code;
            this.title = title;
            this.description = description;
            this.level = level;
            this.badgeColor = badgeColor;
        }
    }

    private static final List<TopicMeta> TOPIC_METAS = List.of(
            new TopicMeta("A1", "A1 • Căn Bản (Beginner Essentials)", "Từ vựng cốt lõi thường gặp nhất trong sinh hoạt và đời sống hàng ngày", "A1", "#10B981"),
            new TopicMeta("A2", "A2 • Sơ Cấp (Elementary Daily Life)", "Mô tả thói quen sinh hoạt, thời tiết, lịch trình và hoạt động gần gũi", "A2", "#06B6D4"),
            new TopicMeta("B1", "B1 • Trung Cấp (Intermediate Communication)", "Giao tiếp công việc tự tin, chia sẻ trải nghiệm và bày tỏ cảm xúc", "B1", "#38BDF8"),
            new TopicMeta("B2", "B2 • Trung Cao Cấp (Upper-Intermediate Professional)", "Thảo luận chuyên ngành, phân tích chiến lược và đàm phán sự nghiệp", "B2", "#818CF8"),
            new TopicMeta("C1", "C1 • Cao Cấp Học Thuật (Advanced Academic)", "Văn bản học thuật, luận điểm khoa học và phân tích trừu tượng", "C1", "#F59E0B"),
            new TopicMeta("C2", "C2 • Thành Thạo Bậc Cao (Proficiency Mastery)", "Đỉnh cao ngôn ngữ, làm chủ hoàn toàn các sắc thái văn học và triết lý", "C2", "#F43F5E"),
            new TopicMeta("IELTS", "IELTS • Học Thuật 7.0+ (IELTS Academic Core)", "Bộ từ vựng học thuật ghi điểm cao trong bài thi Viết Task 2 và Nói Part 3", "IELTS", "#EC4899"),
            new TopicMeta("TOEIC", "TOEIC • Kinh Doanh & Công Sở (Business & Corporate)", "Hợp đồng kinh tế, đàm phán thương mại, nhân sự và tài chính doanh nghiệp", "TOEIC", "#8B5CF6"),
            new TopicMeta("TECH", "TECH • Công Nghệ & AI (Technology & Artificial Intelligence)", "Thuật ngữ trí tuệ nhân tạo, an ninh mạng, kiến trúc đám mây và phần mềm", "TECH", "#3B82F6"),
            new TopicMeta("HEALTH", "HEALTH • Y Tế & Sức Khỏe (Medical & Wellbeing)", "Thuật ngữ y khoa, dinh dưỡng học, phòng bệnh và chăm sóc sức khỏe cộng đồng", "HEALTH", "#14B8A6")
    );

    public List<Map<String, Object>> getTopicsSummary() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (TopicMeta meta : TOPIC_METAS) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("code", meta.code);
            map.put("title", meta.title);
            map.put("description", meta.description);
            map.put("level", meta.level);
            map.put("badgeColor", meta.badgeColor);
            List<Vocabulary> words = vocabularyRepository.findByTopicIgnoreCase(meta.code);
            map.put("wordCount", words.size());
            result.add(map);
        }
        return result;
    }

    public List<Vocabulary> getWordsByTopic(String topicCode) {
        if (topicCode == null || topicCode.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return vocabularyRepository.findByTopicIgnoreCaseOrderByIdDesc(topicCode.trim());
    }

    public List<Vocabulary> crawlMoreWordsForTopic(String topicCode) {
        String cleanTopic = topicCode.toUpperCase().trim();
        String datamuseKeyword = switch (cleanTopic) {
            case "TECH" -> "computer";
            case "HEALTH" -> "medical";
            case "TOEIC" -> "corporate";
            case "IELTS" -> "academic";
            case "C2" -> "philosophical";
            case "C1" -> "intellectual";
            case "B2" -> "strategic";
            case "B1" -> "collaborative";
            case "A2" -> "travel";
            default -> "community";
        };

        List<Vocabulary> newlyAdded = new ArrayList<>();
        try {
            String url = "https://api.datamuse.com/words?ml=" + datamuseKeyword + "&max=12";
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(java.time.Duration.ofSeconds(4))
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonNode arr = objectMapper.readTree(resp.body());
                if (arr.isArray()) {
                    for (JsonNode item : arr) {
                        if (item.has("word")) {
                            String w = item.get("word").asText().trim().toLowerCase();
                            if (w.matches("^[a-zA-Z\\-]+$")) {
                                try {
                                    var existingOpt = vocabularyRepository.findByWordIgnoreCase(w);
                                    if (existingOpt.isPresent()) {
                                        Vocabulary existing = existingOpt.get();
                                        if (!cleanTopic.equalsIgnoreCase(existing.getTopic())) {
                                            existing.setTopic(cleanTopic);
                                            vocabularyRepository.save(existing);
                                            newlyAdded.add(existing);
                                        }
                                    } else {
                                        Vocabulary v = dictionaryService.lookupWord(w);
                                        if (v != null) {
                                            v.setTopic(cleanTopic);
                                            v.setCefrLevel(cleanTopic.startsWith("A") || cleanTopic.startsWith("B") || cleanTopic.startsWith("C") ? cleanTopic : "B2");
                                            vocabularyRepository.save(v);
                                            newlyAdded.add(v);
                                        }
                                    }
                                } catch (Exception wordEx) {
                                    log.debug("Skip word {}: {}", w, wordEx.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Crawl more words failed for {}: {}", topicCode, e.getMessage());
        }
        return newlyAdded;
    }

    @PostConstruct
    public void seedCuratedTopics() {
        long count = vocabularyRepository.count();
        if (count >= 100) {
            log.info("Vocabulary database already seeded with {} words.", count);
            return;
        }

        log.info("Seeding curated multi-level & topic English vocabulary database...");

        // 1. A1 • Căn Bản
        seedWord("hello", "xin chào, lời chào", "An expression of greeting.", "noun", "A1", "A1", "/həˈloʊ/", "Hello! Welcome to our English class.");
        seedWord("family", "gia đình, tổ ấm", "A group of individuals living together.", "noun", "A1", "A1", "/ˈfæm.əl.i/", "Spending time with family is precious.");
        seedWord("friend", "người bạn, bạn bè", "A person with whom one has a bond of mutual affection.", "noun", "A1", "A1", "/frend/", "A loyal friend is a great treasure.");
        seedWord("water", "nước uống, nguồn nước", "A transparent, odorless liquid essential for life.", "noun", "A1", "A1", "/ˈwɑː.t̬ɚ/", "Drink plenty of water every day.");
        seedWord("school", "trường học, học đường", "An institution for educating children.", "noun", "A1", "A1", "/skuːl/", "Children learn new ideas at school.");
        seedWord("travel", "du lịch, đi lại", "Make a journey, typically of some length.", "verb", "A1", "A1", "/ˈtræv.əl/", "I love to travel to historic cities.");
        seedWord("happy", "hạnh phúc, vui vẻ", "Feeling or showing pleasure or contentment.", "adjective", "A1", "A1", "/ˈhæp.i/", "A positive mindset keeps you happy.");
        seedWord("listen", "lắng nghe, chú ý lắng nghe", "Give one's attention to a sound.", "verb", "A1", "A1", "/ˈlɪs.ən/", "Listen carefully to the teacher.");
        seedWord("morning", "buổi sáng, bình minh", "The period of time between midnight and noon.", "noun", "A1", "A1", "/ˈmɔːr.nɪŋ/", "I enjoy walking in the early morning.");
        seedWord("evening", "buổi tối, hoàng hôn", "The period of time at the end of the day.", "noun", "A1", "A1", "/ˈiːv.nɪŋ/", "We read together every evening.");

        // 2. A2 • Sơ Cấp
        seedWord("schedule", "lịch trình, thời gian biểu", "A plan for carrying out a process or procedure.", "noun", "A2", "A2", "/ˈskedʒ.uːl/", "Keep a tidy schedule to manage your day.");
        seedWord("weather", "thời tiết, khí hậu", "The state of the atmosphere at a place and time.", "noun", "A2", "A2", "/ˈweð.ɚ/", "The weather today is mild and pleasant.");
        seedWord("describe", "miêu tả, thuật lại", "Give an account in words of someone or something.", "verb", "A2", "A2", "/dɪˈskraɪb/", "Can you describe the picture in detail?");
        seedWord("invite", "mời, gửi lời mời", "Make a polite, formal, or friendly request.", "verb", "A2", "A2", "/ɪnˈvaɪt/", "They will invite all colleagues to the party.");
        seedWord("convenient", "tiện lợi, thuận tiện", "Fitting in well with a person's needs or comfort.", "adjective", "A2", "A2", "/kənˈviː.ni.ənt/", "Online shopping is very convenient.");
        seedWord("routine", "thói quen, nề nếp hàng ngày", "A sequence of actions regularly followed.", "noun", "A2", "A2", "/ruːˈtiːn/", "A morning workout routine boosts energy.");
        seedWord("advice", "lời khuyên, lời chỉ dẫn", "Guidance or recommendations offered.", "noun", "A2", "A2", "/ədˈvaɪs/", "Seek advice from experienced mentors.");
        seedWord("journey", "hành trình, chuyến đi", "An act of traveling from one place to another.", "noun", "A2", "A2", "/ˈdʒɝː.ni/", "Learning a language is an exciting journey.");
        seedWord("holiday", "kỳ nghỉ, ngày lễ", "An extended period of leisure and recreation.", "noun", "A2", "A2", "/ˈhɑː.lə.deɪ/", "We planned a relaxing seaside holiday.");
        seedWord("prepare", "chuẩn bị, sửa soạn", "Make something ready for use or consideration.", "verb", "A2", "A2", "/prɪˈper/", "Prepare well before your examination.");

        // 3. B1 • Trung Cấp
        seedWord("appreciate", "trân trọng, đánh giá cao", "Recognize the full worth of something.", "verb", "B1", "B1", "/əˈpriː.ʃi.eɪt/", "I appreciate your generous assistance.");
        seedWord("enthusiastic", "nhiệt huyết, hào hứng", "Having or showing intense interest or eager enjoyment.", "adjective", "B1", "B1", "/ɪnˌθuː.ziˈæs.tɪk/", "The team is enthusiastic about the new project.");
        seedWord("hesitate", "do dự, ngập ngừng", "Pause before saying or doing something through uncertainty.", "verb", "B1", "B1", "/ˈhez.ə.teɪt/", "Do not hesitate to ask questions in class.");
        seedWord("recommend", "tiến cử, khuyên dùng", "Put forward with approval as being suitable.", "verb", "B1", "B1", "/ˌrek.əˈmend/", "I highly recommend this grammar handbook.");
        seedWord("experience", "kinh nghiệm, trải nghiệm", "Practical contact with and observation of facts or events.", "noun", "B1", "B1", "/ɪkˈspɪr.i.əns/", "Hands-on experience strengthens knowledge.");
        seedWord("confidence", "sự tự tin, lòng tin tưởng", "The feeling of certainty about something.", "noun", "B1", "B1", "/ˈkɑːn.fə.dəns/", "Speaking daily builds English speaking confidence.");
        seedWord("opportunity", "cơ hội, thời cơ", "A set of circumstances making it possible to do something.", "noun", "B1", "B1", "/ˌɑː.pɚˈtuː.nə.t̬i/", "Seize every opportunity to practice conversation.");
        seedWord("challenge", "thách thức, thử thách", "A task or situation that tests someone's abilities.", "noun", "B1", "B1", "/ˈtʃæl.ɪndʒ/", "Embrace each challenge as a growth step.");
        seedWord("colleague", "đồng nghiệp, cộng sự", "A person with whom one works in a profession.", "noun", "B1", "B1", "/ˈkɑː.liːɡ/", "She consulted with her senior colleague.");
        seedWord("decision", "quyết định, sự lựa chọn", "A conclusion or resolution reached after consideration.", "noun", "B1", "B1", "/dɪˈsɪʒ.ən/", "Think carefully before making an important decision.");

        // 4. B2 • Trung Cao Cấp
        seedWord("collaborate", "hợp tác, phối hợp làm việc", "Work jointly on an activity, especially to produce or create something.", "verb", "B2", "B2", "/kəˈlæb.ə.reɪt/", "Engineers collaborate to build innovative tools.");
        seedWord("resilient", "kiên cường, bền bỉ, phục hồi nhanh", "Able to withstand or recover quickly from difficult conditions.", "adjective", "B2", "B2", "/rɪˈzɪl.i.ənt/", "Resilient organizations adapt swiftly to market changes.");
        seedWord("perspective", "quan điểm, góc nhìn", "A particular attitude toward or way of regarding something.", "noun", "B2", "B2", "/pɚˈspek.tɪv/", "Traveling provides a fresh perspective on life.");
        seedWord("negotiate", "đàm phán, thương lượng", "Obtain or bring about by discussion.", "verb", "B2", "B2", "/nəˈɡoʊ.ʃi.eɪt/", "They gathered to negotiate the trade contract.");
        seedWord("efficient", "hiệu quả, năng suất cao", "Achieving maximum productivity with minimum wasted effort.", "adjective", "B2", "B2", "/ɪˈfɪʃ.ənt/", "Modern automation ensures efficient workflows.");
        seedWord("strategy", "chiến lược, kế sách", "A plan of action designed to achieve a long-term goal.", "noun", "B2", "B2", "/ˈstræt̬.ə.dʒi/", "A sound business strategy guarantees sustainable growth.");
        seedWord("innovation", "sự đổi mới, sáng kiến đột phá", "A new method, idea, or product.", "noun", "B2", "B2", "/ˌɪn.əˈveɪ.ʃən/", "Technological innovation drives economic progress.");
        seedWord("crucial", "tối quan trọng, có tính quyết định", "Decisive or critical, especially in the success or failure of something.", "adjective", "B2", "B2", "/ˈkruː.ʃəl/", "Quality rest is crucial for cognitive focus.");
        seedWord("sustainable", "bền vững, duy trì lâu dài", "Able to be maintained at a certain rate or level.", "adjective", "B2", "B2", "/səˈsteɪ.nə.bəl/", "We advocate for sustainable environmental policies.");
        seedWord("implement", "thực thi, triển khai", "Put a decision, plan, or agreement into effect.", "verb", "B2", "B2", "/ˈɪm.plə.ment/", "The company will implement the new policy next month.");

        // 5. C1 • Cao Cấp Học Thuật
        seedWord("breakthrough", "bước đột phá, phát kiến lớn", "A sudden, dramatic, and important discovery or development.", "noun", "C1", "C1", "/ˈbreɪk.θruː/", "The vaccine represents a major medical breakthrough.");
        seedWord("revolutionize", "cách mạng hóa, đổi mới toàn diện", "Change something radically or fundamentally.", "verb", "C1", "C1", "/ˌrev.əˈluː.ʃə.naɪz/", "AI will revolutionize international education.");
        seedWord("hypothesis", "giả thuyết khoa học", "A proposed explanation made as a starting point for further investigation.", "noun", "C1", "C1", "/haɪˈpɑː.θə.sɪs/", "The researchers tested the empirical hypothesis.");
        seedWord("comprehensive", "toàn diện, bao quát sâu rộng", "Complete, including all or nearly all elements of something.", "adjective", "C1", "C1", "/ˌkɑːm.prəˈhen.sɪv/", "The report provides a comprehensive overview of global trade.");
        seedWord("profound", "sâu sắc, uyên thâm", "Having or showing great knowledge, insight, or emotional depth.", "adjective", "C1", "C1", "/prəˈfaʊnd/", "His philosophy had a profound impact on society.");
        seedWord("intricate", "tinh xảo, phức tạp tinh vi", "Very complicated or detailed.", "adjective", "C1", "C1", "/ˈɪn.trə.kət/", "The watch possesses an intricate mechanical movement.");
        seedWord("diminish", "thu nhỏ, làm giảm bớt", "Make or become less in size, extent, or importance.", "verb", "C1", "C1", "/dɪˈmɪn.ɪʃ/", "Prompt action will diminish financial risks.");
        seedWord("consequence", "hệ quả, kết quả tất yếu", "A result or effect of an action or condition.", "noun", "C1", "C1", "/ˈkɑːn.sə.kwəns/", "Every major decision carries an inevitable consequence.");
        seedWord("paradigm", "mô hình mẫu, hệ hình tư duy", "A typical example or pattern of something; a distinct concept.", "noun", "C1", "C1", "/ˈper.ə.daɪm/", "Quantum theory created a new scientific paradigm.");
        seedWord("distinction", "sự phân biệt, nét đặc trưng ưu tú", "A difference or contrast between similar things.", "noun", "C1", "C1", "/dɪˈstɪŋk.ʃən/", "He graduated with highest academic distinction.");

        // 6. C2 • Thành Thạo Bậc Cao
        seedWord("ubiquitous", "hiện diện khắp nơi, phổ quát", "Present, appearing, or found everywhere.", "adjective", "C2", "C2", "/juːˈbɪk.wə.t̬əs/", "Smartphones have become ubiquitous across modern societies.");
        seedWord("ephemeral", "phù du, sớm nở tối tàn", "Lasting for a very short time.", "adjective", "C2", "C2", "/ɪˈfem.ər.əl/", "The cherry blossom's beauty is delicate and ephemeral.");
        seedWord("eloquent", "hùng biện, lưu loát truyền cảm", "Fluent or persuasive in speaking or writing.", "adjective", "C2", "C2", "/ˈel.ə.kwənt/", "Her eloquent speech captivated the academic audience.");
        seedWord("serendipity", "sự tình cờ may mắn kỳ diệu", "The occurrence of events by chance in a happy or beneficial way.", "noun", "C2", "C2", "/ˌser.ənˈdɪp.ə.t̬i/", "Meeting my research co-author was sheer serendipity.");
        seedWord("pragmatic", "thực dụng, thực tiễn", "Dealing with things sensibly and realistically based on practical conditions.", "adjective", "C2", "C2", "/præɡˈmæt̬.ɪk/", "They took a pragmatic approach to dispute resolution.");
        seedWord("ineffable", "khôn tả, không lời nào diễn tả xiết", "Too great or extreme to be expressed in words.", "adjective", "C2", "C2", "/ɪnˈef.ə.bəl/", "Standing atop the summit brought ineffable joy.");
        seedWord("quintessential", "tinh túy, mẫu mực điển hình", "Representing the most perfect or typical example of a quality.", "adjective", "C2", "C2", "/ˌkwɪn.təˈsen.ʃəl/", "The afternoon tea is a quintessential British tradition.");
        seedWord("juxtapose", "đặt cạnh nhau để đối chiếu", "Place or deal with close together for contrasting effect.", "verb", "C2", "C2", "/ˌdʒʌk.stəˈpoʊz/", "The exhibition juxtaposes contemporary art with ancient sculpture.");
        seedWord("ameliorate", "cải thiện, làm cho tốt đẹp hơn", "Make something bad or unsatisfactory better.", "verb", "C2", "C2", "/əˈmiːl.jə.reɪt/", "Social programs strive to ameliorate living conditions.");
        seedWord("panacea", "thuốc trị bách bệnh, giải pháp vạn năng", "A remedy for all difficulties or diseases.", "noun", "C2", "C2", "/ˌpæn.əˈsiː.ə/", "Technology is transformative, but not a panacea for all societal ills.");

        // 7. IELTS Academic Core
        seedWord("substantiate", "chứng minh, đưa ra căn cứ xác thực", "Provide evidence to support or prove the truth of.", "verb", "IELTS", "C1", "/səbˈstæn.ʃi.eɪt/", "Candidates must substantiate their claims with empirical data.");
        seedWord("correlation", "mối tương quan, quan hệ gắn kết", "A mutual relationship or connection between two or more things.", "noun", "IELTS", "B2", "/ˌkɔːr.əˈleɪ.ʃən/", "There is a strong correlation between literacy and prosperity.");
        seedWord("deteriorate", "suy thoái, xuống cấp", "Become progressively worse.", "verb", "IELTS", "B2", "/dɪˈtɪr.i.ə.reɪt/", "Air quality may deteriorate during dry seasons.");
        seedWord("mitigate", "giảm nhẹ, xoa dịu tác hại", "Make less severe, serious, or painful.", "verb", "IELTS", "B2", "/ˈmɪt̬.ə.ɡeɪt/", "Reforestation helps mitigate climate change impacts.");
        seedWord("discrepancy", "sự chênh lệch, điểm bất đồng", "A lack of compatibility or similarity between two or more facts.", "noun", "IELTS", "C1", "/dɪˈskrep.ən.si/", "Auditors detected a discrepancy in the financial records.");
        seedWord("indispensable", "không thể thiếu, thiết yếu tuyệt đối", "Absolutely necessary.", "adjective", "IELTS", "C1", "/ˌɪn.dɪˈspen.sə.bəl/", "Critical thinking is an indispensable academic skill.");
        seedWord("manifest", "biểu hiện rõ ràng, bộc lộ", "Show something clearly by signs or actions.", "verb", "IELTS", "C1", "/ˈmæn.ə.fest/", "Stress can manifest through physical symptoms.");
        seedWord("catalyst", "chất xúc tác, nhân tố thúc đẩy", "A person or thing that precipitates an event or change.", "noun", "IELTS", "C1", "/ˈkæt̬.əl.ɪst/", "Urban renewal served as a catalyst for local business growth.");

        // 8. TOEIC Business & Corporate
        seedWord("reimbursement", "sự hoàn tiền, thanh toán công tác phí", "Repayment of money spent on company business.", "noun", "TOEIC", "B2", "/ˌriː.ɪmˈbɝːs.mənt/", "Submit receipts to claim travel expense reimbursement.");
        seedWord("specification", "thông số kỹ thuật, quy chuẩn chi tiết", "An act of describing or identifying something precisely.", "noun", "TOEIC", "B2", "/ˌspes.ə.fəˈkeɪ.ʃən/", "The machinery complies with international safety specifications.");
        seedWord("confidential", "tuyệt mật, bảo mật thông tin", "Intended to be kept secret.", "adjective", "TOEIC", "B2", "/ˌkɑːn.fəˈden.ʃəl/", "All client contract documents remain strictly confidential.");
        seedWord("revenue", "doanh thu, lợi tức công ty", "Income, especially when of a company or organization.", "noun", "TOEIC", "B2", "/ˈrev.ə.nuː/", "Quarterly revenue exceeded market projections by 15 percent.");
        seedWord("procurement", "sự thu mua, mua sắm trang thiết bị", "The action of obtaining or procuring equipment or supplies.", "noun", "TOEIC", "C1", "/prəˈkjʊr.mənt/", "The procurement department negotiated competitive pricing.");
        seedWord("stakeholder", "bên liên quan, cổ đông hữu quan", "A person with an interest or concern in something, especially a business.", "noun", "TOEIC", "B2", "/ˈsteɪkˌhoʊl.dɚ/", "We scheduled a briefing for all corporate stakeholders.");
        seedWord("compliance", "sự tuân thủ pháp luật và quy chuẩn", "The action of complying with a wish, command, or regulation.", "noun", "TOEIC", "B2", "/kəmˈplaɪ.əns/", "Regulatory compliance is vital in commercial banking.");

        // 9. TECH • Công Nghệ & AI
        seedWord("algorithm", "thuật toán, giải thuật tính toán", "A process or set of rules to be followed in calculations.", "noun", "TECH", "B2", "/ˈæl.ɡə.rɪ.ðəm/", "The search algorithm ranks pages according to relevancy.");
        seedWord("cybersecurity", "an ninh mạng, bảo mật số", "The practice of protecting systems, networks, and programs from digital attacks.", "noun", "TECH", "B2", "/ˌsaɪ.bɚ.səˈkjʊr.ə.t̬i/", "Strong cybersecurity measures defend against ransomware threats.");
        seedWord("encryption", "mã hóa dữ liệu bảo mật", "The process of converting information or data into a code.", "noun", "TECH", "B2", "/ɪnˈkrɪp.ʃən/", "End-to-end encryption safeguards sensitive communications.");
        seedWord("infrastructure", "cơ sở hạ tầng kỹ thuật", "The basic physical and organizational structures and facilities.", "noun", "TECH", "B2", "/ˈɪn.frəˌstrʌk.tʃɚ/", "Cloud computing providers invest heavily in resilient infrastructure.");
        seedWord("scalability", "khả năng mở rộng quy mô hệ thống", "The capacity to be changed in size or scale to handle growing demands.", "noun", "TECH", "B2", "/ˌskeɪ.ləˈbɪl.ə.t̬i/", "Microservice architecture provides superior system scalability.");
        seedWord("automation", "tự động hóa quy trình", "The use of largely automatic equipment in a system of operation.", "noun", "TECH", "B2", "/ˌɑː.t̬əˈmeɪ.ʃən/", "Test automation reduces release delivery cycles significantly.");
        seedWord("interface", "giao diện người dùng hoặc kết nối hệ thống", "A point where two systems or humans interact.", "noun", "TECH", "B2", "/ˈɪn.t̬ɚ.feɪs/", "An intuitive user interface enriches customer engagement.");

        // 10. HEALTH • Y Tế & Sức Khỏe
        seedWord("diagnosis", "chẩn đoán bệnh học", "The identification of the nature of an illness by examination.", "noun", "HEALTH", "B2", "/ˌdaɪ.əɡˈnoʊ.sɪs/", "Early clinical diagnosis dramatically improves recovery chances.");
        seedWord("prevention", "phòng ngừa dịch bệnh, ngăn ngừa rủi ro", "The action of stopping something from happening or arising.", "noun", "HEALTH", "B2", "/prɪˈven.ʃən/", "Preventive medicine emphasizes wellness and disease prevention.");
        seedWord("immunity", "hệ miễn dịch, sức đề kháng", "The ability of an organism to resist a particular infection.", "noun", "HEALTH", "B2", "/ɪˈmjuː.nə.t̬i/", "Proper sleep and wholesome nutrition reinforce natural immunity.");
        seedWord("symptom", "triệu chứng lâm sàng", "A physical or mental feature indicating a condition of disease.", "noun", "HEALTH", "A2", "/ˈsɪmp.təm/", "Fever and fatigue are typical symptoms of a viral infection.");
        seedWord("cardiovascular", "tim mạch, thuộc về tim và mạch máu", "Relating to the heart and blood vessels.", "adjective", "HEALTH", "C1", "/ˌkɑːr.di.oʊˈvæs.kjə.lɚ/", "Aerobic exercise supports long-term cardiovascular health.");
        seedWord("metabolism", "sự trao đổi chất, chuyển hóa sinh học", "The chemical processes that occur within a living organism to maintain life.", "noun", "HEALTH", "B2", "/məˈtæb.əl.ɪ.zəm/", "A balanced diet maintains a healthy basal metabolism.");
        seedWord("hygiene", "vệ sinh phòng dịch, giữ gìn sạch sẽ", "Conditions or practices conducive to maintaining health and preventing disease.", "noun", "HEALTH", "B1", "/ˈhaɪ.dʒiːn/", "Practicing good hand hygiene stops the spread of contagions.");

        log.info("Finished seeding curated vocabulary database.");
    }

    private void seedWord(String word, String meaningVi, String meaningEn, String pos, String topic, String cefr, String ipa, String example) {
        Optional<Vocabulary> opt = vocabularyRepository.findByWordIgnoreCase(word);
        Vocabulary v = opt.orElseGet(Vocabulary::new);
        v.setWord(word);
        v.setMeaningVi(meaningVi);
        v.setMeaningEn(meaningEn);
        v.setPartOfSpeech(pos);
        v.setTopic(topic);
        v.setCefrLevel(cefr);
        v.setIpa(ipa);
        v.setExampleSentence(example);
        v.setAudioUrl("https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=en&q=" + word);
        vocabularyRepository.save(v);
    }
}

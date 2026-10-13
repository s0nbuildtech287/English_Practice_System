# KẾ HOẠCH PHÁT TRIỂN DỰ ÁN: SMART ENGLISH (READ & LISTEN)
> **Ứng dụng Luyện Tiếng Anh Đọc Hiểu & Nghe Tương Tác Đa Nền Tảng**  
> *Định hướng: Tối ưu tự học thực chiến, chi phí vận hành 0đ, chuẩn kiến trúc làm đẹp CV.*

---

## 1. TỔNG QUAN DỰ ÁN (PROJECT OVERVIEW)

* **Tên dự án dự kiến:** `SmartEnglish` (hoặc `EngPulse`)
* **Mục tiêu sản phẩm:** 
  * Cung cấp trải nghiệm học tiếng Anh tự nhiên qua **Đọc bài thực tế kết hợp Nghe audio người bản xứ (Read & Listen)**.
  * Hỗ trợ tra từ ngữ cảnh 1-chạm (Instant Look-up), lưu sổ tay từ vựng và ôn tập ngắt quãng (Spaced Repetition).
  * Luyện nghe chép chính tả/điền từ (Dictation) không cần dùng micro phức tạp.
* **Nền tảng hỗ trợ (Cross-platform):**
  * **Điện thoại:** Android (xuất file `.apk` cài trực tiếp) & iOS (chạy web mượt như native qua PWA).
  * **Máy tính:** Chạy trực tiếp trên Web (Chrome/Edge) hoặc build native Windows App (`.exe`).
* **Điểm sáng kỹ thuật cho CV (Resume Highlights):**
  * **Backend (Java Spring Boot 3):** Clean Architecture / RESTful APIs, Spring Data JPA, Caching, Flyway migration, tích hợp AI Gateway (Gemini API Free Tier) & Dictionary API.
  * **Frontend (Flutter & Dart):** Quản lý State hiện đại (Riverpod / Bloc), Audio streaming & highlight đồng bộ văn bản, Responsive đa thiết bị (Mobile & Desktop/Web).
  * **Thuật toán:** Triển khai thuật toán Spaced Repetition (SRS - tương tự SuperMemo SM-2/Anki) cho Flashcards.

---

## 2. KIẾN TRÚC & CÔNG NGHỆ (TECH STACK)

```
┌─────────────────────────────────────────────────────────────┐
│                 CLIENT LAYER (FLUTTER)                      │
│   - Mobile (Android APK / iOS PWA)                          │
│   - Desktop / Web (Windows App / Chrome Browser)            │
│   - Tech: Flutter 3.x, Riverpod/Bloc, audioplayers          │
└──────────────────────────────┬──────────────────────────────┘
                               │ REST APIs / JSON
┌──────────────────────────────▼──────────────────────────────┐
│               BACKEND LAYER (JAVA SPRING BOOT 3)            │
│   - Controller / Service / Repository                       │
│   - Security & JWT Authentication                           │
│   - Scheduler (Nhắc ôn từ vựng SRS)                         │
│   - AI Gateway (Gemini 1.5/2.0 Flash)                       │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
┌──────────────▼──────────────┐  ┌────────────▼──────────────┐
│       DATABASE LAYER        │  │     EXTERNAL SERVICES     │
│   - PostgreSQL / SQLite     │  │   - Free Dictionary API   │
│   - Flyway DB Migration     │  │   - Gemini AI (Free Tier) │
│   - Hibernate / JPA         │  │   - Audio Stream (VOA/BBC)│
└─────────────────────────────┘  └───────────────────────────┘
```

* **Backend:** 
  * Java 17 hoặc 21 LTS
  * Framework: Spring Boot 3.x
  * Database: PostgreSQL (Production) / SQLite hoặc H2 (Local Dev nhanh)
  * Build tool: Maven hoặc Gradle
* **App / Client:** 
  * Flutter SDK (Dart)
  * State Management: Riverpod hoặc BLoC
  * Audio: `audioplayers` hoặc `just_audio`
* **External APIs (Chi phí 0 VNĐ):**
  * Tra từ: **Free Dictionary API** (`api.dictionaryapi.dev`)
  * AI hỗ trợ câu hỏi & phân tích: **Google Gemini 1.5/2.0 Flash API** (Free Tier 15 requests/phút)
  * Audio & Bài đọc: Nguồn mở **VOA Learning English & BBC Learning English**

---

## 3. CHI TIẾT CÁC TÍNH NĂNG CỐT LÕI (CORE FEATURES)

### 3.1. Thư viện bài đọc & Nghe (Library & Levels)
* Phân cấp bài theo chuẩn CEFR: **A2 (Dễ) - B1 (Trung cấp) - B2/C1 (Nâng cao)**.
* Lọc bài theo chủ đề: *Công nghệ, Đời sống, Kinh doanh, Khoa học, Tin tức*.
* Tính năng **"Custom Reading"**: Người dùng có thể tự copy/paste văn bản tiếng Anh bất kỳ để học.

### 3.2. Trình đọc & Nghe tương tác (Interactive Audio Reader)
* Phát audio MP3 người bản xứ đọc bài với thanh điều khiển tiện lợi (Play/Pause, tua lại 5s, chỉnh tốc độ 0.75x - 1.0x - 1.25x).
* **Tra từ ngữ cảnh 1-chạm (Tap to Look up):**
  * Bấm vào từ bất kỳ trên bài đọc -> Hiển thị popup nghĩa tiếng Việt, phiên âm IPA, loại từ và phát âm thanh từ đó.
  * Nút **"Lưu từ"**: Đưa từ vào Sổ tay từ vựng kèm câu văn ngữ cảnh.

### 3.3. Luyện nghe đục lỗ / Chép chính tả (Dictation Mode)
* App phát 1 câu audio ngắn (5-10 từ).
* Ẩn 1-2 từ khóa quan trọng để người dùng gõ vào (hoặc gõ lại cả câu).
* Chấm điểm tức thì, giúp rèn luyện khả năng bắt âm nối, âm đuôi mà **không cần micro**.

### 3.4. Sổ tay từ vựng & Ôn tập Spaced Repetition (SRS Flashcards)
* Áp dụng thuật toán lặp lại ngắt quãng (SM-2):
  * Từ mới lưu: Ôn sau 1 ngày -> 3 ngày -> 7 ngày -> 14 ngày -> 30 ngày.
  * Nếu quên: Reset chu kỳ ôn tập.
* Giao diện Flashcard lật mặt trước (Từ + Audio) / mặt sau (Nghĩa + Câu ví dụ thực tế).

### 3.5. Kiểm tra đọc hiểu tự động (AI Comprehension Quiz)
* Mỗi bài đọc có 3-4 câu hỏi trắc nghiệm kiểm tra độ hiểu bài.
* AI (Gemini Flash) tự động sinh bộ câu hỏi và giải thích đáp án ngắn gọn khi nạp bài đọc mới vào hệ thống.

---

## 4. THIẾT KẾ DỮ LIỆU (DATABASE SCHEMA DỰ KIẾN)

1. **`articles`** (Bài đọc):
   * `id` (PK), `title`, `content`, `level` (A2/B1/B2), `topic`, `audio_url`, `duration_seconds`, `created_at`.
2. **`vocabularies`** (Từ điển chuẩn):
   * `id` (PK), `word`, `ipa`, `part_of_speech`, `meaning_vi`, `meaning_en`, `audio_url`, `example_sentence`.
3. **`user_vocab_notebook`** (Sổ tay từ vựng cá nhân):
   * `id` (PK), `user_id`, `vocab_id`, `context_sentence`, `repetition_level`, `ease_factor`, `next_review_at`.
4. **`quizzes`** (Câu hỏi đọc hiểu):
   * `id` (PK), `article_id`, `question`, `options_json`, `correct_answer_index`, `explanation`.
5. **`user_history`** (Lịch sử học tập):
   * `id` (PK), `user_id`, `article_id`, `is_completed`, `quiz_score`, `last_studied_at`.

---

## 5. LỘ TRÌNH TRIỂN KHAI THEO GIAI ĐOẠN (ROADMAP)

### 📌 Giai đoạn 1: Khởi tạo Kiến trúc & Hạ tầng (Sprint 1)
- [ ] Khởi tạo project `backend` (Spring Boot 3, Java 17/21, Maven/Gradle).
- [ ] Thiết lập Database (PostgreSQL/H2) và tạo các Entity / Migration cơ bản.
- [ ] Khởi tạo project `app` (Flutter) với cấu trúc thư mục phân tầng rõ ràng (Clean Architecture).
- [ ] Viết API Service trong Backend tích hợp **Free Dictionary API** để tra từ.

### 📌 Giai đoạn 2: Module Đọc & Nghe Tương Tác (Sprint 2 - Core MVP)
- [ ] Backend: API quản lý bài đọc, API nạp dữ liệu bài đọc kèm audio mẫu (VOA/BBC).
- [ ] Flutter: Xây dựng màn hình Thư viện bài đọc (Danh sách, Filter Level).
- [ ] Flutter: Xây dựng màn hình Trình đọc bài (Text Reader) + Audio Player phát mượt mà.
- [ ] Flutter: Xử lý sự kiện bấm vào từng từ (Text Span Clickable) -> Popup tra từ tức thì + Nút lưu từ.

### 📌 Giai đoạn 3: Flashcard SRS & Luyện Nghe Điền Từ (Sprint 3)
- [ ] Backend: Cài đặt logic thuật toán Spaced Repetition (tính ngày ôn tập tiếp theo).
- [ ] Flutter: Xây dựng màn hình Sổ tay từ vựng + Mini-game lật Flashcard ôn từ.
- [ ] Flutter & Backend: Xây dựng màn hình Luyện nghe chép từ / đục lỗ (Dictation Mode).

### 📌 Giai đoạn 4: Tích hợp AI Quiz & Hoàn thiện CV (Sprint 4)
- [ ] Backend: Tích hợp Gemini 1.5/2.0 Flash để tự động tạo Quiz trắc nghiệm cho bài đọc.
- [ ] Flutter: Màn hình làm Quiz sau khi đọc xong bài.
- [ ] Build xuất file Android `.apk` và chạy thử nghiệm trên Web/Windows.
- [ ] Viết file `README.md` chuyên nghiệp với sơ đồ kiến trúc, API docs (Swagger) để đưa vào CV.

---

## 6. CẤU TRÚC THƯ MỤC DỰ ÁN

```text
PracticeEnglish/
├── PROJECT_PLAN.md               # File kế hoạch tổng thể này
├── backend/                      # Java Spring Boot 3
│   ├── src/main/java/com/smarteng/
│   │   ├── config/               # Security, WebMvc, AI config
│   │   ├── controller/           # REST Controllers
│   │   ├── model/                # JPA Entities
│   │   ├── repository/           # Spring Data Repositories
│   │   └── service/              # Core business logic (Dictionary, SRS, Article)
│   └── pom.xml
└── app/                          # Flutter Application
    ├── lib/
    │   ├── core/                 # Theme, constants, networking
    │   ├── features/
    │   │   ├── article/          # Danh sách bài & Trình đọc nghe
    │   │   ├── dictation/        # Luyện nghe điền từ
    │   │   └── vocabulary/       # Sổ tay từ vựng & Flashcards
    │   └── main.dart
    └── pubspec.yaml
```

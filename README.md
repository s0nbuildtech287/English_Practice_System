# 🎧 SmartEnglish — Interactive Read & Listen Platform

> **Hệ thống luyện tiếng Anh Đọc hiểu & Nghe tương tác đa nền tảng theo chuẩn khung CEFR (A1 -> C2)**  
> *Kiến trúc phân tầng Clean Architecture, vận hành chi phí 0đ, tối ưu cho Portfolio & CV Software Engineer.*

---

## 🌟 Tính Năng Nổi Bật (Key Features)

- **🎧 Nghe & Đọc Song Hành (Interactive Read & Listen):**
  - Trình đọc bài đa cấp độ kết hợp Audio bản xứ (Neural TTS chuẩn Mỹ).
  - Tích hợp điều khiển Audio thanh tiến trình, tua nhanh/chậm và tự động phát liên tục.
- **🎯 Phân Cấp Chuẩn CEFR Toàn Diện (A1 to C2):**
  - **A1 (Căn bản - Beginner)**: Giao tiếp, thói quen sinh hoạt thường nhật.
  - **A2 (Sơ cấp - Elementary)**: Nắm bắt thông tin ngắn gọn, mô tả hoạt động gần gũi.
  - **B1 (Trung cấp - Intermediate)**: Đời sống, công nghệ, y tế & giao tiếp chủ động.
  - **B2 (Trung cao - Upper-Inter)**: Môi trường, bài báo học thuật, sự nghiệp.
  - **C1 (Cao cấp - Advanced)**: Tâm lý học, nhận thức, tư duy logic phản biện.
  - **C2 (Thành thạo - Proficiency)**: Triết học ngôn ngữ, sắc thái văn học đỉnh cao.
- **💡 Tra Cứu Từ Vựng Tức Thì (Instant Multi-tier Dictionary):**
  - Tra từ 1-chạm: Phiên âm IPA chuẩn, từ loại, định nghĩa Anh - Anh và audio phát âm tức thì.
  - Cơ chế dự phòng thông minh (Multi-tier Fallback): FreeDictionary API $\rightarrow$ Datamuse API $\rightarrow$ Google TTS phát âm chuẩn 100%.
- **📓 Sổ Tay Từ Vựng & 3D Flashcards SRS:**
  - Lưu từ chỉ với 1 click vào sổ tay cá nhân.
  - Thẻ ghi nhớ 3D Spaced Repetition (Lặp lại ngắt quãng SM-2) giúp tối đa hóa khả năng ghi nhớ dài hạn.
- **📑 Danh Sách Bài Cần Đọc (Reading Watchlist):**
  - Bookmark các bài đọc yêu thích hoặc tự dán nội dung văn bản cá nhân để học.

---

## 🏗️ Kiến Trúc Hệ Thống (Tech Stack)

```
┌─────────────────────────────────────────────────────────────┐
│                 CLIENT LAYER (FLUTTER)                      │
│   - Flutter Web & Mobile (Android / Windows / Web)          │
│   - Google Fonts (Outfit, Inter), Audioplayers              │
│   - 3D Flip Flashcards, Dark Mode Glassmorphism UI          │
└──────────────────────────────┬──────────────────────────────┘
                               │ RESTful APIs / JSON
┌──────────────────────────────▼──────────────────────────────┐
│               BACKEND LAYER (SPRING BOOT 3)                 │
│   - Java 18 / Spring Boot 3.3.4, Spring Data JPA            │
│   - H2 Database (File persistent mode) / REST Controllers   │
│   - Multi-tier Dictionary Fallback (FreeDict + Datamuse)    │
│   - Local High-Quality Neural Audio Server                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Hướng Dẫn Cài Đặt & Khởi Chạy (Quick Start)

### 1. Yêu cầu môi trường
- **JDK:** Java 17+ (Khuyến nghị Corretto JDK 18 hoặc OpenJDK 21)
- **Flutter SDK:** Flutter 3.x+
- **Python:** 3.10+ (nếu cần sinh thêm audio TTS offline)

### 2. Khởi chạy Backend (Spring Boot)
```bash
cd backend
./gradlew bootRun
```
*Backend sẽ chạy tại `http://localhost:8080` (H2 Console: `/h2-console`, Swagger: `/swagger-ui.html`)*

### 3. Khởi chạy Frontend (Flutter)
```bash
cd app
flutter run -d chrome --web-port 4000
```
*Hoặc khởi chạy bản build tối ưu: `python -m http.server 4000 --directory app/build/web`*

---

## 📄 Bản Quyền & Tác Giả
- Phát triển bởi: **Xuan Son (s0nbuildtech287)**
- Giấy phép: MIT License

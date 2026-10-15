# Practice English — CEFR Academic Language Platform

Nền tảng trực tuyến toàn diện hỗ trợ rèn luyện, nâng cao và kiểm định 4 kỹ năng tiếng Anh học thuật (Reading, Listening, Writing, Speaking) theo chuẩn khung tham chiếu Châu Âu (CEFR A1 đến C2), tích hợp AI Gemini Tutor và thuật toán lặp lại ngắt quãng Spaced Repetition (SRS).

- Live Demo: https://englistpractice.vercel.app/
- Mã nguồn: https://github.com/s0nbuildtech287/English_Practice_System
- Phiên bản: 2.0.0 Production

---

## 1. TỔNG QUAN NỀN TẢNG

Practice English được xây dựng nhằm giải quyết bài toán tự học tiếng Anh học thuật một cách bài bản, khoa học và không phụ thuộc vào các dịch vụ trả phí đắt đỏ. Ứng dụng mô phỏng môi trường khảo thí tiêu chuẩn quốc tế (IELTS / Cambridge / TOEFL), kết hợp công nghệ web hiện đại, xử lý giọng nói Speech-to-Text / Text-to-Speech và trí tuệ nhân tạo để phân tích lỗi sai ngữ pháp, gợi ý nâng cấp từ vựng tức thì.

---

## 2. CHƯƠNG TRÌNH ĐÀO TẠO 4 KỸ NĂNG CỐT LÕI

### Kỹ Năng Đọc Hiểu & Phân Tích Ngữ Liệu (Reading Comprehension)
- Thư viện bài đọc học thuật đồ sộ phân cấp rõ ràng từ A1 (Beginner) đến C2 (Mastery).
- Nội dung bao quát đa lĩnh vực: Khoa học công nghệ, y sinh học, tâm lý học nhận thức, triết học, kinh tế và môi trường.
- Tra từ ngữ cảnh 1-chạm: Hiển thị ngay phiên âm quốc tế IPA, từ loại, định nghĩa Anh - Anh, giải nghĩa tiếng Việt và ví dụ câu thực tế.
- Bộ câu hỏi trắc nghiệm kiểm tra độ hiểu sâu (Reading Quiz) đi kèm từng bài đọc với giải thích đáp án chi tiết.

### Kỹ Năng Nghe Hiểu & Chép Chính Tả (Listening & Dictation)
- Trình phát âm thanh học thuật (Neural Audio Player) chất lượng cao giọng bản ngữ chuẩn Mỹ.
- Chế độ chép chính tả thông minh (Interactive Dictation): Chia bài nghe thành từng câu độc lập, hỗ trợ phím tắt nghe lại, tua chậm tốc độ (0.75x, 1.0x, 1.25x).
- Hệ thống so khớp ký tự tự động (String Diff Matching) chỉ ra chính xác từ nghe sai, từ thiếu và từ thừa.
- Tự động thống kê độ chính xác theo phần trăm và thời gian hoàn thành.

### Phòng Luyện Viết & Chấm Điểm CEFR (Academic Writing Studio)
- Trình soạn thảo văn bản học thuật hỗ trợ đếm số từ, độ phong phú từ vựng (Lexical Diversity) và cấu trúc câu phức.
- Phân tích và chấm điểm bài viết theo thang đo CEFR từ A1 đến C2 dựa trên 4 tiêu chí chuẩn:
  + Task Achievement (Mức độ hoàn thành đề bài)
  + Coherence and Cohesion (Độ mạch lạc và liên kết câu)
  + Lexical Resource (Vốn từ vựng học thuật)
  + Grammatical Range and Accuracy (Ngữ pháp và độ chuẩn xác)
- Gợi ý nâng cấp câu văn thông thường lên cấu trúc câu học thuật nâng cao (Academic Rephrasing).

### Kỹ Năng Phát Âm Chuẩn & Shadowing (Speaking & IPA Analysis)
- Tích hợp Web Speech Recognition API để thu âm và nhận diện giọng nói trực tiếp qua trình duyệt.
- Chế độ Shadowing: Nghe câu mẫu bản ngữ và đọc lại theo ngữ điệu thực tế.
- Phân tích ngữ âm chi tiết: So sánh văn bản nhận diện với câu gốc để tính điểm phát âm chuẩn xác (Accuracy Score).
- Bảng tra cứu 44 âm IPA (International Phonetic Alphabet) kèm khẩu hình và ví dụ minh họa.

---

## 3. CÁC TÍNH NĂNG THÔNG MINH MỞ RỘNG

### Sổ Tay Từ Vựng & Thẻ Flashcards Spaced Repetition (SRS)
- Lưu từ vựng nhanh từ bài đọc vào bộ sưu tập cá nhân chỉ với một cú nhấp chuột.
- Ứng dụng thuật toán lặp lại ngắt quãng SuperMemo SM-2 giúp tối ưu hóa chu kỳ ôn tập theo đường cong quên lãng Ebbinghaus.
- Thẻ ghi nhớ 3D Flip Flashcards tương tác mượt mà, phân loại trạng thái học tập rõ ràng (Đang học, Cần ôn gấp, Đã thành thạo).

### Cố Vấn Học Thuật AI Trực Tuyến 24/7 (AI Academic Advisor)
- Tích hợp mô hình Gemini AI thế hệ mới với vai trò Cố vấn Học thuật Viện Cambridge.
- Giải đáp thắc mắc ngữ pháp phức tạp, sắc thái từ đồng nghĩa (Nuances), giải thích thành ngữ (Idioms/Collocations).
- Viết bài luận mẫu học thuật theo yêu cầu với dàn ý logic và bản dịch song ngữ.

### Trình Cào & Nạp Bài Đọc Tùy Biến (Custom Article Importer)
- Cho phép người học tự dán bất kỳ bài báo, văn bản tiếng Anh yêu thích để chuyển đổi thành bài đọc tương tác.
- Tự động tách đoạn, sinh audio giọng đọc và hỗ trợ tra từ ngữ cảnh như một bài học tiêu chuẩn.

### Định Hình Lộ Trình Học Cá Nhân Hóa (Onboarding Roadmap)
- Khảo sát đầu vào xác định mục tiêu học tập (Giao tiếp, Du học, Công việc, Thi chứng chỉ).
- Đánh giá trình độ xuất phát và thiết lập cấp độ mục tiêu rõ ràng.
- Giao diện Dark Mode phong cách học viện cao cấp (Oxford Navy / Deep Space), tối ưu thị giác khi học tập thời gian dài.

---

## 4. CÔNG NGHỆ VÀ KIẾN TRÚC HỆ THỐNG

### Phía Người Dùng (Frontend Architecture)
- HTML5 ngữ nghĩa và CSS3 hiện đại với hệ thống biến CSS Design Tokens (Oxford Blue, Cambridge Gold, Academic Navy).
- JavaScript thuần (Vanilla JS ES6+) tối ưu hóa hiệu năng tối đa, không phụ thuộc framework cồng kềnh, tải trang tức thì.
- Phông chữ học thuật cao cấp: Plus Jakarta Sans (Giao diện điều hướng) và Newsreader (Nội dung văn bản, bài báo học thuật).
- Web Speech API: Tích hợp trực tiếp SpeechRecognition và SpeechSynthesis.
- Lưu trữ cục bộ LocalStorage: Đồng bộ tiến độ học, từ vựng lưu trữ, lịch sử chép chính tả và trạng thái thẻ SRS offline.

### Phía Máy Chủ & Tích Hợp Đám Mây (Backend & Serverless)
- Vercel Serverless Functions (Node.js runtime) xử lý an toàn các truy vấn AI qua endpoint /api/ai.
- Google Generative AI (Gemini 2.5/Flash API) phục vụ cố vấn học thuật và phân tích bài viết.
- Multi-tier Dictionary Engine: Tích hợp Free Dictionary API và bộ dữ liệu dự phòng offline đảm bảo tra từ không bao giờ gián đoạn.
- Kho dữ liệu âm thanh bản ngữ: Hệ thống file MP3 chất lượng cao được lưu trữ sẵn trong thư mục public/audio.

---

## 5. CẤU TRÚC THƯ MỤC DỰ ÁN

English_Practice_System/
|-- api/
|   `-- ai.js                 (Vercel Serverless Function kết nối Google Gemini API)
|-- public/
|   |-- audio/                (Kho âm thanh giọng đọc bản ngữ cho 24 bài đọc mẫu)
|   |-- fallback-data.js      (Dữ liệu bài đọc mẫu chuẩn CEFR từ A1 đến C2)
|   |-- favicon.svg           (Biểu tượng nhận diện thương hiệu)
|   |-- index.html            (Giao diện Single Page Application và toàn bộ logic client)
|   `-- logo.svg              (Logo vector Practice English)
|-- generate_24_audios.py     (Script Python tự động sinh file audio bằng Neural TTS)
|-- generate_all_audio.py      (Script Python hỗ trợ tổng hợp giọng đọc hàng loạt)
|-- start.bat                 (Script khởi động nhanh môi trường thử nghiệm trên Windows)
|-- start.ps1                 (PowerShell script quản lý môi trường dev local)
|-- vercel.json               (Cấu hình định tuyến và triển khai serverless trên Vercel)
`-- README.md                 (Tài liệu giới thiệu dự án)

---

## 6. HƯỚNG DẪN CÀI ĐẶT VÀ KHỞI CHẠY CỤC BỘ

### Yêu Cầu Môi Trường
- Trình duyệt web hiện đại (Google Chrome, Microsoft Edge, Brave) để hỗ trợ đầy đủ Web Speech API.
- Node.js phiên bản 18 trở lên (hoặc bất kỳ Web Server tĩnh nào như Python http.server, Live Server).

### Các Bước Khởi Chạy Nhanh

Bước 1: Tải mã nguồn về máy tính hoặc mở thư mục dự án
cd English_Practice_System

Bước 2: Cấu hình biến môi trường AI (Tùy chọn)
Tạo file .env tại thư mục gốc với nội dung:
GEMINI_API_KEY=your_gemini_api_key_here

Bước 3: Khởi chạy Web Server cục bộ
Sử dụng công cụ Vercel CLI (khuyên dùng để chạy đồng thời cả API Serverless và giao diện):
npx vercel dev

Hoặc chạy nhanh máy chủ tĩnh bằng Python:
python -m http.server 4000 --directory public

Bước 4: Truy cập vào địa chỉ http://localhost:4000 trên trình duyệt để trải nghiệm toàn bộ tính năng.

---

## 7. TRIỂN KHAI LÊN NỀN TẢNG VERCEL

Dự án đã được cấu hình tối ưu để triển khai chỉ với một bước lên Vercel:
1. Đẩy mã nguồn lên kho lưu trữ GitHub cá nhân.
2. Đăng nhập Vercel, chọn Import Git Repository và trỏ đến repo English_Practice_System.
3. Trong mục Environment Variables, thêm biến:
   - GEMINI_API_KEY: Khóa API lấy từ Google AI Studio.
4. Nhấn Deploy. Hệ thống sẽ tự động build và xuất bản trang web trong vài giây.

---

## 8. TÁC GIẢ & THÔNG TIN LIÊN HỆ

- Tác giả: Bùi Xuân Sơn (s0nbuildtech287)
- GitHub: https://github.com/s0nbuildtech287
- Nền tảng: Practice English - CEFR Academic Language Platform
- Bản quyền: Giấy phép mã nguồn mở MIT License

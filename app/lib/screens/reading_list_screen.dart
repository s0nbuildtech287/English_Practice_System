import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../models/article.dart';
import '../services/api_service.dart';
import 'article_detail_screen.dart';

class ReadingListScreen extends StatefulWidget {
  const ReadingListScreen({super.key});

  @override
  State<ReadingListScreen> createState() => _ReadingListScreenState();
}

class _ReadingListScreenState extends State<ReadingListScreen> {
  List<Article> _bookmarkedArticles = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _fetchBookmarked();
  }

  void _fetchBookmarked() async {
    setState(() => _isLoading = true);
    try {
      final res = await ApiService.getBookmarkedArticles();
      if (mounted) {
        setState(() {
          _bookmarkedArticles = res;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  void _toggleBookmark(Article article) async {
    try {
      await ApiService.toggleBookmark(article.id);
      _fetchBookmarked();
    } catch (_) {}
  }

  void _showAddCustomArticleDialog() {
    final titleController = TextEditingController();
    final contentController = TextEditingController();
    String selectedLevel = 'B1';
    String selectedTopic = 'Custom';

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: const Color(0xFF0F1523),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
      ),
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setModalState) => Padding(
          padding: EdgeInsets.only(
            left: 24,
            right: 24,
            top: 24,
            bottom: MediaQuery.of(ctx).viewInsets.bottom + 24,
          ),
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Center(
                  child: Container(
                    width: 40,
                    height: 4,
                    decoration: BoxDecoration(
                      color: const Color(0xFF334155),
                      borderRadius: BorderRadius.circular(2),
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                Text(
                  'Thêm bài đọc của riêng bạn',
                  style: GoogleFonts.outfit(fontSize: 22, fontWeight: FontWeight.w800, color: Colors.white),
                ),
                const SizedBox(height: 6),
                Text(
                  'Dán bài báo hoặc đoạn văn tiếng Anh để luyện đọc & tra từ tương tác',
                  style: GoogleFonts.inter(fontSize: 13, color: const Color(0xFF94A3B8)),
                ),
                const SizedBox(height: 20),

                // Title Input
                TextField(
                  controller: titleController,
                  style: const TextStyle(color: Colors.white),
                  decoration: InputDecoration(
                    labelText: 'Tiêu đề bài đọc',
                    labelStyle: const TextStyle(color: Color(0xFF94A3B8)),
                    filled: true,
                    fillColor: const Color(0xFF131A2A),
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(14), borderSide: BorderSide.none),
                  ),
                ),
                const SizedBox(height: 14),

                // Level & Topic Selector
                Row(
                  children: [
                    Expanded(
                      child: DropdownButtonFormField<String>(
                        value: selectedLevel,
                        dropdownColor: const Color(0xFF1E293B),
                        style: const TextStyle(color: Colors.white),
                        decoration: InputDecoration(
                          labelText: 'Trình độ',
                          labelStyle: const TextStyle(color: Color(0xFF94A3B8)),
                          filled: true,
                          fillColor: const Color(0xFF131A2A),
                          border: OutlineInputBorder(borderRadius: BorderRadius.circular(14), borderSide: BorderSide.none),
                        ),
                        items: [
                          {'code': 'A1', 'name': 'A1 • Căn bản (Beginner)'},
                          {'code': 'A2', 'name': 'A2 • Sơ cấp (Elementary)'},
                          {'code': 'B1', 'name': 'B1 • Trung cấp (Intermediate)'},
                          {'code': 'B2', 'name': 'B2 • Trung cao (Upper-Inter)'},
                          {'code': 'C1', 'name': 'C1 • Cao cấp (Advanced)'},
                          {'code': 'C2', 'name': 'C2 • Thành thạo (Proficiency)'},
                        ].map((l) => DropdownMenuItem(value: l['code'], child: Text(l['name']!))).toList(),
                        onChanged: (val) => setModalState(() => selectedLevel = val ?? 'B1'),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: DropdownButtonFormField<String>(
                        value: selectedTopic,
                        dropdownColor: const Color(0xFF1E293B),
                        style: const TextStyle(color: Colors.white),
                        decoration: InputDecoration(
                          labelText: 'Chủ đề',
                          labelStyle: const TextStyle(color: Color(0xFF94A3B8)),
                          filled: true,
                          fillColor: const Color(0xFF131A2A),
                          border: OutlineInputBorder(borderRadius: BorderRadius.circular(14), borderSide: BorderSide.none),
                        ),
                        items: ['Custom', 'Technology', 'Daily Life', 'Business'].map((t) => DropdownMenuItem(value: t, child: Text(t))).toList(),
                        onChanged: (val) => setModalState(() => selectedTopic = val ?? 'Custom'),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 14),

                // Content Input
                TextField(
                  controller: contentController,
                  maxLines: 6,
                  style: const TextStyle(color: Colors.white),
                  decoration: InputDecoration(
                    hintText: 'Dán đoạn văn bản tiếng Anh vào đây...',
                    hintStyle: const TextStyle(color: Color(0xFF64748B)),
                    filled: true,
                    fillColor: const Color(0xFF131A2A),
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(14), borderSide: BorderSide.none),
                  ),
                ),
                const SizedBox(height: 20),

                // Submit Button
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF38BDF8),
                      foregroundColor: const Color(0xFF080B11),
                      padding: const EdgeInsets.symmetric(vertical: 16),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                    ),
                    onPressed: () async {
                      if (titleController.text.trim().isEmpty || contentController.text.trim().isEmpty) return;
                      try {
                        await ApiService.createCustomArticle(
                          title: titleController.text.trim(),
                          content: contentController.text.trim(),
                          topic: selectedTopic,
                          level: selectedLevel,
                        );
                        Navigator.pop(ctx);
                        _fetchBookmarked();
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            backgroundColor: Color(0xFF10B981),
                            content: Text('Đã tạo bài đọc mới thành công!'),
                          ),
                        );
                      } catch (_) {}
                    },
                    child: Text('Tạo Bài Đọc Ngay', style: GoogleFonts.outfit(fontWeight: FontWeight.w800, fontSize: 15)),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF080B11),
      appBar: AppBar(
        backgroundColor: const Color(0xFF080B11),
        elevation: 0,
        title: Text(
          'Bài Cần Đọc (Watchlist)',
          style: GoogleFonts.outfit(fontWeight: FontWeight.w800, color: Colors.white, fontSize: 22),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded, color: Color(0xFF38BDF8)),
            onPressed: _fetchBookmarked,
          )
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        backgroundColor: const Color(0xFF38BDF8),
        foregroundColor: const Color(0xFF080B11),
        icon: const Icon(Icons.add_rounded),
        label: Text('Thêm bài đọc', style: GoogleFonts.outfit(fontWeight: FontWeight.w800)),
        onPressed: _showAddCustomArticleDialog,
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator(color: Color(0xFF38BDF8)))
          : _bookmarkedArticles.isEmpty
              ? Center(
                  child: Padding(
                    padding: const EdgeInsets.all(32),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Container(
                          width: 80,
                          height: 80,
                          decoration: BoxDecoration(
                            color: const Color(0xFF131A2A),
                            shape: BoxShape.circle,
                            border: Border.all(color: const Color(0xFF1E293B)),
                          ),
                          child: const Icon(Icons.bookmark_border_rounded, size: 40, color: Color(0xFF38BDF8)),
                        ),
                        const SizedBox(height: 20),
                        Text(
                          'Chưa có bài đọc nào được lưu!',
                          style: GoogleFonts.outfit(color: Colors.white, fontSize: 20, fontWeight: FontWeight.bold),
                          textAlign: TextAlign.center,
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'Bấm vào biểu tượng bookmark ở danh sách bài đọc hoặc tự dán bài đọc của bạn qua nút "+ Thêm bài đọc" bên dưới nhé.',
                          style: GoogleFonts.inter(color: const Color(0xFF94A3B8), fontSize: 14, height: 1.5),
                          textAlign: TextAlign.center,
                        ),
                      ],
                    ),
                  ),
                )
              : ListView.builder(
                  padding: const EdgeInsets.fromLTRB(20, 10, 20, 90),
                  itemCount: _bookmarkedArticles.length,
                  itemBuilder: (ctx, i) {
                    final article = _bookmarkedArticles[i];
                    return _buildArticleItem(article);
                  },
                ),
    );
  }

  Widget _buildArticleItem(Article article) {
    return Container(
      margin: const EdgeInsets.only(bottom: 14),
      decoration: BoxDecoration(
        color: const Color(0xFF0F1523),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFF1E293B), width: 1.2),
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          borderRadius: BorderRadius.circular(20),
          onTap: () {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (ctx) => ArticleDetailScreen(article: article),
              ),
            );
          },
          child: Padding(
            padding: const EdgeInsets.all(18),
            child: Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                            decoration: BoxDecoration(
                              color: const Color(0xFF1A2333),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: Text(
                              article.topic,
                              style: GoogleFonts.inter(
                                fontSize: 11,
                                color: const Color(0xFF38BDF8),
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                            decoration: BoxDecoration(
                              color: const Color(0xFF38BDF8).withValues(alpha: 0.15),
                              borderRadius: BorderRadius.circular(6),
                              border: Border.all(color: const Color(0xFF38BDF8).withValues(alpha: 0.4)),
                            ),
                            child: Text(
                              'CEFR ${article.level}',
                              style: GoogleFonts.inter(fontSize: 10, color: const Color(0xFF38BDF8), fontWeight: FontWeight.bold),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      Text(
                        article.title,
                        style: GoogleFonts.outfit(
                          fontSize: 16,
                          fontWeight: FontWeight.w700,
                          color: Colors.white,
                          height: 1.3,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        '${article.durationSeconds ~/ 60}m audio • ${article.sourceName}',
                        style: GoogleFonts.inter(fontSize: 12, color: const Color(0xFF64748B)),
                      ),
                    ],
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.bookmark_remove_rounded, color: Color(0xFFEF4444)),
                  onPressed: () => _toggleBookmark(article),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

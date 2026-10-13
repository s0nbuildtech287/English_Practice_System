import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../models/article.dart';
import '../services/api_service.dart';
import 'article_detail_screen.dart';
import 'reading_list_screen.dart';
import 'vocab_explorer_screen.dart';
import 'flashcards_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _currentTab = 0;
  List<Article> _articles = [];
  bool _isLoading = true;
  String _selectedLevel = 'ALL';
  String _selectedTopic = 'ALL';

  final List<Map<String, dynamic>> _cefrLevels = [
    {'code': 'ALL', 'name': 'Tất cả', 'color': Color(0xFF38BDF8), 'sub': 'Mọi cấp độ'},
    {'code': 'A1', 'name': 'A1 • Căn bản', 'color': Color(0xFF10B981), 'sub': 'Beginner'},
    {'code': 'A2', 'name': 'A2 • Sơ cấp', 'color': Color(0xFF06B6D4), 'sub': 'Elementary'},
    {'code': 'B1', 'name': 'B1 • Trung cấp', 'color': Color(0xFF38BDF8), 'sub': 'Intermediate'},
    {'code': 'B2', 'name': 'B2 • Trung cao', 'color': Color(0xFF818CF8), 'sub': 'Upper-Inter'},
    {'code': 'C1', 'name': 'C1 • Cao cấp', 'color': Color(0xFFF59E0B), 'sub': 'Advanced'},
    {'code': 'C2', 'name': 'C2 • Thành thạo', 'color': Color(0xFFF43F5E), 'sub': 'Proficiency'},
  ];

  final Map<String, String> _cefrDescriptions = {
    'A1': 'A1 Căn bản (Beginner): Mẫu câu đơn giản, nói về bản thân, thói quen sinh hoạt thường nhật.',
    'A2': 'A2 Sơ cấp (Elementary): Nắm bắt thông tin ngắn gọn, miêu tả thói quen và sự kiện gần gũi.',
    'B1': 'B1 Trung cấp (Intermediate): Giao tiếp chủ động, hiểu bài viết về công nghệ, đời sống thường nhật.',
    'B2': 'B2 Trung cao cấp (Upper-Intermediate): Hiểu các bài báo chuyên sâu, văn bản phức tạp và khoa học.',
    'C1': 'C1 Cao cấp (Advanced): Đọc hiểu tài liệu học thuật, diễn đạt tự nhiên và phân tích logic cao.',
    'C2': 'C2 Thành thạo (Proficiency): Bậc cao nhất, làm chủ mọi sắc thái ngữ nghĩa, triết học và văn học.',
  };

  final List<Map<String, dynamic>> _topics = [
    {'id': 'ALL', 'name': 'Tất cả', 'icon': Icons.auto_awesome},
    {'id': 'Daily Life', 'name': 'Đời sống', 'icon': Icons.wb_sunny_rounded},
    {'id': 'Health', 'name': 'Sức khỏe', 'icon': Icons.favorite_rounded},
    {'id': 'Technology', 'name': 'Công nghệ & AI', 'icon': Icons.biotech_rounded},
    {'id': 'Environment', 'name': 'Môi trường', 'icon': Icons.eco_rounded},
    {'id': 'Psychology', 'name': 'Tâm lý học', 'icon': Icons.psychology_rounded},
    {'id': 'Philosophy & AI', 'name': 'Triết học', 'icon': Icons.auto_stories_rounded},
  ];

  @override
  void initState() {
    super.initState();
    _fetchArticles();
  }

  void _fetchArticles() async {
    setState(() => _isLoading = true);
    try {
      final levelParam = _selectedLevel == 'ALL' ? null : _selectedLevel;
      final topicParam = _selectedTopic == 'ALL' ? null : _selectedTopic;
      final res = await ApiService.getArticles(level: levelParam, topic: topicParam);
      if (mounted) {
        setState(() {
          _articles = res;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  void _toggleBookmark(Article article) async {
    try {
      final updated = await ApiService.toggleBookmark(article.id);
      setState(() {
        article.isBookmarked = updated.isBookmarked;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          backgroundColor: updated.isBookmarked ? const Color(0xFF10B981) : const Color(0xFF64748B),
          content: Text(updated.isBookmarked ? 'Đã thêm vào "Bài cần đọc"!' : 'Đã bỏ lưu bài đọc!'),
          duration: const Duration(seconds: 1),
        ),
      );
    } catch (_) {}
  }

  Widget _getCurrentBody() {
    switch (_currentTab) {
      case 0:
        return _buildArticlesTab();
      case 1:
        return const ReadingListScreen();
      case 2:
        return const VocabExplorerScreen();
      case 3:
        return const FlashcardsScreen();
      default:
        return _buildArticlesTab();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF080B11),
      body: _getCurrentBody(),
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          color: const Color(0xFF0F1523).withValues(alpha: 0.98),
          border: const Border(top: BorderSide(color: Color(0xFF1E293B), width: 1.2)),
        ),
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _buildNavItem(0, Icons.headphones_rounded, 'Nghe & Đọc'),
                _buildNavItem(1, Icons.bookmark_rounded, 'Bài cần đọc'),
                _buildNavItem(2, Icons.search_rounded, 'Từ mới'),
                _buildNavItem(3, Icons.style_rounded, 'Sổ tay từ'),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildNavItem(int index, IconData icon, String label) {
    final isSelected = _currentTab == index;
    return InkWell(
      onTap: () => setState(() => _currentTab = index),
      borderRadius: BorderRadius.circular(16),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
        decoration: BoxDecoration(
          gradient: isSelected
              ? const LinearGradient(
                  colors: [Color(0xFF38BDF8), Color(0xFF6366F1)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                )
              : null,
          color: isSelected ? null : Colors.transparent,
          borderRadius: BorderRadius.circular(16),
          boxShadow: isSelected
              ? [
                  BoxShadow(
                    color: const Color(0xFF38BDF8).withValues(alpha: 0.35),
                    blurRadius: 10,
                    offset: const Offset(0, 4),
                  )
                ]
              : null,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              color: isSelected ? Colors.white : const Color(0xFF64748B),
              size: 20,
            ),
            const SizedBox(height: 3),
            Text(
              label,
              style: GoogleFonts.outfit(
                fontSize: 11,
                fontWeight: isSelected ? FontWeight.w800 : FontWeight.w500,
                color: isSelected ? Colors.white : const Color(0xFF94A3B8),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildArticlesTab() {
    return SafeArea(
      child: CustomScrollView(
        slivers: [
          // Header & Daily Streak Status
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 20, 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(
                        children: [
                          Container(
                            width: 42,
                            height: 42,
                            decoration: BoxDecoration(
                              gradient: const LinearGradient(
                                colors: [Color(0xFF38BDF8), Color(0xFF6366F1)],
                                begin: Alignment.topLeft,
                                end: Alignment.bottomRight,
                              ),
                              borderRadius: BorderRadius.circular(14),
                              boxShadow: [
                                BoxShadow(
                                  color: const Color(0xFF38BDF8).withValues(alpha: 0.3),
                                  blurRadius: 10,
                                  offset: const Offset(0, 4),
                                )
                              ],
                            ),
                            child: const Icon(Icons.headphones_rounded, color: Colors.white, size: 22),
                          ),
                          const SizedBox(width: 12),
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Smart English',
                                style: GoogleFonts.outfit(
                                  fontSize: 22,
                                  fontWeight: FontWeight.w800,
                                  color: Colors.white,
                                  letterSpacing: -0.5,
                                ),
                              ),
                              Text(
                                'Interactive Read & Listen',
                                style: GoogleFonts.inter(
                                  fontSize: 12,
                                  fontWeight: FontWeight.w500,
                                  color: const Color(0xFF38BDF8),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                        decoration: BoxDecoration(
                          color: const Color(0xFFF59E0B).withValues(alpha: 0.15),
                          borderRadius: BorderRadius.circular(20),
                          border: Border.all(color: const Color(0xFFF59E0B).withValues(alpha: 0.4)),
                        ),
                        child: Row(
                          children: [
                            const Text('🔥', style: TextStyle(fontSize: 14)),
                            const SizedBox(width: 4),
                            Text(
                              '3 Ngày chuỗi',
                              style: GoogleFonts.outfit(
                                fontSize: 13,
                                fontWeight: FontWeight.w700,
                                color: const Color(0xFFFBBF24),
                              ),
                            ),
                          ],
                        ),
                      )
                    ],
                  ),
                  const SizedBox(height: 18),
                  _buildHeroFeaturedCard(),
                ],
              ),
            ),
          ),

          // Filters Section (Level + Topics)
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        'Cấp độ CEFR',
                        style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w700, color: Colors.white),
                      ),
                      Text(
                        '${_articles.length} bài học',
                        style: GoogleFonts.inter(fontSize: 12, color: const Color(0xFF64748B)),
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    child: Row(
                      children: _cefrLevels.map((lvl) {
                        final code = lvl['code'] as String;
                        final isSel = _selectedLevel == code;
                        final color = lvl['color'] as Color;
                        return Padding(
                          padding: const EdgeInsets.only(right: 8),
                          child: InkWell(
                            onTap: () {
                              setState(() => _selectedLevel = code);
                              _fetchArticles();
                            },
                            borderRadius: BorderRadius.circular(14),
                            child: AnimatedContainer(
                              duration: const Duration(milliseconds: 200),
                              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                              decoration: BoxDecoration(
                                color: isSel ? color.withValues(alpha: 0.2) : const Color(0xFF131A2A),
                                borderRadius: BorderRadius.circular(14),
                                border: Border.all(
                                  color: isSel ? color : const Color(0xFF1E293B),
                                  width: isSel ? 1.5 : 1,
                                ),
                              ),
                              child: Row(
                                children: [
                                  if (code != 'ALL') ...[
                                    Container(
                                      width: 8,
                                      height: 8,
                                      decoration: BoxDecoration(color: color, shape: BoxShape.circle),
                                    ),
                                    const SizedBox(width: 6),
                                  ],
                                  Text(
                                    lvl['name'] as String,
                                    style: GoogleFonts.inter(
                                      fontSize: 13,
                                      fontWeight: isSel ? FontWeight.w800 : FontWeight.w500,
                                      color: isSel ? Colors.white : const Color(0xFF94A3B8),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                        );
                      }).toList(),
                    ),
                  ),

                  // CEFR level description guide banner
                  if (_selectedLevel != 'ALL' && _cefrDescriptions.containsKey(_selectedLevel)) ...[
                    const SizedBox(height: 10),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                      decoration: BoxDecoration(
                        color: _getLevelColor(_selectedLevel).withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: _getLevelColor(_selectedLevel).withValues(alpha: 0.3)),
                      ),
                      child: Row(
                        children: [
                          Icon(Icons.info_outline_rounded, size: 16, color: _getLevelColor(_selectedLevel)),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              _cefrDescriptions[_selectedLevel]!,
                              style: GoogleFonts.inter(
                                fontSize: 12,
                                color: const Color(0xFFE2E8F0),
                                height: 1.35,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                  const SizedBox(height: 14),

                  // Topics Scroll
                  SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    child: Row(
                      children: _topics.map((t) {
                        final isSel = _selectedTopic == t['id'];
                        return Padding(
                          padding: const EdgeInsets.only(right: 8),
                          child: InkWell(
                            onTap: () {
                              setState(() => _selectedTopic = t['id']);
                              _fetchArticles();
                            },
                            borderRadius: BorderRadius.circular(12),
                            child: Container(
                              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                              decoration: BoxDecoration(
                                color: isSel ? const Color(0xFF6366F1).withValues(alpha: 0.2) : Colors.transparent,
                                borderRadius: BorderRadius.circular(12),
                                border: Border.all(
                                  color: isSel ? const Color(0xFF6366F1) : const Color(0xFF1E293B),
                                ),
                              ),
                              child: Row(
                                children: [
                                  Icon(
                                    t['icon'] as IconData,
                                    size: 14,
                                    color: isSel ? const Color(0xFF818CF8) : const Color(0xFF64748B),
                                  ),
                                  const SizedBox(width: 6),
                                  Text(
                                    t['name'] as String,
                                    style: GoogleFonts.inter(
                                      fontSize: 12,
                                      fontWeight: isSel ? FontWeight.w600 : FontWeight.w400,
                                      color: isSel ? const Color(0xFFC7D2FE) : const Color(0xFF94A3B8),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                        );
                      }).toList(),
                    ),
                  ),
                  const SizedBox(height: 16),
                ],
              ),
            ),
          ),

          // Article List
          _isLoading
              ? const SliverFillRemaining(
                  child: Center(
                    child: CircularProgressIndicator(color: Color(0xFF38BDF8)),
                  ),
                )
              : SliverPadding(
                  padding: const EdgeInsets.fromLTRB(20, 0, 20, 30),
                  sliver: SliverList(
                    delegate: SliverChildBuilderDelegate(
                      (ctx, i) => _buildArticleCard(_articles[i], i),
                      childCount: _articles.length,
                    ),
                  ),
                ),
        ],
      ),
    );
  }

  Widget _buildHeroFeaturedCard() {
    if (_articles.isEmpty) return const SizedBox.shrink();
    final featured = _articles.first;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF0C1929)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFF6366F1).withValues(alpha: 0.4), width: 1.5),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF6366F1).withValues(alpha: 0.2),
            blurRadius: 20,
            offset: const Offset(0, 8),
          )
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: const Color(0xFF6366F1).withValues(alpha: 0.3),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  'BÀI ĐỌC TÂM ĐIỂM',
                  style: GoogleFonts.outfit(
                    fontSize: 11,
                    fontWeight: FontWeight.w800,
                    color: const Color(0xFFC7D2FE),
                    letterSpacing: 0.5,
                  ),
                ),
              ),
              Row(
                children: [
                  IconButton(
                    icon: Icon(
                      featured.isBookmarked ? Icons.bookmark_rounded : Icons.bookmark_border_rounded,
                      color: featured.isBookmarked ? const Color(0xFF38BDF8) : const Color(0xFF64748B),
                    ),
                    onPressed: () => _toggleBookmark(featured),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: _getLevelColor(featured.level).withValues(alpha: 0.2),
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(color: _getLevelColor(featured.level), width: 1.2),
                    ),
                    child: Text(
                      _getLevelFullName(featured.level),
                      style: GoogleFonts.inter(
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                        color: _getLevelColor(featured.level),
                      ),
                    ),
                  ),
                ],
              )
            ],
          ),
          const SizedBox(height: 12),
          Text(
            featured.title,
            style: GoogleFonts.outfit(
              fontSize: 20,
              fontWeight: FontWeight.w800,
              color: Colors.white,
              height: 1.3,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            featured.content.substring(0, featured.content.length > 110 ? 110 : featured.content.length) + '...',
            style: GoogleFonts.inter(fontSize: 13, color: const Color(0xFF94A3B8), height: 1.5),
          ),
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  const Icon(Icons.graphic_eq_rounded, size: 16, color: Color(0xFF38BDF8)),
                  const SizedBox(width: 6),
                  Text(
                    '${featured.durationSeconds ~/ 60} phút audio • ${featured.sourceName}',
                    style: GoogleFonts.inter(fontSize: 12, color: const Color(0xFF94A3B8)),
                  ),
                ],
              ),
              ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF38BDF8),
                  foregroundColor: const Color(0xFF080B11),
                  elevation: 0,
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                ),
                onPressed: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (ctx) => ArticleDetailScreen(article: featured),
                    ),
                  );
                },
                icon: const Icon(Icons.play_arrow_rounded, size: 18),
                label: Text(
                  'Luyện ngay',
                  style: GoogleFonts.outfit(fontWeight: FontWeight.w800, fontSize: 13),
                ),
              ),
            ],
          )
        ],
      ),
    );
  }

  Color _getLevelColor(String level) {
    switch (level.toUpperCase()) {
      case 'A1':
        return const Color(0xFF10B981);
      case 'A2':
        return const Color(0xFF06B6D4);
      case 'B1':
        return const Color(0xFF38BDF8);
      case 'B2':
        return const Color(0xFF818CF8);
      case 'C1':
        return const Color(0xFFF59E0B);
      case 'C2':
        return const Color(0xFFF43F5E);
      default:
        return const Color(0xFF64748B);
    }
  }

  String _getLevelFullName(String level) {
    switch (level.toUpperCase()) {
      case 'A1':
        return 'A1 • Căn bản';
      case 'A2':
        return 'A2 • Sơ cấp';
      case 'B1':
        return 'B1 • Trung cấp';
      case 'B2':
        return 'B2 • Trung cao';
      case 'C1':
        return 'C1 • Cao cấp';
      case 'C2':
        return 'C2 • Thành thạo';
      default:
        return 'Level $level';
    }
  }

  Widget _buildArticleCard(Article article, int index) {
    final levelBg = _getLevelColor(article.level);
    final levelText = _getLevelFullName(article.level);

    return Container(
      margin: const EdgeInsets.only(bottom: 14),
      decoration: BoxDecoration(
        color: const Color(0xFF0F1523),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFF1E293B), width: 1.2),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.3),
            blurRadius: 10,
            offset: const Offset(0, 4),
          )
        ],
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
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: const Color(0xFF1A2333),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        article.topic,
                        style: GoogleFonts.inter(
                          fontSize: 12,
                          color: const Color(0xFF38BDF8),
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                    Row(
                      children: [
                        IconButton(
                          padding: EdgeInsets.zero,
                          constraints: const BoxConstraints(),
                          icon: Icon(
                            article.isBookmarked ? Icons.bookmark_rounded : Icons.bookmark_border_rounded,
                            color: article.isBookmarked ? const Color(0xFF38BDF8) : const Color(0xFF64748B),
                            size: 20,
                          ),
                          onPressed: () => _toggleBookmark(article),
                        ),
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                          decoration: BoxDecoration(
                            color: levelBg.withValues(alpha: 0.15),
                            borderRadius: BorderRadius.circular(8),
                            border: Border.all(color: levelBg.withValues(alpha: 0.5)),
                          ),
                          child: Text(
                            levelText,
                            style: GoogleFonts.inter(
                              fontSize: 11,
                              fontWeight: FontWeight.bold,
                              color: levelBg,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Text(
                  article.title,
                  style: GoogleFonts.outfit(
                    fontSize: 17,
                    fontWeight: FontWeight.w700,
                    color: Colors.white,
                    height: 1.35,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  article.content.substring(0, article.content.length > 90 ? 90 : article.content.length) + '...',
                  style: GoogleFonts.inter(
                    fontSize: 13,
                    color: const Color(0xFF94A3B8),
                    height: 1.45,
                  ),
                ),
                const SizedBox(height: 14),
                Row(
                  children: [
                    const Icon(Icons.access_time_rounded, size: 14, color: Color(0xFF64748B)),
                    const SizedBox(width: 4),
                    Text(
                      '${article.durationSeconds ~/ 60}m audio',
                      style: GoogleFonts.inter(fontSize: 12, color: const Color(0xFF64748B)),
                    ),
                    const SizedBox(width: 12),
                    const Icon(Icons.source_outlined, size: 14, color: Color(0xFF64748B)),
                    const SizedBox(width: 4),
                    Text(
                      article.sourceName,
                      style: GoogleFonts.inter(fontSize: 12, color: const Color(0xFF64748B)),
                    ),
                    const Spacer(),
                    Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: const Color(0xFF38BDF8).withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: const Icon(Icons.arrow_forward_rounded, size: 16, color: Color(0xFF38BDF8)),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

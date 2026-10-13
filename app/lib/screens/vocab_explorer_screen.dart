import 'package:flutter/material.dart';
import 'package:audioplayers/audioplayers.dart';
import 'package:google_fonts/google_fonts.dart';
import '../models/vocabulary.dart';
import '../services/api_service.dart';

class VocabExplorerScreen extends StatefulWidget {
  const VocabExplorerScreen({super.key});

  @override
  State<VocabExplorerScreen> createState() => _VocabExplorerScreenState();
}

class _VocabExplorerScreenState extends State<VocabExplorerScreen> {
  final TextEditingController _searchController = TextEditingController();
  late AudioPlayer _audioPlayer;

  Vocabulary? _searchResult;
  bool _isSearching = false;
  bool _isSaved = false;

  String _selectedCefr = 'ALL';

  final List<Map<String, dynamic>> _curatedCategories = [
    {
      'code': 'A1',
      'title': 'A1 • Căn bản (Beginner)',
      'desc': 'Từ vựng cốt lõi thường gặp nhất trong sinh hoạt và đời sống',
      'color': Color(0xFF10B981),
      'words': ['hello', 'family', 'friend', 'water', 'school', 'travel', 'happy', 'listen'],
    },
    {
      'code': 'A2',
      'title': 'A2 • Sơ cấp (Elementary)',
      'desc': 'Mô tả thói quen sinh hoạt, thời tiết, lịch trình và hoạt động gần gũi',
      'color': Color(0xFF06B6D4),
      'words': ['schedule', 'weather', 'describe', 'invite', 'convenient', 'routine', 'advice', 'journey'],
    },
    {
      'code': 'B1',
      'title': 'B1 • Trung cấp (Intermediate)',
      'desc': 'Giao tiếp công việc tự tin, chia sẻ trải nghiệm và bày tỏ cảm xúc',
      'color': Color(0xFF38BDF8),
      'words': ['appreciate', 'enthusiastic', 'hesitate', 'recommend', 'experience', 'confidence', 'opportunity', 'challenge'],
    },
    {
      'code': 'B2',
      'title': 'B2 • Trung cao cấp (Upper-Intermediate)',
      'desc': 'Thảo luận chuyên ngành, phân tích chiến lược và đàm phán sự nghiệp',
      'color': Color(0xFF818CF8),
      'words': ['collaborate', 'resilient', 'perspective', 'negotiate', 'efficient', 'strategy', 'innovation', 'crucial'],
    },
    {
      'code': 'C1',
      'title': 'C1 • Cao cấp (Advanced)',
      'desc': 'Văn bản học thuật, luận điểm khoa học và phân tích trừu tượng',
      'color': Color(0xFFF59E0B),
      'words': ['breakthrough', 'revolutionize', 'hypothesis', 'comprehensive', 'profound', 'intricate', 'diminish', 'consequence'],
    },
    {
      'code': 'C2',
      'title': 'C2 • Thành thạo (Proficiency)',
      'desc': 'Đỉnh cao ngôn ngữ, làm chủ hoàn toàn các sắc thái văn học và triết lý',
      'color': Color(0xFFF43F5E),
      'words': ['ubiquitous', 'ephemeral', 'eloquent', 'serendipity', 'pragmatic', 'paradigm', 'ineffable', 'quintessential'],
    },
  ];

  @override
  void initState() {
    super.initState();
    _audioPlayer = AudioPlayer();
  }

  @override
  void dispose() {
    _searchController.dispose();
    _audioPlayer.dispose();
    super.dispose();
  }

  void _searchWord(String query) async {
    final clean = query.trim();
    if (clean.isEmpty) return;

    setState(() {
      _isSearching = true;
      _isSaved = false;
    });

    try {
      final res = await ApiService.lookupWord(clean);
      if (mounted) {
        setState(() {
          _searchResult = res;
          _isSearching = false;
        });
        if (res.audioUrl != null && res.audioUrl!.isNotEmpty) {
          _audioPlayer.play(UrlSource(res.audioUrl!));
        }
      }
    } catch (e) {
      if (mounted) setState(() => _isSearching = false);
    }
  }

  void _saveCurrentWord() async {
    if (_searchResult == null) return;
    try {
      await ApiService.saveWord(
        word: _searchResult!.word,
        ipa: _searchResult!.ipa,
        meaningEn: _searchResult!.meaningEn,
        audioUrl: _searchResult!.audioUrl,
        contextSentence: _searchResult!.exampleSentence ?? 'Từ vựng tra cứu từ Từ điển',
      );
      setState(() => _isSaved = true);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          backgroundColor: const Color(0xFF10B981),
          content: Text('Đã lưu "${_searchResult!.word}" vào Sổ tay Flashcards!'),
        ),
      );
    } catch (_) {}
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF080B11),
      appBar: AppBar(
        backgroundColor: const Color(0xFF080B11),
        elevation: 0,
        title: Text(
          'Từ Mới & Tra Cứu (Dictionary)',
          style: GoogleFonts.outfit(fontWeight: FontWeight.w800, color: Colors.white, fontSize: 22),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(20, 10, 20, 30),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Search Input Field
            Container(
              decoration: BoxDecoration(
                color: const Color(0xFF0F1523),
                borderRadius: BorderRadius.circular(18),
                border: Border.all(color: const Color(0xFF38BDF8).withValues(alpha: 0.4), width: 1.2),
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFF38BDF8).withValues(alpha: 0.1),
                    blurRadius: 15,
                    offset: const Offset(0, 4),
                  )
                ],
              ),
              child: TextField(
                controller: _searchController,
                style: GoogleFonts.inter(color: Colors.white, fontSize: 16),
                textInputAction: TextInputAction.search,
                onSubmitted: _searchWord,
                decoration: InputDecoration(
                  hintText: 'Nhập từ tiếng Anh để tra nghĩa & phát âm...',
                  hintStyle: GoogleFonts.inter(color: const Color(0xFF64748B), fontSize: 14),
                  prefixIcon: const Icon(Icons.search_rounded, color: Color(0xFF38BDF8)),
                  suffixIcon: IconButton(
                    icon: const Icon(Icons.arrow_forward_rounded, color: Color(0xFF38BDF8)),
                    onPressed: () => _searchWord(_searchController.text),
                  ),
                  border: InputBorder.none,
                  contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                ),
              ),
            ),
            const SizedBox(height: 24),

            // Search Result Card (if any)
            if (_isSearching)
              const Center(
                child: Padding(
                  padding: EdgeInsets.symmetric(vertical: 40),
                  child: CircularProgressIndicator(color: Color(0xFF38BDF8)),
                ),
              )
            else if (_searchResult != null)
              _buildSearchResultCard()
            else ...[
              // Empty search guide banner
              Container(
                padding: const EdgeInsets.all(18),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [Color(0xFF131A2A), Color(0xFF0F1523)],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: const Color(0xFF1E293B)),
                ),
                child: Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: const Color(0xFF38BDF8).withValues(alpha: 0.15),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(Icons.lightbulb_outline_rounded, color: Color(0xFF38BDF8), size: 24),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Tra từ điển 1-chạm',
                            style: GoogleFonts.outfit(fontWeight: FontWeight.w700, color: Colors.white, fontSize: 15),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            'Gõ từ vựng bất kỳ hoặc chọn nhanh bộ từ vựng cốt lõi bên dưới để luyện phát âm.',
                            style: GoogleFonts.inter(fontSize: 12, color: const Color(0xFF94A3B8), height: 1.4),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ],

            const SizedBox(height: 28),

            // Curated Oxford Core Vocabulary
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Bộ Từ Vựng Theo Chuẩn CEFR',
                  style: GoogleFonts.outfit(fontSize: 18, fontWeight: FontWeight.w800, color: Colors.white),
                ),
                Text(
                  'A1 -> C2',
                  style: GoogleFonts.inter(fontSize: 12, fontWeight: FontWeight.bold, color: const Color(0xFF38BDF8)),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Text(
              'Chạm vào từ để xem phiên âm, định nghĩa và nghe phát âm chuẩn',
              style: GoogleFonts.inter(fontSize: 13, color: const Color(0xFF64748B)),
            ),
            const SizedBox(height: 14),

            // CEFR Filter Chips
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: [
                  {'code': 'ALL', 'name': 'Tất cả (A1-C2)', 'color': const Color(0xFF38BDF8)},
                  {'code': 'A1', 'name': 'A1 • Căn bản', 'color': const Color(0xFF10B981)},
                  {'code': 'A2', 'name': 'A2 • Sơ cấp', 'color': const Color(0xFF06B6D4)},
                  {'code': 'B1', 'name': 'B1 • Trung cấp', 'color': const Color(0xFF38BDF8)},
                  {'code': 'B2', 'name': 'B2 • Trung cao', 'color': const Color(0xFF818CF8)},
                  {'code': 'C1', 'name': 'C1 • Cao cấp', 'color': const Color(0xFFF59E0B)},
                  {'code': 'C2', 'name': 'C2 • Thành thạo', 'color': const Color(0xFFF43F5E)},
                ].map((item) {
                  final code = item['code'] as String;
                  final isSel = _selectedCefr == code;
                  final color = item['color'] as Color;
                  return Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: InkWell(
                      onTap: () => setState(() => _selectedCefr = code),
                      borderRadius: BorderRadius.circular(12),
                      child: AnimatedContainer(
                        duration: const Duration(milliseconds: 200),
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 7),
                        decoration: BoxDecoration(
                          color: isSel ? color.withValues(alpha: 0.2) : const Color(0xFF131A2A),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(
                            color: isSel ? color : const Color(0xFF1E293B),
                            width: isSel ? 1.5 : 1,
                          ),
                        ),
                        child: Text(
                          item['name'] as String,
                          style: GoogleFonts.inter(
                            fontSize: 12,
                            fontWeight: isSel ? FontWeight.w700 : FontWeight.w500,
                            color: isSel ? Colors.white : const Color(0xFF94A3B8),
                          ),
                        ),
                      ),
                    ),
                  );
                }).toList(),
              ),
            ),
            const SizedBox(height: 16),

            ...(_selectedCefr == 'ALL'
                    ? _curatedCategories
                    : _curatedCategories.where((c) => c['code'] == _selectedCefr))
                .map((cat) => _buildCategorySection(cat)),
          ],
        ),
      ),
    );
  }

  Widget _buildSearchResultCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFF131A2A), Color(0xFF0F1523)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFF38BDF8).withValues(alpha: 0.4), width: 1.5),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF38BDF8).withValues(alpha: 0.15),
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
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    _searchResult!.word,
                    style: GoogleFonts.outfit(fontSize: 32, fontWeight: FontWeight.w800, color: Colors.white),
                  ),
                  if (_searchResult!.ipa.isNotEmpty)
                    Text(
                      _searchResult!.ipa,
                      style: GoogleFonts.inter(fontSize: 18, color: const Color(0xFF38BDF8), fontStyle: FontStyle.italic),
                    ),
                ],
              ),
              Row(
                children: [
                  if (_searchResult!.audioUrl != null && _searchResult!.audioUrl!.isNotEmpty)
                    IconButton.filled(
                      style: IconButton.styleFrom(
                        backgroundColor: const Color(0xFF38BDF8).withValues(alpha: 0.2),
                        foregroundColor: const Color(0xFF38BDF8),
                      ),
                      icon: const Icon(Icons.volume_up_rounded, size: 26),
                      onPressed: () {
                        _audioPlayer.play(UrlSource(_searchResult!.audioUrl!));
                      },
                    ),
                  const SizedBox(width: 8),
                  ElevatedButton.icon(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: _isSaved ? const Color(0xFF10B981) : const Color(0xFF6366F1),
                      foregroundColor: Colors.white,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                    ),
                    onPressed: _isSaved ? null : _saveCurrentWord,
                    icon: Icon(_isSaved ? Icons.check_rounded : Icons.bookmark_add_rounded, size: 18),
                    label: Text(_isSaved ? 'Đã lưu' : 'Lưu Flashcard', style: GoogleFonts.outfit(fontWeight: FontWeight.bold)),
                  ),
                ],
              )
            ],
          ),
          if (_searchResult!.partOfSpeech.isNotEmpty) ...[
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 3),
              decoration: BoxDecoration(
                color: const Color(0xFF1E293B),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(
                _searchResult!.partOfSpeech.toUpperCase(),
                style: GoogleFonts.outfit(color: const Color(0xFF94A3B8), fontWeight: FontWeight.bold, fontSize: 11),
              ),
            ),
          ],
          const SizedBox(height: 14),
          Text(
            'ĐỊNH NGHĨA ANH - ANH',
            style: GoogleFonts.outfit(color: const Color(0xFF64748B), fontSize: 11, fontWeight: FontWeight.w800),
          ),
          const SizedBox(height: 6),
          Text(
            _searchResult!.meaningEn.isNotEmpty ? _searchResult!.meaningEn : 'Không tìm thấy định nghĩa.',
            style: GoogleFonts.inter(color: Colors.white, fontSize: 16, height: 1.45),
          ),
          if (_searchResult!.exampleSentence != null && _searchResult!.exampleSentence!.isNotEmpty) ...[
            const SizedBox(height: 14),
            Text(
              'CÂU VÍ DỤ',
              style: GoogleFonts.outfit(color: const Color(0xFF64748B), fontSize: 11, fontWeight: FontWeight.w800),
            ),
            const SizedBox(height: 6),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: const Color(0xFF080B11),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: const Color(0xFF1E293B)),
              ),
              child: Text(
                '"${_searchResult!.exampleSentence}"',
                style: GoogleFonts.inter(color: const Color(0xFF38BDF8), fontStyle: FontStyle.italic, fontSize: 14),
              ),
            ),
          ]
        ],
      ),
    );
  }

  Widget _buildCategorySection(Map<String, dynamic> cat) {
    final color = cat['color'] as Color;
    final words = cat['words'] as List<String>;
    final code = cat['code'] as String;
    final desc = cat['desc'] as String? ?? '';

    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: const Color(0xFF0F1523),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withValues(alpha: 0.25), width: 1.2),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: color.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: color.withValues(alpha: 0.6)),
                ),
                child: Text(
                  code,
                  style: GoogleFonts.outfit(fontSize: 12, fontWeight: FontWeight.w800, color: color),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Text(
                  cat['title'] as String,
                  style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w700, color: Colors.white),
                ),
              ),
            ],
          ),
          if (desc.isNotEmpty) ...[
            const SizedBox(height: 6),
            Text(
              desc,
              style: GoogleFonts.inter(fontSize: 12, color: const Color(0xFF94A3B8)),
            ),
          ],
          const SizedBox(height: 14),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: words.map((w) {
              return ActionChip(
                backgroundColor: const Color(0xFF131A2A),
                side: BorderSide(color: color.withValues(alpha: 0.3)),
                label: Text(
                  w,
                  style: GoogleFonts.inter(color: const Color(0xFFE2E8F0), fontWeight: FontWeight.w500, fontSize: 13),
                ),
                avatar: Icon(Icons.volume_up_outlined, size: 16, color: color),
                onPressed: () {
                  _searchController.text = w;
                  _searchWord(w);
                },
              );
            }).toList(),
          ),
        ],
      ),
    );
  }
}

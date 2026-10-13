import 'package:flutter/material.dart';
import 'package:audioplayers/audioplayers.dart';
import 'package:google_fonts/google_fonts.dart';
import '../models/vocabulary.dart';
import '../services/api_service.dart';

class FlashcardsScreen extends StatefulWidget {
  const FlashcardsScreen({super.key});

  @override
  State<FlashcardsScreen> createState() => _FlashcardsScreenState();
}

class _FlashcardsScreenState extends State<FlashcardsScreen> {
  List<SavedWord> _words = [];
  bool _isLoading = true;
  int _viewMode = 0; // 0: Word Bank List, 1: 3D Flashcard Mode
  int _currentIndex = 0;
  bool _showBack = false;
  late AudioPlayer _audioPlayer;

  @override
  void initState() {
    super.initState();
    _audioPlayer = AudioPlayer();
    _loadWords();
  }

  @override
  void dispose() {
    _audioPlayer.dispose();
    super.dispose();
  }

  void _loadWords() async {
    setState(() => _isLoading = true);
    try {
      final res = await ApiService.getSavedVocab();
      if (mounted) {
        setState(() {
          _words = res;
          _isLoading = false;
          _currentIndex = 0;
          _showBack = false;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  void _review(bool remembered) async {
    if (_words.isEmpty) return;
    final current = _words[_currentIndex];
    try {
      await ApiService.reviewCard(current.id, remembered);
    } catch (_) {}

    setState(() {
      _showBack = false;
      if (_currentIndex < _words.length - 1) {
        _currentIndex++;
      } else {
        _loadWords();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF080B11),
      appBar: AppBar(
        backgroundColor: const Color(0xFF080B11),
        elevation: 0,
        title: Text(
          'Sổ Tay Từ Vựng & Flashcards',
          style: GoogleFonts.outfit(fontWeight: FontWeight.w800, color: Colors.white, fontSize: 22),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded, color: Color(0xFF38BDF8)),
            onPressed: _loadWords,
          )
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator(color: Color(0xFF38BDF8)))
          : _words.isEmpty
              ? _buildEmptyState()
              : Column(
                  children: [
                    // View Mode Toggle (Word Bank vs Flashcard Review)
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                      child: Container(
                        padding: const EdgeInsets.all(4),
                        decoration: BoxDecoration(
                          color: const Color(0xFF0F1523),
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(color: const Color(0xFF1E293B)),
                        ),
                        child: Row(
                          children: [
                            Expanded(
                              child: InkWell(
                                onTap: () => setState(() => _viewMode = 0),
                                borderRadius: BorderRadius.circular(12),
                                child: Container(
                                  padding: const EdgeInsets.symmetric(vertical: 10),
                                  decoration: BoxDecoration(
                                    color: _viewMode == 0 ? const Color(0xFF38BDF8) : Colors.transparent,
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  alignment: Alignment.center,
                                  child: Row(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      Icon(
                                        Icons.format_list_bulleted_rounded,
                                        size: 18,
                                        color: _viewMode == 0 ? const Color(0xFF080B11) : const Color(0xFF94A3B8),
                                      ),
                                      const SizedBox(width: 8),
                                      Text(
                                        'Danh sách từ (${_words.length})',
                                        style: GoogleFonts.outfit(
                                          fontWeight: FontWeight.w700,
                                          fontSize: 13,
                                          color: _viewMode == 0 ? const Color(0xFF080B11) : const Color(0xFF94A3B8),
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ),
                            ),
                            Expanded(
                              child: InkWell(
                                onTap: () => setState(() => _viewMode = 1),
                                borderRadius: BorderRadius.circular(12),
                                child: Container(
                                  padding: const EdgeInsets.symmetric(vertical: 10),
                                  decoration: BoxDecoration(
                                    color: _viewMode == 1 ? const Color(0xFF6366F1) : Colors.transparent,
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  alignment: Alignment.center,
                                  child: Row(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      Icon(
                                        Icons.style_rounded,
                                        size: 18,
                                        color: _viewMode == 1 ? Colors.white : const Color(0xFF94A3B8),
                                      ),
                                      const SizedBox(width: 8),
                                      Text(
                                        'Luyện thẻ 3D (SRS)',
                                        style: GoogleFonts.outfit(
                                          fontWeight: FontWeight.w700,
                                          fontSize: 13,
                                          color: _viewMode == 1 ? Colors.white : const Color(0xFF94A3B8),
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),

                    // Content View
                    Expanded(
                      child: _viewMode == 0 ? _buildWordBankListView() : _buildFlashcardsView(),
                    ),
                  ],
                ),
    );
  }

  Widget _buildWordBankListView() {
    return ListView.builder(
      padding: const EdgeInsets.fromLTRB(20, 10, 20, 30),
      itemCount: _words.length,
      itemBuilder: (ctx, i) {
        final w = _words[i];
        return Container(
          margin: const EdgeInsets.only(bottom: 12),
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: const Color(0xFF0F1523),
            borderRadius: BorderRadius.circular(18),
            border: Border.all(color: const Color(0xFF1E293B)),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      Text(
                        w.word,
                        style: GoogleFonts.outfit(fontSize: 18, fontWeight: FontWeight.w800, color: Colors.white),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        w.ipa,
                        style: GoogleFonts.inter(fontSize: 14, color: const Color(0xFF38BDF8), fontStyle: FontStyle.italic),
                      ),
                    ],
                  ),
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                        decoration: BoxDecoration(
                          color: const Color(0xFF6366F1).withValues(alpha: 0.2),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Text(
                          'Cấp ${w.repetitionLevel}',
                          style: GoogleFonts.inter(fontSize: 11, fontWeight: FontWeight.bold, color: const Color(0xFFC7D2FE)),
                        ),
                      ),
                      if (w.audioUrl != null && w.audioUrl!.isNotEmpty) ...[
                        const SizedBox(width: 8),
                        IconButton(
                          icon: const Icon(Icons.volume_up_rounded, color: Color(0xFF38BDF8), size: 20),
                          onPressed: () {
                            _audioPlayer.play(UrlSource(w.audioUrl!));
                          },
                        )
                      ],
                    ],
                  )
                ],
              ),
              const SizedBox(height: 8),
              Text(
                w.meaningEn,
                style: GoogleFonts.inter(fontSize: 14, color: const Color(0xFFCBD5E1), height: 1.4),
              ),
              if (w.contextSentence != null && w.contextSentence!.isNotEmpty) ...[
                const SizedBox(height: 8),
                Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: const Color(0xFF080B11),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Text(
                    '"${w.contextSentence}"',
                    style: GoogleFonts.inter(fontSize: 12, color: const Color(0xFF64748B), fontStyle: FontStyle.italic),
                  ),
                ),
              ],
            ],
          ),
        );
      },
    );
  }

  Widget _buildFlashcardsView() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 10, 20, 24),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Thẻ ${_currentIndex + 1} / ${_words.length}',
                style: GoogleFonts.outfit(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14),
              ),
              Text(
                'Chạm thẻ để lật 🔄',
                style: GoogleFonts.inter(color: const Color(0xFF64748B), fontSize: 12),
              ),
            ],
          ),
          const SizedBox(height: 14),

          // 3D Card
          Expanded(
            child: GestureDetector(
              onTap: () => setState(() => _showBack = !_showBack),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 300),
                width: double.infinity,
                padding: const EdgeInsets.all(28),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [Color(0xFF131A2A), Color(0xFF0F1523)],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                  borderRadius: BorderRadius.circular(28),
                  border: Border.all(
                    color: _showBack
                        ? const Color(0xFF6366F1).withValues(alpha: 0.5)
                        : const Color(0xFF38BDF8).withValues(alpha: 0.4),
                    width: 1.5,
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withValues(alpha: 0.5),
                      blurRadius: 24,
                      offset: const Offset(0, 10),
                    )
                  ],
                ),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    if (!_showBack) ...[
                      Text(
                        _words[_currentIndex].word,
                        style: GoogleFonts.outfit(
                          fontSize: 38,
                          fontWeight: FontWeight.w800,
                          color: Colors.white,
                        ),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 10),
                      Text(
                        _words[_currentIndex].ipa,
                        style: GoogleFonts.inter(
                          fontSize: 20,
                          color: const Color(0xFF38BDF8),
                          fontStyle: FontStyle.italic,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                      const SizedBox(height: 28),
                      if (_words[_currentIndex].audioUrl != null &&
                          _words[_currentIndex].audioUrl!.isNotEmpty)
                        GestureDetector(
                          onTap: () {
                            _audioPlayer.play(UrlSource(_words[_currentIndex].audioUrl!));
                          },
                          child: Container(
                            width: 56,
                            height: 56,
                            decoration: const BoxDecoration(
                              gradient: LinearGradient(
                                colors: [Color(0xFF38BDF8), Color(0xFF6366F1)],
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: const Icon(Icons.volume_up_rounded, color: Colors.white, size: 28),
                          ),
                        ),
                      const SizedBox(height: 36),
                      Text(
                        'Chạm để xem định nghĩa 🔄',
                        style: GoogleFonts.inter(color: const Color(0xFF64748B), fontSize: 13),
                      ),
                    ] else ...[
                      Text(
                        _words[_currentIndex].meaningEn,
                        style: GoogleFonts.inter(
                          fontSize: 18,
                          color: Colors.white,
                          height: 1.5,
                          fontWeight: FontWeight.w500,
                        ),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 20),
                      if (_words[_currentIndex].contextSentence != null &&
                          _words[_currentIndex].contextSentence!.isNotEmpty)
                        Container(
                          padding: const EdgeInsets.all(14),
                          decoration: BoxDecoration(
                            color: const Color(0xFF080B11),
                            borderRadius: BorderRadius.circular(14),
                          ),
                          child: Text(
                            '"${_words[_currentIndex].contextSentence}"',
                            style: GoogleFonts.inter(fontSize: 13, color: const Color(0xFF38BDF8), fontStyle: FontStyle.italic),
                            textAlign: TextAlign.center,
                          ),
                        ),
                    ],
                  ],
                ),
              ),
            ),
          ),
          const SizedBox(height: 20),

          // Action Buttons
          Row(
            children: [
              Expanded(
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFFEF4444).withValues(alpha: 0.15),
                    foregroundColor: const Color(0xFFEF4444),
                    side: const BorderSide(color: Color(0xFFEF4444)),
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  ),
                  onPressed: () => _review(false),
                  child: Text('Quên rồi (Reset)', style: GoogleFonts.outfit(fontWeight: FontWeight.w700)),
                ),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF10B981),
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  ),
                  onPressed: () => _review(true),
                  child: Text('Đã nhớ (+1 Cấp)', style: GoogleFonts.outfit(fontWeight: FontWeight.w700)),
                ),
              ),
            ],
          )
        ],
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
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
              child: const Icon(Icons.style_outlined, size: 40, color: Color(0xFF38BDF8)),
            ),
            const SizedBox(height: 20),
            Text(
              'Chưa có từ vựng nào trong Sổ tay!',
              style: GoogleFonts.outfit(color: Colors.white, fontSize: 20, fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            Text(
              'Hãy mở bài đọc hoặc vào mục "Từ mới & Tra cứu", chạm vào từ và bấm "Lưu Flashcard" để đưa vào Sổ tay nhé.',
              style: GoogleFonts.inter(color: const Color(0xFF94A3B8), fontSize: 14, height: 1.5),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}

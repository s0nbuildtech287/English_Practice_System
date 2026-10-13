import 'package:flutter/material.dart';
import 'package:audioplayers/audioplayers.dart';
import 'package:google_fonts/google_fonts.dart';
import '../models/article.dart';
import '../models/vocabulary.dart';
import '../services/api_service.dart';

class ArticleDetailScreen extends StatefulWidget {
  final Article article;

  const ArticleDetailScreen({super.key, required this.article});

  @override
  State<ArticleDetailScreen> createState() => _ArticleDetailScreenState();
}

class _ArticleDetailScreenState extends State<ArticleDetailScreen> {
  late AudioPlayer _audioPlayer;
  late AudioPlayer _wordAudioPlayer;
  bool _isPlaying = false;
  Duration _duration = Duration.zero;
  Duration _position = Duration.zero;
  double _playbackRate = 1.0;
  double _fontSize = 18.0;

  String? _selectedWord;

  @override
  void initState() {
    super.initState();
    _audioPlayer = AudioPlayer();
    _wordAudioPlayer = AudioPlayer();

    _audioPlayer.onPlayerStateChanged.listen((state) {
      if (mounted) {
        setState(() {
          _isPlaying = state == PlayerState.playing;
        });
      }
    });

    _audioPlayer.onDurationChanged.listen((newDuration) {
      if (mounted) setState(() => _duration = newDuration);
    });

    _audioPlayer.onPositionChanged.listen((newPosition) {
      if (mounted) setState(() => _position = newPosition);
    });
  }

  @override
  void dispose() {
    _audioPlayer.dispose();
    _wordAudioPlayer.dispose();
    super.dispose();
  }

  void _togglePlayPause() async {
    if (widget.article.audioUrl == null || widget.article.audioUrl!.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Không có file âm thanh cho bài đọc này')),
      );
      return;
    }

    if (_isPlaying) {
      await _audioPlayer.pause();
    } else {
      String url = widget.article.audioUrl!;
      if (url.startsWith('/')) {
        url = 'http://localhost:4000$url';
      }
      await _audioPlayer.setPlaybackRate(_playbackRate);
      await _audioPlayer.play(UrlSource(url));
    }
  }

  void _changeSpeed(double speed) async {
    setState(() => _playbackRate = speed);
    await _audioPlayer.setPlaybackRate(speed);
  }

  void _onWordTapped(String rawWord) async {
    final cleanWord = rawWord.replaceAll(RegExp(r'[^a-zA-Z\-]'), '').trim();
    if (cleanWord.isEmpty) return;

    setState(() => _selectedWord = cleanWord);

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) => _WordLookupSheet(
        word: cleanWord,
        contextSentence: widget.article.content,
        wordAudioPlayer: _wordAudioPlayer,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final words = widget.article.content.split(' ');

    return Scaffold(
      backgroundColor: const Color(0xFF080B11),
      appBar: AppBar(
        backgroundColor: const Color(0xFF080B11),
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new_rounded, color: Colors.white, size: 20),
          onPressed: () => Navigator.pop(context),
        ),
        title: Text(
          widget.article.topic,
          style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w700, color: const Color(0xFF38BDF8)),
        ),
        actions: [
          // Font size adjuster
          IconButton(
            icon: const Icon(Icons.text_fields_rounded, color: Color(0xFF94A3B8), size: 20),
            onPressed: () {
              setState(() {
                _fontSize = _fontSize == 18.0 ? 21.0 : (_fontSize == 21.0 ? 16.0 : 18.0);
              });
            },
          ),
          Container(
            margin: const EdgeInsets.only(right: 16),
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
            decoration: BoxDecoration(
              color: const Color(0xFF10B981).withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(10),
              border: Border.all(color: const Color(0xFF10B981).withValues(alpha: 0.5)),
            ),
            alignment: Alignment.center,
            child: Text(
              widget.article.level,
              style: GoogleFonts.inter(fontWeight: FontWeight.bold, color: const Color(0xFF34D399), fontSize: 12),
            ),
          )
        ],
      ),
      body: Stack(
        children: [
          // Reading Content
          Positioned.fill(
            child: SingleChildScrollView(
              padding: const EdgeInsets.fromLTRB(20, 10, 20, 150),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    widget.article.title,
                    style: GoogleFonts.outfit(
                      fontSize: 26,
                      fontWeight: FontWeight.w800,
                      color: Colors.white,
                      height: 1.3,
                      letterSpacing: -0.5,
                    ),
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                        decoration: BoxDecoration(
                          color: const Color(0xFF131A2A),
                          borderRadius: BorderRadius.circular(6),
                        ),
                        child: Text(
                          widget.article.sourceName,
                          style: GoogleFonts.inter(fontSize: 12, fontWeight: FontWeight.w500, color: const Color(0xFF94A3B8)),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        '•  Chạm vào từ để tra nghĩa & phát âm',
                        style: GoogleFonts.inter(fontSize: 12, color: const Color(0xFF64748B)),
                      ),
                    ],
                  ),
                  const Divider(color: Color(0xFF1E293B), height: 32),

                  // Interactive Reading Paragraph
                  Wrap(
                    spacing: 4,
                    runSpacing: 10,
                    children: words.map((w) {
                      final isSelected = _selectedWord != null &&
                          w.toLowerCase().contains(_selectedWord!.toLowerCase());
                      return InkWell(
                        onTap: () => _onWordTapped(w),
                        borderRadius: BorderRadius.circular(6),
                        child: AnimatedContainer(
                          duration: const Duration(milliseconds: 150),
                          padding: const EdgeInsets.symmetric(horizontal: 3, vertical: 2),
                          decoration: BoxDecoration(
                            color: isSelected ? const Color(0xFF38BDF8).withValues(alpha: 0.25) : Colors.transparent,
                            borderRadius: BorderRadius.circular(6),
                            border: isSelected
                                ? Border.all(color: const Color(0xFF38BDF8).withValues(alpha: 0.6))
                                : null,
                          ),
                          child: Text(
                            '$w ',
                            style: GoogleFonts.inter(
                              fontSize: _fontSize,
                              color: isSelected ? const Color(0xFF38BDF8) : const Color(0xFFE2E8F0),
                              height: 1.65,
                              fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                            ),
                          ),
                        ),
                      );
                    }).toList(),
                  ),
                ],
              ),
            ),
          ),

          // Floating Island Audio Player
          Positioned(
            left: 16,
            right: 16,
            bottom: 20,
            child: Container(
              padding: const EdgeInsets.fromLTRB(16, 12, 16, 12),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Color(0xFF131A2A), Color(0xFF0F1523)],
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                ),
                borderRadius: BorderRadius.circular(24),
                border: Border.all(color: const Color(0xFF38BDF8).withValues(alpha: 0.3), width: 1.2),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.6),
                    blurRadius: 25,
                    offset: const Offset(0, 10),
                  ),
                  BoxShadow(
                    color: const Color(0xFF38BDF8).withValues(alpha: 0.15),
                    blurRadius: 20,
                    offset: const Offset(0, 0),
                  )
                ],
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  // Progress Slider
                  SliderTheme(
                    data: SliderTheme.of(context).copyWith(
                      thumbColor: const Color(0xFF38BDF8),
                      activeTrackColor: const Color(0xFF38BDF8),
                      inactiveTrackColor: const Color(0xFF1E293B),
                      trackHeight: 3.5,
                      thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 6),
                    ),
                    child: Slider(
                      min: 0,
                      max: _duration.inSeconds.toDouble() > 0 ? _duration.inSeconds.toDouble() : 1.0,
                      value: _position.inSeconds.toDouble().clamp(0, _duration.inSeconds.toDouble() > 0 ? _duration.inSeconds.toDouble() : 1.0),
                      onChanged: (val) async {
                        final pos = Duration(seconds: val.toInt());
                        await _audioPlayer.seek(pos);
                      },
                    ),
                  ),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      // Time Info
                      Text(
                        '${_formatDuration(_position)} / ${_formatDuration(_duration)}',
                        style: GoogleFonts.inter(fontSize: 12, fontWeight: FontWeight.w600, color: const Color(0xFF94A3B8)),
                      ),

                      // Central Player Controls
                      Row(
                        children: [
                          IconButton(
                            icon: const Icon(Icons.replay_5_rounded, color: Color(0xFF94A3B8), size: 24),
                            onPressed: () {
                              final newPos = _position - const Duration(seconds: 5);
                              _audioPlayer.seek(newPos < Duration.zero ? Duration.zero : newPos);
                            },
                          ),
                          const SizedBox(width: 4),
                          GestureDetector(
                            onTap: _togglePlayPause,
                            child: Container(
                              width: 48,
                              height: 48,
                              decoration: BoxDecoration(
                                gradient: const LinearGradient(
                                  colors: [Color(0xFF38BDF8), Color(0xFF6366F1)],
                                  begin: Alignment.topLeft,
                                  end: Alignment.bottomRight,
                                ),
                                shape: BoxShape.circle,
                                boxShadow: [
                                  BoxShadow(
                                    color: const Color(0xFF38BDF8).withValues(alpha: 0.4),
                                    blurRadius: 14,
                                    offset: const Offset(0, 4),
                                  )
                                ],
                              ),
                              child: Icon(
                                _isPlaying ? Icons.pause_rounded : Icons.play_arrow_rounded,
                                color: Colors.white,
                                size: 28,
                              ),
                            ),
                          ),
                          const SizedBox(width: 4),
                          IconButton(
                            icon: const Icon(Icons.forward_5_rounded, color: Color(0xFF94A3B8), size: 24),
                            onPressed: () {
                              final newPos = _position + const Duration(seconds: 5);
                              _audioPlayer.seek(newPos > _duration ? _duration : newPos);
                            },
                          ),
                        ],
                      ),

                      // Speed Selector
                      PopupMenuButton<double>(
                        initialValue: _playbackRate,
                        onSelected: _changeSpeed,
                        color: const Color(0xFF131A2A),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(14),
                          side: const BorderSide(color: Color(0xFF1E293B)),
                        ),
                        child: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                          decoration: BoxDecoration(
                            color: const Color(0xFF1E293B),
                            borderRadius: BorderRadius.circular(10),
                            border: Border.all(color: const Color(0xFF334155)),
                          ),
                          child: Text(
                            '${_playbackRate}x',
                            style: GoogleFonts.outfit(color: const Color(0xFF38BDF8), fontSize: 13, fontWeight: FontWeight.w700),
                          ),
                        ),
                        itemBuilder: (ctx) => [
                          const PopupMenuItem(value: 0.75, child: Text('0.75x (Chậm)', style: TextStyle(color: Colors.white))),
                          const PopupMenuItem(value: 1.0, child: Text('1.0x (Chuẩn)', style: TextStyle(color: Colors.white))),
                          const PopupMenuItem(value: 1.25, child: Text('1.25x (Nhanh)', style: TextStyle(color: Colors.white))),
                        ],
                      )
                    ],
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  String _formatDuration(Duration d) {
    final m = d.inMinutes.remainder(60).toString().padLeft(2, '0');
    final s = d.inSeconds.remainder(60).toString().padLeft(2, '0');
    return '$m:$s';
  }
}

class _WordLookupSheet extends StatefulWidget {
  final String word;
  final String contextSentence;
  final AudioPlayer wordAudioPlayer;

  const _WordLookupSheet({
    required this.word,
    required this.contextSentence,
    required this.wordAudioPlayer,
  });

  @override
  State<_WordLookupSheet> createState() => _WordLookupSheetState();
}

class _WordLookupSheetState extends State<_WordLookupSheet> {
  Vocabulary? _vocab;
  bool _isLoading = true;
  bool _isSaved = false;

  @override
  void initState() {
    super.initState();
    _fetch();
  }

  void _fetch() async {
    try {
      final res = await ApiService.lookupWord(widget.word);
      if (mounted) {
        setState(() {
          _vocab = res;
          _isLoading = false;
        });
        if (res.audioUrl != null && res.audioUrl!.isNotEmpty) {
          widget.wordAudioPlayer.play(UrlSource(res.audioUrl!));
        }
      }
    } catch (e) {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  void _saveWord() async {
    if (_vocab == null) return;
    try {
      await ApiService.saveWord(
        word: _vocab!.word,
        ipa: _vocab!.ipa,
        meaningEn: _vocab!.meaningEn,
        audioUrl: _vocab!.audioUrl,
        contextSentence: widget.contextSentence,
      );
      if (mounted) {
        setState(() => _isSaved = true);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: const Color(0xFF10B981),
            behavior: SnackBarBehavior.floating,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            content: Row(
              children: [
                const Icon(Icons.check_circle_rounded, color: Colors.white, size: 20),
                const SizedBox(width: 10),
                Text('Đã lưu "${_vocab!.word}" vào Flashcards!'),
              ],
            ),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Lưu từ thất bại')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(26),
      decoration: BoxDecoration(
        color: const Color(0xFF0F1523),
        borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
        border: Border.all(color: const Color(0xFF1E293B), width: 1.5),
        boxShadow: [
          BoxShadow(color: Colors.black.withValues(alpha: 0.6), blurRadius: 30, offset: const Offset(0, -10))
        ],
      ),
      child: _isLoading
          ? const Center(
              child: Padding(
                padding: EdgeInsets.symmetric(vertical: 50),
                child: CircularProgressIndicator(color: Color(0xFF38BDF8)),
              ),
            )
          : Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Drag handle
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
                const SizedBox(height: 18),

                // Word Header & Audio Speaker
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            _vocab?.word ?? widget.word,
                            style: GoogleFonts.outfit(
                              fontSize: 28,
                              fontWeight: FontWeight.w800,
                              color: Colors.white,
                              letterSpacing: -0.5,
                            ),
                          ),
                          if (_vocab?.ipa != null && _vocab!.ipa.isNotEmpty)
                            Text(
                              _vocab!.ipa,
                              style: GoogleFonts.inter(
                                fontSize: 16,
                                color: const Color(0xFF38BDF8),
                                fontStyle: FontStyle.italic,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                        ],
                      ),
                    ),
                    Row(
                      children: [
                        if (_vocab?.audioUrl != null && _vocab!.audioUrl!.isNotEmpty)
                          IconButton.filled(
                            style: IconButton.styleFrom(
                              backgroundColor: const Color(0xFF38BDF8).withValues(alpha: 0.15),
                              foregroundColor: const Color(0xFF38BDF8),
                            ),
                            icon: const Icon(Icons.volume_up_rounded, size: 24),
                            onPressed: () {
                              widget.wordAudioPlayer.play(UrlSource(_vocab!.audioUrl!));
                            },
                          ),
                        const SizedBox(width: 8),
                        ElevatedButton.icon(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: _isSaved ? const Color(0xFF10B981) : const Color(0xFF6366F1),
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                          ),
                          onPressed: _isSaved ? null : _saveWord,
                          icon: Icon(_isSaved ? Icons.check_rounded : Icons.bookmark_add_rounded, size: 18),
                          label: Text(
                            _isSaved ? 'Đã lưu' : 'Lưu Flashcard',
                            style: GoogleFonts.outfit(fontWeight: FontWeight.w700, fontSize: 13),
                          ),
                        ),
                      ],
                    )
                  ],
                ),
                const SizedBox(height: 14),

                if (_vocab?.partOfSpeech != null && _vocab!.partOfSpeech.isNotEmpty)
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 3),
                    decoration: BoxDecoration(
                      color: const Color(0xFF1E293B),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      _vocab!.partOfSpeech.toUpperCase(),
                      style: GoogleFonts.outfit(
                        color: const Color(0xFF94A3B8),
                        fontWeight: FontWeight.w800,
                        fontSize: 11,
                        letterSpacing: 0.5,
                      ),
                    ),
                  ),

                const SizedBox(height: 14),
                Text(
                  'ĐỊNH NGHĨA',
                  style: GoogleFonts.outfit(color: const Color(0xFF64748B), fontSize: 11, fontWeight: FontWeight.w800, letterSpacing: 0.5),
                ),
                const SizedBox(height: 6),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: const Color(0xFF131A2A),
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(color: const Color(0xFF1E293B)),
                  ),
                  child: Text(
                    _vocab?.meaningEn.isNotEmpty == true ? _vocab!.meaningEn : 'Không có định nghĩa trực tuyến.',
                    style: GoogleFonts.inter(color: Colors.white, fontSize: 15, height: 1.45),
                  ),
                ),

                if (_vocab?.exampleSentence != null && _vocab!.exampleSentence!.isNotEmpty) ...[
                  const SizedBox(height: 14),
                  Text(
                    'CÂU VÍ DỤ THỰC TẾ',
                    style: GoogleFonts.outfit(color: const Color(0xFF64748B), fontSize: 11, fontWeight: FontWeight.w800, letterSpacing: 0.5),
                  ),
                  const SizedBox(height: 6),
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(14),
                    decoration: BoxDecoration(
                      color: const Color(0xFF131A2A),
                      borderRadius: BorderRadius.circular(14),
                      border: Border.all(color: const Color(0xFF1E293B)),
                    ),
                    child: Text(
                      '"${_vocab!.exampleSentence}"',
                      style: GoogleFonts.inter(color: const Color(0xFF38BDF8), fontStyle: FontStyle.italic, fontSize: 14, height: 1.4),
                    ),
                  ),
                ],
                const SizedBox(height: 20),
              ],
            ),
    );
  }
}

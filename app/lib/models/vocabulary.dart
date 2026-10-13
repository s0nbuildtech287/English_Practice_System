class Vocabulary {
  final int id;
  final String word;
  final String ipa;
  final String partOfSpeech;
  final String meaningEn;
  final String meaningVi;
  final String? audioUrl;
  final String? exampleSentence;

  Vocabulary({
    required this.id,
    required this.word,
    required this.ipa,
    required this.partOfSpeech,
    required this.meaningEn,
    required this.meaningVi,
    this.audioUrl,
    this.exampleSentence,
  });

  factory Vocabulary.fromJson(Map<String, dynamic> json) {
    return Vocabulary(
      id: json['id'] as int? ?? 0,
      word: json['word'] as String? ?? '',
      ipa: json['ipa'] as String? ?? '',
      partOfSpeech: json['partOfSpeech'] as String? ?? '',
      meaningEn: json['meaningEn'] as String? ?? '',
      meaningVi: json['meaningVi'] as String? ?? '',
      audioUrl: json['audioUrl'] as String?,
      exampleSentence: json['exampleSentence'] as String?,
    );
  }
}

class SavedWord {
  final int id;
  final String word;
  final String ipa;
  final String meaningEn;
  final String? audioUrl;
  final String? contextSentence;
  final int repetitionLevel;
  final String nextReviewDate;

  SavedWord({
    required this.id,
    required this.word,
    required this.ipa,
    required this.meaningEn,
    this.audioUrl,
    this.contextSentence,
    required this.repetitionLevel,
    required this.nextReviewDate,
  });

  factory SavedWord.fromJson(Map<String, dynamic> json) {
    return SavedWord(
      id: json['id'] as int? ?? 0,
      word: json['word'] as String? ?? '',
      ipa: json['ipa'] as String? ?? '',
      meaningEn: json['meaningEn'] as String? ?? '',
      audioUrl: json['audioUrl'] as String?,
      contextSentence: json['contextSentence'] as String?,
      repetitionLevel: json['repetitionLevel'] as int? ?? 0,
      nextReviewDate: json['nextReviewDate'] as String? ?? '',
    );
  }
}

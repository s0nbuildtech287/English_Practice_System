class Article {
  final int id;
  final String title;
  final String topic;
  final String level;
  final String content;
  final String? audioUrl;
  final int durationSeconds;
  final String sourceName;
  bool isBookmarked;

  Article({
    required this.id,
    required this.title,
    required this.topic,
    required this.level,
    required this.content,
    this.audioUrl,
    required this.durationSeconds,
    required this.sourceName,
    this.isBookmarked = false,
  });

  factory Article.fromJson(Map<String, dynamic> json) {
    return Article(
      id: json['id'] as int,
      title: json['title'] as String? ?? '',
      topic: json['topic'] as String? ?? 'General',
      level: json['level'] as String? ?? 'B1',
      content: json['content'] as String? ?? '',
      audioUrl: json['audioUrl'] as String?,
      durationSeconds: json['durationSeconds'] as int? ?? 180,
      sourceName: json['sourceName'] as String? ?? 'VOA Learning English',
      isBookmarked: json['bookmarked'] as bool? ?? false,
    );
  }
}

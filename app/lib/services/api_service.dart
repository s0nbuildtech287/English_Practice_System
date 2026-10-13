import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/article.dart';
import '../models/vocabulary.dart';

class ApiService {
  static const String baseUrl = 'http://localhost:8080/api';

  static Future<List<Article>> getArticles({String? level, String? topic}) async {
    final queryParams = <String, String>{};
    if (level != null && level.isNotEmpty) queryParams['level'] = level;
    if (topic != null && topic.isNotEmpty) queryParams['topic'] = topic;

    final uri = Uri.parse('$baseUrl/articles').replace(queryParameters: queryParams.isEmpty ? null : queryParams);
    final response = await http.get(uri);

    if (response.statusCode == 200) {
      final List data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map((e) => Article.fromJson(e)).toList();
    } else {
      throw Exception('Failed to load articles');
    }
  }

  static Future<List<Article>> getBookmarkedArticles() async {
    final uri = Uri.parse('$baseUrl/articles/bookmarked');
    final response = await http.get(uri);

    if (response.statusCode == 200) {
      final List data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map((e) => Article.fromJson(e)).toList();
    } else {
      throw Exception('Failed to load bookmarked articles');
    }
  }

  static Future<Article> toggleBookmark(int id) async {
    final uri = Uri.parse('$baseUrl/articles/$id/toggle-bookmark');
    final response = await http.post(uri);

    if (response.statusCode == 200) {
      final data = jsonDecode(utf8.decode(response.bodyBytes));
      return Article.fromJson(data);
    } else {
      throw Exception('Failed to toggle bookmark');
    }
  }

  static Future<Article> createCustomArticle({
    required String title,
    required String content,
    required String topic,
    required String level,
  }) async {
    final uri = Uri.parse('$baseUrl/articles');
    final response = await http.post(
      uri,
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'title': title,
        'content': content,
        'topic': topic,
        'level': level,
        'sourceName': 'Người dùng tự thêm',
        'durationSeconds': 180,
        'audioUrl': 'http://localhost:4000/audio/article1.mp3', // fallback audio
      }),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(utf8.decode(response.bodyBytes));
      return Article.fromJson(data);
    } else {
      throw Exception('Failed to create custom article');
    }
  }

  static Future<Vocabulary> lookupWord(String word) async {
    final uri = Uri.parse('$baseUrl/dictionary/lookup?word=${Uri.encodeComponent(word)}');
    final response = await http.get(uri);

    if (response.statusCode == 200) {
      final data = jsonDecode(utf8.decode(response.bodyBytes));
      return Vocabulary.fromJson(data);
    } else {
      throw Exception('Failed to lookup word');
    }
  }

  static Future<SavedWord> saveWord({
    required String word,
    required String ipa,
    required String meaningEn,
    String? audioUrl,
    String? contextSentence,
  }) async {
    final uri = Uri.parse('$baseUrl/vocab-notebook/save');
    final response = await http.post(
      uri,
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'word': word,
        'ipa': ipa,
        'meaningEn': meaningEn,
        'audioUrl': audioUrl ?? '',
        'contextSentence': contextSentence ?? '',
      }),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(utf8.decode(response.bodyBytes));
      return SavedWord.fromJson(data);
    } else {
      throw Exception('Failed to save word');
    }
  }

  static Future<List<SavedWord>> getSavedVocab() async {
    final uri = Uri.parse('$baseUrl/vocab-notebook');
    final response = await http.get(uri);

    if (response.statusCode == 200) {
      final List data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map((e) => SavedWord.fromJson(e)).toList();
    } else {
      throw Exception('Failed to load saved vocabulary');
    }
  }

  static Future<SavedWord> reviewCard(int id, bool remembered) async {
    final uri = Uri.parse('$baseUrl/vocab-notebook/review/$id?remembered=$remembered');
    final response = await http.post(uri);

    if (response.statusCode == 200) {
      final data = jsonDecode(utf8.decode(response.bodyBytes));
      return SavedWord.fromJson(data);
    } else {
      throw Exception('Failed to update review status');
    }
  }
}

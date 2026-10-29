import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;
import '../constants/api_constants.dart';

class ApiService {
  final http.Client _client;

  ApiService({http.Client? client}) : _client = client ?? http.Client();

  Map<String, String> get _headers => {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      };

  // 1. Health Check
  Future<Map<String, dynamic>> getHealth() async {
    final url = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.healthEndpoint}');
    try {
      final response = await _client.get(url, headers: _headers).timeout(const Duration(seconds: 10));
      return _parseResponse(response);
    } catch (e) {
      return {'status': 'offline', 'error': e.toString()};
    }
  }

  // 2. Stress / Text Analysis
  Future<Map<String, dynamic>> analyzeText({
    required String text,
    double? sleepHrs,
    double? workHrs,
  }) async {
    final url = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.analyzeTextEndpoint}');
    final Map<String, dynamic> payload = {'text': text};
    if (sleepHrs != null) payload['sleep_hrs'] = sleepHrs;
    if (workHrs != null) payload['work_hrs'] = workHrs;

    try {
      final response = await _client.post(url, headers: _headers, body: jsonEncode(payload)).timeout(const Duration(seconds: 12));
      return _parseResponse(response);
    } catch (e) {
      throw Exception('Network error during text analysis: $e');
    }
  }

  // 3. Multi-Day Burnout Analysis
  Future<Map<String, dynamic>> analyzeBurnout({
    required List<double> scores,
  }) async {
    final url = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.analyzeBurnoutEndpoint}');
    final body = jsonEncode({'scores': scores});

    try {
      final response = await _client.post(url, headers: _headers, body: body).timeout(const Duration(seconds: 10));
      return _parseResponse(response);
    } catch (e) {
      throw Exception('Network error during burnout analysis: $e');
    }
  }

  // 4. Conversational AI Chat
  Future<Map<String, dynamic>> sendChatMessage({
    required String message,
    required String userId,
    String? conversationId,
    String? currentStressLevel,
  }) async {
    final url = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.chatEndpoint}');
    final Map<String, dynamic> payload = {
      'message': message,
      'user_id': userId,
    };
    if (conversationId != null) payload['conversation_id'] = conversationId;
    if (currentStressLevel != null) payload['current_stress_level'] = currentStressLevel;

    try {
      final response = await _client.post(url, headers: _headers, body: jsonEncode(payload)).timeout(const Duration(seconds: 15));
      return _parseResponse(response);
    } catch (e) {
      throw Exception('Server unreachable: $e');
    }
  }

  // 5. Daily Wellness Quiz Submission
  Future<Map<String, dynamic>> submitDailyQuiz({
    required String userId,
    required String localDate,
    required String timezone,
    required int stressScore,
    required String stressLevel,
    required String recommendation,
    required List<dynamic> answers,
  }) async {
    final url = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.quizSubmitEndpoint}');
    final body = jsonEncode({
      'user_id': userId,
      'local_date': localDate,
      'timezone': timezone,
      'stress_score': stressScore,
      'stress_level': stressLevel,
      'recommendation': recommendation,
      'answers': answers,
    });

    try {
      final response = await _client.post(url, headers: _headers, body: body).timeout(const Duration(seconds: 10));
      return _parseResponse(response);
    } catch (e) {
      throw Exception('Failed to submit daily quiz: $e');
    }
  }

  // 6. Check Quiz Status for Date
  Future<Map<String, dynamic>> getDailyQuizStatus({
    required String userId,
    required String localDate,
  }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.quizStatusEndpoint}').replace(queryParameters: {
      'user_id': userId,
      'local_date': localDate,
    });

    try {
      final response = await _client.get(uri, headers: _headers).timeout(const Duration(seconds: 8));
      return _parseResponse(response);
    } catch (e) {
      return {'already_completed': false, 'error': e.toString()};
    }
  }

  Map<String, dynamic> _parseResponse(http.Response response) {
    if (response.statusCode >= 200 && response.statusCode < 300) {
      return jsonDecode(response.body) as Map<String, dynamic>;
    } else {
      final body = jsonDecode(response.body);
      final errorMsg = body is Map && body.containsKey('error') ? body['error'] : 'HTTP ${response.statusCode}';
      throw HttpException(errorMsg.toString());
    }
  }
}

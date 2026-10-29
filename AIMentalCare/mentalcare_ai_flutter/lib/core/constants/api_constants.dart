import 'package:flutter/foundation.dart';

class ApiConstants {
  // Configurable Base Backend URLs
  // Android Emulator: http://10.0.2.2:5000
  // iOS Simulator / Desktop: http://127.0.0.1:5000
  // Production: https://YOUR-DEPLOYED-FLASK-BACKEND-URL.com
  static const String devAndroidBaseUrl = 'http://10.0.2.2:5000';
  static const String devIosBaseUrl = 'http://127.0.0.1:5000';
  static const String prodBaseUrl = 'https://mentalcare-ai.onrender.com';

  // Toggle for Production environment
  static const bool isProduction = false;

  static String get baseUrl {
    if (isProduction) {
      return prodBaseUrl;
    }
    if (defaultTargetPlatform == TargetPlatform.android) {
      return devAndroidBaseUrl;
    }
    return devIosBaseUrl;
  }

  // API Endpoints
  static const String healthEndpoint = '/health';
  static const String analyzeTextEndpoint = '/api/analyze_text';
  static const String analyzeBurnoutEndpoint = '/api/analyze_burnout';
  static const String chatEndpoint = '/api/chat';
  static const String quizSubmitEndpoint = '/api/quiz/submit';
  static const String quizStatusEndpoint = '/api/quiz/status';
}

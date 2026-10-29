import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../core/network/api_service.dart';
import '../core/utils/database_helper.dart';
import '../models/checkin_model.dart';

class CheckInProvider extends ChangeNotifier {
  final ApiService _apiService = ApiService();
  CheckInModel? _todayCheckIn;
  List<CheckInModel> _allCheckIns = [];
  bool _isLoading = false;

  CheckInModel? get todayCheckIn => _todayCheckIn;
  List<CheckInModel> get allCheckIns => _allCheckIns;
  bool get isLoading => _isLoading;

  Future<void> loadTodayCheckIn(String userId) async {
    _isLoading = true;
    notifyListeners();

    final todayStr = DateFormat('yyyy-MM-dd').format(DateTime.now());

    // Check local database first
    final localData = await DatabaseHelper.instance.getTodayCheckIn(userId, todayStr);
    if (localData != null) {
      _todayCheckIn = CheckInModel.fromMap(localData);
    } else {
      // Check API status
      try {
        final status = await _apiService.getDailyQuizStatus(userId: userId, localDate: todayStr);
        if (status['already_completed'] == true && status['result'] != null) {
          final res = status['result'];
          _todayCheckIn = CheckInModel(
            dailyRecordId: '${userId}_$todayStr',
            userId: userId,
            localDate: todayStr,
            stressScore: res['stress_score'] ?? 50,
            stressLevel: res['stress_level'] ?? 'MODERATE',
            recommendation: res['recommendation'] ?? '',
            createdAt: DateTime.now(),
          );
          await DatabaseHelper.instance.insertCheckIn(_todayCheckIn!.toMap());
        } else {
          _todayCheckIn = null;
        }
      } catch (e) {
        _todayCheckIn = null;
      }
    }

    // Load all historical check-ins
    final allLocal = await DatabaseHelper.instance.getAllCheckIns(userId);
    _allCheckIns = allLocal.map((m) => CheckInModel.fromMap(m)).toList();

    _isLoading = false;
    notifyListeners();
  }

  Future<void> submitCheckIn({
    required String userId,
    required int stressScore,
    required String stressLevel,
    required String recommendation,
    required List<dynamic> answers,
  }) async {
    _isLoading = true;
    notifyListeners();

    final todayStr = DateFormat('yyyy-MM-dd').format(DateTime.now());
    final newCheckIn = CheckInModel(
      dailyRecordId: '${userId}_$todayStr',
      userId: userId,
      localDate: todayStr,
      stressScore: stressScore,
      stressLevel: stressLevel,
      recommendation: recommendation,
      createdAt: DateTime.now(),
    );

    // Save locally
    await DatabaseHelper.instance.insertCheckIn(newCheckIn.toMap());
    _todayCheckIn = newCheckIn;
    _allCheckIns.insert(0, newCheckIn);

    // Send to backend API
    try {
      await _apiService.submitDailyQuiz(
        userId: userId,
        localDate: todayStr,
        timezone: 'UTC',
        stressScore: stressScore,
        stressLevel: stressLevel,
        recommendation: recommendation,
        answers: answers,
      );
    } catch (_) {
      // Network error handled gracefully; offline record saved locally
    }

    _isLoading = false;
    notifyListeners();
  }
}

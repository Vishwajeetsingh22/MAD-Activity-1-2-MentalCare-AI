import 'package:flutter/material.dart';
import '../core/utils/database_helper.dart';
import '../models/journal_model.dart';

class JournalProvider extends ChangeNotifier {
  List<JournalModel> _journals = [];
  bool _isLoading = false;

  List<JournalModel> get journals => _journals;
  bool get isLoading => _isLoading;

  Future<void> loadJournals(String userId) async {
    _isLoading = true;
    notifyListeners();

    final list = await DatabaseHelper.instance.getJournals(userId);
    _journals = list.map((m) => JournalModel.fromMap(m)).toList();

    _isLoading = false;
    notifyListeners();
  }

  Future<void> addJournal({
    required String userId,
    required String title,
    required String content,
    required String moodEmoji,
  }) async {
    final newEntry = JournalModel(
      userId: userId,
      title: title,
      content: content,
      moodEmoji: moodEmoji,
      createdAt: DateTime.now(),
    );

    await DatabaseHelper.instance.insertJournal(newEntry.toMap());
    await loadJournals(userId);
  }

  Future<void> deleteJournal(int id, String userId) async {
    await DatabaseHelper.instance.deleteJournal(id);
    await loadJournals(userId);
  }
}

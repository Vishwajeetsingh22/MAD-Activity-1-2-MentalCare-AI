import 'package:flutter/material.dart';
import '../core/network/api_service.dart';
import '../core/utils/database_helper.dart';
import '../core/utils/local_chat_engine.dart';
import '../models/chat_message_model.dart';

class ChatProvider extends ChangeNotifier {
  final ApiService _apiService = ApiService();
  List<ChatMessageModel> _messages = [];
  bool _isThinking = false;
  String _currentConversationId = 'conv_${DateTime.now().millisecondsSinceEpoch}';

  List<ChatMessageModel> get messages => _messages;
  bool get isThinking => _isThinking;
  String get conversationId => _currentConversationId;

  Future<void> loadMessages(String userId) async {
    final list = await DatabaseHelper.instance.getChatMessages(userId, _currentConversationId);
    _messages = list.map((m) => ChatMessageModel.fromMap(m)).toList();

    if (_messages.isEmpty) {
      await _insertWelcomeGreeting(userId);
    }
    notifyListeners();
  }

  Future<void> _insertWelcomeGreeting(String userId) async {
    const welcome = "Hello! I am your AI MentalCare Assistant. Talk to me about how you're feeling today—whether it's academic deadlines, exams, sleep, or simply wanting a calm pause. How can I support you right now?";
    final welcomeMsg = ChatMessageModel(
      userId: userId,
      conversationId: _currentConversationId,
      sender: 'AI',
      messageText: welcome,
      isCrisis: false,
      timestamp: DateTime.now(),
    );
    await DatabaseHelper.instance.insertChatMessage(welcomeMsg.toMap());
    _messages = [welcomeMsg];
    notifyListeners();
  }

  Future<void> sendMessage({
    required String text,
    required String userId,
    String? currentStressLevel,
  }) async {
    if (text.trim().isEmpty) return;

    final userMsg = ChatMessageModel(
      userId: userId,
      conversationId: _currentConversationId,
      sender: 'USER',
      messageText: text.trim(),
      timestamp: DateTime.now(),
    );

    // Save and update UI
    await DatabaseHelper.instance.insertChatMessage(userMsg.toMap());
    _messages.add(userMsg);
    _isThinking = true;
    notifyListeners();

    try {
      final response = await _apiService.sendChatMessage(
        message: text.trim(),
        userId: userId,
        conversationId: _currentConversationId,
        currentStressLevel: currentStressLevel,
      );

      final replyText = response['reply'] ?? "I'm listening and here to support you.";
      final isCrisis = response['is_crisis'] == true;

      final aiMsg = ChatMessageModel(
        userId: userId,
        conversationId: _currentConversationId,
        sender: 'AI',
        messageText: replyText,
        isCrisis: isCrisis,
        timestamp: DateTime.now(),
      );

      await DatabaseHelper.instance.insertChatMessage(aiMsg.toMap());
      _messages.add(aiMsg);
    } catch (_) {
      // On-device fallback if offline or backend is unreachable
      final localResult = LocalChatEngine.respond(
        message: text.trim(),
        conversationId: _currentConversationId,
        currentStressLevel: currentStressLevel,
      );

      final localMsg = ChatMessageModel(
        userId: userId,
        conversationId: _currentConversationId,
        sender: 'AI',
        messageText: '[On-Device Support]\n${localResult.reply}',
        isCrisis: localResult.isCrisis,
        timestamp: DateTime.now(),
      );

      await DatabaseHelper.instance.insertChatMessage(localMsg.toMap());
      _messages.add(localMsg);
    } finally {
      _isThinking = false;
      notifyListeners();
    }
  }

  Future<void> startNewConversation(String userId) async {
    _currentConversationId = 'conv_${DateTime.now().millisecondsSinceEpoch}';
    LocalChatEngine.clearSession(_currentConversationId);
    await loadMessages(userId);
  }

  Future<void> clearHistory(String userId) async {
    await DatabaseHelper.instance.clearChatMessages(userId, _currentConversationId);
    LocalChatEngine.clearSession(_currentConversationId);
    await _insertWelcomeGreeting(userId);
  }
}

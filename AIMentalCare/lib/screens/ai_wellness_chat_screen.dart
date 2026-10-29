import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import '../core/theme/app_colors.dart';

/// Message model for AI Wellness Assistant Chat
class ChatMessage {
  final String id;
  final String sender; // 'USER' or 'AI'
  final String text;
  final DateTime timestamp;
  final bool isCrisis;

  ChatMessage({
    required this.id,
    required this.sender,
    required this.text,
    required this.timestamp,
    this.isCrisis = false,
  });
}

/// Phase 4 — Premium AI Wellness Chat Screen
class AiWellnessChatScreen extends StatefulWidget {
  final String userId;
  final String userName;
  final String? initialStressLevel;
  final String backendUrl;

  const AiWellnessChatScreen({
    Key? key,
    this.userId = 'user@mentalcare.ai',
    this.userName = 'User',
    this.initialStressLevel = 'Normal baseline',
    this.backendUrl = 'http://10.0.2.2:5000',
  }) : super(key: key);

  @override
  State<AiWellnessChatScreen> createState() => _AiWellnessChatScreenState();
}

typedef AIWellnessChatScreen = AiWellnessChatScreen;

class _AiWellnessChatScreenState extends State<AiWellnessChatScreen> {
  final TextEditingController _textController = TextEditingController();
  final ScrollController _scrollController = ScrollController();

  late String _conversationId;
  bool _isSending = false;
  String? _lastFailedMessage;

  final List<ChatMessage> _messages = [];

  // Local stateful conversation memory for on-device fallback
  final Map<String, dynamic> _localMemory = {
    'stress_stated': false,
    'causes': <String>[],
    'subjects': <String>[],
    'sleep_issue': false,
    'last_intent': 'greeting',
  };

  @override
  void initState() {
    super.initState();
    _conversationId = 'conv_${DateTime.now().millisecondsSinceEpoch}';
    _insertWelcomeMessage();
  }

  @override
  void dispose() {
    _textController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _insertWelcomeMessage() {
    _messages.add(
      ChatMessage(
        id: 'msg_welcome',
        sender: 'AI',
        text: "Hello! I am your AI MentalCare Assistant. Talk to me about how you're feeling today—whether it's academic deadlines, exams, sleep, or simply wanting a calm pause. How can I support you right now?",
        timestamp: DateTime.now(),
      ),
    );
  }

  void _startNewChat() {
    setState(() {
      _conversationId = 'conv_${DateTime.now().millisecondsSinceEpoch}';
      _messages.clear();
      _localMemory['stress_stated'] = false;
      (_localMemory['causes'] as List).clear();
      (_localMemory['subjects'] as List).clear();
      _localMemory['sleep_issue'] = false;
      _localMemory['last_intent'] = 'greeting';
      _insertWelcomeMessage();
    });
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Started a new conversation session.')),
    );
  }

  void _clearChat() {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Clear Chat History'),
        content: const Text('Are you sure you want to clear all messages in this conversation?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.high),
            onPressed: () {
              Navigator.pop(ctx);
              setState(() {
                _messages.clear();
                _insertWelcomeMessage();
              });
            },
            child: const Text('Clear', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  Future<void> _sendMessage([String? prefilledText]) async {
    final text = (prefilledText ?? _textController.text).trim();
    if (text.isEmpty || _isSending) return;

    setState(() {
      _isSending = true;
      _messages.add(
        ChatMessage(
          id: 'msg_${DateTime.now().millisecondsSinceEpoch}',
          sender: 'USER',
          text: text,
          timestamp: DateTime.now(),
        ),
      );
    });

    _textController.clear();
    _scrollToBottom();

    try {
      final response = await http
          .post(
            Uri.parse('${widget.backendUrl}/api/chat'),
            headers: {'Content-Type': 'application/json'},
            body: jsonEncode({
              'user_id': widget.userId,
              'message': text,
              'conversation_id': _conversationId,
              'current_stress_level': widget.initialStressLevel,
            }),
          )
          .timeout(const Duration(seconds: 4));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final reply = data['reply'] ?? 'Thank you for sharing with me.';
        final isCrisis = data['is_crisis'] ?? false;

        setState(() {
          _messages.add(
            ChatMessage(
              id: 'ai_${DateTime.now().millisecondsSinceEpoch}',
              sender: 'AI',
              text: reply,
              timestamp: DateTime.now(),
              isCrisis: isCrisis,
            ),
          );
        });
      } else {
        _handleFailure(text, 'Server responded with code ${response.statusCode}');
      }
    } catch (e) {
      _handleFailure(text, 'Network unavailable (${e.toString()})');
    } finally {
      if (mounted) {
        setState(() {
          _isSending = false;
        });
        _scrollToBottom();
      }
    }
  }

  void _handleFailure(String userText, String reason) {
    _lastFailedMessage = userText;

    // Generate local contextual on-device response
    final localResponse = _generateLocalContextualResponse(userText);

    setState(() {
      _messages.add(
        ChatMessage(
          id: 'ai_${DateTime.now().millisecondsSinceEpoch}',
          sender: 'AI',
          text: '[On-Device Wellness Support]\n$localResponse',
          timestamp: DateTime.now(),
        ),
      );
    });

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Backend unreachable ($reason). On-device wellness response provided.'),
        duration: const Duration(seconds: 4),
        action: SnackBarAction(
          label: 'Retry',
          onPressed: () {
            if (_lastFailedMessage != null) {
              _sendMessage(_lastFailedMessage);
            }
          },
        ),
      ),
    );
  }

  String _generateLocalContextualResponse(String text) {
    final lower = text.toLowerCase();

    // Check crisis
    if (lower.contains('suicide') || lower.contains('kill myself') || lower.contains('end my life')) {
      return "I care deeply about your safety. If you are experiencing thoughts of self-harm, please connect with crisis support immediately. In the US, dial or text 988. In India, call 14416 (Tele-MANAS) or 112. You are not alone, and help is available right now.";
    }

    // Entity extraction
    if (lower.contains('stress') || lower.contains('anxious') || lower.contains('overwhelm')) {
      _localMemory['stress_stated'] = true;
      _localMemory['last_intent'] = 'stress_disclosure';
    }
    if (lower.contains('exam') || lower.contains('test')) {
      (_localMemory['causes'] as List).add('exams');
    }
    if (lower.contains('math') || lower.contains('physics') || lower.contains('coding')) {
      (_localMemory['subjects'] as List).add('study');
    }
    if (lower.contains('sleep') || lower.contains('insomnia') || lower.contains('tired')) {
      _localMemory['sleep_issue'] = true;
    }

    // Context recall
    if (lower.contains('what did i tell you') || lower.contains('do you remember')) {
      final causes = (_localMemory['causes'] as List);
      if (causes.isNotEmpty) {
        return "You mentioned earlier that you're experiencing stress related to your ${causes.join(', ')}. Would you like to break your study schedule down into 25-minute Pomodoro blocks with regular rest pauses?";
      }
      return "We've been talking about your wellness and daily balance. What specific part would you like to revisit?";
    }

    // Affirmation handling
    if (lower.contains('yes') && (_localMemory['last_intent'] == 'stress_disclosure' || lower.contains('question'))) {
      return "Thank you for being open. To help me understand better: Is your stress mostly coming from academic deadlines, lack of restorative sleep, or a heavy daily workload?";
    }

    // Quick prompts handling
    if (lower.contains('relax') || lower.contains('breathing')) {
      return "Let's take a calm pause together. Try the 4-7-8 breathing exercise:\n\n1. Inhale gently through your nose for 4 seconds.\n2. Hold your breath calmly for 7 seconds.\n3. Exhale slowly through your mouth for 8 seconds.\n\nRepeat this cycle 4 times to stimulate your parasympathetic relaxation response.";
    }

    if (lower.contains('plan my day') || lower.contains('schedule')) {
      return "Here is a low-stress, structured approach to your day:\n\n1. Highlight your Top 3 non-negotiable tasks.\n2. Dedicate the morning to the highest cognitive priority.\n3. Schedule a 20-minute movement or mindful break after lunch.\n4. Set a firm cut-off time 1 hour before sleep.\n\nWhich task would you like to tackle first?";
    }

    if (lower.contains('sleep')) {
      return "To restore restful sleep tonight:\n\n1. Dim bright screens 45 minutes before bedtime.\n2. Keep your bedroom cool (around 18-20°C / 65-68°F).\n3. Avoid caffeine after 2:00 PM.\n4. Practice gentle progressive muscle relaxation before turning off the lights.";
    }

    if (lower.contains('check-in') || lower.contains('recent')) {
      return "Based on your recent wellness records, your stress context is tracked as ${widget.initialStressLevel}. Continuing daily morning check-ins will help identify patterns and early signs of burnout.";
    }

    return "Thank you for sharing that with me. I'm here to support you. Would you like to explore relaxation techniques, task management, or sleep habits?";
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.lightBackground,
      appBar: AppBar(
        backgroundColor: AppColors.white,
        elevation: 1,
        titleSpacing: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_rounded, color: AppColors.darkGreen),
          onPressed: () => Navigator.pop(context),
        ),
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: const [
            Text(
              'AI Wellness Assistant ✦',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: AppColors.darkGreen,
              ),
            ),
            Text(
              'Your supportive conversation companion',
              style: TextStyle(
                fontSize: 11,
                color: AppColors.secondaryText,
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: _startNewChat,
            child: const Text(
              '+ New',
              style: TextStyle(color: AppColors.primaryGreen, fontWeight: FontWeight.bold, fontSize: 13),
            ),
          ),
          IconButton(
            tooltip: 'Clear Chat',
            icon: const Icon(Icons.delete_outline_rounded, color: AppColors.secondaryText, size: 20),
            onPressed: _clearChat,
          ),
        ],
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(32),
          child: Container(
            width: double.infinity,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
            color: AppColors.mint,
            child: Text(
              '🌿 Context: Latest Check-in ${widget.initialStressLevel}',
              style: const TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.w600,
                color: AppColors.darkGreen,
              ),
            ),
          ),
        ),
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Chat message list
            Expanded(
              child: ListView.builder(
                controller: _scrollController,
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                itemCount: _messages.length,
                itemBuilder: (context, index) {
                  return _buildMessageItem(_messages[index]);
                },
              ),
            ),

            // AI thinking indicator
            if (_isSending)
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 6),
                alignment: Alignment.centerLeft,
                child: Row(
                  children: const [
                    SizedBox(
                      width: 14,
                      height: 14,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        valueColor: AlwaysStoppedAnimation<Color>(AppColors.aiPurple),
                      ),
                    ),
                    SizedBox(width: 8),
                    Text(
                      'AI is thinking...',
                      style: TextStyle(
                        fontSize: 12,
                        fontStyle: FontStyle.italic,
                        color: AppColors.aiPurple,
                      ),
                    ),
                  ],
                ),
              ),

            // 5 Quick Prompts horizontal bar
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              child: Row(
                children: [
                  _buildQuickPromptChip(
                    '🧘 Help me relax',
                    'Can you guide me through a calming breathing exercise to help me relax?',
                  ),
                  _buildQuickPromptChip(
                    "😟 I'm feeling stressed",
                    "I'm feeling quite stressed and overwhelmed today. What can I do right now to feel better?",
                  ),
                  _buildQuickPromptChip(
                    '📋 Help me plan my day',
                    'Help me structure my day and break down my tasks into manageable, low-stress steps.',
                  ),
                  _buildQuickPromptChip(
                    '🌙 Improve my sleep',
                    'What are some relaxing techniques to help me unwind and sleep better tonight?',
                  ),
                  _buildQuickPromptChip(
                    '📊 Tell me about my recent check-ins',
                    'Tell me about my recent check-ins and how my stress has been tracking.',
                    isPurple: true,
                  ),
                ],
              ),
            ),

            // Non-diagnostic Safety Disclaimer Footer
            Container(
              width: double.infinity,
              color: AppColors.cardBackground,
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 5),
              child: const Text(
                'ℹ️ AI responses provide supportive wellness guidance and do not diagnose medical conditions or prescribe medication.',
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 10, color: AppColors.secondaryText),
              ),
            ),

            // Input Bar
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              decoration: const BoxDecoration(
                color: AppColors.white,
                boxShadow: [
                  BoxShadow(
                    color: Colors.black12,
                    blurRadius: 4,
                    offset: Offset(0, -1),
                  ),
                ],
              ),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Expanded(
                    child: TextField(
                      controller: _textController,
                      minLines: 1,
                      maxLines: 4,
                      textCapitalization: TextCapitalization.sentences,
                      decoration: InputDecoration(
                        hintText: 'Type a message...',
                        hintStyle: const TextStyle(color: AppColors.secondaryText, fontSize: 14),
                        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                        filled: true,
                        fillColor: AppColors.lightBackground,
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(20),
                          borderSide: BorderSide.none,
                        ),
                      ),
                      onSubmitted: (_) => _sendMessage(),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Container(
                    width: 46,
                    height: 46,
                    decoration: BoxDecoration(
                      color: _isSending ? AppColors.secondaryText : AppColors.primaryGreen,
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: IconButton(
                      icon: const Text(
                        '➤',
                        style: TextStyle(color: Colors.white, fontSize: 18),
                      ),
                      onPressed: _isSending ? null : () => _sendMessage(),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildQuickPromptChip(String label, String messageToSend, {bool isPurple = false}) {
    final strokeColor = isPurple ? AppColors.aiPurple : AppColors.primaryGreen;
    final textColor = isPurple ? const Color(0xFF6D28D9) : AppColors.darkGreen;

    return Padding(
      padding: const EdgeInsets.only(right: 6),
      child: OutlinedButton(
        style: OutlinedButton.styleFrom(
          side: BorderSide(color: strokeColor, width: 1.2),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
        ),
        onPressed: () {
          _sendMessage(messageToSend);
        },
        child: Text(
          label,
          style: TextStyle(fontSize: 12, color: textColor, fontWeight: FontWeight.w600),
        ),
      ),
    );
  }

  Widget _buildMessageItem(ChatMessage msg) {
    final isUser = msg.sender == 'USER';
    final timeStr = "${msg.timestamp.hour % 12 == 0 ? 12 : msg.timestamp.hour % 12}:${msg.timestamp.minute.toString().padLeft(2, '0')} ${msg.timestamp.hour >= 12 ? 'PM' : 'AM'}";

    if (isUser) {
      return Align(
        alignment: Alignment.centerRight,
        child: Container(
          margin: const EdgeInsets.only(left: 48, bottom: 10),
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          decoration: const BoxDecoration(
            color: AppColors.primaryGreen,
            borderRadius: BorderRadius.only(
              topLeft: Radius.circular(18),
              topRight: Radius.circular(18),
              bottomLeft: Radius.circular(18),
              bottomRight: Radius.circular(4),
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                msg.text,
                style: const TextStyle(color: Colors.white, fontSize: 14, height: 1.3),
              ),
              const SizedBox(height: 4),
              Text(
                timeStr,
                style: const TextStyle(color: Color(0xFFD1FAE5), fontSize: 10),
              ),
            ],
          ),
        ),
      );
    } else {
      return Align(
        alignment: Alignment.centerLeft,
        child: Container(
          margin: const EdgeInsets.only(right: 48, bottom: 10),
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          decoration: BoxDecoration(
            color: AppColors.white,
            borderRadius: const BorderRadius.only(
              topLeft: Radius.circular(18),
              topRight: Radius.circular(18),
              bottomRight: Radius.circular(18),
              bottomLeft: Radius.circular(4),
            ),
            border: Border.all(color: AppColors.cardBorder, width: 1.0),
            boxShadow: const [
              BoxShadow(
                color: Colors.black12,
                blurRadius: 2,
                offset: Offset(0, 1),
              ),
            ],
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'AI Wellness Assistant ✦',
                style: TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.bold,
                  color: AppColors.aiPurple,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                msg.text,
                style: const TextStyle(color: AppColors.mainText, fontSize: 14, height: 1.35),
              ),
              if (msg.isCrisis) ...[
                const SizedBox(height: 8),
                ElevatedButton.icon(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.high,
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  ),
                  icon: const Icon(Icons.phone_in_talk, size: 16, color: Colors.white),
                  label: const Text('Call Emergency Helpline (988 / 112)', style: TextStyle(color: Colors.white, fontSize: 12)),
                  onPressed: () {
                    // Trigger emergency call intent or dialog
                  },
                ),
              ],
              const SizedBox(height: 4),
              Text(
                timeStr,
                style: const TextStyle(color: AppColors.secondaryText, fontSize: 10),
              ),
            ],
          ),
        ),
      );
    }
  }
}

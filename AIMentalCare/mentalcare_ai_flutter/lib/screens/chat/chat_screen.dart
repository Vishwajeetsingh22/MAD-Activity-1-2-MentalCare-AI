import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../providers/auth_provider.dart';
import '../../providers/chat_provider.dart';
import '../../providers/checkin_provider.dart';

class ChatScreen extends StatefulWidget {
  const ChatScreen({super.key});

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final _messageController = TextEditingController();
  final _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final auth = Provider.of<AuthProvider>(context, listen: false);
      Provider.of<ChatProvider>(context, listen: false).loadMessages(auth.userEmail);
    });
  }

  void _sendMessage([String? promptText]) async {
    final text = promptText ?? _messageController.text;
    if (text.trim().isEmpty) return;

    if (promptText == null) {
      _messageController.clear();
    }

    final auth = Provider.of<AuthProvider>(context, listen: false);
    final checkIn = Provider.of<CheckInProvider>(context, listen: false).todayCheckIn;
    final chatProv = Provider.of<ChatProvider>(context, listen: false);

    await chatProv.sendMessage(
      text: text,
      userId: auth.userEmail,
      currentStressLevel: checkIn?.stressLevel,
    );

    _scrollToBottom();
  }

  void _scrollToBottom() {
    if (_scrollController.hasClients) {
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent + 100,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeOut,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final chatProv = Provider.of<ChatProvider>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('AI Wellness Companion ✦', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 17)),
        backgroundColor: AppColors.deepForest,
        foregroundColor: Colors.white,
        actions: [
          TextButton(
            onPressed: () {
              final auth = Provider.of<AuthProvider>(context, listen: false);
              chatProv.startNewConversation(auth.userEmail);
            },
            child: const Text('+ New', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
          ),
          IconButton(
            icon: const Icon(Icons.delete_outline),
            onPressed: () {
              final auth = Provider.of<AuthProvider>(context, listen: false);
              chatProv.clearHistory(auth.userEmail);
            },
          ),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Chat Message List
            Expanded(
              child: ListView.builder(
                controller: _scrollController,
                padding: const EdgeInsets.all(16),
                itemCount: chatProv.messages.length,
                itemBuilder: (context, index) {
                  final msg = chatProv.messages[index];
                  final isUser = msg.sender == 'USER';

                  return Align(
                    alignment: isUser ? Alignment.centerRight : Alignment.centerLeft,
                    child: Container(
                      margin: const EdgeInsets.only(bottom: 12),
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                      constraints: BoxConstraints(maxWidth: MediaQuery.of(context).size.width * 0.78),
                      decoration: BoxDecoration(
                        color: isUser ? AppColors.primaryGreen : Theme.of(context).cardColor,
                        borderRadius: BorderRadius.only(
                          topLeft: const Radius.circular(18),
                          topRight: const Radius.circular(18),
                          bottomLeft: Radius.circular(isUser ? 18 : 4),
                          bottomRight: Radius.circular(isUser ? 4 : 18),
                        ),
                        border: isUser ? null : Border.all(color: AppColors.cardBorderLight),
                      ),
                      child: Text(
                        msg.messageText,
                        style: TextStyle(
                          color: isUser ? Colors.white : AppColors.textMainLight,
                          fontSize: 14,
                        ),
                      ),
                    ),
                  );
                },
              ),
            ),

            if (chatProv.isThinking)
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                child: Row(
                  children: [
                    SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2, color: AppColors.aiPurple)),
                    SizedBox(width: 8),
                    Text('AI is thinking...', style: TextStyle(fontStyle: FontStyle.italic, color: AppColors.aiPurple, fontSize: 12)),
                  ],
                ),
              ),

            // Prompt Chips
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              child: Row(
                children: [
                  ActionChip(
                    label: const Text('🧘 Help me relax'),
                    onPressed: () => _sendMessage('Can you guide me through a calming breathing exercise?'),
                  ),
                  const SizedBox(width: 6),
                  ActionChip(
                    label: const Text('😟 I\'m feeling stressed'),
                    onPressed: () => _sendMessage('I am feeling stressed today. What can I do right now?'),
                  ),
                  const SizedBox(width: 6),
                  ActionChip(
                    label: const Text('🌙 Improve my sleep'),
                    onPressed: () => _sendMessage('What are some techniques to help me sleep better tonight?'),
                  ),
                ],
              ),
            ),

            // Input Bar
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Theme.of(context).cardColor,
                boxShadow: [
                  BoxShadow(color: Colors.black.withValues(alpha: 0.05), blurRadius: 4, offset: const Offset(0, -2)),
                ],
              ),
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _messageController,
                      decoration: const InputDecoration(
                        hintText: 'Type a message...',
                        contentPadding: EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                      ),
                      maxLines: 3,
                      minLines: 1,
                    ),
                  ),
                  const SizedBox(width: 8),
                  IconButton.filled(
                    onPressed: () => _sendMessage(),
                    style: IconButton.styleFrom(backgroundColor: AppColors.aiPurple),
                    icon: const Icon(Icons.send_rounded, color: Colors.white),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

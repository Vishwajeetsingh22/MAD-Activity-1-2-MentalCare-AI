class ChatMessageModel {
  final int? id;
  final String userId;
  final String conversationId;
  final String sender; // 'USER' or 'AI'
  final String messageText;
  final bool isCrisis;
  final DateTime timestamp;

  ChatMessageModel({
    this.id,
    required this.userId,
    required this.conversationId,
    required this.sender,
    required this.messageText,
    this.isCrisis = false,
    required this.timestamp,
  });

  Map<String, dynamic> toMap() {
    return {
      if (id != null) 'id': id,
      'user_id': userId,
      'conversation_id': conversationId,
      'sender': sender,
      'message_text': messageText,
      'is_crisis': isCrisis ? 1 : 0,
      'timestamp': timestamp.millisecondsSinceEpoch,
    };
  }

  factory ChatMessageModel.fromMap(Map<String, dynamic> map) {
    return ChatMessageModel(
      id: map['id'],
      userId: map['user_id'] ?? '',
      conversationId: map['conversation_id'] ?? '',
      sender: map['sender'] ?? 'USER',
      messageText: map['message_text'] ?? '',
      isCrisis: (map['is_crisis'] ?? 0) == 1,
      timestamp: DateTime.fromMillisecondsSinceEpoch(map['timestamp'] ?? DateTime.now().millisecondsSinceEpoch),
    );
  }
}

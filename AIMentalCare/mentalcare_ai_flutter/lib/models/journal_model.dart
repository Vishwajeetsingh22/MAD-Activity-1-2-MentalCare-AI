class JournalModel {
  final int? id;
  final String userId;
  final String title;
  final String content;
  final String moodEmoji;
  final DateTime createdAt;

  JournalModel({
    this.id,
    required this.userId,
    required this.title,
    required this.content,
    required this.moodEmoji,
    required this.createdAt,
  });

  Map<String, dynamic> toMap() {
    return {
      if (id != null) 'id': id,
      'user_id': userId,
      'title': title,
      'content': content,
      'mood_emoji': moodEmoji,
      'created_at': createdAt.millisecondsSinceEpoch,
    };
  }

  factory JournalModel.fromMap(Map<String, dynamic> map) {
    return JournalModel(
      id: map['id'],
      userId: map['user_id'] ?? '',
      title: map['title'] ?? '',
      content: map['content'] ?? '',
      moodEmoji: map['mood_emoji'] ?? '😊',
      createdAt: DateTime.fromMillisecondsSinceEpoch(map['created_at'] ?? DateTime.now().millisecondsSinceEpoch),
    );
  }
}

class ChatResult {
  final String reply;
  final bool isCrisis;

  ChatResult({required this.reply, required this.isCrisis});
}

class ConversationMemory {
  final String conversationId;
  int turns = 0;
  bool stressStated = false;
  final List<String> causes = [];
  final List<String> subjects = [];
  bool? prepared;
  bool sleepIssue = false;
  bool sleepRecurring = false;
  final Set<String> topics = {};
  String? lastIntent;

  ConversationMemory(this.conversationId);
}

class LocalChatEngine {
  static final Map<String, ConversationMemory> _memoryStore = {};

  static const List<String> crisisKeywords = [
    'suicide', 'kill myself', 'end my life', 'harm myself', 'giving up completely',
    'can\'t go on', 'want to die', 'better off dead', 'extreme crisis', 'no reason to live'
  ];

  static const List<String> medicalKeywords = [
    'diagnose', 'depression', 'bipolar', 'prescribe', 'medication', 'medicine', 'pills', 'adhd', 'schizophrenia'
  ];

  static ConversationMemory _getMemory(String conversationId) {
    return _memoryStore.putIfAbsent(conversationId, () => ConversationMemory(conversationId));
  }

  static ChatResult respond({
    required String message,
    String conversationId = 'default_session',
    String? currentStressLevel,
  }) {
    final lower = message.toLowerCase().trim();
    final mem = _getMemory(conversationId);
    mem.turns++;

    if (lower.isEmpty) {
      return ChatResult(
        reply: "I'm here whenever you'd like to share how you're feeling.",
        isCrisis: false,
      );
    }

    // 1. Crisis Detection
    if (crisisKeywords.any((k) => lower.contains(k))) {
      return ChatResult(
        reply: "I am deeply concerned about what you are going through right now. Please remember that you do not have to carry this alone and immediate help is available. Please reach out to your emergency contact right now, or contact the 988 Suicide & Crisis Lifeline by calling or texting 988 (free, confidential, 24/7). If you are in immediate danger, please contact local emergency services immediately.",
        isCrisis: true,
      );
    }

    // 2. Medical Query Boundary
    if (medicalKeywords.any((k) => lower.contains(k))) {
      return ChatResult(
        reply: "As an AI wellness assistant, I cannot provide medical diagnoses, evaluate clinical psychiatric disorders, or recommend prescription medications. MentalCare AI is strictly an assistive wellness tool. If you are experiencing symptoms of illness, depression, or severe anxiety, I strongly encourage you to speak with a licensed physician or mental health professional.",
        isCrisis: false,
      );
    }

    // Extract Entities & Intent
    if (['stressed', 'stress', 'overwhelmed', 'anxious', 'anxiety', 'pressure'].any((k) => lower.contains(k))) {
      mem.stressStated = true;
      mem.topics.add('stress');
    }

    if (['exam', 'exams', 'test', 'finals', 'tomorrow'].any((k) => lower.contains(k))) {
      if (!mem.causes.contains('exams tomorrow')) {
        mem.causes.add('exams tomorrow');
      }
      mem.topics.add('exams');
    }

    if (['mathematics', 'maths', 'math', 'calculus', 'algebra'].any((k) => lower.contains(k))) {
      if (!mem.subjects.contains('mathematics')) {
        mem.subjects.add('mathematics');
      }
      mem.topics.add('mathematics');
    }

    if (['haven\'t studied', 'didn\'t study', 'not prepared', 'not ready'].any((k) => lower.contains(k))) {
      mem.prepared = false;
    }

    if (['sleep', 'sleeping', 'insomnia', 'can\'t sleep', 'trouble sleeping'].any((k) => lower.contains(k))) {
      mem.sleepIssue = true;
      mem.topics.add('sleep');
    }

    // Memory Recall Queries
    if (['what did i tell you', 'what did i say', 'do you remember', 'recall what i said'].any((k) => lower.contains(k))) {
      if (lower.contains('exam') || lower.contains('exams')) {
        final reply = (mem.subjects.contains('mathematics') || mem.prepared == false)
            ? "You shared that your exams are tomorrow, and you were feeling particularly worried and stressed because you haven't had a chance to study mathematics yet."
            : "You mentioned earlier that your exams are tomorrow and that they have been causing you stress.";
        return ChatResult(reply: reply, isCrisis: false);
      }
      if (lower.contains('sleep')) {
        return ChatResult(
          reply: "You shared that you have also been having trouble sleeping amidst the exam stress.",
          isCrisis: false,
        );
      }
    }

    // Explicit Study Plan Request
    if (['study plan', 'help me study', 'study schedule', 'make a study plan'].any((k) => lower.contains(k))) {
      final subject = mem.subjects.isNotEmpty ? mem.subjects.first : 'your upcoming exam';
      final reply = "Let's build a focused, stress-reducing emergency study plan for $subject tomorrow:\n\n"
          "• Step 1: Formula Triage (40 mins): Write out top essential formulas.\n"
          "• Step 2: High-Yield Practice (50 mins): Work through 3-4 solved problems.\n"
          "• Step 3: Mandatory 10-min Break: Step away & stretch.\n"
          "• Step 4: Sleep Buffer: Stop studying 45 minutes before bed.\n\n"
          "Which specific chapter or unit carries the highest marks weighting?";
      return ChatResult(reply: reply, isCrisis: false);
    }

    // Default Dynamic Response
    final topicsSummary = mem.topics.join(', ');
    final reply = topicsSummary.isNotEmpty
        ? "I hear you. In our conversation, you've mentioned dealing with $topicsSummary. Giving words to your thoughts is an important step in caring for your mental wellbeing. How are you feeling right now as we talk through this?"
        : "I'm listening and I'm here to support you. You can talk freely about whatever is on your mind—whether it's exam deadlines, work pressure, sleep difficulties, or just wanting a mindful pause. How can I best support you right now?";

    return ChatResult(reply: reply, isCrisis: false);
  }

  static void clearSession(String conversationId) {
    _memoryStore.remove(conversationId);
  }
}

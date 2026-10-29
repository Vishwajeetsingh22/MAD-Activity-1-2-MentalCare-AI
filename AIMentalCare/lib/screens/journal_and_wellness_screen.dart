import 'dart:async';
import 'package:flutter/material.dart';
import '../core/theme/app_colors.dart';
import '../core/theme/app_radius.dart';
import '../core/theme/app_spacing.dart';
import '../core/widgets/primary_button.dart';

/// Phase 6 & Final Play Store UI — Journal, Habits, Goals, Breathing & Sleep Tracking
class JournalAndWellnessScreen extends StatefulWidget {
  final int initialTabIndex;

  const JournalAndWellnessScreen({
    Key? key,
    this.initialTabIndex = 0,
  }) : super(key: key);

  @override
  State<JournalAndWellnessScreen> createState() => _JournalAndWellnessScreenState();
}

class _JournalAndWellnessScreenState extends State<JournalAndWellnessScreen>
    with TickerProviderStateMixin {
  late TabController _tabController;

  // --- Journal State ---
  final TextEditingController _journalController = TextEditingController();
  String _selectedMood = '🙂 Good';
  final List<String> _moods = ['😊 Great', '🙂 Good', '😐 Okay', '😟 Low', '😣 Difficult'];

  final List<Map<String, dynamic>> _journalEntries = [
    {
      'id': 'j_1',
      'title': 'Morning stillness & deep focus',
      'content': 'Felt clearer after 7 hours of rest. Pacing my study sessions evenly.',
      'mood': '🙂 Good',
      'timestamp': 'Today, 9:15 AM',
    },
    {
      'id': 'j_2',
      'title': 'Evening screen wind-down',
      'content': 'Need to disconnect earlier tonight to restore mental focus.',
      'mood': '😟 Low',
      'timestamp': 'Yesterday, 10:40 PM',
    },
  ];

  // --- Goals State ---
  final List<Map<String, dynamic>> _goals = [
    {'title': 'Sleep better', 'progress': 0.75, 'label': '75%'},
    {'title': 'Reduce stress', 'progress': 0.50, 'label': '50%'},
    {'title': 'Build healthy habits', 'progress': 0.85, 'label': '85%'},
  ];

  // --- Habits State ---
  final List<Map<String, dynamic>> _habits = [
    {'title': 'Morning breathing', 'subtitle': '5-minute box breathing', 'isCompleted': true},
    {'title': 'Drink water', 'subtitle': 'Hydrate & short pause', 'isCompleted': false},
    {'title': 'Sleep routine', 'subtitle': 'Screen wind-down', 'isCompleted': true},
    {'title': 'Daily check-in', 'subtitle': 'Track stress & mood', 'isCompleted': true},
  ];

  // --- 5-Minute Breathing State (Section 12) ---
  bool _isBreathingActive = false;
  int _breathingSecondsLeft = 300; // 5 mins
  Timer? _breathingTimer;
  String _breathingPhase = 'Inhale';
  int _breathingPhaseCountdown = 4;
  late AnimationController _breathingAnimController;

  // --- Sleep & Activity State (Section 13) ---
  final TextEditingController _sleepController = TextEditingController(text: '6.5');
  final TextEditingController _workController = TextEditingController(text: '8');
  final TextEditingController _screenController = TextEditingController(text: '6');
  final TextEditingController _exerciseController = TextEditingController(text: '30');
  String? _lifestyleScore;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(
      length: 4,
      vsync: this,
      initialIndex: widget.initialTabIndex,
    );

    _breathingAnimController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 4),
    );
  }

  @override
  void dispose() {
    _tabController.dispose();
    _journalController.dispose();
    _sleepController.dispose();
    _workController.dispose();
    _screenController.dispose();
    _exerciseController.dispose();
    _breathingTimer?.cancel();
    _breathingAnimController.dispose();
    super.dispose();
  }

  void _saveJournalEntry() {
    final text = _journalController.text.trim();
    if (text.isEmpty) return;

    final firstLine = text.split('\n').first;
    final snippet = firstLine.length > 36 ? '${firstLine.substring(0, 33)}...' : firstLine;

    setState(() {
      _journalEntries.insert(0, {
        'id': 'j_${DateTime.now().millisecondsSinceEpoch}',
        'title': snippet,
        'content': text,
        'mood': _selectedMood,
        'timestamp': 'Just now',
      });
      _journalController.clear();
    });

    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Journal entry saved ✨')),
    );
  }

  void _toggleBreathing() {
    if (_isBreathingActive) {
      _breathingTimer?.cancel();
      _breathingAnimController.stop();
      setState(() {
        _isBreathingActive = false;
        _breathingSecondsLeft = 300;
        _breathingPhase = 'Inhale';
      });
    } else {
      setState(() {
        _isBreathingActive = true;
        _breathingSecondsLeft = 300;
        _breathingPhase = 'Breathe in';
        _breathingPhaseCountdown = 4;
      });
      _breathingAnimController.repeat(reverse: true);

      _breathingTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
        if (_breathingSecondsLeft <= 1) {
          timer.cancel();
          _breathingAnimController.stop();
          setState(() {
            _isBreathingActive = false;
            _breathingSecondsLeft = 300;
          });
          return;
        }

        setState(() {
          _breathingSecondsLeft--;
          _breathingPhaseCountdown--;
          if (_breathingPhaseCountdown == 0) {
            _breathingPhaseCountdown = 4;
            _breathingPhase = _breathingPhase == 'Breathe in' ? 'Breathe out' : 'Breathe in';
          }
        });
      });
    }
  }

  String _formatTimer(int totalSecs) {
    final m = (totalSecs ~/ 60).toString().padLeft(2, '0');
    final s = (totalSecs % 60).toString().padLeft(2, '0');
    return '$m:$s';
  }

  void _calculateLifestyleInsights() {
    final sleep = double.tryParse(_sleepController.text.trim()) ?? 7.0;
    final work = double.tryParse(_workController.text.trim()) ?? 8.0;
    final screen = double.tryParse(_screenController.text.trim()) ?? 6.0;
    final exercise = double.tryParse(_exerciseController.text.trim()) ?? 30.0;

    // Multi-factor stress formula:
    // Low sleep (<7), high work (>8), high screen (>6), low exercise (<30)
    int score = 40;
    if (sleep < 6) score += 20;
    if (sleep > 8) score -= 10;
    if (work > 8) score += 15;
    if (screen > 6) score += 15;
    if (exercise >= 30) score -= 15;
    score = score.clamp(15, 90);

    setState(() {
      _lifestyleScore = 'Calculated Stress Index: $score%';
    });
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final mainText = isDark ? AppColors.darkMainText : AppColors.mainText;
    final subText = isDark ? AppColors.darkSecondaryText : AppColors.secondaryText;
    final cardBg = isDark ? AppColors.darkSurface : AppColors.white;
    final borderColor = isDark ? AppColors.darkCardBorder : AppColors.cardBorder;

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: Text(
          'Wellness & Habits',
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: isDark ? AppColors.white : AppColors.darkGreen,
          ),
        ),
        bottom: TabBar(
          controller: _tabController,
          indicatorColor: AppColors.primaryGreen,
          labelColor: isDark ? AppColors.white : AppColors.darkGreen,
          unselectedLabelColor: subText,
          labelStyle: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
          tabs: const [
            Tab(text: 'Journal'),
            Tab(text: 'Goals'),
            Tab(text: 'Breathe'),
            Tab(text: 'Sleep'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildJournalView(isDark, cardBg, borderColor, mainText, subText),
          _buildGoalsAndHabitsView(isDark, cardBg, borderColor, mainText, subText),
          _buildBreathingView(isDark, cardBg, borderColor, mainText, subText),
          _buildSleepActivityView(isDark, cardBg, borderColor, mainText, subText),
        ],
      ),
    );
  }

  // --- TAB 1: JOURNAL WRITING ---
  Widget _buildJournalView(
    bool isDark,
    Color cardBg,
    Color borderColor,
    Color mainText,
    Color subText,
  ) {
    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg, vertical: AppSpacing.md),
      children: [
        Container(
          padding: const EdgeInsets.all(AppSpacing.md),
          decoration: BoxDecoration(
            color: cardBg,
            borderRadius: BorderRadius.circular(AppRadius.card),
            border: Border.all(color: borderColor),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'My Journal',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: mainText),
              ),
              const SizedBox(height: 2),
              Text('How are you feeling today?', style: TextStyle(fontSize: 13, color: subText)),
              const SizedBox(height: 10),

              // Mood Selector Chips
              Wrap(
                spacing: 8,
                children: _moods.map((mood) {
                  final isSelected = _selectedMood == mood;
                  return ChoiceChip(
                    label: Text(mood.split(' ').first), // Emoji only for ultra clean look
                    selected: isSelected,
                    selectedColor: AppColors.primaryGreen.withOpacity(0.2),
                    backgroundColor: isDark ? AppColors.darkBackground : AppColors.mint,
                    onSelected: (val) {
                      if (val) setState(() => _selectedMood = mood);
                    },
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(AppRadius.full),
                      side: BorderSide(
                        color: isSelected ? AppColors.primaryGreen : borderColor,
                      ),
                    ),
                  );
                }).toList(),
              ),
              const SizedBox(height: 10),

              // Writing Area
              Container(
                decoration: BoxDecoration(
                  color: isDark ? AppColors.darkBackground : AppColors.backgroundMint.withOpacity(0.4),
                  borderRadius: BorderRadius.circular(AppRadius.md),
                  border: Border.all(color: borderColor),
                ),
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                child: TextField(
                  controller: _journalController,
                  maxLines: 4,
                  style: TextStyle(color: mainText, fontSize: 14),
                  decoration: InputDecoration(
                    hintText: 'Write your thoughts...',
                    hintStyle: TextStyle(color: subText.withOpacity(0.7), fontSize: 13),
                    border: InputBorder.none,
                  ),
                ),
              ),
              const SizedBox(height: 12),

              PrimaryButton(
                text: 'Save Entry',
                onPressed: _saveJournalEntry,
              ),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.md),

        Text(
          'PAST REFLECTIONS',
          style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 0.8, color: subText),
        ),
        const SizedBox(height: 8),
        ..._journalEntries.map((e) {
          return Container(
            margin: const EdgeInsets.only(bottom: 8),
            padding: const EdgeInsets.all(AppSpacing.md),
            decoration: BoxDecoration(
              color: cardBg,
              borderRadius: BorderRadius.circular(AppRadius.card),
              border: Border.all(color: borderColor),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(e['title'] as String, style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: mainText)),
                    Text(e['mood'] as String, style: const TextStyle(fontSize: 12)),
                  ],
                ),
                const SizedBox(height: 4),
                Text(e['content'] as String, style: TextStyle(fontSize: 12, color: subText)),
                const SizedBox(height: 6),
                Text(e['timestamp'] as String, style: TextStyle(fontSize: 10, color: subText.withOpacity(0.7))),
              ],
            ),
          );
        }).toList(),
      ],
    );
  }

  // --- TAB 2: GOALS & HABITS ---
  Widget _buildGoalsAndHabitsView(
    bool isDark,
    Color cardBg,
    Color borderColor,
    Color mainText,
    Color subText,
  ) {
    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg, vertical: AppSpacing.md),
      children: [
        Text(
          'MY WELLNESS GOALS',
          style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 0.8, color: subText),
        ),
        const SizedBox(height: 8),
        ..._goals.map((g) {
          return Container(
            margin: const EdgeInsets.only(bottom: 8),
            padding: const EdgeInsets.all(AppSpacing.md),
            decoration: BoxDecoration(
              color: cardBg,
              borderRadius: BorderRadius.circular(AppRadius.card),
              border: Border.all(color: borderColor),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(g['title'] as String, style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: mainText)),
                    Text(g['label'] as String, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.bold, color: AppColors.primaryGreen)),
                  ],
                ),
                const SizedBox(height: 8),
                ClipRRect(
                  borderRadius: BorderRadius.circular(4),
                  child: LinearProgressIndicator(
                    value: g['progress'] as double,
                    minHeight: 6,
                    backgroundColor: isDark ? AppColors.darkBackground : AppColors.cardBorder,
                    valueColor: const AlwaysStoppedAnimation<Color>(AppColors.primaryGreen),
                  ),
                ),
              ],
            ),
          );
        }).toList(),
        const SizedBox(height: AppSpacing.md),

        Text(
          'DAILY HABITS',
          style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 0.8, color: subText),
        ),
        const SizedBox(height: 8),
        ..._habits.map((h) {
          final isDone = h['isCompleted'] as bool;
          return Container(
            margin: const EdgeInsets.only(bottom: 8),
            padding: const EdgeInsets.symmetric(horizontal: AppSpacing.md, vertical: 12),
            decoration: BoxDecoration(
              color: cardBg,
              borderRadius: BorderRadius.circular(AppRadius.card),
              border: Border.all(color: borderColor),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(h['title'] as String, style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: mainText)),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: isDone ? AppColors.lightMint : (isDark ? AppColors.darkBackground : Colors.grey.shade100),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    isDone ? '✓ Completed' : '○ Pending',
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      color: isDone ? AppColors.darkGreen : subText,
                    ),
                  ),
                ),
              ],
            ),
          );
        }).toList(),
      ],
    );
  }

  // --- TAB 3: BREATHING / RELAXATION (Section 12) ---
  Widget _buildBreathingView(
    bool isDark,
    Color cardBg,
    Color borderColor,
    Color mainText,
    Color subText,
  ) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.lg),
        child: Container(
          padding: const EdgeInsets.all(AppSpacing.xl),
          decoration: BoxDecoration(
            color: cardBg,
            borderRadius: BorderRadius.circular(AppRadius.card),
            border: Border.all(color: borderColor),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                '5-Minute Breathing',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: mainText),
              ),
              const SizedBox(height: 16),

              // Animated Breathing Pulse
              AnimatedBuilder(
                animation: _breathingAnimController,
                builder: (context, child) {
                  final scale = _isBreathingActive ? 0.9 + (_breathingAnimController.value * 0.3) : 1.0;
                  return Transform.scale(
                    scale: scale,
                    child: Container(
                      width: 130,
                      height: 130,
                      decoration: BoxDecoration(
                        color: AppColors.primaryGreen.withOpacity(0.15),
                        shape: BoxShape.circle,
                        border: Border.all(color: AppColors.primaryGreen, width: 2),
                      ),
                      child: Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text(
                              _breathingPhase,
                              style: const TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                                color: AppColors.darkGreen,
                              ),
                            ),
                            if (_isBreathingActive)
                              Text(
                                '${_breathingPhaseCountdown}s',
                                style: const TextStyle(
                                  fontSize: 22,
                                  fontWeight: FontWeight.bold,
                                  color: AppColors.darkGreen,
                                ),
                              ),
                          ],
                        ),
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 16),

              Text(
                _formatTimer(_breathingSecondsLeft),
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                  color: isDark ? AppColors.white : AppColors.darkGreen,
                ),
              ),
              const SizedBox(height: 20),

              ElevatedButton(
                onPressed: _toggleBreathing,
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primaryGreen,
                  foregroundColor: Colors.white,
                  minimumSize: const Size(160, 48),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.button)),
                ),
                child: Text(
                  _isBreathingActive ? 'Pause' : 'Start',
                  style: const TextStyle(fontSize: 14, fontWeight: FontWeight.bold),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // --- TAB 4: SLEEP & ACTIVITY SCREEN (Section 13) ---
  Widget _buildSleepActivityView(
    bool isDark,
    Color cardBg,
    Color borderColor,
    Color mainText,
    Color subText,
  ) {
    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg, vertical: AppSpacing.md),
      children: [
        Text(
          'Sleep & Activity',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: mainText),
        ),
        const SizedBox(height: 2),
        Text(
          'Track habits that may affect your wellbeing.',
          style: TextStyle(fontSize: 13, color: subText),
        ),
        const SizedBox(height: AppSpacing.md),

        Container(
          padding: const EdgeInsets.all(AppSpacing.md),
          decoration: BoxDecoration(
            color: cardBg,
            borderRadius: BorderRadius.circular(AppRadius.card),
            border: Border.all(color: borderColor),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              _buildCompactField(
                label: 'Sleep',
                unit: 'hrs',
                controller: _sleepController,
                icon: Icons.bedtime_rounded,
                isDark: isDark,
                borderColor: borderColor,
                mainText: mainText,
                subText: subText,
              ),
              const SizedBox(height: 12),
              _buildCompactField(
                label: 'Study / Work',
                unit: 'hrs',
                controller: _workController,
                icon: Icons.work_outline_rounded,
                isDark: isDark,
                borderColor: borderColor,
                mainText: mainText,
                subText: subText,
              ),
              const SizedBox(height: 12),
              _buildCompactField(
                label: 'Screen Time',
                unit: 'hrs',
                controller: _screenController,
                icon: Icons.devices_rounded,
                isDark: isDark,
                borderColor: borderColor,
                mainText: mainText,
                subText: subText,
              ),
              const SizedBox(height: 12),
              _buildCompactField(
                label: 'Exercise',
                unit: 'min',
                controller: _exerciseController,
                icon: Icons.fitness_center_rounded,
                isDark: isDark,
                borderColor: borderColor,
                mainText: mainText,
                subText: subText,
              ),
              const SizedBox(height: 18),

              PrimaryButton(
                text: 'Calculate Insights',
                onPressed: _calculateLifestyleInsights,
              ),
            ],
          ),
        ),
        if (_lifestyleScore != null) ...[
          const SizedBox(height: AppSpacing.md),
          Container(
            padding: const EdgeInsets.all(AppSpacing.md),
            decoration: BoxDecoration(
              color: AppColors.primaryGreen.withOpacity(0.12),
              borderRadius: BorderRadius.circular(AppRadius.card),
              border: Border.all(color: AppColors.primaryGreen),
            ),
            child: Row(
              children: [
                const Icon(Icons.insights_rounded, color: AppColors.darkGreen, size: 22),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    _lifestyleScore!,
                    style: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.bold,
                      color: AppColors.darkGreen,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ],
    );
  }

  Widget _buildCompactField({
    required String label,
    required String unit,
    required TextEditingController controller,
    required IconData icon,
    required bool isDark,
    required Color borderColor,
    required Color mainText,
    required Color subText,
  }) {
    return Row(
      children: [
        Icon(icon, size: 20, color: subText),
        const SizedBox(width: 8),
        Expanded(
          child: Text(label, style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: mainText)),
        ),
        SizedBox(
          width: 80,
          height: 40,
          child: TextField(
            controller: controller,
            keyboardType: const TextInputType.numberWithOptions(decimal: true),
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: mainText),
            decoration: InputDecoration(
              suffixText: unit,
              suffixStyle: TextStyle(fontSize: 11, color: subText),
              contentPadding: EdgeInsets.zero,
              filled: true,
              fillColor: isDark ? AppColors.darkBackground : AppColors.backgroundMint.withOpacity(0.4),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(AppRadius.sm),
                borderSide: BorderSide(color: borderColor),
              ),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(AppRadius.sm),
                borderSide: BorderSide(color: borderColor),
              ),
            ),
          ),
        ),
      ],
    );
  }
}

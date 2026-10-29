import 'package:flutter/material.dart';
import '../core/theme/app_colors.dart';
import '../core/theme/app_radius.dart';
import '../core/theme/app_spacing.dart';
import '../core/widgets/primary_button.dart';

/// Phase 3 / Final Play Store UI — Compact Guided Daily Check-in Quiz
class DailyCheckInQuizScreen extends StatefulWidget {
  final String userId;
  final VoidCallback onNavigateToInsights;
  final VoidCallback onNavigateToDashboard;
  final String? userName;

  const DailyCheckInQuizScreen({
    Key? key,
    this.userId = 'user123',
    required this.onNavigateToInsights,
    required this.onNavigateToDashboard,
    this.userName,
  }) : super(key: key);

  @override
  State<DailyCheckInQuizScreen> createState() => _DailyCheckInQuizScreenState();
}

class _DailyCheckInQuizScreenState extends State<DailyCheckInQuizScreen> {
  int _currentStep = 0; // 0 to 4 (5 questions)
  static const int _totalSteps = 5;

  int _selectedMoodIndex = 2; // Default 😐 Okay
  final List<int> _scaleRatings = [3, 3, 3, 3]; // Q2-Q5 ratings

  final TextEditingController _reflectionController = TextEditingController();
  bool _saveToJournal = true;
  bool _isSubmitting = false;

  // Single daily submission per calendar day ($userId_$localDate)
  bool _isAlreadyCompletedToday = false;
  Map<String, dynamic>? _todayResult;

  final List<Map<String, dynamic>> _scaleQuestions = [
    {
      'title': 'What is your current stress level?',
      'options': [
        '🌿 Very Calm / At ease',
        '🍃 Mild / Manageable',
        '😐 Moderate / Noticeable',
        '😟 High / Pressured',
        '🚨 Severe / Overwhelmed',
      ],
      'isNegative': true,
    },
    {
      'title': 'How was your sleep quality last night?',
      'options': [
        '🌙 Poor / Restless (< 5 hrs)',
        '🙁 Broken sleep (5-6 hrs)',
        '😐 Moderate (6-7 hrs)',
        '🙂 Rested & Good (7-8 hrs)',
        '🛌 Deep & Restorative (8+ hrs)',
      ],
      'isNegative': false,
    },
    {
      'title': 'How is your energy and stamina today?',
      'options': [
        '😴 Low / Exhausted',
        '🙁 Sluggish / Low focus',
        '😐 Steady / Moderate',
        '🙂 Good / Active',
        '⚡ High / Vibrant',
      ],
      'isNegative': false,
    },
    {
      'title': 'How often have you noticed overthinking?',
      'options': [
        '🎯 Clear mind / Focused',
        '🙂 Occasional distraction',
        '😐 Moderate mental chatter',
        '😟 Frequent overthinking',
        '🌪️ Racing thought loops',
      ],
      'isNegative': true,
    },
  ];

  final List<Map<String, String>> _moodOptions = [
    {'emoji': '😊', 'label': 'Great'},
    {'emoji': '🙂', 'label': 'Good'},
    {'emoji': '😐', 'label': 'Okay'},
    {'emoji': '😟', 'label': 'Low'},
    {'emoji': '😣', 'label': 'Difficult'},
  ];

  @override
  void dispose() {
    _reflectionController.dispose();
    super.dispose();
  }

  void _completeAssessment() {
    if (_isSubmitting) return;

    setState(() {
      _isSubmitting = true;
    });

    final now = DateTime.now();
    final localDate =
        "${now.year.toString().padLeft(4, '0')}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}";
    final recordId = "${widget.userId}_$localDate";

    final moodScores = [15, 28, 48, 72, 85];
    final moodScore = moodScores[_selectedMoodIndex];

    int scaleStressTotal = 0;
    for (int i = 0; i < 4; i++) {
      final score = _scaleRatings[i];
      final isNeg = _scaleQuestions[i]['isNegative'] as bool;
      scaleStressTotal += isNeg ? score : (6 - score);
    }
    final scalePercent = ((scaleStressTotal / 20.0) * 100).toInt();
    final estimatedScore = ((moodScore * 0.4) + (scalePercent * 0.6)).toInt().clamp(10, 95);

    final level = estimatedScore < 40 ? 'LOW' : (estimatedScore < 70 ? 'MODERATE' : 'HIGH');
    final rec = level == 'LOW'
        ? 'Your wellness balance is steady today. Maintain regular hydration and rest.'
        : (level == 'MODERATE'
            ? 'Moderate stress detected. A 5-minute breathing pause will help restore focus.'
            : 'Elevated stress detected. Take a break from screens and rest.');

    setState(() {
      _isSubmitting = false;
      _isAlreadyCompletedToday = true;
      _todayResult = {
        'recordId': recordId,
        'localDate': localDate,
        'stressScore': estimatedScore,
        'stressLevel': level,
        'recommendation': rec,
        'savedToJournal': _saveToJournal && _reflectionController.text.trim().isNotEmpty,
      };
    });
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Icon(
            Icons.arrow_back_rounded,
            color: isDark ? AppColors.white : AppColors.mainText,
          ),
          onPressed: widget.onNavigateToDashboard,
        ),
        title: Text(
          'Daily Check-in',
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: isDark ? AppColors.white : AppColors.mainText,
          ),
        ),
        centerTitle: true,
      ),
      body: SafeArea(
        child: _isAlreadyCompletedToday ? _buildCompletedView(isDark) : _buildQuizView(isDark),
      ),
    );
  }

  Widget _buildQuizView(bool isDark) {
    final cardBg = isDark ? AppColors.darkSurface : AppColors.white;
    final borderColor = isDark ? AppColors.darkCardBorder : AppColors.cardBorder;
    final mainText = isDark ? AppColors.darkMainText : AppColors.mainText;
    final subText = isDark ? AppColors.darkSecondaryText : AppColors.secondaryText;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Step Counter & Sleek Progress Bar
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Question ${_currentStep + 1} of $_totalSteps',
                style: TextStyle(
                  color: AppColors.primaryGreen,
                  fontSize: 13,
                  fontWeight: FontWeight.bold,
                ),
              ),
              Text(
                '${((_currentStep + 1) / _totalSteps * 100).toInt()}%',
                style: TextStyle(color: subText, fontSize: 12),
              ),
            ],
          ),
          const SizedBox(height: 6),
          ClipRRect(
            borderRadius: BorderRadius.circular(4),
            child: LinearProgressIndicator(
              value: (_currentStep + 1) / _totalSteps,
              backgroundColor: isDark ? AppColors.darkSurface : AppColors.cardBorder,
              valueColor: const AlwaysStoppedAnimation<Color>(AppColors.primaryGreen),
              minHeight: 6,
            ),
          ),
          const SizedBox(height: AppSpacing.lg),

          // Main Question Card
          Expanded(
            child: SingleChildScrollView(
              child: Container(
                padding: const EdgeInsets.all(AppSpacing.lg),
                decoration: BoxDecoration(
                  color: cardBg,
                  borderRadius: BorderRadius.circular(AppRadius.card),
                  border: Border.all(color: borderColor),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    if (_currentStep == 0) ...[
                      Text(
                        'How are you feeling today?',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: mainText,
                        ),
                      ),
                      const SizedBox(height: AppSpacing.md),
                      ...List.generate(_moodOptions.length, (index) {
                        final option = _moodOptions[index];
                        final isSelected = _selectedMoodIndex == index;
                        return Padding(
                          padding: const EdgeInsets.only(bottom: 8),
                          child: _buildSelectableOption(
                            text: '${option['emoji']}  ${option['label']}',
                            isSelected: isSelected,
                            isDark: isDark,
                            cardBg: cardBg,
                            borderColor: borderColor,
                            onTap: () {
                              setState(() {
                                _selectedMoodIndex = index;
                              });
                            },
                          ),
                        );
                      }),
                    ] else ...[
                      _buildScaleStep(_currentStep - 1, isDark, cardBg, borderColor, mainText, subText),
                    ],
                  ],
                ),
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.md),

          // Action Buttons
          Row(
            children: [
              if (_currentStep > 0) ...[
                Expanded(
                  flex: 1,
                  child: OutlinedButton(
                    onPressed: () {
                      setState(() {
                        _currentStep--;
                      });
                    },
                    style: OutlinedButton.styleFrom(
                      side: BorderSide(color: borderColor),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(AppRadius.button),
                      ),
                      minimumSize: const Size.fromHeight(48),
                    ),
                    child: Text(
                      'Back',
                      style: TextStyle(color: subText, fontWeight: FontWeight.bold),
                    ),
                  ),
                ),
                const SizedBox(width: AppSpacing.sm),
              ],
              Expanded(
                flex: 2,
                child: PrimaryButton(
                  text: _currentStep == _totalSteps - 1 ? 'Complete Check-in ✨' : 'Continue',
                  isLoading: _isSubmitting,
                  onPressed: () {
                    if (_currentStep < _totalSteps - 1) {
                      setState(() {
                        _currentStep++;
                      });
                    } else {
                      _completeAssessment();
                    }
                  },
                ),
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.md),
        ],
      ),
    );
  }

  Widget _buildScaleStep(
    int qIndex,
    bool isDark,
    Color cardBg,
    Color borderColor,
    Color mainText,
    Color subText,
  ) {
    final q = _scaleQuestions[qIndex];
    final selectedRating = _scaleRatings[qIndex];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          q['title'] as String,
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: mainText,
          ),
        ),
        const SizedBox(height: AppSpacing.md),
        ...(q['options'] as List<String>).asMap().entries.map((entry) {
          final index = entry.key;
          final optText = entry.value;
          final isSelected = selectedRating == (index + 1);

          return Padding(
            padding: const EdgeInsets.only(bottom: 8),
            child: _buildSelectableOption(
              text: optText,
              isSelected: isSelected,
              isDark: isDark,
              cardBg: cardBg,
              borderColor: borderColor,
              onTap: () {
                setState(() {
                  _scaleRatings[qIndex] = index + 1;
                });
              },
            ),
          );
        }).toList(),
      ],
    );
  }

  Widget _buildSelectableOption({
    required String text,
    required bool isSelected,
    required bool isDark,
    required Color cardBg,
    required Color borderColor,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(AppRadius.md),
      child: Container(
        height: 50,
        padding: const EdgeInsets.symmetric(horizontal: 14),
        decoration: BoxDecoration(
          color: isSelected
              ? (isDark ? AppColors.darkSurface : AppColors.lightMint.withOpacity(0.5))
              : (isDark ? AppColors.darkBackground : AppColors.white),
          borderRadius: BorderRadius.circular(AppRadius.md),
          border: Border.all(
            color: isSelected ? AppColors.primaryGreen : borderColor,
            width: isSelected ? 1.5 : 1.0,
          ),
        ),
        alignment: Alignment.centerLeft,
        child: Row(
          children: [
            Expanded(
              child: Text(
                text,
                style: TextStyle(
                  fontSize: 14,
                  color: isSelected ? AppColors.darkGreen : (isDark ? AppColors.white : AppColors.mainText),
                  fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                ),
              ),
            ),
            if (isSelected)
              const Icon(Icons.check_circle_rounded, color: AppColors.primaryGreen, size: 20),
          ],
        ),
      ),
    );
  }

  Widget _buildCompletedView(bool isDark) {
    final cardBg = isDark ? AppColors.darkSurface : AppColors.white;
    final borderColor = isDark ? AppColors.darkCardBorder : AppColors.cardBorder;
    final mainText = isDark ? AppColors.darkMainText : AppColors.mainText;
    final subText = isDark ? AppColors.darkSecondaryText : AppColors.secondaryText;

    final score = _todayResult?['stressScore'] ?? 38;
    final level = _todayResult?['stressLevel'] ?? 'MODERATE';
    final rec = _todayResult?['recommendation'] ??
        'Your wellness check-in has been saved for today.';

    return SingleChildScrollView(
      padding: const EdgeInsets.all(AppSpacing.lg),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          const SizedBox(height: AppSpacing.lg),
          Container(
            width: 64,
            height: 64,
            decoration: BoxDecoration(
              color: AppColors.primaryGreen.withOpacity(0.15),
              shape: BoxShape.circle,
            ),
            child: const Icon(Icons.check_rounded, color: AppColors.darkGreen, size: 36),
          ),
          const SizedBox(height: AppSpacing.md),
          Text(
            '✓ Check-in complete',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
              color: mainText,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            "You've checked in today.",
            style: TextStyle(fontSize: 14, color: subText),
          ),
          Text(
            'Come back tomorrow.',
            style: TextStyle(fontSize: 13, color: subText),
          ),
          const SizedBox(height: AppSpacing.lg),

          Container(
            padding: const EdgeInsets.all(AppSpacing.lg),
            decoration: BoxDecoration(
              color: cardBg,
              borderRadius: BorderRadius.circular(AppRadius.card),
              border: Border.all(color: borderColor),
            ),
            child: Column(
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      '$level STRESS',
                      style: TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.bold,
                        color: level == 'LOW'
                            ? AppColors.primaryGreen
                            : (level == 'MODERATE' ? AppColors.moderate : AppColors.high),
                      ),
                    ),
                    Text(
                      '$score% Index',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.bold,
                        color: mainText,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Text(
                  rec,
                  style: TextStyle(fontSize: 13, color: subText, height: 1.3),
                  textAlign: TextAlign.start,
                ),
              ],
            ),
          ),
          const SizedBox(height: AppSpacing.xl),

          PrimaryButton(
            text: 'View Insights',
            onPressed: widget.onNavigateToInsights,
          ),
          const SizedBox(height: AppSpacing.sm),
          OutlinedButton(
            onPressed: widget.onNavigateToDashboard,
            style: OutlinedButton.styleFrom(
              side: BorderSide(color: borderColor),
              minimumSize: const Size.fromHeight(48),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(AppRadius.button),
              ),
            ),
            child: Text(
              'Return to Dashboard',
              style: TextStyle(
                color: mainText,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../models/checkin_model.dart';
import '../../providers/auth_provider.dart';
import '../../providers/checkin_provider.dart';
import '../main_shell.dart';

class DailyQuizScreen extends StatefulWidget {
  const DailyQuizScreen({super.key});

  @override
  State<DailyQuizScreen> createState() => _DailyQuizScreenState();
}

class _DailyQuizScreenState extends State<DailyQuizScreen> {
  int _currentStep = 0;
  final int _totalSteps = 5;

  // Selected option indices for Q1 to Q5 (null = not answered yet)
  final List<int?> _answers = [null, null, null, null, null];

  bool _isSubmitting = false;
  bool _isSubmitted = false;
  CheckInModel? _submittedResult;

  final List<Map<String, dynamic>> _questions = const [
    {
      'category': 'OVERALL MOOD',
      'title': '1. How are you feeling overall today?',
      'subtitle': 'Select the emoji that best captures your emotional state right now.',
      'options': [
        '😊 Great / Positive',
        '🙂 Good / Content',
        '😐 Okay / Neutral',
        '😟 Stressed / Overwhelmed',
        '😔 Low / Down'
      ],
    },
    {
      'category': 'STRESS LEVEL',
      'title': '2. What is your current stress level?',
      'subtitle': 'Rate how pressured, anxious, or burdened you feel today.',
      'options': [
        '🌿 1 - Very Calm / At ease',
        '🍃 2 - Mild / Manageable',
        '😐 3 - Moderate / Noticeable',
        '😟 4 - High / Pressured',
        '🚨 5 - Severe / Overwhelmed'
      ],
    },
    {
      'category': 'SLEEP RECOVERY',
      'title': '3. How was your sleep quality last night?',
      'subtitle': 'Restfulness and restorative quality of your rest.',
      'options': [
        '🌙 1 - Poor / Restless (< 5 hrs)',
        '🙁 2 - Broken sleep (5-6 hrs)',
        '😐 3 - Moderate / Fair (6-7 hrs)',
        '🙂 4 - Good / Rested (7-8 hrs)',
        '🛌 5 - Deep & Restorative (8+ hrs)'
      ],
    },
    {
      'category': 'ENERGY & STAMINA',
      'title': '4. How is your physical and mental stamina today?',
      'subtitle': 'Rate your overall alertness, focus, and energy.',
      'options': [
        '😴 1 - Low / Exhausted',
        '🙁 2 - Sluggish / Low focus',
        '😐 3 - Steady / Moderate',
        '🙂 4 - Good / Active',
        '⚡ 5 - High / Vibrant'
      ],
    },
    {
      'category': 'MENTAL FOCUS',
      'title': '5. How often have you noticed racing thoughts or overthinking?',
      'subtitle': 'Repetitive worries, mental loops, or difficulty concentrating.',
      'options': [
        '🎯 1 - Clear mind / Focused',
        '🙂 2 - Occasional distraction',
        '😐 3 - Moderate mental chatter',
        '😟 4 - Frequent overthinking',
        '🌪️ 5 - Paralyzing mental loops'
      ],
    },
  ];

  void _nextStep() {
    if (_answers[_currentStep] == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Please select an answer for question ${_currentStep + 1} before continuing.'),
          backgroundColor: Colors.orange.shade800,
          duration: const Duration(seconds: 2),
        ),
      );
      return;
    }

    if (_currentStep < _totalSteps - 1) {
      setState(() => _currentStep++);
    }
  }

  void _previousStep() {
    if (_currentStep > 0) {
      setState(() => _currentStep--);
    }
  }

  Future<void> _submitCheckIn() async {
    // 1. Validate all 5 questions
    for (int i = 0; i < _totalSteps; i++) {
      if (_answers[i] == null) {
        setState(() => _currentStep = i);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Please select an answer for Question ${i + 1} before submitting.'),
            backgroundColor: Colors.orange.shade800,
            duration: const Duration(seconds: 2),
          ),
        );
        return;
      }
    }

    // 2. Prevent duplicate taps
    if (_isSubmitting) return;

    setState(() => _isSubmitting = true);

    try {
      final auth = Provider.of<AuthProvider>(context, listen: false);
      final checkInProv = Provider.of<CheckInProvider>(context, listen: false);

      // Calculate score based on answers Q2-Q5
      final q2 = _answers[1]!; // Stress (0-4)
      final q3 = 4 - _answers[2]!; // Sleep invert (4-0)
      final q4 = 4 - _answers[3]!; // Energy invert (4-0)
      final q5 = _answers[4]!; // Mental focus (0-4)

      final rawSum = q2 + q3 + q4 + q5; // min 0, max 16
      final score = ((rawSum / 16.0) * 100).round().clamp(5, 98);

      final String level = score < 42
          ? 'LOW'
          : score < 68
              ? 'MODERATE'
              : 'HIGH';

      final String recommendation = level == 'LOW'
          ? 'Great emotional baseline today! Continue your healthy daily routine and restorative habits.'
          : level == 'MODERATE'
              ? 'Moderate stress trajectory observed. Consider taking a 5-minute breathing pause and stepping away from screens.'
              : 'High stress trajectory detected. Prioritize immediate rest, step away from workload demands, and reach out to a trusted contact.';

      final nonNullAnswers = _answers.map((e) => e ?? 0).toList();

      await checkInProv.submitCheckIn(
        userId: auth.userEmail,
        stressScore: score,
        stressLevel: level,
        recommendation: recommendation,
        answers: nonNullAnswers,
      );

      setState(() {
        _isSubmitting = false;
        _isSubmitted = true;
        _submittedResult = checkInProv.todayCheckIn;
      });
    } catch (e) {
      setState(() => _isSubmitting = false);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Submission failed: ${e.toString()}. Please check your connection and retry.'),
          backgroundColor: Colors.red.shade700,
          action: SnackBarAction(
            label: 'Retry',
            textColor: Colors.white,
            onPressed: _submitCheckIn,
          ),
        ),
      );
    }
  }

  void _navigateToInsights() {
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const MainShell(initialIndex: 2)), // Tab 2 = Insights
      (route) => false,
    );
  }

  void _navigateToDashboard() {
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const MainShell(initialIndex: 0)), // Tab 0 = Dashboard
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    final checkInProv = Provider.of<CheckInProvider>(context);

    // If check-in was already completed today prior to opening screen
    if (!_isSubmitted && checkInProv.todayCheckIn != null) {
      _isSubmitted = true;
      _submittedResult = checkInProv.todayCheckIn;
    }

    return Scaffold(
      appBar: AppBar(
        title: Text(_isSubmitted ? 'Check-in Results' : 'Daily Wellness Check-in'),
      ),
      body: SafeArea(
        child: _isSubmitted
            ? _buildSubmittedSuccessView()
            : _buildQuizQuestionFlowView(),
      ),
    );
  }

  // View 1: Step-by-Step Question Flow
  Widget _buildQuizQuestionFlowView() {
    final q = _questions[_currentStep];
    final options = q['options'] as List<String>;
    final progress = (_currentStep + 1) / _totalSteps;

    return Column(
      children: [
        // Progress Header Bar
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
          color: Theme.of(context).cardColor,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    q['category'],
                    style: const TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      color: AppColors.primaryGreen,
                      letterSpacing: 0.8,
                    ),
                  ),
                  Text(
                    'Step ${_currentStep + 1} of $_totalSteps',
                    style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textSecondaryLight,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              ClipRRect(
                borderRadius: BorderRadius.circular(6),
                child: LinearProgressIndicator(
                  value: progress,
                  minHeight: 6,
                  backgroundColor: AppColors.cardBorderLight,
                  valueColor: const AlwaysStoppedAnimation<Color>(AppColors.primaryGreen),
                ),
              ),
            ],
          ),
        ),

        Expanded(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  q['title'],
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: AppColors.textMainLight,
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  q['subtitle'],
                  style: const TextStyle(
                    fontSize: 13,
                    color: AppColors.textSecondaryLight,
                  ),
                ),
                const SizedBox(height: 20),

                // Option Cards
                for (int optIdx = 0; optIdx < options.length; optIdx++) ...[
                  InkWell(
                    onTap: () {
                      setState(() {
                        _answers[_currentStep] = optIdx;
                      });
                    },
                    borderRadius: BorderRadius.circular(16),
                    child: Container(
                      padding: const EdgeInsets.all(16),
                      margin: const EdgeInsets.only(bottom: 12),
                      decoration: BoxDecoration(
                        color: _answers[_currentStep] == optIdx
                            ? AppColors.lightMint
                            : Theme.of(context).cardColor,
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(
                          color: _answers[_currentStep] == optIdx
                              ? AppColors.primaryGreen
                              : AppColors.cardBorderLight,
                          width: _answers[_currentStep] == optIdx ? 2 : 1,
                        ),
                      ),
                      child: Row(
                        children: [
                          Icon(
                            _answers[_currentStep] == optIdx
                                ? Icons.radio_button_checked
                                : Icons.radio_button_unchecked,
                            color: _answers[_currentStep] == optIdx
                                ? AppColors.primaryGreen
                                : AppColors.textSecondaryLight,
                          ),
                          const SizedBox(width: 14),
                          Expanded(
                            child: Text(
                              options[optIdx],
                              style: TextStyle(
                                fontSize: 14,
                                fontWeight: _answers[_currentStep] == optIdx
                                    ? FontWeight.bold
                                    : FontWeight.normal,
                                color: _answers[_currentStep] == optIdx
                                    ? AppColors.darkGreen
                                    : AppColors.textMainLight,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ],
            ),
          ),
        ),

        // Bottom Navigation Bar
        Container(
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            color: Theme.of(context).cardColor,
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.05),
                blurRadius: 6,
                offset: const Offset(0, -2),
              ),
            ],
          ),
          child: Row(
            children: [
              if (_currentStep > 0)
                Expanded(
                  child: OutlinedButton(
                    onPressed: _isSubmitting ? null : _previousStep,
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      side: const BorderSide(color: AppColors.primaryGreen),
                    ),
                    child: const Text('Previous', style: TextStyle(color: AppColors.primaryGreen, fontWeight: FontWeight.bold)),
                  ),
                ),
              if (_currentStep > 0) const SizedBox(width: 12),

              Expanded(
                flex: 2,
                child: _currentStep < _totalSteps - 1
                    ? ElevatedButton(
                        onPressed: _nextStep,
                        style: ElevatedButton.styleFrom(
                          padding: const EdgeInsets.symmetric(vertical: 14),
                        ),
                        child: const Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text('Next Question'),
                            SizedBox(width: 6),
                            Icon(Icons.arrow_forward_rounded, size: 18),
                          ],
                        ),
                      )
                    : ElevatedButton(
                        onPressed: _isSubmitting ? null : _submitCheckIn,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppColors.primaryGreen,
                          padding: const EdgeInsets.symmetric(vertical: 14),
                        ),
                        child: _isSubmitting
                            ? const SizedBox(
                                width: 22,
                                height: 22,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2.5,
                                  valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                                ),
                              )
                            : const Row(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  Icon(Icons.check_circle_outline_rounded, color: Colors.white, size: 20),
                                  SizedBox(width: 8),
                                  Text(
                                    'Submit Check-in',
                                    style: TextStyle(
                                      color: Colors.white,
                                      fontWeight: FontWeight.bold,
                                      fontSize: 16,
                                    ),
                                  ),
                                ],
                              ),
                      ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  // View 2: Post-Submission Success State & Result Card with Working "View Insights" Button
  Widget _buildSubmittedSuccessView() {
    final result = _submittedResult;
    final String level = result?.stressLevel ?? 'NORMAL';
    final int score = result?.stressScore ?? 35;
    final String rec = result?.recommendation ?? 'Great wellness state!';

    final Color badgeColor = level == 'LOW'
        ? AppColors.primaryGreen
        : level == 'MODERATE'
            ? AppColors.stressModerate
            : AppColors.stressHigh;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(20),
      child: Column(
        children: [
          const SizedBox(height: 12),
          const CircleAvatar(
            radius: 36,
            backgroundColor: AppColors.lightMint,
            child: Icon(Icons.check_circle_rounded, size: 52, color: AppColors.primaryGreen),
          ),
          const SizedBox(height: 16),
          const Text(
            '✓ Daily Check-in Completed!',
            style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: AppColors.textMainLight),
          ),
          const SizedBox(height: 6),
          const Text(
            'Your check-in has been saved for today.',
            style: TextStyle(fontSize: 14, color: AppColors.textSecondaryLight),
          ),
          const SizedBox(height: 24),

          // Saved Result Card
          Card(
            child: Padding(
              padding: const EdgeInsets.all(20.0),
              child: Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'TODAY\'S RESULT',
                        style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: AppColors.primaryGreen, letterSpacing: 0.8),
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 5),
                        decoration: BoxDecoration(
                          color: badgeColor.withValues(alpha: 0.15),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(color: badgeColor, width: 1),
                        ),
                        child: Text(
                          '$level • $score%',
                          style: TextStyle(color: badgeColor, fontWeight: FontWeight.bold, fontSize: 13),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  Text(
                    rec,
                    textAlign: TextAlign.center,
                    style: const TextStyle(fontSize: 14, height: 1.4, color: AppColors.textMainLight),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 28),

          // Working "View Insights" Button
          SizedBox(
            width: double.infinity,
            height: 52,
            child: ElevatedButton.icon(
              onPressed: _navigateToInsights,
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primaryGreen,
              ),
              icon: const Icon(Icons.insights_rounded, color: Colors.white),
              label: const Text(
                'View Insights & Trends',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
              ),
            ),
          ),
          const SizedBox(height: 14),

          // Return to Dashboard Button
          SizedBox(
            width: double.infinity,
            height: 50,
            child: OutlinedButton(
              onPressed: _navigateToDashboard,
              style: OutlinedButton.styleFrom(
                side: const BorderSide(color: AppColors.primaryGreen),
              ),
              child: const Text(
                'Return to Dashboard',
                style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold, color: AppColors.primaryGreen),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

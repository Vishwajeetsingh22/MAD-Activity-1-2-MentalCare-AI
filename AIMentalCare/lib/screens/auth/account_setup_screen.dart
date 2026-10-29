import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_spacing.dart';
import '../../core/theme/app_radius.dart';
import '../../core/theme/app_text_styles.dart';
import '../../core/widgets/primary_button.dart';
import '../../services/auth_service.dart';
import '../../main.dart';

/// MentalCare AI - Account Setup & Personalization Screen
///
/// Gates access to the Main Dashboard until the user selects their:
/// 1. Wellness Goals
/// 2. Target Sleep Hours
/// 3. Daily Wellness Quiz Reminder Time
///
/// Saves `accountSetupCompleted = true` so the user never repeats onboarding.
class AccountSetupScreen extends StatefulWidget {
  const AccountSetupScreen({Key? key}) : super(key: key);

  @override
  State<AccountSetupScreen> createState() => _AccountSetupScreenState();
}

class _AccountSetupScreenState extends State<AccountSetupScreen> {
  final List<String> _availableGoals = [
    'Reduce Stress & Burnout',
    'Improve Sleep Quality',
    'Build Daily Habits',
    'Mindfulness & Breathing',
    'Mood Tracking & Journaling',
    'Heart-Rate & Wearable Sync',
  ];

  late final Set<String> _selectedGoals = {
    'Reduce Stress & Burnout',
    'Improve Sleep Quality',
  };

  double _targetSleepHours = 7.5;
  String _selectedReminderTime = '08:30 AM';
  bool _isSaving = false;
  String? _errorMessage;

  final List<String> _reminderTimeOptions = [
    '08:00 AM',
    '08:30 AM',
    '09:00 AM',
    '08:00 PM',
    '09:30 PM',
  ];

  void _toggleGoal(String goal) {
    setState(() {
      if (_selectedGoals.contains(goal)) {
        if (_selectedGoals.length > 1) {
          _selectedGoals.remove(goal);
        }
      } else {
        _selectedGoals.add(goal);
      }
    });
  }

  Future<void> _handleCompleteSetup() async {
    setState(() {
      _isSaving = true;
      _errorMessage = null;
    });

    final authService = AuthService();
    final result = await authService.completeAccountSetup(
      goals: _selectedGoals.toList(),
      targetSleepHours: _targetSleepHours.round(),
      reminderTime: _selectedReminderTime,
    );

    if (!mounted) return;
    setState(() => _isSaving = false);

    if (result.isSuccess) {
      // Setup finished: navigate directly to Main Dashboard and clear auth stack
      Navigator.of(context).pushAndRemoveUntil(
        MaterialPageRoute(builder: (_) => const MentalCareNavigationHost()),
        (route) => false,
      );
    } else {
      setState(() {
        _errorMessage = result.errorMessage ?? "Failed to save profile. Please try again.";
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final bgColor = isDark ? AppColors.darkBackground : AppColors.mint;
    final cardBg = isDark ? AppColors.darkSurface : AppColors.white;
    final borderColor = isDark ? AppColors.darkCardBorder : AppColors.cardBorder;
    final mainText = isDark ? AppColors.darkMainText : AppColors.mainText;
    final subText = isDark ? AppColors.darkSecondaryText : AppColors.secondaryText;

    return Scaffold(
      backgroundColor: bgColor,
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg, vertical: AppSpacing.lg),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Header Badge & Title
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppColors.primaryGreen.withOpacity(0.15),
                      borderRadius: BorderRadius.circular(AppRadius.full),
                    ),
                    child: const Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text('✨', style: TextStyle(fontSize: 12)),
                        SizedBox(width: 4),
                        Text(
                          'Step 3 of 3 • Personalization',
                          style: TextStyle(
                            color: AppColors.darkGreen,
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: AppSpacing.sm),

              Text(
                'Personalize Your Journey',
                style: AppTextStyles.headingMedium.copyWith(
                  color: isDark ? AppColors.darkGreen : AppColors.darkGreen,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 6),
              Text(
                'Help MentalCare AI understand your wellness routine and daily goals.',
                style: AppTextStyles.bodyMedium.copyWith(color: subText),
              ),
              const SizedBox(height: AppSpacing.lg),

              if (_errorMessage != null) ...[
                Container(
                  padding: const EdgeInsets.all(AppSpacing.md),
                  decoration: BoxDecoration(
                    color: AppColors.high.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(AppRadius.md),
                    border: Border.all(color: AppColors.high.withOpacity(0.4)),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.error_outline_rounded, color: AppColors.high, size: 20),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Text(
                          _errorMessage!,
                          style: const TextStyle(color: AppColors.high, fontSize: 13),
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: AppSpacing.md),
              ],

              // Card 1: Wellness Goals Selection
              Container(
                padding: const EdgeInsets.all(AppSpacing.md),
                decoration: BoxDecoration(
                  color: cardBg,
                  borderRadius: BorderRadius.circular(AppRadius.lg),
                  border: Border.all(color: borderColor),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        const Text('🎯', style: TextStyle(fontSize: 18)),
                        const SizedBox(width: 8),
                        Text(
                          'Primary Wellness Goals',
                          style: AppTextStyles.headingSmall.copyWith(
                            color: mainText,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Select what you want to focus on (pick at least one):',
                      style: AppTextStyles.bodySmall.copyWith(color: subText),
                    ),
                    const SizedBox(height: AppSpacing.md),

                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: _availableGoals.map((goal) {
                        final isSelected = _selectedGoals.contains(goal);
                        return FilterChip(
                          label: Text(goal),
                          selected: isSelected,
                          onSelected: (_) => _toggleGoal(goal),
                          selectedColor: AppColors.primaryGreen.withOpacity(0.2),
                          checkmarkColor: AppColors.darkGreen,
                          backgroundColor: isDark ? AppColors.darkBackground : AppColors.mint.withOpacity(0.4),
                          labelStyle: TextStyle(
                            color: isSelected ? AppColors.darkGreen : mainText,
                            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                            fontSize: 13,
                          ),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(AppRadius.full),
                            side: BorderSide(
                              color: isSelected ? AppColors.primaryGreen : borderColor,
                              width: isSelected ? 1.5 : 1.0,
                            ),
                          ),
                        );
                      }).toList(),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.md),

              // Card 2: Target Sleep Hours Slider
              Container(
                padding: const EdgeInsets.all(AppSpacing.md),
                decoration: BoxDecoration(
                  color: cardBg,
                  borderRadius: BorderRadius.circular(AppRadius.lg),
                  border: Border.all(color: borderColor),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Row(
                          children: [
                            const Text('🌙', style: TextStyle(fontSize: 18)),
                            const SizedBox(width: 8),
                            Text(
                              'Target Sleep Duration',
                              style: AppTextStyles.headingSmall.copyWith(
                                color: mainText,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ],
                        ),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                          decoration: BoxDecoration(
                            color: AppColors.primaryGreen.withOpacity(0.15),
                            borderRadius: BorderRadius.circular(AppRadius.sm),
                          ),
                          child: Text(
                            '${_targetSleepHours.toStringAsFixed(1)} hrs',
                            style: const TextStyle(
                              color: AppColors.darkGreen,
                              fontWeight: FontWeight.bold,
                              fontSize: 13,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Recommended for mental recovery: 7 to 9 hours.',
                      style: AppTextStyles.bodySmall.copyWith(color: subText),
                    ),
                    const SizedBox(height: AppSpacing.sm),

                    SliderTheme(
                      data: SliderTheme.of(context).copyWith(
                        activeTrackColor: AppColors.primaryGreen,
                        inactiveTrackColor: isDark ? Colors.grey.shade800 : Colors.grey.shade300,
                        thumbColor: AppColors.darkGreen,
                        overlayColor: AppColors.primaryGreen.withOpacity(0.2),
                      ),
                      child: Slider(
                        value: _targetSleepHours,
                        min: 5.0,
                        max: 10.0,
                        divisions: 10,
                        label: '${_targetSleepHours.toStringAsFixed(1)} hrs',
                        onChanged: (val) {
                          setState(() => _targetSleepHours = val);
                        },
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.md),

              // Card 3: Daily Quiz Reminder Time
              Container(
                padding: const EdgeInsets.all(AppSpacing.md),
                decoration: BoxDecoration(
                  color: cardBg,
                  borderRadius: BorderRadius.circular(AppRadius.lg),
                  border: Border.all(color: borderColor),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        const Text('⏰', style: TextStyle(fontSize: 18)),
                        const SizedBox(width: 8),
                        Text(
                          'Daily Check-in Reminder',
                          style: AppTextStyles.headingSmall.copyWith(
                            color: mainText,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Choose when you prefer to reflect on your stress and mood:',
                      style: AppTextStyles.bodySmall.copyWith(color: subText),
                    ),
                    const SizedBox(height: AppSpacing.md),

                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: _reminderTimeOptions.map((time) {
                        final isSelected = _selectedReminderTime == time;
                        return ChoiceChip(
                          label: Text(time),
                          selected: isSelected,
                          onSelected: (_) {
                            setState(() => _selectedReminderTime = time);
                          },
                          selectedColor: AppColors.primaryGreen.withOpacity(0.2),
                          backgroundColor: isDark ? AppColors.darkBackground : AppColors.mint.withOpacity(0.4),
                          labelStyle: TextStyle(
                            color: isSelected ? AppColors.darkGreen : mainText,
                            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                            fontSize: 13,
                          ),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(AppRadius.md),
                            side: BorderSide(
                              color: isSelected ? AppColors.primaryGreen : borderColor,
                              width: isSelected ? 1.5 : 1.0,
                            ),
                          ),
                        );
                      }).toList(),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.md),

              // Medical Disclaimer Notice
              Container(
                padding: const EdgeInsets.all(AppSpacing.md),
                decoration: BoxDecoration(
                  color: isDark ? AppColors.darkCardBorder.withOpacity(0.2) : Colors.grey.shade100,
                  borderRadius: BorderRadius.circular(AppRadius.md),
                ),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('🌿', style: TextStyle(fontSize: 16)),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'MentalCare AI is an assistive digital wellness companion designed to support stress reduction and healthy habits. It is not a replacement for clinical diagnosis or emergency medical care.',
                        style: TextStyle(
                          fontSize: 11,
                          color: subText,
                          height: 1.4,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.xl),

              // Complete Setup CTA
              PrimaryButton(
                text: 'Complete Setup & Enter Dashboard',
                isLoading: _isSaving,
                onPressed: _handleCompleteSetup,
              ),
              const SizedBox(height: AppSpacing.md),
            ],
          ),
        ),
      ),
    );
  }
}

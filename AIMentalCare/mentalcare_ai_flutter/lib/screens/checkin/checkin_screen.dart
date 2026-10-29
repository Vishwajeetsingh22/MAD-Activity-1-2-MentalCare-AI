import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../providers/checkin_provider.dart';
import '../main_shell.dart';
import 'daily_quiz_screen.dart';

class CheckInScreen extends StatelessWidget {
  const CheckInScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final checkInProv = Provider.of<CheckInProvider>(context);
    final today = checkInProv.todayCheckIn;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(18.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('AI Wellness Check-in 🧠', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold)),
          const SizedBox(height: 6),
          const Text('Track your daily emotional baseline once per calendar day.', style: TextStyle(color: AppColors.textSecondaryLight)),
          const SizedBox(height: 20),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(20.0),
              child: Column(
                children: [
                  Icon(
                    today != null ? Icons.check_circle_rounded : Icons.psychology_rounded,
                    size: 64,
                    color: today != null ? AppColors.primaryGreen : AppColors.aiPurple,
                  ),
                  const SizedBox(height: 16),
                  Text(
                    today != null ? '✓ Today\'s Check-in Completed' : 'Ready for Today\'s Check-in',
                    style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    today != null
                        ? 'Stress Level: ${today.stressLevel} (${today.stressScore}%)\n${today.recommendation}'
                        : 'Complete your 1-minute daily assessment to receive personalized AI recommendations.',
                    textAlign: TextAlign.center,
                    style: const TextStyle(fontSize: 13, color: AppColors.textSecondaryLight, height: 1.4),
                  ),
                  const SizedBox(height: 20),
                  SizedBox(
                    width: double.infinity,
                    height: 48,
                    child: ElevatedButton.icon(
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(builder: (_) => const DailyQuizScreen()),
                        );
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: today != null ? AppColors.darkGreen : AppColors.primaryGreen,
                      ),
                      icon: Icon(today != null ? Icons.refresh_rounded : Icons.play_arrow_rounded, color: Colors.white),
                      label: Text(today != null ? 'Retake Check-in' : 'Start Check-in Now'),
                    ),
                  ),
                  if (today != null) ...[
                    const SizedBox(height: 12),
                    SizedBox(
                      width: double.infinity,
                      height: 48,
                      child: OutlinedButton.icon(
                        onPressed: () {
                          Navigator.of(context).pushAndRemoveUntil(
                            MaterialPageRoute(builder: (_) => const MainShell(initialIndex: 2)), // Tab 2 = Insights
                            (route) => false,
                          );
                        },
                        style: OutlinedButton.styleFrom(
                          side: const BorderSide(color: AppColors.primaryGreen),
                        ),
                        icon: const Icon(Icons.insights_rounded, color: AppColors.primaryGreen),
                        label: const Text('View Insights & Trends', style: TextStyle(color: AppColors.primaryGreen, fontWeight: FontWeight.bold)),
                      ),
                    ),
                  ],
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

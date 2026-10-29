import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../providers/auth_provider.dart';
import '../../providers/checkin_provider.dart';
import '../checkin/daily_quiz_screen.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final auth = Provider.of<AuthProvider>(context, listen: false);
      Provider.of<CheckInProvider>(context, listen: false).loadTodayCheckIn(auth.userEmail);
    });
  }

  String _getGreeting(String name) {
    final hour = DateTime.now().hour;
    if (hour >= 5 && hour < 12) {
      return 'Good morning, $name 👋';
    } else if (hour >= 12 && hour < 17) {
      return 'Good afternoon, $name 👋';
    } else if (hour >= 17 && hour < 21) {
      return 'Good evening, $name 👋';
    } else {
      return 'Good night, $name 🌙';
    }
  }

  String _getSubtitle() {
    final hour = DateTime.now().hour;
    if (hour >= 5 && hour < 12) {
      return 'Start your day with a quick wellness check-in.';
    } else if (hour >= 12 && hour < 17) {
      return 'Take a moment to check in with yourself.';
    } else if (hour >= 17 && hour < 21) {
      return 'Reflect on your day and take some time to unwind.';
    } else {
      return 'Wind down and give yourself some time to rest.';
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = Provider.of<AuthProvider>(context);
    final checkInProv = Provider.of<CheckInProvider>(context);
    final today = checkInProv.todayCheckIn;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(18.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 1. TIME-SPECIFIC GREETING
          Text(
            _getGreeting(auth.userName),
            style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: AppColors.textMainLight),
          ),
          const SizedBox(height: 4),
          Text(
            _getSubtitle(),
            style: const TextStyle(fontSize: 14, color: AppColors.textSecondaryLight),
          ),
          const SizedBox(height: 18),

          // 2. TODAY'S WELLNESS SUMMARY
          Card(
            child: Padding(
              padding: const EdgeInsets.all(18.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    "TODAY'S WELLNESS",
                    style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: AppColors.primaryGreen, letterSpacing: 0.8),
                  ),
                  const SizedBox(height: 14),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      _buildMetricCol('Stress', today?.stressLevel ?? '--', today != null ? '${today.stressScore}%' : 'No data', AppColors.primaryGreen),
                      const SizedBox(height: 36, child: VerticalDivider(color: AppColors.cardBorderLight)),
                      _buildMetricCol('Sleep', '7h', 'Rest recovery', AppColors.textMainLight),
                      const SizedBox(height: 36, child: VerticalDivider(color: AppColors.cardBorderLight)),
                      _buildMetricCol('Progress', '20%', '1/5 tasks', AppColors.darkGreen),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 14),

          // 3. DAILY CHECK-IN CARD
          Card(
            child: Padding(
              padding: const EdgeInsets.all(18.0),
              child: Row(
                children: [
                  Text(today != null ? '✓' : '🧠', style: const TextStyle(fontSize: 32)),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          today != null ? '✓ Today\'s check-in complete' : 'How are you feeling today?',
                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          today != null ? 'Come back tomorrow for your next check-in.' : 'Take 1 minute to reflect on your wellness.',
                          style: const TextStyle(fontSize: 12, color: AppColors.textSecondaryLight),
                        ),
                      ],
                    ),
                  ),
                  ElevatedButton(
                    onPressed: () {
                      Navigator.of(context).push(
                        MaterialPageRoute(builder: (_) => const DailyQuizScreen()),
                      );
                    },
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                    ),
                    child: Text(today != null ? 'Review' : 'Start'),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 14),

          // 4. STRESS TREND CARD
          Card(
            child: Padding(
              padding: const EdgeInsets.all(18.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('📊 Stress Trend', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                          SizedBox(height: 2),
                          Text('Your recent wellness pattern', style: TextStyle(fontSize: 12, color: AppColors.textSecondaryLight)),
                        ],
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                        decoration: BoxDecoration(
                          color: AppColors.lightMint,
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Text(
                          today?.stressLevel ?? 'NORMAL',
                          style: const TextStyle(color: AppColors.darkGreen, fontWeight: FontWeight.bold, fontSize: 12),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 14),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        'Stress: ${today?.stressLevel ?? 'Normal baseline'}',
                        style: const TextStyle(fontSize: 14),
                      ),
                      const Text(
                        'Trend: Baseline',
                        style: TextStyle(fontWeight: FontWeight.bold, color: AppColors.primaryGreen),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 14),

          // 5. NEXT SCHEDULED REMINDER CARD
          const Card(
            child: Padding(
              padding: EdgeInsets.all(14.0),
              child: Row(
                children: [
                  Text('⏰', style: TextStyle(fontSize: 20)),
                  SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      'Daily Reminder: Scheduled for 9:00 AM',
                      style: TextStyle(fontSize: 12, color: AppColors.textSecondaryLight),
                    ),
                  ),
                  Text(
                    'Settings',
                    style: TextStyle(fontWeight: FontWeight.bold, color: AppColors.primaryGreen, fontSize: 12),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),

          // Disclaimer
          const Center(
            child: Text(
              'ℹ️ AI responses and stress estimates are provided for general wellness support and are not medical diagnoses.',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 11, color: AppColors.textSecondaryLight),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMetricCol(String label, String value, String sub, Color valueColor) {
    return Column(
      children: [
        Text(label, style: const TextStyle(fontSize: 12, color: AppColors.textSecondaryLight)),
        const SizedBox(height: 4),
        Text(value, style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: valueColor)),
        const SizedBox(height: 2),
        Text(sub, style: const TextStyle(fontSize: 11, color: AppColors.textSecondaryLight)),
      ],
    );
  }
}

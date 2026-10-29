import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../providers/auth_provider.dart';
import '../main_shell.dart';

class OnboardingScreen extends StatefulWidget {
  const OnboardingScreen({super.key});

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen> {
  final _nameController = TextEditingController();
  int _currentStep = 1;

  @override
  void initState() {
    super.initState();
    final auth = Provider.of<AuthProvider>(context, listen: false);
    if (auth.userName.isNotEmpty && auth.userName != 'Friend') {
      _nameController.text = auth.userName;
    }
  }

  void _finishSetup() async {
    final auth = Provider.of<AuthProvider>(context, listen: false);
    final name = _nameController.text.trim().isNotEmpty ? _nameController.text.trim() : auth.userName;
    await auth.completeAccountSetup(name);

    if (!mounted) return;
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const MainShell()),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            children: [
              LinearProgressIndicator(
                value: _currentStep / 2,
                backgroundColor: AppColors.cardBorderLight,
                valueColor: const AlwaysStoppedAnimation<Color>(AppColors.primaryGreen),
              ),
              const SizedBox(height: 24),
              Expanded(
                child: _currentStep == 1 ? _buildStep1() : _buildStep2(),
              ),
              SizedBox(
                width: double.infinity,
                height: 52,
                child: ElevatedButton(
                  onPressed: () {
                    if (_currentStep == 1) {
                      setState(() => _currentStep = 2);
                    } else {
                      _finishSetup();
                    }
                  },
                  child: Text(_currentStep == 1 ? 'Continue' : 'Start My Wellness Journey'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStep1() {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Personalize Your Journey 🌿',
          style: TextStyle(fontSize: 26, fontWeight: FontWeight.bold, color: AppColors.primaryGreen),
        ),
        const SizedBox(height: 8),
        const Text(
          'How should MentalCare AI address you?',
          style: TextStyle(fontSize: 15, color: AppColors.textSecondaryLight),
        ),
        const SizedBox(height: 24),
        TextFormField(
          controller: _nameController,
          decoration: const InputDecoration(
            labelText: 'Preferred Name',
            prefixIcon: Icon(Icons.person, color: AppColors.primaryGreen),
          ),
        ),
      ],
    );
  }

  Widget _buildStep2() {
    return const Column(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Your Daily Wellness Routine ✨',
          style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: AppColors.primaryGreen),
        ),
        SizedBox(height: 8),
        Text(
          'MentalCare AI will customize your daily check-in reminders and AI recommendations based on your preferences.',
          style: TextStyle(fontSize: 14, color: AppColors.textSecondaryLight),
        ),
        SizedBox(height: 24),
        Card(
          child: Padding(
            padding: EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(Icons.check_circle_outline, color: AppColors.primaryGreen),
                    SizedBox(width: 8),
                    Text('Daily Check-in (1-minute mood quiz)', style: TextStyle(fontWeight: FontWeight.bold)),
                  ],
                ),
                SizedBox(height: 12),
                Row(
                  children: [
                    Icon(Icons.check_circle_outline, color: AppColors.primaryGreen),
                    SizedBox(width: 8),
                    Text('AI Wellness Companion Chatbot', style: TextStyle(fontWeight: FontWeight.bold)),
                  ],
                ),
                SizedBox(height: 12),
                Row(
                  children: [
                    Icon(Icons.check_circle_outline, color: AppColors.primaryGreen),
                    SizedBox(width: 8),
                    Text('Multi-day Stress & Burnout Analytics', style: TextStyle(fontWeight: FontWeight.bold)),
                  ],
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';

class AboutScreen extends StatefulWidget {
  const AboutScreen({super.key});

  @override
  State<AboutScreen> createState() => _AboutScreenState();
}

class _AboutScreenState extends State<AboutScreen> {
  bool _structureExpanded = false;
  bool _disclaimerExpanded = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('About MentalCare AI')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(18.0),
        child: Column(
          children: [
            // Header
            Center(
              child: Column(
                children: [
                  Image.asset(
                    'assets/images/ic_logo.png',
                    width: 72,
                    height: 72,
                    errorBuilder: (context, error, stackTrace) => const Icon(
                      Icons.psychology_rounded,
                      size: 64,
                      color: AppColors.primaryGreen,
                    ),
                  ),
                  const SizedBox(height: 10),
                  const Text('MentalCare AI', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: AppColors.primaryGreen)),
                  const SizedBox(height: 2),
                  const Text('Understand. Relax. Thrive.', style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: AppColors.aiPurple)),
                  const SizedBox(height: 6),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(color: AppColors.aiPurpleLight, borderRadius: BorderRadius.circular(12)),
                    child: const Text('Version 2.4.0 • Cross-Platform Production Edition', style: TextStyle(fontSize: 11, color: AppColors.aiPurpleDark)),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            // Product Summary Card
            const Card(
              child: Padding(
                padding: EdgeInsets.all(18.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('About MentalCare AI', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppColors.primaryGreen)),
                    SizedBox(height: 8),
                    Text(
                      'MentalCare AI is an AI-powered mental wellness application designed to help users understand their emotional wellbeing, track stress patterns, maintain healthy habits, and receive personalised wellness support.',
                      style: TextStyle(fontSize: 13, height: 1.4),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 12),

            // Key Features Card
            const Card(
              child: Padding(
                padding: EdgeInsets.all(18.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Key Features', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppColors.primaryGreen)),
                    SizedBox(height: 8),
                    Text(
                      '🧠 AI Wellness Check-ins — Understand your daily emotional state\n'
                      '📊 Stress Insights — View patterns and trends from your check-ins\n'
                      '📓 Personal Journal — Record thoughts and reflect on your wellbeing\n'
                      '🤖 AI Wellness Assistant — Contextual conversations for wellness support\n'
                      '🌿 Wellness Activities — Breathing, relaxation, and healthy habits\n'
                      '😴 Sleep & Lifestyle — Track sleep, activity, and lifestyle factors\n'
                      '🔔 Smart Reminders — Receive reminders for check-ins and activities\n'
                      '⌚ Health & Wearables — View supported health information when connected\n'
                      '🛡 Safety & Emergency — Access crisis support and trusted contacts',
                      style: TextStyle(fontSize: 12, height: 1.5),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 12),

            // Expandable Project Structure
            Card(
              child: Padding(
                padding: const EdgeInsets.all(18.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    InkWell(
                      onTap: () => setState(() => _structureExpanded = !_structureExpanded),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text('Project Structure', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppColors.primaryGreen)),
                          Text(_structureExpanded ? 'Collapse ▲' : 'Expand ▼', style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: AppColors.textSecondaryLight)),
                        ],
                      ),
                    ),
                    if (_structureExpanded) ...[
                      const SizedBox(height: 12),
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: Theme.of(context).cardColor,
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(color: AppColors.cardBorderLight),
                        ),
                        child: const Text(
                          'mentalcare_ai_flutter/lib/\n'
                          '├── core/            (Network, Theme, Constants & Engines)\n'
                          '├── models/          (CheckIn, ChatMessage & User models)\n'
                          '├── providers/       (Auth, CheckIn, Chat & Journal providers)\n'
                          '├── screens/         (Dashboard, CheckIn, Insights, Chat, Profile)\n'
                          '└── widgets/         (Custom UI components)\n\n'
                          'backend_server/\n'
                          '└── app.py           (Python Flask REST API)',
                          style: TextStyle(fontFamily: 'monospace', fontSize: 11, height: 1.4),
                        ),
                      ),
                    ],
                  ],
                ),
              ),
            ),
            const SizedBox(height: 12),

            // Technology Stack
            const Card(
              child: Padding(
                padding: EdgeInsets.all(18.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Technology Stack', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppColors.primaryGreen)),
                    SizedBox(height: 8),
                    Text(
                      'Flutter • Dart • Provider • SQLite & Sqflite • Python Flask REST API • On-Device NLP & Chat Engine • Shared Preferences',
                      style: TextStyle(fontSize: 12, height: 1.4),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 12),

            // Project Authors
            const Card(
              child: Padding(
                padding: EdgeInsets.all(18.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Project Authors', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppColors.primaryGreen)),
                    SizedBox(height: 8),
                    Text(
                      '• Joylyn Princita Fernandes (25MCAR0099)\n'
                      '• Syed Faizan Pasha (25MCAR0138)\n'
                      '• Vishwajeet Singh (25MCAR0219)\n\n'
                      'Group 11\n'
                      'Master of Computer Applications (MCA)\n'
                      'Jain (Deemed-to-be University)\n'
                      'JGI Knowledge Campus, Jayanagar, Bengaluru',
                      style: TextStyle(fontSize: 12, height: 1.5),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 12),

            // Expandable Disclaimer
            Card(
              child: Padding(
                padding: const EdgeInsets.all(18.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    InkWell(
                      onTap: () => setState(() => _disclaimerExpanded = !_disclaimerExpanded),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text('Wellness Disclaimer', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold, color: AppColors.primaryGreen)),
                          Text(_disclaimerExpanded ? 'Collapse ▲' : 'Expand ▼', style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: AppColors.textSecondaryLight)),
                        ],
                      ),
                    ),
                    if (_disclaimerExpanded) ...[
                      const SizedBox(height: 10),
                      const Text(
                        'MentalCare AI is a wellness and early-warning support tool, not a medical diagnostic application. It does not diagnose medical conditions or prescribe treatment. If you are experiencing a mental health emergency, please contact 988 or local emergency services immediately.',
                        style: TextStyle(fontSize: 12, color: AppColors.textSecondaryLight, height: 1.4),
                      ),
                    ],
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),

            const Text(
              'MentalCare AI • Understand. Relax. Thrive.',
              style: TextStyle(fontSize: 11, color: AppColors.textSecondaryLight),
            ),
          ],
        ),
      ),
    );
  }
}

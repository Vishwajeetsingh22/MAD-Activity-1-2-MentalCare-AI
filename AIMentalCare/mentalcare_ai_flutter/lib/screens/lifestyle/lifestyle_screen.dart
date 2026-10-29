import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';
import '../journal/journal_screen.dart';

class LifestyleScreen extends StatelessWidget {
  const LifestyleScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(18.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Lifestyle & Habits 🌿', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold)),
          const SizedBox(height: 6),
          const Text('Track sleep, work balance, guided relaxation, and journal reflections.', style: TextStyle(color: AppColors.textSecondaryLight)),
          const SizedBox(height: 20),
          Card(
            child: ListTile(
              leading: const Icon(Icons.menu_book_rounded, color: AppColors.primaryGreen),
              title: const Text('Personal Journal', style: TextStyle(fontWeight: FontWeight.bold)),
              subtitle: const Text('Record thoughts & reflect on your wellbeing'),
              trailing: const Icon(Icons.arrow_forward_ios, size: 16),
              onTap: () {
                Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const JournalScreen()),
                );
              },
            ),
          ),
          const SizedBox(height: 12),
          const Card(
            child: Padding(
              padding: EdgeInsets.all(18.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Sleep & Stamina', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                  SizedBox(height: 8),
                  Text('Average Sleep Duration: 7.0 hours / night', style: TextStyle(color: AppColors.textSecondaryLight)),
                  SizedBox(height: 4),
                  Text('Screen Time Balance: Moderate', style: TextStyle(color: AppColors.textSecondaryLight)),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

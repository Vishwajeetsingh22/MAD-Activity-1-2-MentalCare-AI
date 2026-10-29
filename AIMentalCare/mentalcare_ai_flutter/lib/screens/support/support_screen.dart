import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../core/theme/app_colors.dart';

class SupportScreen extends StatelessWidget {
  const SupportScreen({super.key});

  void _callNumber(String number) async {
    final uri = Uri.parse('tel:$number');
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri);
    }
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(18.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Safety & Emergency Support 🛡', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold)),
          const SizedBox(height: 6),
          const Text('Immediate assistance when you need urgent help or crisis intervention.', style: TextStyle(color: AppColors.textSecondaryLight)),
          const SizedBox(height: 20),
          Card(
            color: const Color(0xFFFEF2F2),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(20),
              side: const BorderSide(color: Color(0xFFFCA5A5), width: 1.5),
            ),
            child: Padding(
              padding: const EdgeInsets.all(20.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('🚨 Emergency SOS Lifeline', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFFDC2626))),
                  const SizedBox(height: 6),
                  const Text('If you or someone you know is in immediate distress, call 24/7 crisis support lines immediately.', style: TextStyle(fontSize: 13, color: AppColors.textMainLight)),
                  const SizedBox(height: 18),
                  SizedBox(
                    width: double.infinity,
                    height: 50,
                    child: ElevatedButton.icon(
                      onPressed: () => _callNumber('988'),
                      style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFFDC2626)),
                      icon: const Icon(Icons.phone, color: Colors.white),
                      label: const Text('Call 988 Crisis Lifeline'),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          Card(
            child: ListTile(
              leading: const Icon(Icons.contact_phone_outlined, color: AppColors.primaryGreen),
              title: const Text('Trusted Contacts', style: TextStyle(fontWeight: FontWeight.bold)),
              subtitle: const Text('Family members or friends to notify during high stress'),
              trailing: const Icon(Icons.arrow_forward_ios, size: 16),
              onTap: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Trusted Contacts Manager')),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

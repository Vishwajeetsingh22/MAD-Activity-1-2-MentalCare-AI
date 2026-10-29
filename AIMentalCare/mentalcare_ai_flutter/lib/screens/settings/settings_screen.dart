import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../providers/theme_provider.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final themeProv = Provider.of<ThemeProvider>(context);

    return Scaffold(
      appBar: AppBar(title: const Text('Settings ⚙️')),
      body: ListView(
        padding: const EdgeInsets.all(18.0),
        children: [
          const Text('Appearance', style: TextStyle(fontWeight: FontWeight.bold, color: AppColors.primaryGreen, fontSize: 13, letterSpacing: 0.8)),
          const SizedBox(height: 8),
          Card(
            child: ListTile(
              leading: const Icon(Icons.brightness_6_outlined, color: AppColors.primaryGreen),
              title: const Text('Theme Mode', style: TextStyle(fontWeight: FontWeight.bold)),
              subtitle: Text(
                themeProv.themeMode == ThemeMode.light
                    ? 'Light Theme'
                    : themeProv.themeMode == ThemeMode.dark
                        ? 'Dark Theme'
                        : 'System Default',
              ),
              trailing: DropdownButton<ThemeMode>(
                value: themeProv.themeMode,
                underline: const SizedBox(),
                items: const [
                  DropdownMenuItem(value: ThemeMode.system, child: Text('System')),
                  DropdownMenuItem(value: ThemeMode.light, child: Text('Light')),
                  DropdownMenuItem(value: ThemeMode.dark, child: Text('Dark')),
                ],
                onChanged: (mode) {
                  if (mode != null) themeProv.setThemeMode(mode);
                },
              ),
            ),
          ),
          const SizedBox(height: 20),
          const Text('Notifications & Reminders', style: TextStyle(fontWeight: FontWeight.bold, color: AppColors.primaryGreen, fontSize: 13, letterSpacing: 0.8)),
          const SizedBox(height: 8),
          Card(
            child: SwitchListTile(
              secondary: const Icon(Icons.notifications_none_rounded, color: AppColors.primaryGreen),
              title: const Text('Daily Check-in Reminder', style: TextStyle(fontWeight: FontWeight.bold)),
              subtitle: const Text('Receive a daily notification prompt at 9:00 AM'),
              value: true,
              onChanged: (val) {},
            ),
          ),
        ],
      ),
    );
  }
}

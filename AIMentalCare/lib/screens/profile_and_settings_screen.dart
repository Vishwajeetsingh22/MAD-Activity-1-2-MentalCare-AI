import 'package:flutter/material.dart';
import '../core/theme/app_colors.dart';
import '../core/theme/app_spacing.dart';
import '../core/theme/app_radius.dart';
import '../services/auth_service.dart';
import 'auth/login_screen.dart';

/// Phase 9 & Final Play Store UI — Premium Profile, Settings, Crisis Support & About
class ProfileAndSettingsScreen extends StatefulWidget {
  final int initialTabIndex;

  const ProfileAndSettingsScreen({
    Key? key,
    this.initialTabIndex = 0,
  }) : super(key: key);

  @override
  State<ProfileAndSettingsScreen> createState() => _ProfileAndSettingsScreenState();
}

class _ProfileAndSettingsScreenState extends State<ProfileAndSettingsScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  // Profile state
  String _userName = "Vishwajeet Singh";
  String _userEmail = "user@mentalcare.ai";

  // Settings state
  String _themeMode = "System"; // Light, Dark, System
  bool _dailyCheckInReminders = true;
  TimeOfDay _reminderTime = const TimeOfDay(hour: 20, minute: 30);
  bool _quietHoursEnabled = true;

  @override
  void initState() {
    super.initState();
    final user = AuthService().currentUser;
    if (user != null) {
      _userName = user.fullName.isNotEmpty ? user.fullName : _userName;
      _userEmail = user.email.isNotEmpty ? user.email : _userEmail;
    }
    _tabController = TabController(
      length: 2,
      vsync: this,
      initialIndex: widget.initialTabIndex,
    );
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  String _getInitials(String name) {
    final parts = name.trim().split(RegExp(r'\s+'));
    if (parts.length >= 2) {
      return "${parts[0][0].toUpperCase()}${parts[1][0].toUpperCase()}";
    } else if (parts.isNotEmpty && parts[0].isNotEmpty) {
      return parts[0].substring(0, parts[0].length >= 2 ? 2 : 1).toUpperCase();
    }
    return "MC";
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final cardBg = isDark ? AppColors.darkSurface : AppColors.white;
    final borderColor = isDark ? AppColors.darkCardBorder : AppColors.cardBorder;
    final mainText = isDark ? AppColors.darkMainText : AppColors.mainText;
    final subText = isDark ? AppColors.darkSecondaryText : AppColors.secondaryText;

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      appBar: AppBar(
        title: Text(
          "Profile & Settings",
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: isDark ? AppColors.white : AppColors.darkGreen,
          ),
        ),
        backgroundColor: Colors.transparent,
        elevation: 0,
        bottom: TabBar(
          controller: _tabController,
          indicatorColor: AppColors.primaryGreen,
          labelColor: isDark ? AppColors.white : AppColors.darkGreen,
          unselectedLabelColor: subText,
          labelStyle: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
          tabs: const [
            Tab(text: "Profile"),
            Tab(text: "Settings"),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildProfileTab(isDark, cardBg, borderColor, mainText, subText),
          _buildSettingsTab(isDark, cardBg, borderColor, mainText, subText),
        ],
      ),
    );
  }

  // =========================================================================
  // PROFILE TAB
  // =========================================================================
  Widget _buildProfileTab(
    bool isDark,
    Color cardBg,
    Color borderColor,
    Color mainText,
    Color subText,
  ) {
    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg, vertical: AppSpacing.md),
      children: [
        // 1. Profile Avatar & Identity Card
        Container(
          padding: const EdgeInsets.all(AppSpacing.md),
          decoration: BoxDecoration(
            color: cardBg,
            borderRadius: BorderRadius.circular(AppRadius.card),
            border: Border.all(color: borderColor),
          ),
          child: Row(
            children: [
              CircleAvatar(
                radius: 28,
                backgroundColor: isDark ? AppColors.darkBackground : AppColors.lightMint,
                child: Text(
                  _getInitials(_userName),
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: AppColors.darkGreen,
                  ),
                ),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      _userName,
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: mainText,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      _userEmail,
                      style: TextStyle(fontSize: 12, color: subText),
                    ),
                  ],
                ),
              ),
              IconButton(
                icon: const Icon(Icons.edit_outlined, size: 20),
                color: subText,
                onPressed: _showEditProfileDialog,
              ),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.md),

        // 2. CRISIS & SAFETY SUPPORT (Section 11)
        _buildGroupCard(
          title: "CRISIS & SAFETY SUPPORT",
          cardBg: cardBg,
          borderColor: borderColor,
          subText: subText,
          children: [
            Text(
              "Immediate support when you need it.",
              style: TextStyle(fontSize: 12, color: subText),
            ),
            const SizedBox(height: 10),

            // Primary Emergency SOS Call Button
            ElevatedButton.icon(
              onPressed: () => _showCrisisDialog("Emergency SOS Contact", "+91 98765 43210"),
              icon: const Text('🚨', style: TextStyle(fontSize: 16)),
              label: const Text(
                "Call Emergency SOS",
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
              ),
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.high,
                foregroundColor: Colors.white,
                elevation: 0,
                minimumSize: const Size.fromHeight(44),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.button)),
              ),
            ),
            const SizedBox(height: 8),

            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: () => _showCrisisDialog("Crisis Lifeline", "988"),
                    style: OutlinedButton.styleFrom(
                      side: BorderSide(color: borderColor),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.button)),
                    ),
                    child: Text(
                      "Call Support (988)",
                      style: TextStyle(color: mainText, fontSize: 12, fontWeight: FontWeight.bold),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton(
                    onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text("Emergency Contacts: 2 registered")),
                    ),
                    style: OutlinedButton.styleFrom(
                      side: BorderSide(color: borderColor),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.button)),
                    ),
                    child: Text(
                      "Manage Contacts",
                      style: TextStyle(color: mainText, fontSize: 12, fontWeight: FontWeight.bold),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 6),
            Text(
              "MentalCare AI is not a medical diagnostic service.",
              style: TextStyle(fontSize: 10, color: subText.withOpacity(0.8)),
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.md),

        // 3. ABOUT MENTALCARE AI (Section 9, 10, 24)
        _buildGroupCard(
          title: "ABOUT MENTALCARE AI",
          cardBg: cardBg,
          borderColor: borderColor,
          subText: subText,
          children: [
            _buildActionRow(
              icon: Icons.info_outline_rounded,
              title: "About MentalCare AI",
              subtitle: "Product summary, features & authors",
              onTap: _showProductAboutDialog,
              mainText: mainText,
              subText: subText,
            ),
            Divider(color: borderColor, height: 1),
            _buildActionRow(
              icon: Icons.shield_outlined,
              title: "Privacy & Data Policy",
              subtitle: "Encrypted on-device wellness data",
              onTap: _showPrivacyDialog,
              mainText: mainText,
              subText: subText,
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.lg),

        // Logout Action
        OutlinedButton(
          onPressed: _showLogoutDialog,
          style: OutlinedButton.styleFrom(
            side: const BorderSide(color: AppColors.high),
            padding: const EdgeInsets.symmetric(vertical: 12),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.button)),
          ),
          child: const Text(
            "Log Out",
            style: TextStyle(
              color: AppColors.high,
              fontWeight: FontWeight.bold,
              fontSize: 13,
            ),
          ),
        ),
        const SizedBox(height: AppSpacing.xl),
      ],
    );
  }

  // =========================================================================
  // SETTINGS TAB
  // =========================================================================
  Widget _buildSettingsTab(
    bool isDark,
    Color cardBg,
    Color borderColor,
    Color mainText,
    Color subText,
  ) {
    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg, vertical: AppSpacing.md),
      children: [
        // 1. Appearance Section
        _buildGroupCard(
          title: "APPEARANCE",
          cardBg: cardBg,
          borderColor: borderColor,
          subText: subText,
          children: [
            Row(
              children: [
                _buildThemeOption("Light", "Light", isDark),
                const SizedBox(width: 8),
                _buildThemeOption("Dark", "Dark", isDark),
                const SizedBox(width: 8),
                _buildThemeOption("System", "System", isDark),
              ],
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.md),

        // 2. Notifications Section
        _buildGroupCard(
          title: "NOTIFICATIONS",
          cardBg: cardBg,
          borderColor: borderColor,
          subText: subText,
          children: [
            SwitchListTile(
              contentPadding: EdgeInsets.zero,
              title: Text("Daily Check-in Reminder", style: TextStyle(color: mainText, fontWeight: FontWeight.bold, fontSize: 13)),
              value: _dailyCheckInReminders,
              activeThumbColor: AppColors.primaryGreen,
              onChanged: (val) => setState(() => _dailyCheckInReminders = val),
            ),
            if (_dailyCheckInReminders) ...[
              ListTile(
                contentPadding: EdgeInsets.zero,
                title: Text("Reminder Time", style: TextStyle(color: mainText, fontSize: 13)),
                trailing: Text(
                  _reminderTime.format(context),
                  style: const TextStyle(color: AppColors.primaryGreen, fontWeight: FontWeight.bold, fontSize: 13),
                ),
                onTap: () async {
                  final picked = await showTimePicker(context: context, initialTime: _reminderTime);
                  if (picked != null) setState(() => _reminderTime = picked);
                },
              ),
            ],
            SwitchListTile(
              contentPadding: EdgeInsets.zero,
              title: Text("Quiet Hours (10 PM - 7 AM)", style: TextStyle(color: mainText, fontSize: 13)),
              value: _quietHoursEnabled,
              activeThumbColor: AppColors.primaryGreen,
              onChanged: (val) => setState(() => _quietHoursEnabled = val),
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.md),

        // 3. Health & Wearable Integration
        _buildGroupCard(
          title: "HEALTH & WEARABLE",
          cardBg: cardBg,
          borderColor: borderColor,
          subText: subText,
          children: [
            ListTile(
              contentPadding: EdgeInsets.zero,
              title: Text("Optical PPG Sensor", style: TextStyle(color: mainText, fontSize: 13, fontWeight: FontWeight.bold)),
              subtitle: Text("Sensor telemetry for pulse trends", style: TextStyle(color: subText, fontSize: 11)),
              trailing: TextButton(
                onPressed: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text("Optical PPG pulse active")),
                  );
                },
                child: const Text("Manage"),
              ),
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.xl),
      ],
    );
  }

  // =========================================================================
  // HELPER DIALOGS (PRODUCT-READY ABOUT & CRISIS)
  // =========================================================================

  /// Professional About Screen (Sections 9, 10, 24)
  void _showProductAboutDialog() {
    bool isStructureExpanded = false;

    showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (context, setDialogState) {
          final isDark = Theme.of(context).brightness == Brightness.dark;
          final mainText = isDark ? AppColors.darkMainText : AppColors.mainText;
          final subText = isDark ? AppColors.darkSecondaryText : AppColors.secondaryText;

          return AlertDialog(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.card)),
            title: Row(
              children: [
                const Text('🌿', style: TextStyle(fontSize: 22)),
                const SizedBox(width: 8),
                Text(
                  "About MentalCare AI",
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: mainText),
                ),
              ],
            ),
            content: SingleChildScrollView(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Text(
                    "Understand. Relax. Thrive.",
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: AppColors.primaryGreen,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    "MentalCare AI is an AI-powered mental wellness application designed to help users understand their emotional wellbeing, track stress patterns, maintain healthy habits, and receive personalised wellness support.",
                    style: TextStyle(fontSize: 12, color: subText, height: 1.35),
                  ),
                  const SizedBox(height: 14),

                  // WHAT YOU CAN DO (Section 9)
                  Text(
                    "WHAT YOU CAN DO",
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      letterSpacing: 0.8,
                      color: isDark ? AppColors.primaryGreen : AppColors.darkGreen,
                    ),
                  ),
                  const SizedBox(height: 6),
                  ...[
                    "🧠 Daily wellness check-ins",
                    "📊 Stress insights and trends",
                    "📓 Personal journaling",
                    "🤖 AI wellness assistant",
                    "🌿 Personalised wellness support",
                    "😴 Sleep & lifestyle tracking",
                    "🔔 Wellness reminders",
                    "⌚ Supported health/wearable integration",
                  ].map((item) => Padding(
                        padding: const EdgeInsets.symmetric(vertical: 2),
                        child: Text(item, style: TextStyle(fontSize: 12, color: mainText)),
                      )),
                  const SizedBox(height: 14),

                  // WELLNESS DISCLAIMER (Section 9)
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: isDark ? AppColors.darkBackground : AppColors.mint,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      "MentalCare AI is a wellness and early-warning support tool, not a medical diagnostic application.",
                      style: TextStyle(fontSize: 11, color: isDark ? AppColors.darkGreen : AppColors.darkGreen, height: 1.3),
                    ),
                  ),
                  const SizedBox(height: 14),

                  // PROJECT AUTHORS (Section 9)
                  Text(
                    "PROJECT AUTHORS",
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      letterSpacing: 0.8,
                      color: isDark ? AppColors.primaryGreen : AppColors.darkGreen,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: isDark ? AppColors.darkBackground : Colors.grey.shade50,
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(color: AppColors.cardBorder),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text("Joylyn Princita Fernandes (25MCAR0099)", style: TextStyle(fontSize: 12, color: mainText, fontWeight: FontWeight.w600)),
                        Text("Syed Faizan Pasha (25MCAR0138)", style: TextStyle(fontSize: 12, color: mainText, fontWeight: FontWeight.w600)),
                        Text("Vishwajeet Singh (25MCAR0219)", style: TextStyle(fontSize: 12, color: mainText, fontWeight: FontWeight.w600)),
                        const SizedBox(height: 4),
                        Text("Group 11 • Master of Computer Applications\nJain (Deemed-to-be University)", style: TextStyle(fontSize: 11, color: subText)),
                      ],
                    ),
                  ),
                  const SizedBox(height: 12),

                  // PROJECT STRUCTURE (Section 10 - Compact Expandable)
                  InkWell(
                    onTap: () {
                      setDialogState(() {
                        isStructureExpanded = !isStructureExpanded;
                      });
                    },
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          "Project Structure",
                          style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: mainText),
                        ),
                        Icon(
                          isStructureExpanded ? Icons.expand_less_rounded : Icons.chevron_right_rounded,
                          size: 18,
                          color: subText,
                        ),
                      ],
                    ),
                  ),
                  if (isStructureExpanded) ...[
                    const SizedBox(height: 6),
                    Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: isDark ? AppColors.darkBackground : Colors.grey.shade100,
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Text(
                        "lib/\n├── core/ (theme, widgets)\n├── models/ (user, stress)\n├── services/ (auth, api)\n└── screens/ (auth, dashboard, quiz, chat)\nAIMentalCareApp/ (Android Native & Wearable)\nbackend_server/ (AI NLP Services)",
                        style: TextStyle(fontFamily: 'monospace', fontSize: 10, color: subText),
                      ),
                    ),
                  ],
                  const SizedBox(height: 12),

                  Text(
                    "Version 2.0.0",
                    style: TextStyle(fontSize: 11, color: subText),
                  ),
                ],
              ),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(ctx),
                child: const Text("Close"),
              ),
            ],
          );
        },
      ),
    );
  }

  void _showCrisisDialog(String service, String number) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.card)),
        title: Text("Call $service"),
        content: Text("Proceed to dial $number?"),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text("Cancel")),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.high),
            onPressed: () {
              Navigator.pop(ctx);
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(content: Text("Calling $service ($number)...")),
              );
            },
            child: const Text("Call", style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  void _showPrivacyDialog() {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.card)),
        title: const Text("Privacy & Data Protection"),
        content: const Text(
          "• On-Device Storage: Encrypted in local SQLite / Room.\n"
          "• Zero Third-Party Brokers: Behavioral telemetry is never shared.\n"
          "• Biometric Control: Wearable sensor telemetry is permission-gated.\n"
          "• Offline Fallback: Mental state analysis works without cloud dependency.",
          style: TextStyle(fontSize: 13, height: 1.4),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text("Understood")),
        ],
      ),
    );
  }

  void _showEditProfileDialog() {
    final nameController = TextEditingController(text: _userName);
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.card)),
        title: const Text("Edit Name"),
        content: TextField(
          controller: nameController,
          decoration: const InputDecoration(labelText: "Full Name"),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text("Cancel")),
          ElevatedButton(
            onPressed: () {
              if (nameController.text.trim().isNotEmpty) {
                setState(() => _userName = nameController.text.trim());
                Navigator.pop(ctx);
              }
            },
            child: const Text("Save"),
          ),
        ],
      ),
    );
  }

  void _showLogoutDialog() {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.card)),
        title: const Text("Confirm Logout"),
        content: const Text("Are you sure you want to sign out of MentalCare AI?"),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text("Cancel")),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.high),
            onPressed: () async {
              Navigator.pop(ctx);
              await AuthService().signOut();
              if (mounted) {
                Navigator.of(context).pushAndRemoveUntil(
                  MaterialPageRoute(builder: (_) => const LoginScreen()),
                  (route) => false,
                );
              }
            },
            child: const Text("Log Out", style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  Widget _buildGroupCard({
    required String title,
    required Color cardBg,
    required Color borderColor,
    required Color subText,
    required List<Widget> children,
  }) {
    return Container(
      padding: const EdgeInsets.all(AppSpacing.md),
      decoration: BoxDecoration(
        color: cardBg,
        borderRadius: BorderRadius.circular(AppRadius.card),
        border: Border.all(color: borderColor),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.bold,
              letterSpacing: 0.8,
              color: subText,
            ),
          ),
          const SizedBox(height: 8),
          ...children,
        ],
      ),
    );
  }

  Widget _buildActionRow({
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
    required Color mainText,
    required Color subText,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(AppRadius.md),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Row(
          children: [
            Icon(icon, size: 20, color: AppColors.primaryGreen),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold, color: mainText)),
                  Text(subtitle, style: TextStyle(fontSize: 11, color: subText)),
                ],
              ),
            ),
            Icon(Icons.chevron_right_rounded, size: 18, color: subText),
          ],
        ),
      ),
    );
  }

  Widget _buildThemeOption(String mode, String label, bool isDark) {
    final isSelected = _themeMode == mode;
    return Expanded(
      child: InkWell(
        onTap: () {
          setState(() => _themeMode = mode);
        },
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 10),
          decoration: BoxDecoration(
            color: isSelected
                ? AppColors.primaryGreen
                : (isDark ? AppColors.darkBackground : AppColors.mint),
            borderRadius: BorderRadius.circular(AppRadius.md),
            border: Border.all(
              color: isSelected ? AppColors.primaryGreen : AppColors.cardBorder,
            ),
          ),
          child: Center(
            child: Text(
              label,
              style: TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.bold,
                color: isSelected ? Colors.white : AppColors.darkGreen,
              ),
            ),
          ),
        ),
      ),
    );
  }
}

import 'package:flutter/material.dart';
import '../core/theme/app_colors.dart';
import '../core/theme/app_radius.dart';
import '../core/theme/app_spacing.dart';
import '../core/theme/app_text_styles.dart';

/// Phase 2 / Final Transformation — Play Store Ready Home Dashboard
class HomeDashboardScreen extends StatefulWidget {
  final String userName;
  final bool isDailyQuizCompleted;
  final String? stressLevel;
  final int? stressScore;
  final int? sleepHours;
  final int? planProgressPercent;
  final List<dynamic> recentStressHistory;
  final int? heartRateBpm;
  final String? heartRateTime;
  final String? heartRateSource;
  final void Function(int tabIndex)? onNavigateToTab;

  const HomeDashboardScreen({
    Key? key,
    this.userName = 'Vishwajeet',
    this.isDailyQuizCompleted = false,
    this.stressLevel,
    this.stressScore,
    this.sleepHours,
    this.planProgressPercent,
    this.recentStressHistory = const [],
    this.heartRateBpm,
    this.heartRateTime,
    this.heartRateSource,
    this.onNavigateToTab,
  }) : super(key: key);

  @override
  State<HomeDashboardScreen> createState() => _HomeDashboardScreenState();
}

class _HomeDashboardScreenState extends State<HomeDashboardScreen> {
  // Time-specific greeting based on device local time
  Map<String, String> _getTimeSpecificGreeting(String name) {
    final now = DateTime.now();
    final hour = now.hour;
    final minute = now.minute;
    final totalMinutes = hour * 60 + minute;

    if (totalMinutes >= 300 && totalMinutes <= 719) {
      return {
        'greeting': 'Good morning, $name 👋',
        'subtitle': 'Start your day with a quick check-in.',
      };
    } else if (totalMinutes >= 720 && totalMinutes <= 1019) {
      return {
        'greeting': 'Good afternoon, $name 👋',
        'subtitle': 'Take a moment to check in.',
      };
    } else if (totalMinutes >= 1020 && totalMinutes <= 1259) {
      return {
        'greeting': 'Good evening, $name 👋',
        'subtitle': 'Take a moment to unwind.',
      };
    } else {
      return {
        'greeting': 'Good night, $name 🌙',
        'subtitle': 'Wind down and rest.',
      };
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final greetingInfo = _getTimeSpecificGreeting(widget.userName);
    final cardBg = isDark ? AppColors.darkSurface : AppColors.white;
    final borderColor = isDark ? AppColors.darkCardBorder : AppColors.cardBorder;
    final mainText = isDark ? AppColors.darkMainText : AppColors.mainText;
    final subText = isDark ? AppColors.darkSecondaryText : AppColors.secondaryText;

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg, vertical: AppSpacing.md),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // --- 1. CLEAN APP HEADER ---
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      Container(
                        width: 36,
                        height: 36,
                        decoration: BoxDecoration(
                          color: isDark ? AppColors.darkSurface : AppColors.lightMint,
                          shape: BoxShape.circle,
                        ),
                        child: const Center(
                          child: Text('🌿', style: TextStyle(fontSize: 20)),
                        ),
                      ),
                      const SizedBox(width: 10),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'MentalCare AI',
                            style: AppTextStyles.titleSmall.copyWith(
                              fontWeight: FontWeight.bold,
                              color: isDark ? AppColors.darkGreen : AppColors.darkGreen,
                            ),
                          ),
                          Text(
                            'Understand. Relax. Thrive.',
                            style: TextStyle(
                              fontSize: 11,
                              color: subText,
                              letterSpacing: 0.2,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                  IconButton(
                    icon: Icon(Icons.notifications_none_rounded, color: subText),
                    tooltip: 'Reminders',
                    onPressed: () {
                      if (widget.onNavigateToTab != null) {
                        widget.onNavigateToTab!(4); // Profile & Settings
                      }
                    },
                  ),
                ],
              ),
              const SizedBox(height: AppSpacing.md),

              // --- 2. TIME-SPECIFIC GREETING ---
              Text(
                greetingInfo['greeting']!,
                style: AppTextStyles.headline.copyWith(
                  fontWeight: FontWeight.bold,
                  color: mainText,
                ),
              ),
              const SizedBox(height: 2),
              Text(
                greetingInfo['subtitle']!,
                style: AppTextStyles.bodyMedium.copyWith(color: subText),
              ),
              const SizedBox(height: AppSpacing.md),

              // --- 3. TODAY'S WELLNESS STATS CARD ---
              Container(
                padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg, vertical: AppSpacing.md),
                decoration: BoxDecoration(
                  color: cardBg,
                  borderRadius: BorderRadius.circular(AppRadius.card),
                  border: Border.all(color: borderColor),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          "TODAY'S WELLNESS",
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                            letterSpacing: 0.8,
                            color: isDark ? AppColors.primaryGreen : AppColors.darkGreen,
                          ),
                        ),
                        Icon(Icons.bolt_rounded, size: 16, color: AppColors.primaryGreen),
                      ],
                    ),
                    const SizedBox(height: AppSpacing.md),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        _buildStatColumn(
                          label: 'Stress',
                          value: widget.stressLevel ?? '--',
                          valueColor: _getStressColor(widget.stressLevel),
                          mainText: mainText,
                          subText: subText,
                        ),
                        Container(width: 1, height: 36, color: borderColor),
                        _buildStatColumn(
                          label: 'Sleep',
                          value: widget.sleepHours != null ? '${widget.sleepHours}h' : '--',
                          valueColor: mainText,
                          mainText: mainText,
                          subText: subText,
                        ),
                        Container(width: 1, height: 36, color: borderColor),
                        _buildStatColumn(
                          label: 'Progress',
                          value: widget.planProgressPercent != null
                              ? '${widget.planProgressPercent}%'
                              : '0%',
                          valueColor: AppColors.primaryGreen,
                          mainText: mainText,
                          subText: subText,
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.md),

              // --- 4. DAILY CHECK-IN CARD ---
              Container(
                padding: const EdgeInsets.all(AppSpacing.md),
                decoration: BoxDecoration(
                  color: widget.isDailyQuizCompleted
                      ? (isDark ? AppColors.darkSurface : AppColors.lightMint.withOpacity(0.4))
                      : cardBg,
                  borderRadius: BorderRadius.circular(AppRadius.card),
                  border: Border.all(
                    color: widget.isDailyQuizCompleted ? AppColors.primaryGreen : borderColor,
                  ),
                ),
                child: Row(
                  children: [
                    Container(
                      width: 44,
                      height: 44,
                      decoration: BoxDecoration(
                        color: widget.isDailyQuizCompleted
                            ? AppColors.primaryGreen.withOpacity(0.15)
                            : (isDark ? AppColors.darkBackground : AppColors.lightMint),
                        shape: BoxShape.circle,
                      ),
                      child: Center(
                        child: Icon(
                          widget.isDailyQuizCompleted
                              ? Icons.check_circle_rounded
                              : Icons.psychology_rounded,
                          color: widget.isDailyQuizCompleted
                              ? AppColors.darkGreen
                              : AppColors.primaryGreen,
                          size: 24,
                        ),
                      ),
                    ),
                    const SizedBox(width: AppSpacing.md),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            widget.isDailyQuizCompleted
                                ? "✓ Today's check-in complete"
                                : 'How are you feeling today?',
                            style: TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.bold,
                              color: mainText,
                            ),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            widget.isDailyQuizCompleted
                                ? 'Come back tomorrow.'
                                : 'Take a moment to check in with yourself.',
                            style: TextStyle(fontSize: 12, color: subText),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(width: AppSpacing.sm),
                    ElevatedButton(
                      onPressed: () {
                        if (widget.onNavigateToTab != null) {
                          widget.onNavigateToTab!(1); // Go to Check-in Quiz tab
                        }
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primaryGreen,
                        foregroundColor: Colors.white,
                        elevation: 0,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(AppRadius.button),
                        ),
                        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                      ),
                      child: Text(
                        widget.isDailyQuizCompleted ? 'View' : 'Start Check-in',
                        style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.md),

              // --- 5. STRESS TREND (COMPACT CARD) ---
              Container(
                padding: const EdgeInsets.all(AppSpacing.md),
                decoration: BoxDecoration(
                  color: cardBg,
                  borderRadius: BorderRadius.circular(AppRadius.card),
                  border: Border.all(color: borderColor),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          'STRESS TREND',
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                            letterSpacing: 0.8,
                            color: subText,
                          ),
                        ),
                        TextButton(
                          onPressed: () {
                            if (widget.onNavigateToTab != null) {
                              widget.onNavigateToTab!(2); // Go to Insights tab
                            }
                          },
                          style: TextButton.styleFrom(
                            padding: EdgeInsets.zero,
                            minimumSize: Size.zero,
                            tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                          ),
                          child: const Text(
                            'View Insights >',
                            style: TextStyle(
                              fontSize: 12,
                              color: AppColors.primaryGreen,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: AppSpacing.sm),
                    if (widget.recentStressHistory.isEmpty) ...[
                      Padding(
                        padding: const EdgeInsets.symmetric(vertical: 8.0),
                        child: Row(
                          children: [
                            Icon(Icons.show_chart_rounded, size: 28, color: subText.withOpacity(0.6)),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    'No trend yet',
                                    style: TextStyle(
                                      fontSize: 13,
                                      fontWeight: FontWeight.bold,
                                      color: mainText,
                                    ),
                                  ),
                                  Text(
                                    'Complete a few check-ins to see your pattern.',
                                    style: TextStyle(fontSize: 12, color: subText),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    ] else ...[
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            'Latest: ${widget.stressLevel ?? "Normal"} (${widget.stressScore ?? 35}%)',
                            style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold, color: mainText),
                          ),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                            decoration: BoxDecoration(
                              color: AppColors.primaryGreen.withOpacity(0.12),
                              borderRadius: BorderRadius.circular(10),
                            ),
                            child: const Text(
                              'Stable Pattern',
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.bold,
                                color: AppColors.darkGreen,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.md),

              // --- 6. HEART RATE TELEMETRY (COMPACT & NON-DIAGNOSTIC) ---
              Container(
                padding: const EdgeInsets.all(AppSpacing.md),
                decoration: BoxDecoration(
                  color: cardBg,
                  borderRadius: BorderRadius.circular(AppRadius.card),
                  border: Border.all(color: borderColor),
                ),
                child: Row(
                  children: [
                    Container(
                      width: 40,
                      height: 40,
                      decoration: BoxDecoration(
                        color: widget.heartRateBpm != null
                            ? AppColors.high.withOpacity(0.1)
                            : (isDark ? AppColors.darkBackground : AppColors.mint),
                        shape: BoxShape.circle,
                      ),
                      child: Icon(
                        Icons.favorite_rounded,
                        size: 22,
                        color: widget.heartRateBpm != null ? AppColors.high : subText,
                      ),
                    ),
                    const SizedBox(width: AppSpacing.md),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Text(
                                widget.heartRateBpm != null
                                    ? '${widget.heartRateBpm} BPM'
                                    : 'Connect Wearable',
                                style: TextStyle(
                                  fontSize: 14,
                                  fontWeight: FontWeight.bold,
                                  color: mainText,
                                ),
                              ),
                              const SizedBox(width: 6),
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                decoration: BoxDecoration(
                                  color: widget.heartRateBpm != null
                                      ? AppColors.lightMint
                                      : (isDark ? AppColors.darkBackground : Colors.grey.shade200),
                                  borderRadius: BorderRadius.circular(8),
                                ),
                                child: Text(
                                  widget.heartRateBpm != null ? 'Active' : 'Offline',
                                  style: TextStyle(
                                    fontSize: 10,
                                    fontWeight: FontWeight.bold,
                                    color: widget.heartRateBpm != null
                                        ? AppColors.darkGreen
                                        : subText,
                                  ),
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 2),
                          Text(
                            widget.heartRateBpm != null
                                ? 'Updated: ${widget.heartRateTime ?? "10:42 AM"} • Sensor Telemetry'
                                : 'Connect a supported health source to view heart rate.',
                            style: TextStyle(fontSize: 11, color: subText),
                          ),
                        ],
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.chevron_right_rounded, size: 20),
                      color: subText,
                      onPressed: () {
                        if (widget.onNavigateToTab != null) {
                          widget.onNavigateToTab!(4); // Profile/Settings
                        }
                      },
                    ),
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.lg),

              // --- 7. QUICK ACTIONS (MODERN ICONS) ---
              Text(
                'QUICK ACTIONS',
                style: TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 0.8,
                  color: subText,
                ),
              ),
              const SizedBox(height: AppSpacing.sm),
              Row(
                children: [
                  _buildQuickActionItem(
                    icon: Icons.menu_book_rounded,
                    label: 'Journal',
                    color: AppColors.aiPurple,
                    cardBg: cardBg,
                    borderColor: borderColor,
                    mainText: mainText,
                    onTap: () {
                      if (widget.onNavigateToTab != null) {
                        widget.onNavigateToTab!(3); // Wellness tab
                      }
                    },
                  ),
                  const SizedBox(width: AppSpacing.sm),
                  _buildQuickActionItem(
                    icon: Icons.insights_rounded,
                    label: 'Insights',
                    color: AppColors.primaryGreen,
                    cardBg: cardBg,
                    borderColor: borderColor,
                    mainText: mainText,
                    onTap: () {
                      if (widget.onNavigateToTab != null) {
                        widget.onNavigateToTab!(2); // Insights tab
                      }
                    },
                  ),
                  const SizedBox(width: AppSpacing.sm),
                  _buildQuickActionItem(
                    icon: Icons.self_improvement_rounded,
                    label: 'Relax',
                    color: AppColors.darkGreen,
                    cardBg: cardBg,
                    borderColor: borderColor,
                    mainText: mainText,
                    onTap: () {
                      if (widget.onNavigateToTab != null) {
                        widget.onNavigateToTab!(3); // Wellness tab
                      }
                    },
                  ),
                  const SizedBox(width: AppSpacing.sm),
                  _buildQuickActionItem(
                    icon: Icons.flag_rounded,
                    label: 'Goals',
                    color: AppColors.moderate,
                    cardBg: cardBg,
                    borderColor: borderColor,
                    mainText: mainText,
                    onTap: () {
                      if (widget.onNavigateToTab != null) {
                        widget.onNavigateToTab!(3); // Wellness tab
                      }
                    },
                  ),
                ],
              ),
              const SizedBox(height: AppSpacing.xl),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStatColumn({
    required String label,
    required String value,
    required Color valueColor,
    required Color mainText,
    required Color subText,
  }) {
    return Column(
      children: [
        Text(
          value,
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: valueColor,
          ),
        ),
        const SizedBox(height: 2),
        Text(
          label,
          style: TextStyle(
            fontSize: 12,
            color: subText,
            fontWeight: FontWeight.w500,
          ),
        ),
      ],
    );
  }

  Widget _buildQuickActionItem({
    required IconData icon,
    required String label,
    required Color color,
    required Color cardBg,
    required Color borderColor,
    required Color mainText,
    required VoidCallback onTap,
  }) {
    return Expanded(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(AppRadius.card),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 14),
          decoration: BoxDecoration(
            color: cardBg,
            borderRadius: BorderRadius.circular(AppRadius.card),
            border: Border.all(color: borderColor),
          ),
          child: Column(
            children: [
              Icon(icon, color: color, size: 24),
              const SizedBox(height: 6),
              Text(
                label,
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                  color: mainText,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Color _getStressColor(String? level) {
    switch (level?.toUpperCase()) {
      case 'LOW':
        return AppColors.primaryGreen;
      case 'MODERATE':
        return AppColors.moderate;
      case 'HIGH':
        return AppColors.high;
      default:
        return AppColors.primaryGreen;
    }
  }
}

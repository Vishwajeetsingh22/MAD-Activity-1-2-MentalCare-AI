import 'dart:math';
import 'package:flutter/material.dart';
import '../core/theme/app_colors.dart';
import '../core/theme/app_radius.dart';
import '../core/theme/app_spacing.dart';

/// Data model representing a single check-in entry
class CheckInRecord {
  final String id;
  final String localDate; // 'yyyy-MM-dd'
  final DateTime timestamp;
  final String stressLevel; // 'LOW', 'MODERATE', 'HIGH'
  final int stressScore; // 0 to 100
  final String userText;
  final String indicators;

  CheckInRecord({
    required this.id,
    required this.localDate,
    required this.timestamp,
    required this.stressLevel,
    required this.stressScore,
    this.userText = '',
    this.indicators = 'Mood: Stable',
  });
}

/// Phase 5 / Play Store UI — Compact Wellness Insights Screen
class StressInsightsScreen extends StatelessWidget {
  final List<CheckInRecord> checkInHistory;
  final int journalCount;
  final String? userName;

  const StressInsightsScreen({
    Key? key,
    this.checkInHistory = const [],
    this.journalCount = 0,
    this.userName,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final cardBg = isDark ? AppColors.darkSurface : AppColors.white;
    final borderColor = isDark ? AppColors.darkCardBorder : AppColors.cardBorder;
    final mainText = isDark ? AppColors.darkMainText : AppColors.mainText;
    final subText = isDark ? AppColors.darkSecondaryText : AppColors.secondaryText;

    // Deduplication Rule:
    // Multiple submissions on the SAME local calendar day are counted as ONE calendar day
    final Map<String, CheckInRecord> uniqueByDate = {};
    for (final record in checkInHistory) {
      if (!uniqueByDate.containsKey(record.localDate)) {
        uniqueByDate[record.localDate] = record;
      }
    }

    final distinctRecords = uniqueByDate.values.toList();
    distinctRecords.sort((a, b) => b.timestamp.compareTo(a.timestamp));

    final latestRecord = distinctRecords.isNotEmpty ? distinctRecords.first : null;
    final recentDistinct = distinctRecords.take(7).toList();
    final chronological7Days = recentDistinct.reversed.toList();
    final scores = chronological7Days.map((e) => e.stressScore).toList();
    final dayLabels = chronological7Days.map((e) => _formatDayLabel(e.timestamp)).toList();
    final uniqueDaysThisWeek = min(7, distinctRecords.length);

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: Text(
          'Your Wellness Insights',
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: isDark ? AppColors.white : AppColors.darkGreen,
          ),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg, vertical: AppSpacing.sm),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // --- 1. CURRENT STRESS STATUS ---
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
                          'CURRENT STRESS',
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                            letterSpacing: 0.8,
                            color: subText,
                          ),
                        ),
                        Text(
                          latestRecord != null ? _formatDate(latestRecord.timestamp) : 'Today',
                          style: TextStyle(fontSize: 11, color: subText),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    if (latestRecord == null) ...[
                      Text(
                        'No check-ins yet',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: mainText,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        'Complete your first check-in.',
                        style: TextStyle(fontSize: 12, color: subText),
                      ),
                    ] else ...[
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            '${latestRecord.stressLevel} Stress',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                              color: mainText,
                            ),
                          ),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 3),
                            decoration: BoxDecoration(
                              color: _getStressColor(latestRecord.stressLevel).withOpacity(0.12),
                              borderRadius: BorderRadius.circular(10),
                            ),
                            child: Text(
                              '${latestRecord.stressScore}% Index',
                              style: TextStyle(
                                fontSize: 12,
                                fontWeight: FontWeight.bold,
                                color: _getStressColor(latestRecord.stressLevel),
                              ),
                            ),
                          ),
                        ],
                      ),
                      if (latestRecord.userText.isNotEmpty) ...[
                        const SizedBox(height: 4),
                        Text(
                          '"${latestRecord.userText}"',
                          style: TextStyle(fontSize: 12, color: subText, fontStyle: FontStyle.italic),
                        ),
                      ],
                    ],
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.md),

              // --- 2. RECENT PATTERN (COMPACT CHART) ---
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
                          'RECENT PATTERN',
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                            letterSpacing: 0.8,
                            color: subText,
                          ),
                        ),
                        if (scores.isNotEmpty)
                          Text(
                            '7-Day Trend',
                            style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: AppColors.primaryGreen),
                          ),
                      ],
                    ),
                    const SizedBox(height: 10),
                    if (scores.isEmpty) ...[
                      Padding(
                        padding: const EdgeInsets.symmetric(vertical: 14.0),
                        child: Center(
                          child: Column(
                            children: [
                              Icon(Icons.show_chart_rounded, size: 32, color: subText.withOpacity(0.5)),
                              const SizedBox(height: 6),
                              Text(
                                'No trend yet',
                                style: TextStyle(
                                  fontSize: 13,
                                  fontWeight: FontWeight.bold,
                                  color: mainText,
                                ),
                              ),
                              const SizedBox(height: 2),
                              Text(
                                'Complete a few check-ins to see your pattern.',
                                style: TextStyle(fontSize: 12, color: subText),
                                textAlign: TextAlign.center,
                              ),
                            ],
                          ),
                        ),
                      ),
                    ] else ...[
                      SizedBox(
                        height: 130,
                        width: double.infinity,
                        child: CustomPaint(
                          painter: StressTrendChartPainter(scores: scores, labels: dayLabels),
                        ),
                      ),
                    ],
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.md),

              // --- 3. CHECK-IN PROGRESS ---
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
                          'Check-in Progress',
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.bold,
                            color: mainText,
                          ),
                        ),
                        Text(
                          '$uniqueDaysThisWeek / 7 days',
                          style: const TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.bold,
                            color: AppColors.primaryGreen,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    ClipRRect(
                      borderRadius: BorderRadius.circular(4),
                      child: LinearProgressIndicator(
                        value: uniqueDaysThisWeek / 7.0,
                        minHeight: 6,
                        backgroundColor: isDark ? AppColors.darkBackground : AppColors.cardBorder,
                        valueColor: const AlwaysStoppedAnimation<Color>(AppColors.primaryGreen),
                      ),
                    ),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        Icon(Icons.local_fire_department_rounded, size: 16, color: AppColors.moderate),
                        const SizedBox(width: 4),
                        Text(
                          '$uniqueDaysThisWeek day streak',
                          style: TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.bold,
                            color: AppColors.moderate,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.md),

              // --- 4. YOUR INSIGHTS (SHORT CARDS BASED ONLY ON REAL DATA) ---
              Text(
                'YOUR INSIGHTS',
                style: TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 0.8,
                  color: subText,
                ),
              ),
              const SizedBox(height: AppSpacing.sm),
              _buildInsightCards(scores, latestRecord, cardBg, borderColor, mainText, subText),
              const SizedBox(height: AppSpacing.md),

              // --- 5. RECOMMENDED ACTIVITIES (COMPACT) ---
              Text(
                'RECOMMENDED FOR YOU',
                style: TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 0.8,
                  color: subText,
                ),
              ),
              const SizedBox(height: AppSpacing.sm),
              _buildRecommendedActivities(latestRecord?.stressLevel, cardBg, borderColor, mainText, subText),
              const SizedBox(height: AppSpacing.xl),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildInsightCards(
    List<int> scores,
    CheckInRecord? latest,
    Color cardBg,
    Color borderColor,
    Color mainText,
    Color subText,
  ) {
    if (scores.isEmpty) {
      return Container(
        padding: const EdgeInsets.all(AppSpacing.md),
        decoration: BoxDecoration(
          color: cardBg,
          borderRadius: BorderRadius.circular(AppRadius.card),
          border: Border.all(color: borderColor),
        ),
        child: Row(
          children: [
            const Text('🌿', style: TextStyle(fontSize: 18)),
            const SizedBox(width: 10),
            Expanded(
              child: Text(
                'Complete a check-in to see your personal trends.',
                style: TextStyle(fontSize: 12, color: subText),
              ),
            ),
          ],
        ),
      );
    }

    String shortInsight;
    String icon = '🌿';

    final avg = (scores.reduce((a, b) => a + b) / scores.length).round();
    if (avg >= 70) {
      shortInsight = 'Stress has been elevated recently. Take a short pause today.';
      icon = '🌬️';
    } else if (avg <= 40) {
      shortInsight = 'Your emotional balance is strong. Keep up your routine!';
      icon = '✨';
    } else {
      shortInsight = 'Moderate steady pattern detected. Stay hydrated and rest well.';
      icon = '🌿';
    }

    return Container(
      padding: const EdgeInsets.all(AppSpacing.md),
      decoration: BoxDecoration(
        color: cardBg,
        borderRadius: BorderRadius.circular(AppRadius.card),
        border: Border.all(color: borderColor),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(icon, style: const TextStyle(fontSize: 20)),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              shortInsight,
              style: TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w500,
                color: mainText,
                height: 1.3,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRecommendedActivities(
    String? stressLevel,
    Color cardBg,
    Color borderColor,
    Color mainText,
    Color subText,
  ) {
    final isHigh = stressLevel?.toUpperCase() == 'HIGH';
    final items = isHigh
        ? [
            ('🧘 Box Breathing', '4-minute calm rhythm'),
            ('🛑 Sensory Grounding', '5-4-3-2-1 mindful anchor'),
          ]
        : [
            ('🌬️ Deep Breathing', 'Quick 5-minute pause'),
            ('✍️ Reflection', 'Note 3 restful moments'),
          ];

    return Row(
      children: items.map((item) {
        return Expanded(
          child: Container(
            margin: const EdgeInsets.only(right: 8),
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
                  item.$1,
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.bold,
                    color: mainText,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  item.$2,
                  style: TextStyle(fontSize: 11, color: subText),
                ),
              ],
            ),
          ),
        );
      }).toList(),
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

  String _formatDate(DateTime dt) => '${dt.day}/${dt.month}/${dt.year}';
  String _formatDayLabel(DateTime dt) {
    const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    return days[dt.weekday - 1];
  }
}

/// CustomPainter for a compact stress chart
class StressTrendChartPainter extends CustomPainter {
  final List<int> scores;
  final List<String> labels;

  StressTrendChartPainter({required this.scores, required this.labels});

  @override
  void paint(Canvas canvas, Size size) {
    if (scores.isEmpty) return;

    const padding = 16.0;
    final w = size.width - (padding * 2);
    final h = size.height - (padding * 2);

    final linePaint = Paint()
      ..color = AppColors.primaryGreen
      ..strokeWidth = 2.5
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;

    final dotPaint = Paint()
      ..color = AppColors.primaryGreen
      ..style = PaintingStyle.fill;

    final gridPaint = Paint()
      ..color = AppColors.cardBorder.withOpacity(0.5)
      ..strokeWidth = 0.8;

    for (int lvl in [25, 50, 75]) {
      final y = padding + h * (1.0 - (lvl / 100.0));
      canvas.drawLine(Offset(padding, y), Offset(size.width - padding, y), gridPaint);
    }

    if (scores.length == 1) {
      final cx = padding + w / 2;
      final cy = padding + h * (1.0 - (scores[0] / 100.0));
      canvas.drawLine(Offset(padding, cy), Offset(size.width - padding, cy), linePaint);
      canvas.drawCircle(Offset(cx, cy), 5, dotPaint);
      return;
    }

    final stepX = w / (scores.length - 1);
    final points = <Offset>[];

    for (int i = 0; i < scores.length; i++) {
      final cx = padding + i * stepX;
      final cy = padding + h * (1.0 - (scores[i] / 100.0));
      points.add(Offset(cx, cy));
    }

    final path = Path();
    path.moveTo(points[0].dx, points[0].dy);

    for (int i = 1; i < points.length; i++) {
      final prev = points[i - 1];
      final curr = points[i];
      final cpx1 = (prev.dx + curr.dx) / 2;
      final cpy1 = prev.dy;
      final cpx2 = (prev.dx + curr.dx) / 2;
      final cpy2 = curr.dy;
      path.cubicTo(cpx1, cpy1, cpx2, cpy2, curr.dx, curr.dy);
    }

    canvas.drawPath(path, linePaint);

    for (int i = 0; i < points.length; i++) {
      canvas.drawCircle(points[i], 3.5, dotPaint);
      if (i < labels.length) {
        final tp = TextPainter(
          text: TextSpan(text: labels[i], style: const TextStyle(fontSize: 9, color: AppColors.secondaryText)),
          textDirection: TextDirection.ltr,
        )..layout();
        tp.paint(canvas, Offset(points[i].dx - (tp.width / 2), size.height - 12));
      }
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}

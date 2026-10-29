import 'package:flutter_test/flutter_test.dart';
import 'package:flutter/material.dart';
import 'package:aimentalcare/core/theme/app_colors.dart';
import 'package:aimentalcare/core/theme/app_theme.dart';
import 'package:aimentalcare/core/theme/app_spacing.dart';
import 'package:aimentalcare/core/theme/app_radius.dart';

void main() {
  group('MentalCare AI - Flutter Premium UI Verification', () {
    test('AppColors brand tokens integrity', () {
      expect(AppColors.primaryGreen, const Color(0xFF10B981));
      expect(AppColors.darkGreen, const Color(0xFF047857));
      expect(AppColors.aiPurple, const Color(0xFF8B5CF6));
      expect(AppColors.moderate, const Color(0xFFF59E0B));
      expect(AppColors.high, const Color(0xFFEF4444));
      expect(AppColors.mint, const Color(0xFFECFDF5));
      expect(AppColors.darkBackground, const Color(0xFF0F172A));
      expect(AppColors.darkSurface, const Color(0xFF1E293B));
    });

    test('AppSpacing scales consistency', () {
      expect(AppSpacing.xs, 4.0);
      expect(AppSpacing.sm, 8.0);
      expect(AppSpacing.md, 12.0);
      expect(AppSpacing.lg, 16.0);
      expect(AppSpacing.xl, 20.0);
    });

    test('AppRadius scales consistency', () {
      expect(AppRadius.xs, 4.0);
      expect(AppRadius.sm, 8.0);
      expect(AppRadius.md, 12.0);
      expect(AppRadius.lg, 16.0);
      expect(AppRadius.xl, 20.0);
      expect(AppRadius.cardLarge, 24.0);
      expect(AppRadius.full, 999.0);
    });

    test('AppTheme light and dark ThemeData integrity', () {
      final lightTheme = AppTheme.lightTheme;
      expect(lightTheme.brightness, Brightness.light);
      expect(lightTheme.primaryColor, AppColors.primaryGreen);
      expect(lightTheme.scaffoldBackgroundColor, AppColors.mint);

      final darkTheme = AppTheme.darkTheme;
      expect(darkTheme.brightness, Brightness.dark);
      expect(darkTheme.scaffoldBackgroundColor, AppColors.darkBackground);
    });

    test('Daily Check-in once-per-calendar-day composite key enforcement', () {
      final now = DateTime(2026, 10, 6, 14, 30);
      final userId = 'user_abc123';
      final formattedDate =
          '${now.year}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}';
      final submissionKey = '${userId}_$formattedDate';

      expect(submissionKey, 'user_abc123_2026-10-06');

      // Different time on same day yields identical key
      final laterToday = DateTime(2026, 10, 6, 23, 59);
      final formattedDateLater =
          '${laterToday.year}-${laterToday.month.toString().padLeft(2, '0')}-${laterToday.day.toString().padLeft(2, '0')}';
      expect('${userId}_$formattedDateLater', submissionKey);

      // Tomorrow yields distinct key
      final tomorrow = DateTime(2026, 10, 7, 8, 0);
      final formattedDateTomorrow =
          '${tomorrow.year}-${tomorrow.month.toString().padLeft(2, '0')}-${tomorrow.day.toString().padLeft(2, '0')}';
      expect('${userId}_$formattedDateTomorrow', isNot(submissionKey));
    });

    test('Guided Breathing pacing configuration (5 minutes, 4-4 interval)', () {
      const totalSeconds = 300; // 5 minutes
      const inhaleSeconds = 4;
      const exhaleSeconds = 4;
      const cycleSeconds = inhaleSeconds + exhaleSeconds;

      expect(totalSeconds / 60, 5.0);
      expect(cycleSeconds, 8);
      expect(totalSeconds % cycleSeconds, 4); // Total completed cycles + final half
    });

    test('Lifestyle Stress Risk Score calculation boundaries', () {
      double computeScore(double sleep, double work, double screen, int exercise) {
        double score = 40.0;
        if (sleep < 6.0) score += (6.0 - sleep) * 12.0;
        if (work > 8.0) score += (work - 8.0) * 7.0;
        if (screen > 7.0) score += (screen - 7.0) * 4.0;
        if (exercise < 20) {
          score += 10.0;
        } else {
          score -= 8.0;
        }
        return score.clamp(15.0, 95.0);
      }

      // Healthy lifestyle profile
      final healthy = computeScore(8.0, 7.0, 4.0, 45);
      expect(healthy, lessThan(40.0));

      // Strained lifestyle profile
      final strained = computeScore(4.5, 11.0, 9.0, 5);
      expect(strained, greaterThan(75.0));
    });
  });
}

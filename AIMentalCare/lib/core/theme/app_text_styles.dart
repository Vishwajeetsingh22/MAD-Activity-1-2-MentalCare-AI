import 'package:flutter/material.dart';
import 'app_colors.dart';

/// MentalCare AI - Typography Hierarchy
class AppTextStyles {
  static const TextStyle display = TextStyle(
    fontSize: 28,
    fontWeight: FontWeight.bold,
    color: AppColors.mainText,
    letterSpacing: -0.5,
  );

  static const TextStyle headline = TextStyle(
    fontSize: 22,
    fontWeight: FontWeight.bold,
    color: AppColors.mainText,
    letterSpacing: -0.2,
  );

  static const TextStyle title = TextStyle(
    fontSize: 18,
    fontWeight: FontWeight.bold,
    color: AppColors.mainText,
  );

  static const TextStyle titleMedium = TextStyle(
    fontSize: 16,
    fontWeight: FontWeight.bold,
    color: AppColors.mainText,
  );

  static const TextStyle titleSmall = TextStyle(
    fontSize: 15,
    fontWeight: FontWeight.w600,
    color: AppColors.mainText,
  );

  static const TextStyle headingLarge = display;
  static const TextStyle headingMedium = headline;
  static const TextStyle headingSmall = title;
  static const TextStyle h2 = headline;
  static const TextStyle h3 = title;

  static const TextStyle bodyLarge = TextStyle(
    fontSize: 16,
    fontWeight: FontWeight.normal,
    color: AppColors.mainText,
    height: 1.45,
  );

  static const TextStyle body = TextStyle(
    fontSize: 14,
    fontWeight: FontWeight.normal,
    color: AppColors.mainText,
    height: 1.45,
  );

  static const TextStyle bodyMedium = body;

  static const TextStyle bodySecondary = TextStyle(
    fontSize: 14,
    fontWeight: FontWeight.normal,
    color: AppColors.secondaryText,
    height: 1.45,
  );

  static const TextStyle caption = TextStyle(
    fontSize: 12,
    fontWeight: FontWeight.normal,
    color: AppColors.secondaryText,
  );

  static const TextStyle bodySmall = caption;

  static const TextStyle label = TextStyle(
    fontSize: 11,
    fontWeight: FontWeight.bold,
    color: AppColors.primaryGreen,
    letterSpacing: 0.8,
  );

  static const TextStyle button = TextStyle(
    fontSize: 14,
    fontWeight: FontWeight.bold,
    letterSpacing: 0.2,
  );
}

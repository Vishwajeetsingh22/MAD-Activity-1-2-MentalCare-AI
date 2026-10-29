import 'package:flutter/material.dart';

/// MentalCare AI - Corner Radius Tokens (20-24px rounded card aesthetic)
class AppRadius {
  static const double xs = 4.0;
  static const double sm = 8.0;
  static const double md = 12.0;
  static const double lg = 16.0;
  static const double xl = 20.0;
  static const double card = 20.0;
  static const double cardLarge = 24.0;
  static const double button = 12.0;
  static const double input = 12.0;
  static const double chip = 16.0;
  static const double pill = 999.0;
  static const double full = 999.0;

  // BorderRadius helpers
  static const BorderRadius borderRadiusCard = BorderRadius.all(Radius.circular(card));
  static const BorderRadius borderRadiusCardLarge = BorderRadius.all(Radius.circular(cardLarge));
  static const BorderRadius borderRadiusButton = BorderRadius.all(Radius.circular(button));
  static const BorderRadius borderRadiusInput = BorderRadius.all(Radius.circular(input));
  static const BorderRadius borderRadiusChip = BorderRadius.all(Radius.circular(chip));
  static const BorderRadius borderRadiusPill = BorderRadius.all(Radius.circular(pill));
}

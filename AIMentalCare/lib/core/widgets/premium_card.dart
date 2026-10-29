import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_radius.dart';
import '../theme/app_shadows.dart';
import '../theme/app_spacing.dart';

/// Reusable Premium Card with 20-24px rounded corners and subtle wellness border
class PremiumCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry padding;
  final VoidCallback? onTap;
  final Color? backgroundColor;
  final Color? borderColor;

  const PremiumCard({
    Key? key,
    required this.child,
    this.padding = AppSpacing.paddingCard,
    this.onTap,
    this.backgroundColor,
    this.borderColor,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final defaultBg = isDark ? AppColors.darkSurface : AppColors.white;
    final defaultBorder = isDark ? AppColors.darkCardBorder : AppColors.cardBorder;

    Widget cardWidget = Container(
      decoration: BoxDecoration(
        color: backgroundColor ?? defaultBg,
        borderRadius: AppRadius.borderRadiusCard,
        border: Border.all(color: borderColor ?? defaultBorder, width: 1.0),
        boxShadow: isDark ? AppShadows.none : AppShadows.card,
      ),
      padding: padding,
      child: child,
    );

    if (onTap != null) {
      return Material(
        color: Colors.transparent,
        child: InkWell(
          borderRadius: AppRadius.borderRadiusCard,
          onTap: onTap,
          child: cardWidget,
        ),
      );
    }

    return cardWidget;
  }
}

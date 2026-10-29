import 'dart:async';
import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_spacing.dart';
import '../../core/theme/app_radius.dart';
import '../../core/theme/app_text_styles.dart';
import '../../core/widgets/primary_button.dart';
import '../../services/auth_service.dart';
import 'phone_verification_screen.dart';
import 'register_screen.dart';

/// MentalCare AI - Email Verification Screen
class EmailVerificationScreen extends StatefulWidget {
  final String email;
  final String phoneNumber;

  const EmailVerificationScreen({
    Key? key,
    required this.email,
    required this.phoneNumber,
  }) : super(key: key);

  @override
  State<EmailVerificationScreen> createState() => _EmailVerificationScreenState();
}

class _EmailVerificationScreenState extends State<EmailVerificationScreen> {
  bool _isChecking = false;
  bool _isResending = false;
  int _cooldownSeconds = 45;
  Timer? _cooldownTimer;
  String? _statusMessage;
  bool _isError = false;

  @override
  void initState() {
    super.initState();
    _startCooldownTimer();
  }

  @override
  void dispose() {
    _cooldownTimer?.cancel();
    super.dispose();
  }

  void _startCooldownTimer() {
    setState(() => _cooldownSeconds = 45);
    _cooldownTimer?.cancel();
    _cooldownTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_cooldownSeconds > 0) {
        setState(() => _cooldownSeconds--);
      } else {
        timer.cancel();
      }
    });
  }

  Future<void> _checkEmailVerified() async {
    setState(() {
      _isChecking = true;
      _statusMessage = null;
      _isError = false;
    });

    final isVerified = await AuthService().checkEmailVerified();

    if (!mounted) return;
    setState(() => _isChecking = false);

    if (isVerified) {
      // Proceed directly to Phone Verification
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(
          builder: (_) => PhoneVerificationScreen(
            phoneNumber: widget.phoneNumber,
            isLinking: true,
          ),
        ),
      );
    } else {
      setState(() {
        _isError = true;
        _statusMessage = "Please verify your email before continuing. Tap the link sent to your inbox.";
      });
    }
  }

  Future<void> _handleResendEmail() async {
    if (_cooldownSeconds > 0 || _isResending) return;

    setState(() {
      _isResending = true;
      _statusMessage = null;
      _isError = false;
    });

    await AuthService().sendEmailVerification();

    if (!mounted) return;
    setState(() {
      _isResending = false;
      _statusMessage = "A fresh verification link has been sent to ${widget.email}.";
    });
    _startCooldownTimer();
  }

  void _simulateEmailLinkClick() {
    // Allows testing/demoing email verification
    AuthService().simulateEmailVerifiedByLink();
    setState(() {
      _statusMessage = "Email verified! Tap 'I\'ve verified my email' to continue.";
      _isError = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final bgColor = isDark ? AppColors.darkBackground : AppColors.mint;
    final cardBg = isDark ? AppColors.darkSurface : AppColors.white;
    final borderColor = isDark ? AppColors.darkCardBorder : AppColors.cardBorder;
    final mainText = isDark ? AppColors.darkMainText : AppColors.mainText;
    final subText = isDark ? AppColors.darkSecondaryText : AppColors.secondaryText;

    return Scaffold(
      backgroundColor: bgColor,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back_rounded, color: mainText),
          onPressed: () {
            Navigator.of(context).pushReplacement(
              MaterialPageRoute(builder: (_) => const RegisterScreen()),
            );
          },
        ),
      ),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg, vertical: AppSpacing.md),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Mail Icon Banner
                Center(
                  child: Container(
                    width: 80,
                    height: 80,
                    decoration: BoxDecoration(
                      color: isDark ? AppColors.darkSurface : AppColors.white,
                      shape: BoxShape.circle,
                      boxShadow: [
                        BoxShadow(
                          color: AppColors.primaryGreen.withOpacity(0.18),
                          blurRadius: 16,
                          offset: const Offset(0, 4),
                        ),
                      ],
                    ),
                    child: const Center(
                      child: Text('✉️', style: TextStyle(fontSize: 38)),
                    ),
                  ),
                ),
                const SizedBox(height: AppSpacing.lg),

                Text(
                  'Verify your email',
                  textAlign: TextAlign.center,
                  style: AppTextStyles.headingMedium.copyWith(
                    color: isDark ? AppColors.darkGreen : AppColors.darkGreen,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  "We've sent a verification link to:",
                  textAlign: TextAlign.center,
                  style: AppTextStyles.bodyMedium.copyWith(color: subText),
                ),
                const SizedBox(height: 4),
                Text(
                  widget.email,
                  textAlign: TextAlign.center,
                  style: AppTextStyles.bodyLarge.copyWith(
                    fontWeight: FontWeight.bold,
                    color: mainText,
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  "Please verify your email before continuing.",
                  textAlign: TextAlign.center,
                  style: AppTextStyles.bodySmall.copyWith(color: subText),
                ),
                const SizedBox(height: AppSpacing.lg),

                // Status notice
                if (_statusMessage != null) ...[
                  Container(
                    padding: const EdgeInsets.all(AppSpacing.md),
                    decoration: BoxDecoration(
                      color: _isError ? AppColors.high.withOpacity(0.1) : AppColors.primaryGreen.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(AppRadius.md),
                      border: Border.all(
                        color: _isError ? AppColors.high.withOpacity(0.4) : AppColors.primaryGreen.withOpacity(0.4),
                      ),
                    ),
                    child: Row(
                      children: [
                        Icon(
                          _isError ? Icons.info_outline_rounded : Icons.check_circle_outline_rounded,
                          color: _isError ? AppColors.high : AppColors.primaryGreen,
                          size: 20,
                        ),
                        const SizedBox(width: 10),
                        Expanded(
                          child: Text(
                            _statusMessage!,
                            style: TextStyle(
                              color: _isError ? AppColors.high : AppColors.darkGreen,
                              fontSize: 13,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: AppSpacing.lg),
                ],

                // Action Card
                Container(
                  padding: const EdgeInsets.all(AppSpacing.lg),
                  decoration: BoxDecoration(
                    color: cardBg,
                    borderRadius: BorderRadius.circular(AppRadius.lg),
                    border: Border.all(color: borderColor),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      // "I've verified my email" button
                      PrimaryButton(
                        text: "I've verified my email",
                        isLoading: _isChecking,
                        onPressed: _checkEmailVerified,
                      ),
                      const SizedBox(height: AppSpacing.md),

                      // Resend verification button with cooldown timer
                      OutlinedButton(
                        onPressed: _cooldownSeconds == 0 && !_isResending ? _handleResendEmail : null,
                        style: OutlinedButton.styleFrom(
                          minimumSize: const Size.fromHeight(48),
                          side: BorderSide(
                            color: _cooldownSeconds == 0 ? AppColors.primaryGreen : borderColor,
                          ),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(AppRadius.md),
                          ),
                        ),
                        child: Text(
                          _cooldownSeconds > 0
                              ? "Resend available in ${_cooldownSeconds}s"
                              : "Resend verification email",
                          style: TextStyle(
                            color: _cooldownSeconds == 0 ? AppColors.primaryGreen : subText,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                      const SizedBox(height: AppSpacing.sm),

                      // Change email button
                      TextButton(
                        onPressed: () {
                          Navigator.of(context).pushReplacement(
                            MaterialPageRoute(builder: (_) => const RegisterScreen()),
                          );
                        },
                        child: Text(
                          "Change email",
                          style: TextStyle(color: subText, fontSize: 13),
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: AppSpacing.lg),

                // Quick test simulation helper for academic demo review
                Center(
                  child: TextButton.icon(
                    onPressed: _simulateEmailLinkClick,
                    icon: const Icon(Icons.bolt_rounded, size: 16, color: AppColors.aiPurple),
                    label: const Text(
                      "Simulate Clicking Email Link (Demo Mode)",
                      style: TextStyle(color: AppColors.aiPurple, fontSize: 12),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

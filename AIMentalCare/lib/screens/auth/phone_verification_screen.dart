import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_spacing.dart';
import '../../core/theme/app_radius.dart';
import '../../core/theme/app_text_styles.dart';
import '../../core/widgets/primary_button.dart';
import '../../services/auth_service.dart';
import 'account_setup_screen.dart';
import '../../main.dart';

/// MentalCare AI - Phone Number OTP Verification Screen
///
/// Supports:
/// 1. Linking mode (New User: email registered -> verifying phone -> links credential to current user)
/// 2. Login mode (Existing User: verifying phone -> sign in with OTP -> routes to dashboard/setup)
class PhoneVerificationScreen extends StatefulWidget {
  final String phoneNumber;
  final bool isLinking; // true for new user registration linking, false for phone login
  final String? verificationId;

  const PhoneVerificationScreen({
    Key? key,
    required this.phoneNumber,
    this.isLinking = true,
    this.verificationId,
  }) : super(key: key);

  @override
  State<PhoneVerificationScreen> createState() => _PhoneVerificationScreenState();
}

class _PhoneVerificationScreenState extends State<PhoneVerificationScreen> {
  final List<TextEditingController> _controllers =
      List.generate(6, (_) => TextEditingController());
  final List<FocusNode> _focusNodes = List.generate(6, (_) => FocusNode());

  bool _isVerifying = false;
  bool _isResending = false;
  int _cooldownSeconds = 30;
  Timer? _cooldownTimer;
  String? _errorMessage;
  String? _successMessage;
  String _activeVerificationId = 'vid_initial';

  @override
  void initState() {
    super.initState();
    _activeVerificationId = widget.verificationId ?? 'vid_${DateTime.now().millisecondsSinceEpoch}';
    _startCooldownTimer();
  }

  @override
  void dispose() {
    _cooldownTimer?.cancel();
    for (final controller in _controllers) {
      controller.dispose();
    }
    for (final node in _focusNodes) {
      node.dispose();
    }
    super.dispose();
  }

  void _startCooldownTimer() {
    setState(() => _cooldownSeconds = 30);
    _cooldownTimer?.cancel();
    _cooldownTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_cooldownSeconds > 0) {
        setState(() => _cooldownSeconds--);
      } else {
        timer.cancel();
      }
    });
  }

  String get _enteredOtp =>
      _controllers.map((c) => c.text.trim()).join();

  void _onDigitChanged(int index, String value) {
    setState(() {
      _errorMessage = null;
    });

    if (value.isNotEmpty) {
      // If user pasted multi-digit text
      if (value.length > 1) {
        final digits = value.replaceAll(RegExp(r'\D'), '');
        for (int i = 0; i < 6 && i < digits.length; i++) {
          _controllers[i].text = digits[i];
        }
        final nextIndex = digits.length < 6 ? digits.length : 5;
        _focusNodes[nextIndex].requestFocus();
        if (digits.length >= 6) {
          _verifyOtp();
        }
        return;
      }

      // Single digit advance
      if (index < 5) {
        _focusNodes[index + 1].requestFocus();
      } else {
        _focusNodes[index].unfocus();
        if (_enteredOtp.length == 6) {
          _verifyOtp();
        }
      }
    } else {
      // Backspace
      if (index > 0) {
        _focusNodes[index - 1].requestFocus();
      }
    }
  }

  Future<void> _verifyOtp() async {
    final otp = _enteredOtp;
    if (otp.length != 6) {
      setState(() {
        _errorMessage = "Please enter the complete 6-digit verification code.";
      });
      return;
    }

    setState(() {
      _isVerifying = true;
      _errorMessage = null;
    });

    final authService = AuthService();
    AuthResult result;

    if (widget.isLinking) {
      // LINK phone credential to existing user profile (PREVENTS DUPLICATE ACCOUNTS)
      result = await authService.verifyAndLinkPhoneOtp(
        verificationId: _activeVerificationId,
        smsCode: otp,
      );
    } else {
      // Existing user phone OTP login
      result = await authService.signInWithPhoneOtp(
        verificationId: _activeVerificationId,
        smsCode: otp,
      );
    }

    if (!mounted) return;
    setState(() => _isVerifying = false);

    if (result.isSuccess) {
      final user = result.userProfile;
      if (widget.isLinking || user == null || !user.accountSetupCompleted) {
        // Direct transition into Account Setup (No second login required!)
        Navigator.of(context).pushAndRemoveUntil(
          MaterialPageRoute(builder: (_) => const AccountSetupScreen()),
          (route) => false,
        );
      } else {
        // Existing user who already completed account setup -> Direct to Dashboard
        Navigator.of(context).pushAndRemoveUntil(
          MaterialPageRoute(builder: (_) => const MentalCareNavigationHost()),
          (route) => false,
        );
      }
    } else {
      setState(() {
        _errorMessage = result.errorMessage ?? "Invalid verification code. Please try again.";
      });
    }
  }

  Future<void> _handleResendOtp() async {
    if (_cooldownSeconds > 0 || _isResending) return;

    setState(() {
      _isResending = true;
      _errorMessage = null;
      _successMessage = null;
    });

    await AuthService().sendPhoneOtp(
      phoneNumber: widget.phoneNumber,
      onCodeSent: (newVid) {
        if (!mounted) return;
        setState(() {
          _activeVerificationId = newVid;
          _isResending = false;
          _successMessage = "A fresh 6-digit OTP has been sent to ${widget.phoneNumber}.";
        });
        _startCooldownTimer();
      },
      onError: (err) {
        if (!mounted) return;
        setState(() {
          _isResending = false;
          _errorMessage = err;
        });
      },
    );
  }

  void _fillDemoOtp() {
    const demoCode = "123456";
    for (int i = 0; i < 6; i++) {
      _controllers[i].text = demoCode[i];
    }
    setState(() {
      _errorMessage = null;
    });
    _verifyOtp();
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
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg, vertical: AppSpacing.md),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Branded Phone Icon
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
                      child: Text('📱', style: TextStyle(fontSize: 38)),
                    ),
                  ),
                ),
                const SizedBox(height: AppSpacing.lg),

                Text(
                  'Verify Mobile Number',
                  textAlign: TextAlign.center,
                  style: AppTextStyles.headingMedium.copyWith(
                    color: isDark ? AppColors.darkGreen : AppColors.darkGreen,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  'We sent a 6-digit verification code to:',
                  textAlign: TextAlign.center,
                  style: AppTextStyles.bodyMedium.copyWith(color: subText),
                ),
                const SizedBox(height: 4),
                Text(
                  widget.phoneNumber,
                  textAlign: TextAlign.center,
                  style: AppTextStyles.bodyLarge.copyWith(
                    fontWeight: FontWeight.bold,
                    color: mainText,
                  ),
                ),
                const SizedBox(height: AppSpacing.lg),

                // Error / Success notification banner
                if (_errorMessage != null) ...[
                  Container(
                    padding: const EdgeInsets.all(AppSpacing.md),
                    decoration: BoxDecoration(
                      color: AppColors.high.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(AppRadius.md),
                      border: Border.all(color: AppColors.high.withOpacity(0.4)),
                    ),
                    child: Row(
                      children: [
                        const Icon(Icons.error_outline_rounded, color: AppColors.high, size: 20),
                        const SizedBox(width: 10),
                        Expanded(
                          child: Text(
                            _errorMessage!,
                            style: const TextStyle(color: AppColors.high, fontSize: 13),
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: AppSpacing.md),
                ],

                if (_successMessage != null) ...[
                  Container(
                    padding: const EdgeInsets.all(AppSpacing.md),
                    decoration: BoxDecoration(
                      color: AppColors.primaryGreen.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(AppRadius.md),
                      border: Border.all(color: AppColors.primaryGreen.withOpacity(0.4)),
                    ),
                    child: Row(
                      children: [
                        const Icon(Icons.check_circle_outline_rounded, color: AppColors.primaryGreen, size: 20),
                        const SizedBox(width: 10),
                        Expanded(
                          child: Text(
                            _successMessage!,
                            style: const TextStyle(color: AppColors.darkGreen, fontSize: 13),
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: AppSpacing.md),
                ],

                // OTP Card
                Container(
                  padding: const EdgeInsets.all(AppSpacing.lg),
                  decoration: BoxDecoration(
                    color: cardBg,
                    borderRadius: BorderRadius.circular(AppRadius.lg),
                    border: Border.all(color: borderColor),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(0.04),
                        blurRadius: 10,
                        offset: const Offset(0, 4),
                      ),
                    ],
                  ),
                  child: Column(
                    children: [
                      // 6 Pin Digit Input Boxes
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: List.generate(6, (index) {
                          return SizedBox(
                            width: 44,
                            height: 54,
                            child: TextField(
                              controller: _controllers[index],
                              focusNode: _focusNodes[index],
                              textAlign: TextAlign.center,
                              keyboardType: TextInputType.number,
                              inputFormatters: [
                                FilteringTextInputFormatter.digitsOnly,
                                LengthLimitingTextInputFormatter(1),
                              ],
                              style: TextStyle(
                                fontSize: 22,
                                fontWeight: FontWeight.bold,
                                color: mainText,
                              ),
                              decoration: InputDecoration(
                                contentPadding: EdgeInsets.zero,
                                filled: true,
                                fillColor: isDark ? AppColors.darkBackground : AppColors.mint.withOpacity(0.4),
                                enabledBorder: OutlineInputBorder(
                                  borderRadius: BorderRadius.circular(AppRadius.sm),
                                  borderSide: BorderSide(
                                    color: _controllers[index].text.isNotEmpty
                                        ? AppColors.primaryGreen
                                        : borderColor,
                                    width: _controllers[index].text.isNotEmpty ? 1.5 : 1.0,
                                  ),
                                ),
                                focusedBorder: OutlineInputBorder(
                                  borderRadius: BorderRadius.circular(AppRadius.sm),
                                  borderSide: const BorderSide(
                                    color: AppColors.primaryGreen,
                                    width: 2.0,
                                  ),
                                ),
                              ),
                              onChanged: (val) => _onDigitChanged(index, val),
                            ),
                          );
                        }),
                      ),
                      const SizedBox(height: AppSpacing.xl),

                      // Verify Button
                      PrimaryButton(
                        text: widget.isLinking ? 'Verify & Continue' : 'Verify & Log In',
                        isLoading: _isVerifying,
                        onPressed: _enteredOtp.length == 6 ? _verifyOtp : null,
                      ),
                      const SizedBox(height: AppSpacing.md),

                      // Resend OTP timer button
                      OutlinedButton(
                        onPressed: _cooldownSeconds == 0 && !_isResending ? _handleResendOtp : null,
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
                              ? "Resend OTP in ${_cooldownSeconds}s"
                              : "Resend verification code",
                          style: TextStyle(
                            color: _cooldownSeconds == 0 ? AppColors.primaryGreen : subText,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                      const SizedBox(height: AppSpacing.sm),

                      // Change Number Button
                      TextButton(
                        onPressed: () => Navigator.of(context).pop(),
                        child: Text(
                          "Change mobile number",
                          style: TextStyle(color: subText, fontSize: 13),
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: AppSpacing.lg),

                // Quick Demo Auto-fill Helper for Viva/Testing
                Center(
                  child: TextButton.icon(
                    onPressed: _fillDemoOtp,
                    icon: const Icon(Icons.bolt_rounded, size: 16, color: AppColors.aiPurple),
                    label: const Text(
                      "Auto-fill Demo OTP: 123456",
                      style: TextStyle(color: AppColors.aiPurple, fontSize: 12, fontWeight: FontWeight.bold),
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

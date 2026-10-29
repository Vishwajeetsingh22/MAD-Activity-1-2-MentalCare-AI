import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_text_styles.dart';
import '../../services/auth_service.dart';
import 'login_screen.dart';
import 'email_verification_screen.dart';
import 'phone_verification_screen.dart';
import 'account_setup_screen.dart';
import '../../main.dart';

/// MentalCare AI - Branded Splash Screen & Authentication Router
class SplashScreen extends StatefulWidget {
  const SplashScreen({Key? key}) : super(key: key);

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen>
    with SingleTickerProviderStateMixin {
  late AnimationController _animController;
  late Animation<double> _fadeAnimation;

  @override
  void initState() {
    super.initState();
    _animController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1000),
    );
    _fadeAnimation = CurvedAnimation(
      parent: _animController,
      curve: Curves.easeIn,
    );
    _animController.forward();

    _navigateAfterDelay();
  }

  @override
  void dispose() {
    _animController.dispose();
    super.dispose();
  }

  Future<void> _navigateAfterDelay() async {
    await Future.delayed(const Duration(milliseconds: 1400));
    if (!mounted) return;

    final authService = AuthService();
    final flowState = authService.checkCurrentFlowState();

    Widget targetScreen;
    switch (flowState) {
      case AuthFlowState.unauthenticated:
        targetScreen = const LoginScreen();
        break;
      case AuthFlowState.needsEmailVerification:
        targetScreen = EmailVerificationScreen(
          email: authService.currentUser?.email ?? 'user@mentalcare.ai',
          phoneNumber: authService.currentUser?.phoneNumber ?? '+91 98765 43210',
        );
        break;
      case AuthFlowState.needsPhoneVerification:
        targetScreen = PhoneVerificationScreen(
          phoneNumber: authService.currentUser?.phoneNumber ?? '+91 98765 43210',
          isLinking: true,
        );
        break;
      case AuthFlowState.needsAccountSetup:
        targetScreen = const AccountSetupScreen();
        break;
      case AuthFlowState.authenticatedAndReady:
        targetScreen = const MentalCareNavigationHost();
        break;
    }

    Navigator.of(context).pushReplacement(
      PageRouteBuilder(
        pageBuilder: (context, animation, secondaryAnimation) => targetScreen,
        transitionsBuilder: (context, animation, secondaryAnimation, child) {
          return FadeTransition(opacity: animation, child: child);
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      backgroundColor: isDark ? AppColors.darkBackground : AppColors.mint,
      body: Center(
        child: FadeTransition(
          opacity: _fadeAnimation,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Branded Wellness Icon
              Container(
                width: 96,
                height: 96,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: isDark ? AppColors.darkSurface : AppColors.white,
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.primaryGreen.withOpacity(0.2),
                      blurRadius: 20,
                      offset: const Offset(0, 6),
                    ),
                  ],
                ),
                child: const Center(
                  child: Text(
                    '🌿',
                    style: TextStyle(fontSize: 48),
                  ),
                ),
              ),
              const SizedBox(height: 24),

              // Title
              Text(
                'MentalCare AI',
                style: AppTextStyles.headingLarge.copyWith(
                  color: isDark ? AppColors.darkGreen : AppColors.darkGreen,
                  letterSpacing: 0.5,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),

              // Tagline
              Text(
                'Understand. Relax. Thrive.',
                style: AppTextStyles.bodyMedium.copyWith(
                  color: isDark ? AppColors.darkSecondaryText : AppColors.secondaryText,
                  letterSpacing: 0.8,
                ),
              ),
              const SizedBox(height: 36),

              // Subtle Loading Indicator
              const SizedBox(
                width: 24,
                height: 24,
                child: CircularProgressIndicator(
                  strokeWidth: 2.5,
                  valueColor: AlwaysStoppedAnimation<Color>(AppColors.primaryGreen),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

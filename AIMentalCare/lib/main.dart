import 'package:flutter/material.dart';
import 'core/theme/app_colors.dart';
import 'core/theme/app_theme.dart';
import 'core/widgets/ai_chat_button.dart';
import 'screens/home_dashboard_screen.dart';
import 'screens/daily_checkin_quiz_screen.dart';
import 'screens/stress_insights_screen.dart';
import 'screens/journal_and_wellness_screen.dart';
import 'screens/profile_and_settings_screen.dart';
import 'screens/ai_wellness_chat_screen.dart';
import 'screens/auth/splash_screen.dart';
import 'services/auth_service.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MentalCareApp());
}

/// MentalCare AI - Flutter Application Root
/// Tagline: Understand. Relax. Thrive.
class MentalCareApp extends StatefulWidget {
  const MentalCareApp({Key? key}) : super(key: key);

  static _MentalCareAppState? of(BuildContext context) =>
      context.findAncestorStateOfType<_MentalCareAppState>();

  @override
  State<MentalCareApp> createState() => _MentalCareAppState();
}

class _MentalCareAppState extends State<MentalCareApp> {
  ThemeMode _themeMode = ThemeMode.system;

  void setThemeMode(ThemeMode mode) {
    setState(() {
      _themeMode = mode;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'MentalCare AI',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: _themeMode,
      home: const SplashScreen(),
    );
  }
}

/// Main Navigation Shell with 5 bottom navigation tabs and floating AI Assistant button
class MentalCareNavigationHost extends StatefulWidget {
  const MentalCareNavigationHost({Key? key}) : super(key: key);

  @override
  State<MentalCareNavigationHost> createState() =>
      _MentalCareNavigationHostState();
}

class _MentalCareNavigationHostState extends State<MentalCareNavigationHost> {
  int _currentIndex = 0;

  void _onTabSelected(int index) {
    setState(() {
      _currentIndex = index;
    });
  }

  String get _currentUserName =>
      AuthService().currentUser?.fullName.isNotEmpty == true
          ? AuthService().currentUser!.fullName
          : 'Joylyn Fernandes';

  void _openAiChat() {
    Navigator.of(context).push(
      PageRouteBuilder(
        pageBuilder: (context, animation, secondaryAnimation) =>
            AIWellnessChatScreen(userName: _currentUserName),
        transitionsBuilder: (context, animation, secondaryAnimation, child) {
          const begin = Offset(0.0, 1.0);
          const end = Offset.zero;
          const curve = Curves.easeOutCubic;
          final tween =
              Tween(begin: begin, end: end).chain(CurveTween(curve: curve));
          return SlideTransition(
            position: animation.drive(tween),
            child: child,
          );
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final navBg = isDark ? AppColors.darkSurface : AppColors.white;
    final unselectedColor =
        isDark ? AppColors.darkSecondaryText : AppColors.secondaryText;

    final screens = [
      HomeDashboardScreen(
        userName: _currentUserName,
        onNavigateToTab: _onTabSelected,
      ),
      DailyCheckInQuizScreen(
        userId: AuthService().currentUser?.uid ?? 'user123',
        onNavigateToInsights: () => _onTabSelected(2),
        onNavigateToDashboard: () => _onTabSelected(0),
      ),
      StressInsightsScreen(
        userName: _currentUserName,
      ),
      const JournalAndWellnessScreen(),
      const ProfileAndSettingsScreen(),
    ];

    return Scaffold(
      body: IndexedStack(
        index: _currentIndex,
        children: screens,
      ),
      floatingActionButton: _currentIndex != 1
          ? Padding(
              padding: const EdgeInsets.only(bottom: 8.0, right: 4.0),
              child: AIChatButton(
                onTap: _openAiChat,
                tooltip: 'AI Assistant',
              ),
            )
          : null,
      floatingActionButtonLocation: FloatingActionButtonLocation.endFloat,
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: _onTabSelected,
        backgroundColor: navBg,
        elevation: 2,
        height: 64,
        indicatorColor: isDark
            ? AppColors.primaryGreen.withOpacity(0.22)
            : AppColors.lightMint,
        destinations: [
          NavigationDestination(
            icon: Icon(Icons.dashboard_outlined, color: unselectedColor),
            selectedIcon:
                const Icon(Icons.dashboard_rounded, color: AppColors.darkGreen),
            label: 'Dashboard',
          ),
          NavigationDestination(
            icon: Icon(Icons.psychology_outlined, color: unselectedColor),
            selectedIcon:
                const Icon(Icons.psychology_rounded, color: AppColors.darkGreen),
            label: 'Check-in',
          ),
          NavigationDestination(
            icon: Icon(Icons.insights_outlined, color: unselectedColor),
            selectedIcon:
                const Icon(Icons.insights_rounded, color: AppColors.darkGreen),
            label: 'Insights',
          ),
          NavigationDestination(
            icon: Icon(Icons.self_improvement_outlined, color: unselectedColor),
            selectedIcon:
                const Icon(Icons.self_improvement_rounded, color: AppColors.darkGreen),
            label: 'Wellness',
          ),
          NavigationDestination(
            icon: Icon(Icons.person_outline_rounded, color: unselectedColor),
            selectedIcon:
                const Icon(Icons.person_rounded, color: AppColors.darkGreen),
            label: 'Profile',
          ),
        ],
      ),
    );
  }
}

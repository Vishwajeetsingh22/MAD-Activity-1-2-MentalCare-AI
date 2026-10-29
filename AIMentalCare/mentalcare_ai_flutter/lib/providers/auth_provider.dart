import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AuthProvider extends ChangeNotifier {
  String _userEmail = 'user@mentalcare.ai';
  String _userName = 'Friend';
  bool _isLoggedIn = false;
  bool _accountSetupCompleted = false;

  String get userEmail => _userEmail;
  String get userName => _userName;
  bool get isLoggedIn => _isLoggedIn;
  bool get accountSetupCompleted => _accountSetupCompleted;

  AuthProvider() {
    _loadUserSession();
  }

  Future<void> _loadUserSession() async {
    final prefs = await SharedPreferences.getInstance();
    _userEmail = prefs.getString('user_email') ?? 'user@mentalcare.ai';
    _userName = prefs.getString('user_name') ?? 'Friend';
    _isLoggedIn = prefs.containsKey('user_email');
    _accountSetupCompleted = prefs.getBool('account_setup_completed') ?? false;
    notifyListeners();
  }

  Future<void> register({
    required String name,
    required String email,
    required String password,
  }) async {
    final prefs = await SharedPreferences.getInstance();
    _userName = name;
    _userEmail = email;
    _isLoggedIn = true;
    _accountSetupCompleted = false;

    await prefs.setString('user_name', name);
    await prefs.setString('user_email', email);
    await prefs.setBool('account_setup_completed', false);
    notifyListeners();
  }

  Future<void> login({
    required String email,
    required String password,
  }) async {
    final prefs = await SharedPreferences.getInstance();
    _userEmail = email;
    final fallbackName = email.split('@').first;
    _userName = prefs.getString('user_name') ?? (fallbackName.isNotEmpty ? fallbackName[0].toUpperCase() + fallbackName.substring(1) : 'Friend');
    _isLoggedIn = true;
    _accountSetupCompleted = prefs.getBool('account_setup_completed') ?? true;

    await prefs.setString('user_email', email);
    await prefs.setString('user_name', _userName);
    notifyListeners();
  }

  Future<void> completeAccountSetup(String name) async {
    final prefs = await SharedPreferences.getInstance();
    _userName = name;
    _accountSetupCompleted = true;

    await prefs.setString('user_name', name);
    await prefs.setBool('account_setup_completed', true);
    notifyListeners();
  }

  Future<void> updateProfile({required String name, required String email}) async {
    final prefs = await SharedPreferences.getInstance();
    _userName = name;
    _userEmail = email;

    await prefs.setString('user_name', name);
    await prefs.setString('user_email', email);
    notifyListeners();
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('user_email');
    await prefs.remove('user_name');
    await prefs.remove('account_setup_completed');

    _userEmail = 'user@mentalcare.ai';
    _userName = 'Friend';
    _isLoggedIn = false;
    _accountSetupCompleted = false;
    notifyListeners();
  }
}

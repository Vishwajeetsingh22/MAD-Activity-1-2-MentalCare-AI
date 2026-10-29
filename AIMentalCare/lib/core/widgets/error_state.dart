import 'package:flutter/material.dart';
import '../theme/app_text_styles.dart';
import 'primary_button.dart';

/// Reusable Error State view
class ErrorState extends StatelessWidget {
  final String title;
  final String message;
  final String buttonText;
  final VoidCallback onRetry;

  const ErrorState({
    Key? key,
    this.title = 'Something went wrong',
    this.message = 'Unable to complete your request. Please check your connection and try again.',
    this.buttonText = 'Try Again',
    required this.onRetry,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(28.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('⚠️', style: TextStyle(fontSize: 40)),
            const SizedBox(height: 16),
            Text(
              title,
              style: AppTextStyles.title,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            Text(
              message,
              style: AppTextStyles.bodySecondary,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 20),
            PrimaryButton(
              text: buttonText,
              onPressed: onRetry,
            ),
          ],
        ),
      ),
    );
  }
}

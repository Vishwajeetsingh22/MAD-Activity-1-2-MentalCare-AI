import 'package:flutter_test/flutter_test.dart';
import 'package:mentalcare_ai_flutter/app.dart';

void main() {
  testWidgets('MentalCareApp smoke test', (WidgetTester tester) async {
    await tester.pumpWidget(const MentalCareApp());
    await tester.pump(const Duration(seconds: 3));
    expect(find.byType(MentalCareApp), findsOneWidget);
  });
}

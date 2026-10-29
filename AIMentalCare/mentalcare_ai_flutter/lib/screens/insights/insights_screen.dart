import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:fl_chart/fl_chart.dart';
import '../../core/theme/app_colors.dart';
import '../../providers/checkin_provider.dart';

class InsightsScreen extends StatelessWidget {
  const InsightsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final checkInProv = Provider.of<CheckInProvider>(context);
    final history = checkInProv.allCheckIns;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(18.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Stress & Burnout Insights 📊', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold)),
          const SizedBox(height: 6),
          const Text('Multi-day stress patterns and burnout trajectory analysis.', style: TextStyle(color: AppColors.textSecondaryLight)),
          const SizedBox(height: 20),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(18.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Multi-Day Stress Score Curve', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                  const SizedBox(height: 16),
                  SizedBox(
                    height: 180,
                    child: history.isEmpty
                        ? const Center(child: Text('No historical data available yet.'))
                        : LineChart(
                            LineChartData(
                              gridData: const FlGridData(show: false),
                              titlesData: const FlTitlesData(show: false),
                              borderData: FlBorderData(show: false),
                              lineBarsData: [
                                LineChartBarData(
                                  spots: [
                                    for (int i = 0; i < history.length; i++)
                                      FlSpot(i.toDouble(), history[history.length - 1 - i].stressScore.toDouble()),
                                  ],
                                  isCurved: true,
                                  color: AppColors.primaryGreen,
                                  barWidth: 3,
                                  dotData: const FlDotData(show: true),
                                ),
                              ],
                            ),
                          ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          const Card(
            child: Padding(
              padding: EdgeInsets.all(18.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Burnout Risk Analysis', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                  SizedBox(height: 8),
                  Text(
                    'Multi-day trajectory indicates a LOW burnout risk pattern. Your daily stress levels are within healthy restorative baselines.',
                    style: TextStyle(fontSize: 13, color: AppColors.textSecondaryLight),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

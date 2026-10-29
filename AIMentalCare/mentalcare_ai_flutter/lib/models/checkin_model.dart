class CheckInModel {
  final String dailyRecordId;
  final String userId;
  final String localDate;
  final int stressScore;
  final String stressLevel;
  final String recommendation;
  final DateTime createdAt;

  CheckInModel({
    required this.dailyRecordId,
    required this.userId,
    required this.localDate,
    required this.stressScore,
    required this.stressLevel,
    required this.recommendation,
    required this.createdAt,
  });

  Map<String, dynamic> toMap() {
    return {
      'daily_record_id': dailyRecordId,
      'user_id': userId,
      'local_date': localDate,
      'stress_score': stressScore,
      'stress_level': stressLevel,
      'recommendation': recommendation,
      'created_at': createdAt.millisecondsSinceEpoch,
    };
  }

  factory CheckInModel.fromMap(Map<String, dynamic> map) {
    return CheckInModel(
      dailyRecordId: map['daily_record_id'] ?? '',
      userId: map['user_id'] ?? '',
      localDate: map['local_date'] ?? '',
      stressScore: map['stress_score'] ?? 0,
      stressLevel: map['stress_level'] ?? 'LOW',
      recommendation: map['recommendation'] ?? '',
      createdAt: DateTime.fromMillisecondsSinceEpoch(map['created_at'] ?? DateTime.now().millisecondsSinceEpoch),
    );
  }
}

class UserModel {
  final String userId;
  final String fullName;
  final String email;
  final bool accountSetupCompleted;

  UserModel({
    required this.userId,
    required this.fullName,
    required this.email,
    required this.accountSetupCompleted,
  });

  Map<String, dynamic> toMap() {
    return {
      'user_id': userId,
      'full_name': fullName,
      'email': email,
      'account_setup_completed': accountSetupCompleted ? 1 : 0,
    };
  }

  factory UserModel.fromMap(Map<String, dynamic> map) {
    return UserModel(
      userId: map['user_id'] ?? '',
      fullName: map['full_name'] ?? '',
      email: map['email'] ?? '',
      accountSetupCompleted: (map['account_setup_completed'] ?? 0) == 1,
    );
  }
}

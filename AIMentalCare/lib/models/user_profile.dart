import 'dart:convert';

/// MentalCare AI - User Profile Model
///
/// Tracks authenticated user metadata, verification status,
/// and Account Setup completion in Firestore (users/{uid}).
class UserProfile {
  final String uid;
  final String fullName;
  final String email;
  final String phoneNumber;
  final bool isEmailVerified;
  final bool isPhoneVerified;
  final bool accountSetupCompleted;
  final List<String> wellnessGoals;
  final int targetSleepHours;
  final String reminderTime;
  final int createdAt;
  final int updatedAt;

  const UserProfile({
    required this.uid,
    required this.fullName,
    required this.email,
    required this.phoneNumber,
    this.isEmailVerified = false,
    this.isPhoneVerified = false,
    this.accountSetupCompleted = false,
    this.wellnessGoals = const [],
    this.targetSleepHours = 7,
    this.reminderTime = '08:30 PM',
    required this.createdAt,
    required this.updatedAt,
  });

  UserProfile copyWith({
    String? uid,
    String? fullName,
    String? email,
    String? phoneNumber,
    bool? isEmailVerified,
    bool? isPhoneVerified,
    bool? accountSetupCompleted,
    List<String>? wellnessGoals,
    int? targetSleepHours,
    String? reminderTime,
    int? createdAt,
    int? updatedAt,
  }) {
    return UserProfile(
      uid: uid ?? this.uid,
      fullName: fullName ?? this.fullName,
      email: email ?? this.email,
      phoneNumber: phoneNumber ?? this.phoneNumber,
      isEmailVerified: isEmailVerified ?? this.isEmailVerified,
      isPhoneVerified: isPhoneVerified ?? this.isPhoneVerified,
      accountSetupCompleted:
          accountSetupCompleted ?? this.accountSetupCompleted,
      wellnessGoals: wellnessGoals ?? this.wellnessGoals,
      targetSleepHours: targetSleepHours ?? this.targetSleepHours,
      reminderTime: reminderTime ?? this.reminderTime,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'uid': uid,
      'fullName': fullName,
      'email': email,
      'phoneNumber': phoneNumber,
      'isEmailVerified': isEmailVerified,
      'isPhoneVerified': isPhoneVerified,
      'accountSetupCompleted': accountSetupCompleted,
      'wellnessGoals': wellnessGoals,
      'targetSleepHours': targetSleepHours,
      'reminderTime': reminderTime,
      'createdAt': createdAt,
      'updatedAt': updatedAt,
    };
  }

  factory UserProfile.fromMap(Map<String, dynamic> map, {String? uid}) {
    return UserProfile(
      uid: uid ?? map['uid'] as String? ?? '',
      fullName: map['fullName'] as String? ?? '',
      email: map['email'] as String? ?? '',
      phoneNumber: map['phoneNumber'] as String? ?? '',
      isEmailVerified: map['isEmailVerified'] as bool? ?? false,
      isPhoneVerified: map['isPhoneVerified'] as bool? ?? false,
      accountSetupCompleted: map['accountSetupCompleted'] as bool? ?? false,
      wellnessGoals: List<String>.from(map['wellnessGoals'] as List? ?? []),
      targetSleepHours: map['targetSleepHours'] as int? ?? 7,
      reminderTime: map['reminderTime'] as String? ?? '08:30 PM',
      createdAt: map['createdAt'] as int? ?? DateTime.now().millisecondsSinceEpoch,
      updatedAt: map['updatedAt'] as int? ?? DateTime.now().millisecondsSinceEpoch,
    );
  }

  String toJson() => json.encode(toMap());

  factory UserProfile.fromJson(String source) =>
      UserProfile.fromMap(json.decode(source) as Map<String, dynamic>);
}

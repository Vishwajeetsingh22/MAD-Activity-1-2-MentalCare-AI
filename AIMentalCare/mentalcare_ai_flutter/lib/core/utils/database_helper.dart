import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';

class DatabaseHelper {
  static final DatabaseHelper instance = DatabaseHelper._init();
  static Database? _database;

  DatabaseHelper._init();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDB('mentalcare_ai.db');
    return _database!;
  }

  Future<Database> _initDB(String filePath) async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, filePath);

    return await openDatabase(
      path,
      version: 1,
      onCreate: _createDB,
    );
  }

  Future _createDB(Database db, int version) async {
    // 1. Users Table
    await db.execute('''
      CREATE TABLE users (
        user_id TEXT PRIMARY KEY,
        full_name TEXT NOT NULL,
        email TEXT NOT NULL,
        account_setup_completed INTEGER NOT NULL DEFAULT 0
      )
    ''');

    // 2. Chat Messages Table
    await db.execute('''
      CREATE TABLE chat_messages (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id TEXT NOT NULL,
        conversation_id TEXT NOT NULL,
        sender TEXT NOT NULL,
        message_text TEXT NOT NULL,
        is_crisis INTEGER NOT NULL DEFAULT 0,
        timestamp INTEGER NOT NULL
      )
    ''');

    // 3. Daily Check-Ins Table
    await db.execute('''
      CREATE TABLE mental_check_ins (
        daily_record_id TEXT PRIMARY KEY,
        user_id TEXT NOT NULL,
        local_date TEXT NOT NULL,
        stress_score INTEGER NOT NULL,
        stress_level TEXT NOT NULL,
        recommendation TEXT NOT NULL,
        created_at INTEGER NOT NULL
      )
    ''');

    // 4. Journal Entries Table
    await db.execute('''
      CREATE TABLE journal_entries (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id TEXT NOT NULL,
        title TEXT NOT NULL,
        content TEXT NOT NULL,
        mood_emoji TEXT NOT NULL,
        created_at INTEGER NOT NULL
      )
    ''');

    // 5. Goals Table
    await db.execute('''
      CREATE TABLE goals (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id TEXT NOT NULL,
        title TEXT NOT NULL,
        category TEXT NOT NULL,
        target_days INTEGER NOT NULL,
        completed_days INTEGER NOT NULL,
        is_completed INTEGER NOT NULL DEFAULT 0
      )
    ''');

    // 6. Trusted Contacts Table
    await db.execute('''
      CREATE TABLE trusted_contacts (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id TEXT NOT NULL,
        contact_name TEXT NOT NULL,
        phone_number TEXT NOT NULL,
        relationship TEXT NOT NULL
      )
    ''');
  }

  // --- CRUD HELPERS ---

  // Chat Messages
  Future<int> insertChatMessage(Map<String, dynamic> row) async {
    final db = await instance.database;
    return await db.insert('chat_messages', row);
  }

  Future<List<Map<String, dynamic>>> getChatMessages(String userId, String conversationId) async {
    final db = await instance.database;
    return await db.query(
      'chat_messages',
      where: 'user_id = ? AND conversation_id = ?',
      whereArgs: [userId, conversationId],
      orderBy: 'timestamp ASC',
    );
  }

  Future<int> clearChatMessages(String userId, String conversationId) async {
    final db = await instance.database;
    return await db.delete(
      'chat_messages',
      where: 'user_id = ? AND conversation_id = ?',
      whereArgs: [userId, conversationId],
    );
  }

  // Check-ins
  Future<int> insertCheckIn(Map<String, dynamic> row) async {
    final db = await instance.database;
    return await db.insert(
      'mental_check_ins',
      row,
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<Map<String, dynamic>?> getTodayCheckIn(String userId, String localDate) async {
    final db = await instance.database;
    final dailyRecordId = '${userId}_$localDate';
    final result = await db.query(
      'mental_check_ins',
      where: 'daily_record_id = ?',
      whereArgs: [dailyRecordId],
    );
    return result.isNotEmpty ? result.first : null;
  }

  Future<List<Map<String, dynamic>>> getAllCheckIns(String userId) async {
    final db = await instance.database;
    return await db.query(
      'mental_check_ins',
      where: 'user_id = ?',
      whereArgs: [userId],
      orderBy: 'created_at DESC',
    );
  }

  // Journal
  Future<int> insertJournal(Map<String, dynamic> row) async {
    final db = await instance.database;
    return await db.insert('journal_entries', row);
  }

  Future<List<Map<String, dynamic>>> getJournals(String userId) async {
    final db = await instance.database;
    return await db.query(
      'journal_entries',
      where: 'user_id = ?',
      whereArgs: [userId],
      orderBy: 'created_at DESC',
    );
  }

  Future<int> deleteJournal(int id) async {
    final db = await instance.database;
    return await db.delete('journal_entries', where: 'id = ?', whereArgs: [id]);
  }
}

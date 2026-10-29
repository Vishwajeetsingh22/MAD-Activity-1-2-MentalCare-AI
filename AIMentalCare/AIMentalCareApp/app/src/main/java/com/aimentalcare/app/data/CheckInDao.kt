package com.aimentalcare.app.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CheckInDao {
    // Check-in CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckInEntity): Long

    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC")
    fun getAllCheckIns(): LiveData<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE userId = :userId ORDER BY timestamp DESC")
    fun getCheckInsByUser(userId: String): LiveData<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC LIMIT 7")
    suspend fun getRecentCheckIns(): List<CheckInEntity>

    @Query("SELECT * FROM check_ins WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllCheckInsSync(userId: String): List<CheckInEntity>

    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC LIMIT 1")
    fun getLatestCheckIn(): LiveData<CheckInEntity?>

    @Query("SELECT COUNT(*) FROM check_ins WHERE userId = :userId")
    fun getCheckInCount(userId: String): LiveData<Int>

    // Once-per-day Daily Quiz Queries
    @Query("SELECT * FROM check_ins WHERE (dailyRecordId = :dailyRecordId OR (userId = :userId AND localDate = :localDate)) ORDER BY timestamp DESC LIMIT 1")
    suspend fun getTodayDailyCheckInSync(userId: String, localDate: String, dailyRecordId: String): CheckInEntity?

    @Query("SELECT * FROM check_ins WHERE (dailyRecordId = :dailyRecordId OR (userId = :userId AND localDate = :localDate)) ORDER BY timestamp DESC LIMIT 1")
    fun getTodayDailyCheckIn(userId: String, localDate: String, dailyRecordId: String): LiveData<CheckInEntity?>

    @Query("SELECT * FROM check_ins WHERE userId = :userId AND localDate = :localDate ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestCheckInForDateSync(userId: String, localDate: String): CheckInEntity?

    @Query("SELECT DISTINCT localDate FROM check_ins WHERE userId = :userId AND localDate != '' ORDER BY localDate DESC")
    suspend fun getDistinctCheckInDates(userId: String): List<String>

    @Query("SELECT COUNT(DISTINCT localDate) FROM check_ins WHERE userId = :userId AND localDate != ''")
    suspend fun getUniqueCheckInDaysCountSync(userId: String): Int

    // Users CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity): Long

    @Query("SELECT * FROM Users WHERE userId = :userId LIMIT 1")
    fun getUserById(userId: String): LiveData<UserEntity?>

    @Query("SELECT * FROM Users WHERE userId = :userId LIMIT 1")
    suspend fun getUserByIdSync(userId: String): UserEntity?

    @Query("UPDATE Users SET accountSetupCompleted = :completed WHERE userId = :userId")
    suspend fun updateUserSetupStatus(userId: String, completed: Boolean)

    // Stress Results
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStressResult(result: StressResultEntity): Long

    @Query("SELECT * FROM StressResults ORDER BY timestamp DESC")
    fun getAllStressResults(): LiveData<List<StressResultEntity>>

    // Lifestyle Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLifestyle(log: LifestyleEntity): Long

    @Query("SELECT * FROM lifestyle_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLifestyle(): LiveData<LifestyleEntity?>

    // Trusted Contacts CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrustedContact(contact: TrustedContactEntity): Long

    @Update
    suspend fun updateTrustedContact(contact: TrustedContactEntity)

    @Delete
    suspend fun deleteTrustedContact(contact: TrustedContactEntity)

    @Query("DELETE FROM TrustedContacts WHERE contactId = :contactId")
    suspend fun deleteTrustedContactById(contactId: Long)

    @Query("SELECT * FROM TrustedContacts LIMIT 1")
    fun getTrustedContact(): LiveData<TrustedContactEntity?>

    @Query("SELECT * FROM TrustedContacts WHERE userId = :userId ORDER BY contactId DESC")
    fun getTrustedContactsByUser(userId: String): LiveData<List<TrustedContactEntity>>

    @Query("SELECT * FROM TrustedContacts WHERE userId = :userId ORDER BY contactId DESC")
    suspend fun getTrustedContactsList(userId: String): List<TrustedContactEntity>

    // Chatbot Messages CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(msg: ChatMessageEntity): Long

    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND conversationId = :convId ORDER BY timestamp ASC")
    fun getChatMessages(userId: String, convId: String): LiveData<List<ChatMessageEntity>>

    @Query("SELECT DISTINCT conversationId FROM chat_messages WHERE userId = :userId ORDER BY timestamp DESC")
    fun getDistinctConversations(userId: String): LiveData<List<String>>

    @Query("SELECT DISTINCT conversationId FROM chat_messages WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getDistinctConversationsSync(userId: String): List<String>

    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND conversationId = :convId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessageForConversation(userId: String, convId: String): ChatMessageEntity?

    @Query("DELETE FROM chat_messages WHERE userId = :userId AND conversationId = :convId")
    suspend fun clearChatMessages(userId: String, convId: String)

    // Journal CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(entry: JournalEntity): Long

    @Update
    suspend fun updateJournal(entry: JournalEntity)

    @Delete
    suspend fun deleteJournal(entry: JournalEntity)

    @Query("DELETE FROM journal_entries WHERE journalId = :journalId")
    suspend fun deleteJournalById(journalId: Long)

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY timestamp DESC")
    fun getJournalsByUser(userId: String): LiveData<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY timestamp DESC")
    fun searchJournals(userId: String, query: String): LiveData<List<JournalEntity>>

    @Query("SELECT COUNT(*) FROM journal_entries WHERE userId = :userId")
    fun getJournalCount(userId: String): LiveData<Int>

    // Goals CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("DELETE FROM wellness_goals WHERE goalId = :goalId")
    suspend fun deleteGoalById(goalId: Long)

    @Query("SELECT * FROM wellness_goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getGoalsByUser(userId: String): LiveData<List<GoalEntity>>

    @Query("SELECT COUNT(*) FROM wellness_goals WHERE userId = :userId AND isCompleted = 0")
    fun getActiveGoalsCount(userId: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM wellness_goals WHERE userId = :userId AND isCompleted = 1")
    fun getCompletedGoalsCount(userId: String): LiveData<Int>

    // Daily Plan CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyPlan(plan: DailyPlanEntity): Long

    @Query("SELECT * FROM daily_plan_entries WHERE userId = :userId AND dateString = :dateString LIMIT 1")
    fun getDailyPlanByDate(userId: String, dateString: String): LiveData<DailyPlanEntity?>

    @Query("SELECT * FROM daily_plan_entries WHERE userId = :userId AND dateString = :dateString LIMIT 1")
    suspend fun getDailyPlanByDateSync(userId: String, dateString: String): DailyPlanEntity?

    // Wellness Activities CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWellnessActivity(activity: WellnessActivityEntity): Long

    @Query("SELECT * FROM wellness_activities WHERE userId = :userId ORDER BY completedAt DESC")
    fun getRecentActivitiesByUser(userId: String): LiveData<List<WellnessActivityEntity>>

    @Query("SELECT COUNT(*) FROM wellness_activities WHERE userId = :userId")
    fun getActivityCount(userId: String): LiveData<Int>

    // Wearable & Heart Rate CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeartRate(record: HeartRateEntity): Long

    @Query("SELECT * FROM heart_rate_records WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestHeartRate(userId: String): LiveData<HeartRateEntity?>

    @Query("SELECT * FROM heart_rate_records WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestHeartRateSync(userId: String): HeartRateEntity?

    @Query("SELECT * FROM heart_rate_records WHERE userId = :userId ORDER BY timestamp DESC LIMIT 10")
    fun getRecentHeartRates(userId: String): LiveData<List<HeartRateEntity>>

    @Query("SELECT * FROM heart_rate_records WHERE userId = :userId ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecentHeartRatesSync(userId: String): List<HeartRateEntity>
}

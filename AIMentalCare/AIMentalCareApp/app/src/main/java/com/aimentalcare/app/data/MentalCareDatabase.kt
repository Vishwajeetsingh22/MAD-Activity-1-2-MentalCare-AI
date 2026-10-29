package com.aimentalcare.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CheckInEntity::class,
        LifestyleEntity::class,
        UserEntity::class,
        StressResultEntity::class,
        TrustedContactEntity::class,
        ChatMessageEntity::class,
        JournalEntity::class,
        GoalEntity::class,
        DailyPlanEntity::class,
        WellnessActivityEntity::class,
        HeartRateEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class MentalCareDatabase : RoomDatabase() {

    abstract fun checkInDao(): CheckInDao

    companion object {
        @Volatile
        private var INSTANCE: MentalCareDatabase? = null

        private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Users ADD COLUMN accountSetupCompleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE check_ins ADD COLUMN dailyRecordId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE check_ins ADD COLUMN localDate TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE check_ins ADD COLUMN timezone TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE check_ins SET localDate = date(timestamp / 1000, 'unixepoch', 'localtime') WHERE localDate = ''")
            }
        }

        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `heart_rate_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `bpm` INTEGER NOT NULL,
                        `accuracy` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `source` TEXT NOT NULL,
                        `sensorName` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): MentalCareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MentalCareDatabase::class.java,
                    "mental_care_db"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

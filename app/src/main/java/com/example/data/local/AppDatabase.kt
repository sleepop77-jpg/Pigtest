package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        EconomyEntry::class,
        SessionLog::class,
        Task::class,
        StudyGoal::class,
        Subject::class,
        FlashcardDeck::class,
        Flashcard::class,
        Note::class,
        StoreItem::class,
        Stock::class,
        StockPortfolio::class,
        StudyGroup::class,
        UserProfile::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun economyDao(): EconomyDao
    abstract fun sessionDao(): SessionDao
    abstract fun taskDao(): TaskDao
    abstract fun studyGoalDao(): StudyGoalDao
    abstract fun subjectDao(): SubjectDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun noteDao(): NoteDao
    abstract fun storeItemDao(): StoreItemDao
    abstract fun stockDao(): StockDao
    abstract fun stockPortfolioDao(): StockPortfolioDao
    abstract fun studyGroupDao(): StudyGroupDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_os_db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getDatabase(context: Context): AppDatabase = getInstance(context)

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val freshProfile = UserProfile(
                id = 1,
                username = "",
                fullName = "",
                email = "",
                major = "",
                bio = "",
                avatarId = "mascot_headphones",
                dailyStudyTargetHours = 4.0f,
                hasCompletedOnboarding = false,
                notificationsEnabled = true,
                soundVibrationEnabled = true,
                preferredTheme = "AUTO"
            )
            database.userProfileDao().insertOrUpdate(freshProfile)

            val initialSubjects = listOf(
                Subject("math", "Mathematics", 0, 0f, 0, 0, "#D9534F"),
                Subject("history", "World History", 0, 0f, 0, 0, "#F5A623"),
                Subject("spanish", "Spanish Language", 0, 0f, 0, 0, "#9C27B0"),
                Subject("physics", "Quantum Physics", 0, 0f, 0, 0, "#20B2AA"),
                Subject("cs", "Computer Science", 0, 0f, 0, 0, "#00BCD4")
            )
            database.subjectDao().insertAll(initialSubjects)

            val stocks = listOf(
                Stock("stock_math", "\$MATH", "Mathematics", 1.00f, 0f, 0),
                Stock("stock_cs", "\$CS", "Computer Science", 1.00f, 0f, 0),
                Stock("stock_phys", "\$PHYS", "Quantum Physics", 1.00f, 0f, 0),
                Stock("stock_span", "\$SPAN", "Spanish Language", 1.00f, 0f, 0),
                Stock("stock_hist", "\$HIST", "World History", 1.00f, 0f, 0)
            )
            database.stockDao().insertAll(stocks)

            val storeItems = listOf(
                StoreItem("item_golden_desk", "Golden Desk Aesthetic", "Adorn your mascot desk with brilliant gold trimming.", "Mascot", 150, false, null, 0),
                StoreItem("item_cyberpunk", "Cyberpunk StudyBuddy", "Neon visor and cyber headset for your study buddy.", "Mascot", 250, false, null, 0),
                StoreItem("item_math_matrix", "Matrix Hacker Theme", "Emerald terminal aesthetic for intense study sessions.", "Theme", 200, false, "math", 50),
                StoreItem("item_spanish_fiesta", "Sol & Coral Palette", "Warm Andalusian sunset colors for all OS app icons.", "Theme", 200, false, "spanish", 60),
                StoreItem("item_savage_alerts", "Savage Notifications Pack", "Unlock the most hilariously brutal sarcastic reminders.", "Perk", 80, true, null, 0),
                StoreItem("item_night_owl_skin", "Night Owl Mascot Skin", "Cute sleepy nightcap with starry halo.", "Mascot", 180, false, null, 0),
                StoreItem("item_nyc_sphere", "Vegas Sphere Buddy", "A giant glowing sphere with a live animated face. FREE launch gift!", "Mascot", 0, false, null, 0)
            )
            database.storeItemDao().insertAll(storeItems)
        }
    }
}

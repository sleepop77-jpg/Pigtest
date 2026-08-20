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
    version = 2,
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
            // Seed initial Economy
            database.economyDao().insert(
                EconomyEntry(
                    fameDelta = 100,
                    shameDelta = 0,
                    reason = "Welcome to StudyOS! Initial starter Fame."
                )
            )

            // Seed Subjects
            val initialSubjects = listOf(
                Subject("math", "Mathematics", 65, 14.5f, 120, 95, "#D9534F"),
                Subject("history", "World History", 42, 8.0f, 60, 42, "#F5A623"),
                Subject("spanish", "Spanish Language", 78, 19.2f, 150, 130, "#9C27B0"),
                Subject("physics", "Quantum Physics", 30, 6.0f, 40, 26, "#20B2AA"),
                Subject("cs", "Computer Science", 85, 24.0f, 200, 185, "#00BCD4")
            )
            database.subjectDao().insertAll(initialSubjects)

            // Seed Study Goals
            database.studyGoalDao().insert(
                StudyGoal(
                    title = "Complete 50 Math Flashcards",
                    subject = "Mathematics",
                    goalType = "flashcard",
                    targetValue = 50,
                    currentValue = 23,
                    deadlineText = "Friday 11:59 PM",
                    completed = false,
                    claimedReward = false,
                    rewardFame = 200
                )
            )
            database.studyGoalDao().insert(
                StudyGoal(
                    title = "Study 15 hours in Computer Science",
                    subject = "Computer Science",
                    goalType = "time",
                    targetValue = 15,
                    currentValue = 9,
                    deadlineText = "Sunday 11:59 PM",
                    completed = false,
                    claimedReward = false,
                    rewardFame = 100
                )
            )
            database.studyGoalDao().insert(
                StudyGoal(
                    title = "Maintain 7-day study streak",
                    subject = "General",
                    goalType = "streak",
                    targetValue = 7,
                    currentValue = 4,
                    deadlineText = "Continuous",
                    completed = false,
                    claimedReward = false,
                    rewardFame = 100
                )
            )

            // Seed Tasks
            database.taskDao().insert(
                Task(
                    title = "Review Linear Algebra Chapter 4",
                    subject = "Mathematics",
                    completed = false,
                    priority = "High"
                )
            )
            database.taskDao().insert(
                Task(
                    title = "Implement Binary Search Tree in Kotlin",
                    subject = "Computer Science",
                    completed = false,
                    priority = "Medium"
                )
            )
            database.taskDao().insert(
                Task(
                    title = "Memorize 20 Spanish irregular verbs",
                    subject = "Spanish Language",
                    completed = true,
                    priority = "Low"
                )
            )

            // Seed Flashcards Decks
            val deckMath = FlashcardDeck("deck_math", "Calculus & Linear Algebra", "Mathematics", 5, 75)
            val deckCS = FlashcardDeck("deck_cs", "Data Structures & Algorithms", "Computer Science", 5, 85)
            val deckSpan = FlashcardDeck("deck_span", "Spanish Conversation Essentials", "Spanish Language", 5, 80)
            database.flashcardDao().insertDecks(listOf(deckMath, deckCS, deckSpan))

            // Seed Flashcards
            database.flashcardDao().insertCards(listOf(
                Flashcard(deckId = "deck_math", question = "What is the derivative of sin(x)?", answer = "cos(x)", correctCount = 4, reviewedCount = 5),
                Flashcard(deckId = "deck_math", question = "What is the integral of e^x dx?", answer = "e^x + C", correctCount = 5, reviewedCount = 5),
                Flashcard(deckId = "deck_math", question = "State Euler's formula.", answer = "e^(i*pi) + 1 = 0", correctCount = 3, reviewedCount = 4),
                Flashcard(deckId = "deck_math", question = "What is the rank of an invertible n x n matrix?", answer = "n (Full rank)", correctCount = 4, reviewedCount = 5),
                Flashcard(deckId = "deck_math", question = "What is the limit of (sin x)/x as x approaches 0?", answer = "1", correctCount = 5, reviewedCount = 5),

                Flashcard(deckId = "deck_cs", question = "What is the average time complexity of QuickSort?", answer = "O(n log n)", correctCount = 8, reviewedCount = 9),
                Flashcard(deckId = "deck_cs", question = "What is Dijkstra's algorithm used for?", answer = "Shortest paths in weighted graphs with non-negative edges", correctCount = 7, reviewedCount = 8),
                Flashcard(deckId = "deck_cs", question = "What is the difference between a Process and a Thread?", answer = "Threads share memory within the same process address space", correctCount = 6, reviewedCount = 7),
                Flashcard(deckId = "deck_cs", question = "Explain CAP theorem.", answer = "Consistency, Availability, and Partition tolerance (pick any two)", correctCount = 7, reviewedCount = 8),
                Flashcard(deckId = "deck_cs", question = "What is memoization in Dynamic Programming?", answer = "Caching results of expensive function calls to avoid recomputation", correctCount = 9, reviewedCount = 9),

                Flashcard(deckId = "deck_span", question = "¿Cómo se dice 'To study hard' en español?", answer = "Estudiar duro / Quemarse las pestañas", correctCount = 6, reviewedCount = 7),
                Flashcard(deckId = "deck_span", question = "¿Qué significa 'El éxito requiere constancia'?", answer = "Success requires consistency", correctCount = 7, reviewedCount = 7),
                Flashcard(deckId = "deck_span", question = "Past participle of 'Escribir':", answer = "Escrito", correctCount = 5, reviewedCount = 6)
            ))

            // Seed Notes
            database.noteDao().insert(
                Note(
                    title = "Key Formulas for Midterm Prep",
                    subject = "Mathematics",
                    content = "1. Eigenvalues: det(A - λI) = 0\n2. Matrix trace is the sum of eigenvalues.\n3. Orthogonal projection formula: P = A(A^T A)^(-1) A^T\n4. Cauchy-Schwarz Inequality: |u·v| <= ||u|| ||v||"
                )
            )
            database.noteDao().insert(
                Note(
                    title = "System Architecture Checklist",
                    subject = "Computer Science",
                    content = "1. Separation of concerns: UI -> ViewModel -> Repository -> Local Room / Remote API.\n2. StateFlow with collectAsStateWithLifecycle.\n3. Coroutines on Dispatchers.IO for storage."
                )
            )

            // Seed Stocks
            val stocks = listOf(
                Stock("stock_math", "\$MATH", "Mathematics", 1.85f, 14.2f, 340),
                Stock("stock_cs", "\$CS", "Computer Science", 3.45f, 22.4f, 520),
                Stock("stock_phys", "\$PHYS", "Quantum Physics", 2.10f, 18.7f, 210),
                Stock("stock_span", "\$SPAN", "Spanish Language", 1.20f, 6.5f, 180),
                Stock("stock_hist", "\$HIST", "World History", 0.92f, -4.1f, 95)
            )
            database.stockDao().insertAll(stocks)

            // Seed Stock Portfolio
            database.stockPortfolioDao().insert(
                StockPortfolio("stock_math", "\$MATH", 20, 1.60f)
            )
            database.stockPortfolioDao().insert(
                StockPortfolio("stock_cs", "\$CS", 15, 2.90f)
            )

            // Seed Store Items
            val storeItems = listOf(
                StoreItem("item_golden_desk", "Golden Desk Aesthetic", "Adorn your mascot desk with brilliant gold trimming.", "Mascot", 150, false, null, 0),
                StoreItem("item_cyberpunk", "Cyberpunk StudyBuddy", "Neon visor and cyber headset for your study buddy.", "Mascot", 250, false, null, 0),
                StoreItem("item_math_matrix", "Matrix Hacker Theme", "Emerald terminal aesthetic for intense study sessions.", "Theme", 200, false, "math", 50),
                StoreItem("item_spanish_fiesta", "Sol & Coral Palette", "Warm Andalusian sunset colors for all OS app icons.", "Theme", 200, false, "spanish", 60),
                StoreItem("item_savage_alerts", "Savage Notifications Pack", "Unlock the most hilariously brutal sarcastic reminders.", "Perk", 80, true, null, 0),
                StoreItem("item_night_owl_skin", "Night Owl Mascot Skin", "Cute sleepy nightcap with starry halo.", "Mascot", 180, false, null, 0)
            )
            database.storeItemDao().insertAll(storeItems)

            // Seed Study Groups
            val studyGroups = listOf(
                StudyGroup("group_stem", "Late Night STEM Grinders", "STEM99", 28, 142, 200, false, 0, true),
                StudyGroup("group_med", "Med School & Bio Cohort", "MED2026", 45, 310, 400, true, 10, false),
                StudyGroup("group_poly", "Global Polyglots Club", "LANG44", 19, 88, 150, false, 0, false)
            )
            database.studyGroupDao().insertAll(studyGroups)

            // Seed sample session logs for Analytics Heatmap
            val now = System.currentTimeMillis()
            val dayMillis = 86400000L
            val sessionSamples = listOf(
                SessionLog(sessionType = "Pomodoro", subject = "Mathematics", durationMinutes = 25, timestamp = now - dayMillis * 0, completed = true),
                SessionLog(sessionType = "Pomodoro", subject = "Computer Science", durationMinutes = 25, timestamp = now - dayMillis * 0, completed = true),
                SessionLog(sessionType = "ExamPrep", subject = "Computer Science", durationMinutes = 50, timestamp = now - dayMillis * 1, completed = true, isExamPrep = true),
                SessionLog(sessionType = "Pomodoro", subject = "Spanish Language", durationMinutes = 25, timestamp = now - dayMillis * 2, completed = true),
                SessionLog(sessionType = "Flashcards", subject = "Mathematics", durationMinutes = 15, timestamp = now - dayMillis * 3, completed = true),
                SessionLog(sessionType = "Pomodoro", subject = "Quantum Physics", durationMinutes = 25, timestamp = now - dayMillis * 4, completed = true),
                SessionLog(sessionType = "Notes", subject = "World History", durationMinutes = 30, timestamp = now - dayMillis * 5, completed = true)
            )
            sessionSamples.forEach { database.sessionDao().insert(it) }

            // Seed User Profile
            val defaultProfile = UserProfile(
                id = 1,
                username = "Kartik_Scholar",
                fullName = "Kartik Sharma",
                email = "kartitk2121@gmail.com",
                major = "Computer Science & Engineering",
                bio = "Crushing daily focus targets with StudyOS Pomodoros and building high-yield study stocks!",
                avatarId = "mascot_headphones",
                dailyStudyTargetHours = 4.0f,
                hasCompletedOnboarding = true,
                notificationsEnabled = true,
                soundVibrationEnabled = true,
                preferredTheme = "AUTO"
            )
            database.userProfileDao().insertOrUpdate(defaultProfile)
        }
    }
}

package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "economy_entries")
data class EconomyEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val fameDelta: Int = 0,
    val shameDelta: Int = 0,
    val reason: String = ""
)

@Entity(tableName = "session_logs")
data class SessionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionType: String, // "Pomodoro", "ExamPrep", "Flashcards", "Notes"
    val subject: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val completed: Boolean = true,
    val isExamPrep: Boolean = false
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String = "General",
    val completed: Boolean = false,
    val priority: String = "Medium", // "High", "Medium", "Low"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_goals")
data class StudyGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String = "General",
    val goalType: String, // "time", "flashcard", "note", "streak"
    val targetValue: Int, // e.g. 15 hours, 50 cards, 3 notes, 7 days
    val currentValue: Int = 0,
    val deadlineText: String = "Friday 11:59 PM",
    val completed: Boolean = false,
    val claimedReward: Boolean = false,
    val rewardFame: Int = 50
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey val id: String, // e.g. "math", "history", "spanish", "physics", "cs"
    val name: String,
    val masteryPercent: Int = 20, // 0 - 100
    val studyHoursTotal: Float = 0f,
    val cardsReviewed: Int = 0,
    val cardsCorrect: Int = 0,
    val colorHex: String = "#D9534F"
)

@Entity(tableName = "flashcard_decks")
data class FlashcardDeck(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val totalCards: Int = 0,
    val masteryRate: Int = 0
)

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckId: String,
    val question: String,
    val answer: String,
    val correctCount: Int = 0,
    val reviewedCount: Int = 0
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String = "General",
    val content: String,
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "store_items")
data class StoreItem(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String, // "Theme", "Mascot", "Perk"
    val costFame: Int,
    val unlocked: Boolean = false,
    val requiredMasterySubject: String? = null,
    val requiredMasteryLevel: Int = 0
)

@Entity(tableName = "stocks")
data class Stock(
    @PrimaryKey val id: String,
    val symbol: String,
    val subjectName: String,
    val currentPrice: Float,
    val weeklyPercentChange: Float,
    val studyVolumeThisWeek: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_portfolio")
data class StockPortfolio(
    @PrimaryKey val stockId: String,
    val symbol: String,
    val sharesOwned: Int = 0,
    val averageBuyPrice: Float = 0f
)

@Entity(tableName = "study_groups")
data class StudyGroup(
    @PrimaryKey val id: String,
    val name: String,
    val inviteCode: String,
    val memberCount: Int,
    val currentPomodoros: Int,
    val targetPomodoros: Int,
    val isPremium: Boolean = false,
    val weeklyFee: Int = 0,
    val userJoined: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val username: String = "Kartik_Scholar",
    val fullName: String = "Kartik Sharma",
    val email: String = "kartitk2121@gmail.com",
    val major: String = "Computer Science & Engineering",
    val bio: String = "Crushing daily focus targets with StudyOS Pomodoros and building high-yield study stocks!",
    val avatarId: String = "mascot_headphones", // mascot_headphones, mascot_crown, mascot_flame, mascot_sleepy, mascot_glasses
    val dailyStudyTargetHours: Float = 4.0f,
    val hasCompletedOnboarding: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val soundVibrationEnabled: Boolean = true,
    val preferredTheme: String = "AUTO" // "LIGHT", "DARK", "AUTO"
)


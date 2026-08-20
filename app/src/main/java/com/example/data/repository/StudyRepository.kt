package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class StudyRepository(private val database: AppDatabase) {

    // Economy
    val allEconomyEntries: Flow<List<EconomyEntry>> = database.economyDao().getAllEntries()
    val totalFame: Flow<Int> = database.economyDao().getTotalFame()
    val totalShame: Flow<Int> = database.economyDao().getTotalShame()

    suspend fun addFame(amount: Int, reason: String) {
        if (amount > 0) {
            database.economyDao().insert(
                EconomyEntry(
                    fameDelta = amount,
                    shameDelta = 0,
                    reason = reason
                )
            )
        }
    }

    suspend fun addShame(amount: Int, reason: String) {
        if (amount > 0) {
            database.economyDao().insert(
                EconomyEntry(
                    fameDelta = 0,
                    shameDelta = amount,
                    reason = reason
                )
            )
        }
    }

    suspend fun cancelShameWithFame(amount: Int) {
        // Fame cancels Shame
        database.economyDao().insert(
            EconomyEntry(
                fameDelta = 0,
                shameDelta = -amount,
                reason = "Fame cancelled Shame through active study!"
            )
        )
    }

    suspend fun spendFame(amount: Int, reason: String): Boolean {
        val currentFame = database.economyDao().getTotalFame().first()
        if (currentFame >= amount) {
            database.economyDao().insert(
                EconomyEntry(
                    fameDelta = -amount,
                    shameDelta = 0,
                    reason = reason
                )
            )
            return true
        }
        return false
    }

    // Sessions
    val allSessions: Flow<List<SessionLog>> = database.sessionDao().getAllSessions()
    val totalPomodoros: Flow<Int> = database.sessionDao().getTotalPomodorosCount()
    
    fun getTodayPomodoros(startOfDay: Long): Flow<Int> {
        return database.sessionDao().getTodayPomodorosCount(startOfDay)
    }

    suspend fun recordStudySession(
        sessionType: String,
        subject: String,
        durationMinutes: Int,
        isExamPrep: Boolean = false,
        customFameEarned: Int? = null
    ) {
        database.sessionDao().insert(
            SessionLog(
                sessionType = sessionType,
                subject = subject,
                durationMinutes = durationMinutes,
                isExamPrep = isExamPrep
            )
        )

        // Add Fame (default +2 per minute, or 2.5 per min for 1h+ loop completion boost)
        val fameEarned = customFameEarned ?: (durationMinutes * 2)
        addFame(fameEarned, "Study Session ($durationMinutes mins in $subject)")

        // Update subject study time
        val subjects = database.subjectDao().getAllSubjects().first()
        val match = subjects.firstOrNull { it.name.equals(subject, ignoreCase = true) || it.id.equals(subject, ignoreCase = true) }
        if (match != null) {
            val updatedHours = match.studyHoursTotal + (durationMinutes / 60f)
            val updatedMastery = (match.masteryPercent + (durationMinutes / 10)).coerceAtMost(100)
            database.subjectDao().update(
                match.copy(
                    studyHoursTotal = updatedHours,
                    masteryPercent = updatedMastery
                )
            )
        }
    }

    // Tasks
    val allTasks: Flow<List<Task>> = database.taskDao().getAllTasks()
    suspend fun insertTask(task: Task) = database.taskDao().insert(task)
    suspend fun updateTask(task: Task) = database.taskDao().update(task)
    suspend fun deleteTask(task: Task) = database.taskDao().delete(task)

    // Goals
    val allGoals: Flow<List<StudyGoal>> = database.studyGoalDao().getAllGoals()
    suspend fun insertGoal(goal: StudyGoal) = database.studyGoalDao().insert(goal)
    suspend fun updateGoal(goal: StudyGoal) = database.studyGoalDao().update(goal)
    suspend fun deleteGoal(goal: StudyGoal) = database.studyGoalDao().delete(goal)
    suspend fun claimGoalReward(goal: StudyGoal) {
        if (!goal.claimedReward) {
            database.studyGoalDao().update(goal.copy(claimedReward = true, completed = true))
            addFame(goal.rewardFame, "Completed Goal: ${goal.title}")
        }
    }

    // Subjects
    val allSubjects: Flow<List<Subject>> = database.subjectDao().getAllSubjects()
    suspend fun insertSubject(subject: Subject) = database.subjectDao().insert(subject)
    suspend fun updateSubject(subject: Subject) = database.subjectDao().update(subject)

    // Flashcards
    val allDecks: Flow<List<FlashcardDeck>> = database.flashcardDao().getAllDecks()
    fun getCardsForDeck(deckId: String): Flow<List<Flashcard>> = database.flashcardDao().getCardsForDeck(deckId)
    suspend fun insertDeck(deck: FlashcardDeck) = database.flashcardDao().insertDeck(deck)
    suspend fun insertCard(card: Flashcard) = database.flashcardDao().insertCard(card)
    suspend fun recordFlashcardAnswer(card: Flashcard, isCorrect: Boolean, subjectName: String) {
        val updated = card.copy(
            reviewedCount = card.reviewedCount + 1,
            correctCount = if (isCorrect) card.correctCount + 1 else card.correctCount
        )
        database.flashcardDao().updateCard(updated)

        // Update subject stats
        val subjects = database.subjectDao().getAllSubjects().first()
        val subj = subjects.firstOrNull { it.name.equals(subjectName, ignoreCase = true) || it.id.equals(subjectName, ignoreCase = true) }
        if (subj != null) {
            val totalReviewed = subj.cardsReviewed + 1
            val totalCorrect = if (isCorrect) subj.cardsCorrect + 1 else subj.cardsCorrect
            val accuracy = if (totalReviewed > 0) (totalCorrect * 100) / totalReviewed else subj.masteryPercent
            database.subjectDao().update(
                subj.copy(
                    cardsReviewed = totalReviewed,
                    cardsCorrect = totalCorrect,
                    masteryPercent = accuracy.coerceIn(0, 100)
                )
            )
        }
    }

    // Notes
    val allNotes: Flow<List<Note>> = database.noteDao().getAllNotes()
    suspend fun getNoteById(id: Long) = database.noteDao().getNoteById(id)
    suspend fun insertNote(note: Note) = database.noteDao().insert(note)
    suspend fun updateNote(note: Note) = database.noteDao().update(note)
    suspend fun deleteNote(note: Note) = database.noteDao().delete(note)

    // Store
    val allStoreItems: Flow<List<StoreItem>> = database.storeItemDao().getAllItems()
    suspend fun purchaseStoreItem(item: StoreItem): Boolean {
        if (item.unlocked) return true
        val success = spendFame(item.costFame, "Purchased Store Item: ${item.name}")
        if (success) {
            database.storeItemDao().update(item.copy(unlocked = true))
            return true
        }
        return false
    }

    // Stocks
    val allStocks: Flow<List<Stock>> = database.stockDao().getAllStocks()
    val portfolio: Flow<List<StockPortfolio>> = database.stockPortfolioDao().getPortfolio()

    suspend fun buyStock(stock: Stock, quantity: Int): Boolean {
        val totalCostFame = (stock.currentPrice * quantity * 10).toInt() // 1 stock dollar = 10 Fame
        val success = spendFame(totalCostFame, "Bought $quantity shares of ${stock.symbol}")
        if (success) {
            val currentPos = database.stockPortfolioDao().getPosition(stock.id)
            if (currentPos != null) {
                val newShares = currentPos.sharesOwned + quantity
                val newAvgPrice = ((currentPos.sharesOwned * currentPos.averageBuyPrice) + (quantity * stock.currentPrice)) / newShares
                database.stockPortfolioDao().update(currentPos.copy(sharesOwned = newShares, averageBuyPrice = newAvgPrice))
            } else {
                database.stockPortfolioDao().insert(
                    StockPortfolio(
                        stockId = stock.id,
                        symbol = stock.symbol,
                        sharesOwned = quantity,
                        averageBuyPrice = stock.currentPrice
                    )
                )
            }
            return true
        }
        return false
    }

    suspend fun sellStock(stock: Stock, quantity: Int): Boolean {
        val currentPos = database.stockPortfolioDao().getPosition(stock.id) ?: return false
        if (currentPos.sharesOwned < quantity) return false

        val proceedsFame = (stock.currentPrice * quantity * 10).toInt()
        addFame(proceedsFame, "Sold $quantity shares of ${stock.symbol}")

        val remainingShares = currentPos.sharesOwned - quantity
        if (remainingShares > 0) {
            database.stockPortfolioDao().update(currentPos.copy(sharesOwned = remainingShares))
        } else {
            database.stockPortfolioDao().delete(currentPos)
        }
        return true
    }

    // Study Groups
    val allStudyGroups: Flow<List<StudyGroup>> = database.studyGroupDao().getAllGroups()
    suspend fun joinStudyGroup(group: StudyGroup) {
        database.studyGroupDao().update(
            group.copy(
                userJoined = true,
                memberCount = group.memberCount + 1
            )
        )
    }
    suspend fun leaveStudyGroup(group: StudyGroup) {
        database.studyGroupDao().update(
            group.copy(
                userJoined = false,
                memberCount = (group.memberCount - 1).coerceAtLeast(1)
            )
        )
    }
    suspend fun createStudyGroup(group: StudyGroup) {
        database.studyGroupDao().insert(group)
    }

    // User Profile & Preferences
    val userProfile: Flow<UserProfile?> = database.userProfileDao().getUserProfile()

    suspend fun updateUserProfile(profile: UserProfile) {
        database.userProfileDao().insertOrUpdate(profile)
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        val current = database.userProfileDao().getUserProfile().first() ?: UserProfile()
        database.userProfileDao().insertOrUpdate(current.copy(hasCompletedOnboarding = completed))
    }

    suspend fun setPreferredTheme(theme: String) {
        val current = database.userProfileDao().getUserProfile().first() ?: UserProfile()
        database.userProfileDao().insertOrUpdate(current.copy(preferredTheme = theme))
    }

    suspend fun resetAllData() {
        database.economyDao().clearAll()
        AppDatabase.populateInitialData(database)
    }
}

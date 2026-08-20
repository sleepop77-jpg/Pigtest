package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EconomyDao {
    @Query("SELECT * FROM economy_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<EconomyEntry>>

    @Query("SELECT COALESCE(SUM(fameDelta), 0) FROM economy_entries")
    fun getTotalFame(): Flow<Int>

    @Query("SELECT COALESCE(SUM(shameDelta), 0) FROM economy_entries")
    fun getTotalShame(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EconomyEntry): Long

    @Query("DELETE FROM economy_entries")
    suspend fun clearAll()
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM session_logs ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionLog>>

    @Query("SELECT COUNT(*) FROM session_logs WHERE sessionType = 'Pomodoro' OR sessionType = 'ExamPrep'")
    fun getTotalPomodorosCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM session_logs WHERE (sessionType = 'Pomodoro' OR sessionType = 'ExamPrep') AND timestamp >= :startOfDay")
    fun getTodayPomodorosCount(startOfDay: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionLog): Long
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY completed ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}

@Dao
interface StudyGoalDao {
    @Query("SELECT * FROM study_goals ORDER BY completed ASC, id ASC")
    fun getAllGoals(): Flow<List<StudyGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: StudyGoal): Long

    @Update
    suspend fun update(goal: StudyGoal)

    @Delete
    suspend fun delete(goal: StudyGoal)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY masteryPercent DESC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: String): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: Subject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subjects: List<Subject>)

    @Update
    suspend fun update(subject: Subject)
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcard_decks")
    fun getAllDecks(): Flow<List<FlashcardDeck>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    fun getCardsForDeck(deckId: String): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: FlashcardDeck)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecks(decks: List<FlashcardDeck>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Flashcard): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<Flashcard>)

    @Update
    suspend fun updateCard(card: Flashcard)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY lastModified DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}

@Dao
interface StoreItemDao {
    @Query("SELECT * FROM store_items")
    fun getAllItems(): Flow<List<StoreItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StoreItem>)

    @Update
    suspend fun update(item: StoreItem)
}

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks ORDER BY weeklyPercentChange DESC")
    fun getAllStocks(): Flow<List<Stock>>

    @Query("SELECT * FROM stocks WHERE id = :id LIMIT 1")
    suspend fun getStockById(id: String): Stock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stocks: List<Stock>)

    @Update
    suspend fun update(stock: Stock)
}

@Dao
interface StockPortfolioDao {
    @Query("SELECT * FROM stock_portfolio")
    fun getPortfolio(): Flow<List<StockPortfolio>>

    @Query("SELECT * FROM stock_portfolio WHERE stockId = :stockId LIMIT 1")
    suspend fun getPosition(stockId: String): StockPortfolio?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(portfolio: StockPortfolio)

    @Update
    suspend fun update(portfolio: StockPortfolio)

    @Delete
    suspend fun delete(portfolio: StockPortfolio)
}

@Dao
interface StudyGroupDao {
    @Query("SELECT * FROM study_groups")
    fun getAllGroups(): Flow<List<StudyGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(groups: List<StudyGroup>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: StudyGroup)

    @Update
    suspend fun update(group: StudyGroup)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)
}

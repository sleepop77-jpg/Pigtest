package com.example.core

import android.content.Context
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class TimerMode(val displayName: String) {
    STANDARD("Standard Timer"),
    LOOP("Loop Mode")
}

class FocusTimerManager(
    private val context: Context,
    private val repository: StudyRepository,
    private val economyManager: EconomyManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _selectedSubject = MutableStateFlow("Mathematics")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _customDurationMinutes = MutableStateFlow(25)
    val customDurationMinutes: StateFlow<Int> = _customDurationMinutes.asStateFlow()

    private val _currentTotalDurationSeconds = MutableStateFlow(25 * 60)
    val currentTotalDurationSeconds: StateFlow<Int> = _currentTotalDurationSeconds.asStateFlow()

    private val _secondsRemaining = MutableStateFlow(25 * 60)
    val secondsRemaining: StateFlow<Int> = _secondsRemaining.asStateFlow()

    private val _currentRound = MutableStateFlow(1)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()
    val totalRounds = 4
    val goalSessions = 15

    private val _timerMode = MutableStateFlow(TimerMode.STANDARD)
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    private val _celebrationMessage = MutableStateFlow<String?>(null)
    val celebrationMessage: StateFlow<String?> = _celebrationMessage.asStateFlow()

    init {
        scope.launch {
            while (true) {
                delay(1000L)
                if (_isRunning.value) {
                    economyManager.startStudySession(_selectedSubject.value)
                    val currentSecs = _secondsRemaining.value
                    if (currentSecs > 1) {
                        _secondsRemaining.value = currentSecs - 1
                    } else if (currentSecs == 1) {
                        _secondsRemaining.value = 0
                        handleTimerCompletion()
                    }
                }
            }
        }
    }

    private suspend fun handleTimerCompletion() {
        val studyDurationMins = (_currentTotalDurationSeconds.value / 60).coerceAtLeast(1)
        val isOneHourPlus = studyDurationMins >= 60
        val isLoop = _timerMode.value == TimerMode.LOOP
        
        // Base Fame (+2/min) is already paid LIVE by EconomyManager ticks.
        // Completion must NOT pay it again (that was the double-Fame loophole).
        val baseFame = studyDurationMins * 2
        
        // Loop Boost: the advertised extra +0.5/min for 1h+ loops, paid once here.
        val loopBoost = if (isLoop && isOneHourPlus) studyDurationMins / 2 else 0
        if (loopBoost > 0) {
            repository.addFame(loopBoost, "Loop Boost Bonus (1h+ focus, +0.5 Fame/min extra)")
        }
        
        repository.recordStudySession(
            sessionType = "FocusTimer",
            subject = _selectedSubject.value,
            durationMinutes = studyDurationMins,
            isExamPrep = false,
            customFameEarned = 0
        )
        NotificationHelper.sendPomodoroFinished(context, _selectedSubject.value, studyDurationMins, baseFame + loopBoost)
        
        if (isLoop) {
            _currentRound.value += 1
            _celebrationMessage.value = if (isOneHourPlus) {
                "1-Hour+ Loop Completed! $baseFame Fame live + $loopBoost Boost!"
            } else {
                "Loop Cycle #${_currentRound.value} Completed! +$baseFame Fame earned live!"
            }
            _secondsRemaining.value = _currentTotalDurationSeconds.value
        } else {
            _isRunning.value = false
            economyManager.stopStudySession()
            _celebrationMessage.value = "Focus Timer Complete! +$baseFame Fame earned during session!"
            _secondsRemaining.value = _currentTotalDurationSeconds.value
            if (_currentRound.value < totalRounds) {
                _currentRound.value += 1
            }
        }
    }

    fun start() {
        _isRunning.value = true
        economyManager.startStudySession(_selectedSubject.value)
    }

    fun pause() {
        _isRunning.value = false
        economyManager.stopStudySession()
    }

    fun toggle() {
        if (_isRunning.value) pause() else start()
    }

    fun reset() {
        _isRunning.value = false
        economyManager.stopStudySession()
        _secondsRemaining.value = _currentTotalDurationSeconds.value
    }

    fun setDurationMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(1, 480)
        _customDurationMinutes.value = clamped
        _currentTotalDurationSeconds.value = clamped * 60
        _secondsRemaining.value = clamped * 60
        _isRunning.value = false
        economyManager.stopStudySession()
    }

    fun adjustMinutes(delta: Int) {
        val newMins = (_customDurationMinutes.value + delta).coerceIn(1, 480)
        setDurationMinutes(newMins)
    }

    fun setSubject(subject: String) {
        _selectedSubject.value = subject
        if (_isRunning.value) {
            economyManager.startStudySession(subject)
        }
    }

    fun setTimerMode(mode: TimerMode) {
        _timerMode.value = mode
    }

    fun clearCelebration() {
        _celebrationMessage.value = null
    }

    fun cleanup() {
        scope.cancel()
    }
}
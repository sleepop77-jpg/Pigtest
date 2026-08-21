package com.example.core

import com.example.data.repository.StudyRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class SarcasticNotification(
    val id: String,
    val title: String,
    val message: String,
    val urgencyLevel: String
)

enum class MascotState {
    IDLE, STUDYING, WINNING, HIGH_SHAME, NIGHT_OWL, STREAK, SINGING, FRUSTRATED, BURNING
}

class EconomyManager(
    private val repository: StudyRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    val totalFame: StateFlow<Int> = repository.totalFame.stateIn(scope, SharingStarted.Eagerly, 100)
    val totalShame: StateFlow<Int> = repository.totalShame.stateIn(scope, SharingStarted.Eagerly, 0)
    val currentStreakDays = MutableStateFlow(4)

    private val _activeNotification = MutableStateFlow<SarcasticNotification?>(null)
    val activeNotification: StateFlow<SarcasticNotification?> = _activeNotification.asStateFlow()

    private val _isStudyingNow = MutableStateFlow(false)
    val isStudyingNow: StateFlow<Boolean> = _isStudyingNow.asStateFlow()

    private val _currentActiveSubject = MutableStateFlow("Mathematics")
    val currentActiveSubject: StateFlow<String> = _currentActiveSubject.asStateFlow()

    private val _continuousStudySeconds = MutableStateFlow(0)
    val continuousStudySeconds: StateFlow<Int> = _continuousStudySeconds.asStateFlow()

    private val _isDangerHours = MutableStateFlow(false)
    val isDangerHours: StateFlow<Boolean> = _isDangerHours.asStateFlow()

    private var hasAwardedBurnBonus = false
    private var dangerAlertShown = false

    private val _mascotState = MutableStateFlow(MascotState.IDLE)
    val mascotState: StateFlow<MascotState> = _mascotState.asStateFlow()

    private val idleSavageMessages = listOf(
        "Your textbooks called. They filed a missing persons report on you.",
        "An entire hour evaporated. Just like your GPA if you continue this behavior.",
        "Your procrastination is legendary. Unfortunately, employers do not hire for that.",
        "At this rate, your Shame score will qualify for a Guinness World Record.",
        "Breaking news: Looking at your phone does not automatically absorb calculus formulas.",
        "Your competitor is currently on their third 50-minute exam prep round. Just saying."
    )
    private val idleNormalMessages = listOf(
        "StudyOS Reminder: 30 minutes of idle time recorded. Start a 25m Focus Timer to earn +50 Fame!",
        "Your daily study streak of 4 days is waiting for today's session.",
        "Fame cancels Shame! Jump into a quick review to protect your leaderboard standing."
    )
    private val idleGentleMessages = listOf(
        "Ready to continue your journey? A quick 15-minute session will keep your momentum strong.",
        "Take a breath, grab some water, and let's conquer one flashcard deck today."
    )

    companion object {
        fun dangerHoursNow(): Boolean {
            val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return h in 16 until 18
        }
    }

    init {
        _isDangerHours.value = dangerHoursNow()
        scope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000L)
                val danger = dangerHoursNow()
                if (danger != _isDangerHours.value) {
                    _isDangerHours.value = danger
                    dangerAlertShown = false
                }
                if (_isStudyingNow.value) {
                    val currentSecs = _continuousStudySeconds.value + 1
                    _continuousStudySeconds.value = currentSecs
                    if (currentSecs >= 10800 && !hasAwardedBurnBonus) {
                        hasAwardedBurnBonus = true
                        repository.addFame(100, "3-Hour Unstoppable Burning Overdrive (+100 Bonus Fame)!")
                    }
                }
            }
        }
        scope.launch {
            combine(totalFame, totalShame, _isStudyingNow, currentStreakDays, _continuousStudySeconds) { fame, shame, studying, streak, continuousSecs ->
                when {
                    studying && continuousSecs >= 10800 -> MascotState.BURNING
                    studying -> MascotState.STUDYING
                    streak >= 7 -> MascotState.STREAK
                    shame > fame && shame > 50 -> MascotState.HIGH_SHAME
                    fame >= 300 -> MascotState.WINNING
                    !studying && dangerHoursNow() -> MascotState.FRUSTRATED
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 22 || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 5 -> MascotState.NIGHT_OWL
                    else -> MascotState.IDLE
                }
            }.collect { newState -> _mascotState.value = newState }
        }
        scope.launch(Dispatchers.Default) {
            while (true) {
                delay(60_000L)
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val isShameEligibleHours = hour in 5 until 22
                if (_isStudyingNow.value) {
                    repository.addFame(2, "Active Study Tick (+2 Fame)")
                    if (totalShame.value > 0) repository.cancelShameWithFame(1)
                } else if (isShameEligibleHours) {
                    if (hour in 16 until 18) {
                        repository.addShame(3, "DANGER HOURS (4-6 PM): +3 Shame/min! You MUST study at this hour!")
                    } else {
                        repository.addShame(1, "Idle between 5am-10pm (+1 Shame)")
                    }
                }
            }
        }
        scope.launch(Dispatchers.Default) {
            while (true) {
                delay(20_000L)
                if (_isDangerHours.value && !_isStudyingNow.value && !dangerAlertShown) {
                    dangerAlertShown = true
                    _activeNotification.value = SarcasticNotification(
                        id = System.currentTimeMillis().toString(),
                        title = "DANGER HOURS ACTIVE (4-6 PM)",
                        message = "AAAHHH! You MUST study at this hour! Shame is x3 (+3/min) until 6 PM. Start a Focus Timer NOW!",
                        urgencyLevel = "Savage"
                    )
                }
            }
        }
    }

    fun startStudySession(subject: String) {
        _currentActiveSubject.value = subject
        _isStudyingNow.value = true
    }

    fun stopStudySession() {
        _isStudyingNow.value = false
        _continuousStudySeconds.value = 0
        hasAwardedBurnBonus = false
    }

    fun setMascotOverrideState(state: MascotState) { _mascotState.value = state }
    fun dismissNotification() { _activeNotification.value = null }
    fun cleanup() { scope.cancel() }

    fun triggerSimulatedNotification(intensity: String) {
        val msg = when (intensity) {
            "Savage" -> idleSavageMessages.random()
            "Gentle" -> idleGentleMessages.random()
            else -> idleNormalMessages.random()
        }
        _activeNotification.value = SarcasticNotification(
            id = System.currentTimeMillis().toString(),
            title = "StudyOS AI Monitor [$intensity Mode]",
            message = msg,
            urgencyLevel = intensity
        )
    }
}
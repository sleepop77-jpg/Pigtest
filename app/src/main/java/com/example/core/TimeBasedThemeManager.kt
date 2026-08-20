package com.example.core

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

enum class ThemeMode {
    AUTO_TIME_BASED,
    FORCE_CORAL_DAY,
    FORCE_NIGHT_MAROON
}

enum class TimeOfDayPhase {
    MORNING_SUNRISE, // 5 AM - 8 AM
    DAY_CORAL,       // 8 AM - 5 PM
    EVENING_CORAL,   // 5 PM - 9 PM
    NIGHT_MAROON     // 9 PM - 5 AM
}

class TimeBasedThemeManager {
    private val _themeMode = MutableStateFlow(ThemeMode.AUTO_TIME_BASED)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _notificationIntensity = MutableStateFlow("Savage") // "Gentle", "Normal", "Savage"
    val notificationIntensity: StateFlow<String> = _notificationIntensity.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun toggleDarkMode() {
        if (isDarkThemeActive()) {
            _themeMode.value = ThemeMode.FORCE_CORAL_DAY
        } else {
            _themeMode.value = ThemeMode.FORCE_NIGHT_MAROON
        }
    }

    fun setNotificationIntensity(intensity: String) {
        _notificationIntensity.value = intensity
    }

    fun getCurrentPhase(): TimeOfDayPhase {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 5..7 -> TimeOfDayPhase.MORNING_SUNRISE
            hour in 8..16 -> TimeOfDayPhase.DAY_CORAL
            hour in 17..20 -> TimeOfDayPhase.EVENING_CORAL
            else -> TimeOfDayPhase.NIGHT_MAROON
        }
    }

    fun isDarkThemeActive(): Boolean {
        return when (_themeMode.value) {
            ThemeMode.FORCE_NIGHT_MAROON -> true
            ThemeMode.FORCE_CORAL_DAY -> false
            ThemeMode.AUTO_TIME_BASED -> {
                val phase = getCurrentPhase()
                phase == TimeOfDayPhase.NIGHT_MAROON
            }
        }
    }
}

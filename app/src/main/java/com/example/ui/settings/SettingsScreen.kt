package com.example.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.EconomyManager
import com.example.core.ThemeMode
import com.example.core.TimeBasedThemeManager
import com.example.data.repository.StudyRepository
import com.example.ui.common.StudyIcons
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeManager: TimeBasedThemeManager,
    economyManager: EconomyManager,
    repository: StudyRepository,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val currentThemeMode by themeManager.themeMode.collectAsState()
    val isDarkMode = themeManager.isDarkThemeActive()
    val notificationIntensity by themeManager.notificationIntensity.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }

    val screenBg = if (isDarkMode) PrimaryNightMaroon else PrimaryCoral
    val cardBg = if (isDarkMode) SurfaceNightCard else SurfaceCream
    val textColor = if (isDarkMode) OnSurfaceNight else OnSurfaceDark
    val textMuted = if (isDarkMode) OnSurfaceNightMuted else OnSurfaceMuted

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = screenBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "StudyOS Settings",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = StudyIcons.Back,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = screenBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Quick Link Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToProfile() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AccentTeal.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = StudyIcons.Person,
                                    contentDescription = null,
                                    tint = AccentTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Scholar Profile & Targets",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = textColor
                            )
                            Text(
                                text = "Edit username, major, bio & daily targets",
                                fontSize = 12.sp,
                                color = textMuted
                            )
                        }
                    }

                    Icon(
                        imageVector = StudyIcons.ChevronRight,
                        contentDescription = null,
                        tint = textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Dark Mode & Theme Mode Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "APPEARANCE & DISPLAY THEME",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isDarkMode) FameGold else PrimaryCoralDark,
                        letterSpacing = 1.sp
                    )

                    // Dark Mode Toggle Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) StudyIcons.DarkMode else StudyIcons.LightMode,
                                contentDescription = null,
                                tint = if (isDarkMode) FameGold else PrimaryCoral,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Dark Theme",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                                Text(
                                    text = if (isDarkMode) "Deep Velvet Maroon Night" else "Warm Coral Daylight",
                                    fontSize = 11.sp,
                                    color = textMuted
                                )
                            }
                        }

                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    themeManager.setThemeMode(ThemeMode.FORCE_NIGHT_MAROON)
                                } else {
                                    themeManager.setThemeMode(ThemeMode.FORCE_CORAL_DAY)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = FameGold,
                                checkedTrackColor = PrimaryNightCard,
                                uncheckedThumbColor = PrimaryCoral,
                                uncheckedTrackColor = SurfaceCreamLight
                            )
                        )
                    }

                    Divider(color = textMuted.copy(alpha = 0.2f))

                    Text(
                        text = "Theme Schedule Mode",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = textColor
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentThemeMode == ThemeMode.AUTO_TIME_BASED,
                            onClick = { themeManager.setThemeMode(ThemeMode.AUTO_TIME_BASED) },
                            label = { Text("Auto Circadian", fontSize = 11.sp) },
                            leadingIcon = { Icon(StudyIcons.AutoMode, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        FilterChip(
                            selected = currentThemeMode == ThemeMode.FORCE_CORAL_DAY,
                            onClick = { themeManager.setThemeMode(ThemeMode.FORCE_CORAL_DAY) },
                            label = { Text("Coral Day", fontSize = 11.sp) },
                            leadingIcon = { Icon(StudyIcons.LightMode, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        FilterChip(
                            selected = currentThemeMode == ThemeMode.FORCE_NIGHT_MAROON,
                            onClick = { themeManager.setThemeMode(ThemeMode.FORCE_NIGHT_MAROON) },
                            label = { Text("Night Maroon", fontSize = 11.sp) },
                            leadingIcon = { Icon(StudyIcons.DarkMode, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }

                    Text(
                        text = "Auto mode automatically shifts background at 5 AM (Sunrise), 8 AM (Coral), 5 PM (Sunset), and 9 PM (Night Maroon).",
                        fontSize = 12.sp,
                        color = textMuted
                    )
                }
            }

            // Onboarding Tutorial Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ONBOARDING & TUTORIAL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isDarkMode) FameGold else PrimaryCoralDark,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "View the interactive guided tour for Pomodoro timers, Fame/Shame economy, and Study Stocks.",
                        fontSize = 12.sp,
                        color = textMuted
                    )

                    OutlinedButton(
                        onClick = onNavigateToOnboarding,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isDarkMode) FameGold else PrimaryCoralDark
                        )
                    ) {
                        Icon(StudyIcons.Help, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Replay Onboarding Tutorial", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Notification Sarcasm Intensity Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "IDLE NOTIFICATION INTENSITY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isDarkMode) FameGold else PrimaryCoralDark,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Gentle", "Normal", "Savage").forEach { intensity ->
                            FilterChip(
                                selected = notificationIntensity == intensity,
                                onClick = { themeManager.setNotificationIntensity(intensity) },
                                label = { Text(intensity) }
                            )
                        }
                    }

                    Text(
                        text = when (notificationIntensity) {
                            "Savage" -> "Brutally honest, sarcastic reminders when idle for 20+ minutes during study hours."
                            "Gentle" -> "Encouraging, gentle reminders every 2 hours."
                            else -> "Balanced reminders every 30-60 minutes."
                        },
                        fontSize = 12.sp,
                        color = textMuted
                    )

                    Button(
                        onClick = {
                            economyManager.triggerSimulatedNotification(notificationIntensity)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Triggered simulated $notificationIntensity notification!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDarkMode) PrimaryNightCard else PrimaryCoral),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Preview $notificationIntensity Alert", color = Color.White)
                    }
                }
            }

            // Economy Protocol Summary
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "STREAMLINED ECONOMY RULES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isDarkMode) FameGold else PrimaryCoralDark,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "• +2 Fame per minute in active study session\n• +1 Shame per minute NOT studying (5 AM - 10 PM)\n• Fame cancels Shame directly\n• Achievement Bonuses: +50 Goal, +100 7-Day Streak, +75 Mastery Milestone\n• Use Fame to trade Study Stocks or unlock Store items",
                        fontSize = 13.sp,
                        color = textColor,
                        lineHeight = 18.sp
                    )
                }
            }

            // Data Reset Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "DATABASE & STORAGE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = WarningRed,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Reset all local database tables back to original seed data.",
                        fontSize = 12.sp,
                        color = textMuted
                    )

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset All Data", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset StudyOS Data?", fontWeight = FontWeight.Bold) },
            text = { Text("This will reset all Fame, Shame, goals, notes, and session logs to the default state.") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.resetAllData()
                            showResetDialog = false
                            snackbarHostState.showSnackbar("All data has been reset to defaults.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningRed)
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

package com.example.ui.launcher

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.EconomyManager
import com.example.core.MascotState
import com.example.core.TimeBasedThemeManager
import com.example.core.TimeOfDayPhase
import com.example.data.repository.StudyRepository
import com.example.ui.common.StudyIcons
import com.example.ui.theme.*

data class AppGridItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val badge: String? = null,
    val badgeColor: Color = Color(0xFFE53935),
    val route: String
)

@Composable
fun LauncherScreen(
    repository: StudyRepository,
    economyManager: EconomyManager,
    themeManager: TimeBasedThemeManager,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalFame by economyManager.totalFame.collectAsState()
    val totalShame by economyManager.totalShame.collectAsState()
    val streakDays by economyManager.currentStreakDays.collectAsState()
    val mascotState by economyManager.mascotState.collectAsState()
    val activeNotification by economyManager.activeNotification.collectAsState()

    val tasks by repository.allTasks.collectAsState(initial = emptyList())
    val notes by repository.allNotes.collectAsState(initial = emptyList())
    val studyGroups by repository.allStudyGroups.collectAsState(initial = emptyList())
    val userProfile by repository.userProfile.collectAsState(initial = null)
    val totalPomodoros by repository.totalPomodoros.collectAsState(initial = 0)

    val unfinishedTasksCount = tasks.count { !it.completed }

    // Pure functional OS apps (removed Tutorial app icon as requested)
    val appGridItems = remember(unfinishedTasksCount, notes.size, studyGroups.size) {
        listOf(
            AppGridItem("Timer", StudyIcons.PomodoroTimer, Color(0xFFD9534F), "Focus", Color(0xFFD32F2F), "pomodoro"),
            AppGridItem("Tasks", StudyIcons.TasksGoals, Color(0xFF4CAF50), if (unfinishedTasksCount > 0) "$unfinishedTasksCount" else null, Color(0xFF2E7D32), "tasks_goals"),
            AppGridItem("Stocks", StudyIcons.StudyStocks, Color(0xFF20B2AA), "+14%", Color(0xFF00796B), "stocks"),
            AppGridItem("Flashcards", StudyIcons.Flashcards, Color(0xFF9C27B0), "Active", Color(0xFF7B1FA2), "flashcards"),
            AppGridItem("Notes OS", StudyIcons.Notes, Color(0xFFF5A623), "${notes.size}", Color(0xFFE65100), "notes"),
            AppGridItem("Analytics", StudyIcons.Analytics, Color(0xFF00BCD4), "Heatmap", Color(0xFF0097A7), "analytics"),
            AppGridItem("Fame Store", StudyIcons.FameStore, Color(0xFFFFD700), "Shop", Color(0xFFF57F17), "store"),
            AppGridItem("Leaderboard", StudyIcons.Leaderboard, Color(0xFF78909C), "Tiers", Color(0xFF455A64), "leaderboard"),
            AppGridItem("Study Squad", StudyIcons.StudyGroups, Color(0xFF3F51B5), "${studyGroups.size}", Color(0xFF283593), "groups"),
            AppGridItem("Profile", StudyIcons.Person, Color(0xFFE91E63), "VIP", Color(0xFFC2185B), "profile"),
            AppGridItem("Settings", StudyIcons.Settings, Color(0xFF757575), null, Color.Gray, "settings")
        )
    }

    val currentPhase = themeManager.getCurrentPhase()
    val isNightMode = themeManager.isDarkThemeActive()

    val backgroundBrush = remember(currentPhase, isNightMode) {
        when {
            isNightMode -> Brush.verticalGradient(
                listOf(Color(0xFF4A2C2C), Color(0xFF241515))
            )
            currentPhase == TimeOfDayPhase.MORNING_SUNRISE -> Brush.verticalGradient(
                listOf(Color(0xFFE8706C), Color(0xFFF5A623), Color(0xFFD9534F))
            )
            currentPhase == TimeOfDayPhase.EVENING_CORAL -> Brush.verticalGradient(
                listOf(Color(0xFFC94440), Color(0xFF4A2C2C))
            )
            else -> Brush.verticalGradient(
                listOf(Color(0xFFD9534F), Color(0xFFC94440))
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 0. Top Bar: Scholar identity + Dark theme toggle + Settings quick link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.clickable { onNavigateToRoute("profile") }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = StudyIcons.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = userProfile?.fullName ?: "Scholar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isNightMode) "Night Maroon Mode" else "Day Coral Mode",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dark Mode Toggle Switch
                    IconButton(
                        onClick = { themeManager.toggleDarkMode() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                    ) {
                        Icon(
                            imageVector = if (isNightMode) StudyIcons.LightMode else StudyIcons.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = if (isNightMode) FameGold else Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Settings
                    IconButton(
                        onClick = { onNavigateToRoute("settings") },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                    ) {
                        Icon(
                            imageVector = StudyIcons.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // 1. Dynamic Circadian Status Bar (Fame vs Shame Balance)
            StatusBarComposable(
                fame = totalFame,
                shame = totalShame,
                streakDays = streakDays,
                onStatusBarClick = { onNavigateToRoute("pomodoro") }
            )

            // Anti-Procrastination Sarcastic/Motivational Notification Alert (if active)
            AnimatedVisibility(
                visible = activeNotification != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (activeNotification != null) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (activeNotification!!.urgencyLevel == "Savage") Color(0xFF330000) else Color(0xFF4A2800)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = StudyIcons.ShameDanger,
                                    contentDescription = "Alert",
                                    tint = if (activeNotification!!.urgencyLevel == "Savage") WarningRed else FameGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = activeNotification!!.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = activeNotification!!.message,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            IconButton(onClick = { economyManager.dismissNotification() }) {
                                Icon(
                                    imageVector = StudyIcons.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Hero Interactive Mascot Focus Card (Unified with Pomodoro Aesthetic)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Tap-responsive animated Mascot with quotes and burst particles
                    InteractiveMascot(
                        state = mascotState,
                        size = 110.dp,
                        showArc = true,
                        progressArc = 0.85f
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (mascotState) {
                                MascotState.STUDYING -> "Deep Focus Active"
                                MascotState.STREAK -> "$streakDays-Day Streak On Fire! 🔥"
                                MascotState.WINNING -> "Champion Mode (+300 Fame)"
                                MascotState.HIGH_SHAME -> "Study now to eliminate Shame!"
                                MascotState.NIGHT_OWL -> "Night Owl Focus Ready"
                                else -> "Tap Mascot for Motivation!"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )

                        Text(
                            text = "+2 Fame/min in active sessions",
                            fontSize = 12.sp,
                            color = FameGold,
                            fontWeight = FontWeight.SemiBold
                        )

                        Button(
                            onClick = { onNavigateToRoute("pomodoro") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) {
                            Icon(
                                imageVector = StudyIcons.Play,
                                contentDescription = null,
                                tint = if (isNightMode) PrimaryNightMaroon else PrimaryCoral,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Start Pomodoro",
                                color = if (isNightMode) PrimaryNightMaroon else PrimaryCoral,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 3. Quick Stats & Challenge Widgets
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickStatsWidget(
                    fameEarnedThisWeek = totalFame,
                    shameIncurredThisWeek = totalShame,
                    globalRank = 1,
                    streakDays = streakDays,
                    onClick = { onNavigateToRoute("analytics") }
                )

                ChallengeWidget(
                    challengeTitle = "Focus Milestone: Log 4 Pomodoro Sessions",
                    currentProgress = totalPomodoros,
                    targetGoal = 4,
                    rewardFame = 200,
                    deadlineText = "Daily Goal",
                    onClick = { onNavigateToRoute("pomodoro") }
                )
            }

            // 4. OS App Grid (Unified with theme styling)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "STUDYOS APPLICATIONS",
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp
                )

                // 4-Column Grid Rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    appGridItems.take(4).forEach { item ->
                        AppIcon(
                            title = item.title,
                            icon = item.icon,
                            accentColor = item.color,
                            badgeText = item.badge,
                            badgeColor = item.badgeColor,
                            onClick = { onNavigateToRoute(item.route) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    appGridItems.drop(4).take(4).forEach { item ->
                        AppIcon(
                            title = item.title,
                            icon = item.icon,
                            accentColor = item.color,
                            badgeText = item.badge,
                            badgeColor = item.badgeColor,
                            onClick = { onNavigateToRoute(item.route) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    appGridItems.drop(8).take(3).forEach { item ->
                        AppIcon(
                            title = item.title,
                            icon = item.icon,
                            accentColor = item.color,
                            badgeText = item.badge,
                            badgeColor = item.badgeColor,
                            onClick = { onNavigateToRoute(item.route) }
                        )
                    }
                    // Empty spacer to balance 4-column layout
                    Spacer(modifier = Modifier.width(68.dp))
                }
            }
        }
    }
}

@Composable
fun AppIcon(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    badgeText: String? = null,
    badgeColor: Color = Color(0xFFE53935),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .width(72.dp)
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (badgeText != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun QuickStatsWidget(
    fameEarnedThisWeek: Int,
    shameIncurredThisWeek: Int,
    globalRank: Int,
    streakDays: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Weekly Focus Stats",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
                Text(
                    text = "+$fameEarnedThisWeek Fame · $shameIncurredThisWeek Shame",
                    fontSize = 11.sp,
                    color = FameGold
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = StudyIcons.StreakFlame,
                        contentDescription = null,
                        tint = FameGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$streakDays Days Streak",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeWidget(
    challengeTitle: String,
    currentProgress: Int,
    targetGoal: Int,
    rewardFame: Int,
    deadlineText: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = challengeTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "+$rewardFame Fame",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = FameGold
                )
            }

            val progress = (currentProgress.toFloat() / targetGoal.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = FameGold,
                trackColor = Color.White.copy(alpha = 0.2f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$currentProgress / $targetGoal completed",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = deadlineText,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

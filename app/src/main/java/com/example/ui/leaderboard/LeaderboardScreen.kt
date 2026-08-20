package com.example.ui.leaderboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.TimeBasedThemeManager
import com.example.data.repository.StudyRepository
import com.example.ui.common.StudyIcons
import com.example.ui.theme.*

data class ScholarTier(
    val tierName: String,
    val minHours: Float,
    val minFame: Int,
    val badgeColor: Color,
    val perkText: String,
    val isAchieved: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    repository: StudyRepository,
    themeManager: TimeBasedThemeManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalFame by repository.totalFame.collectAsState(initial = 100)
    val userProfile by repository.userProfile.collectAsState(initial = null)
    val subjects by repository.allSubjects.collectAsState(initial = emptyList())
    val sessions by repository.allSessions.collectAsState(initial = emptyList())

    val isNightMode = themeManager.isDarkThemeActive()

    val totalHoursStudied = remember(sessions) {
        sessions.filter { it.completed }.sumOf { it.durationMinutes } / 60.0f
    }

    var selectedTab by remember { mutableStateOf("Scholar Tiers") }

    val tiers = remember(totalHoursStudied, totalFame) {
        listOf(
            ScholarTier("Novice Scholar", 0f, 0, Color(0xFF8D6E63), "Unlocked StudyOS Starter Pack", true),
            ScholarTier("Bronze Scholar", 2f, 150, Color(0xFFCD7F32), "Access to Study Stocks Exchange", totalHoursStudied >= 2f && totalFame >= 150),
            ScholarTier("Silver Scholar", 10f, 300, Color(0xFFC0C0C0), "+10% Fame Yield on Pomodoros", totalHoursStudied >= 10f && totalFame >= 300),
            ScholarTier("Gold Scholar", 25f, 600, FameGold, "+25% Stock Dividend Yields", totalHoursStudied >= 25f && totalFame >= 600),
            ScholarTier("Platinum Scholar", 50f, 1200, Color(0xFF00BCD4), "Exclusive Cyberpunk StudyBuddy Skin", totalHoursStudied >= 50f && totalFame >= 1200),
            ScholarTier("Grandmaster", 100f, 2500, Color(0xFFE91E63), "Grandmaster Golden Desk & Crown", totalHoursStudied >= 100f && totalFame >= 2500)
        )
    }

    val currentTier = tiers.lastOrNull { it.isAchieved } ?: tiers.first()

    val backgroundBrush = remember(isNightMode) {
        if (isNightMode) Brush.verticalGradient(listOf(Color(0xFF4A2C2C), Color(0xFF241515)))
        else Brush.verticalGradient(listOf(Color(0xFFD9534F), Color(0xFFC94440)))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scholar Rankings & Tiers",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // User Rank Card
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = currentTier.badgeColor,
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = StudyIcons.Trophy,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = userProfile?.fullName ?: "Scholar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = OnSurfaceDark
                                        )
                                        Text(
                                            text = currentTier.tierName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = currentTier.badgeColor
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = FameGold.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "$totalFame Fame",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = OnSurfaceDark,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Divider(color = Color.Black.copy(alpha = 0.08f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Study Hours", fontSize = 11.sp, color = OnSurfaceMuted)
                                    Text(String.format("%.1f hrs", totalHoursStudied), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurfaceDark)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Current Tier", fontSize = 11.sp, color = OnSurfaceMuted)
                                    Text(currentTier.tierName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = currentTier.badgeColor)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Active Subjects", fontSize = 11.sp, color = OnSurfaceMuted)
                                    Text("${subjects.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurfaceDark)
                                }
                            }
                        }
                    }
                }

                // Tab Switcher: Scholar Tiers vs Subject Mastery
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Scholar Tiers", "Subject Mastery").forEach { tab ->
                            val isSelected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable { selectedTab = tab }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) PrimaryCoralDark else Color.White
                                )
                            }
                        }
                    }
                }

                if (selectedTab == "Scholar Tiers") {
                    items(tiers) { tier ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (tier.isAchieved) Color.White else Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = tier.badgeColor,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (tier.isAchieved) StudyIcons.Check else StudyIcons.Lock,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = tier.tierName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (tier.isAchieved) OnSurfaceDark else Color.White
                                        )
                                        Text(
                                            text = tier.perkText,
                                            fontSize = 11.sp,
                                            color = if (tier.isAchieved) OnSurfaceMuted else Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Text(
                                    text = "${tier.minHours.toInt()}h · ${tier.minFame} F",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tier.isAchieved) tier.badgeColor else Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                } else {
                    // Subject Mastery Rank
                    val sortedSubjects = subjects.sortedByDescending { it.studyHoursTotal }
                    items(sortedSubjects, key = { it.id }) { sub ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = PrimaryCoral,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = sub.name.take(2).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = sub.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = OnSurfaceDark
                                        )
                                        Text(
                                            text = "${sub.masteryPercent}% Mastery Score",
                                            fontSize = 11.sp,
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${sub.studyHoursTotal} hrs",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = OnSurfaceDark
                                    )
                                    Text(
                                        text = "${sub.cardsReviewed} Cards",
                                        fontSize = 11.sp,
                                        color = OnSurfaceMuted
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

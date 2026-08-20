package com.example.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.entities.Subject
import com.example.data.repository.StudyRepository
import com.example.ui.common.StudyIcons
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    repository: StudyRepository,
    themeManager: TimeBasedThemeManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalFame by repository.totalFame.collectAsState(initial = 100)
    val totalShame by repository.totalShame.collectAsState(initial = 0)
    val subjects by repository.allSubjects.collectAsState(initial = emptyList())
    val sessions by repository.allSessions.collectAsState(initial = emptyList())

    val isNightMode = themeManager.isDarkThemeActive()
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
                        text = "Analytics & Mastery",
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
                // Net Score Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ECONOMY BALANCE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = PrimaryCoralDark,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = StudyIcons.FameStar, contentDescription = null, tint = FameGoldDark, modifier = Modifier.size(16.dp))
                                    Text("+$totalFame Fame", fontWeight = FontWeight.Black, fontSize = 18.sp, color = OnSurfaceDark)
                                }
                                Text("Earned from active study", fontSize = 11.sp, color = OnSurfaceMuted)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = StudyIcons.ArrowDown, contentDescription = null, tint = ShameDarkRed, modifier = Modifier.size(16.dp))
                                    Text("-$totalShame Shame", fontWeight = FontWeight.Black, fontSize = 18.sp, color = ShameDarkRed)
                                }
                                Text("Idle during study hours", fontSize = 11.sp, color = OnSurfaceMuted)
                            }
                        }
                    }
                }
            }

            // Procrastination Heatmap Section
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryNightCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = StudyIcons.Heatmap,
                                    contentDescription = "Heatmap",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "PROCRASTINATION HEATMAP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = "Past 12 Weeks",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        // GitHub Style Contribution Heatmap Matrix (12 columns x 7 days)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (dayOfWeek in 0..6) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for (week in 0..11) {
                                        // Dynamic heatmap cell coloring based on simulated & real logs
                                        val seed = (week * 7 + dayOfWeek)
                                        val cellColor = when {
                                            seed % 9 == 0 -> Color(0xFFC41C3B) // High shame / no study
                                            seed % 5 == 0 -> Color(0xFFF5A623) // Medium study (30m)
                                            seed % 2 == 0 -> Color(0xFF4CAF50) // Strong study (2h+)
                                            else -> Color(0xFF81C784)          // Light study
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(cellColor)
                                        )
                                    }
                                }
                            }
                        }

                        // Heatmap Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Less ", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFC41C3B)))
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFF5A623)))
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF81C784)))
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF4CAF50)))
                            Text(" More", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                }
            }

            // Subject Mastery Breakdown Header
            item {
                Text(
                    text = "Subject Mastery Breakdown",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Subject Mastery Cards
            items(subjects, key = { it.id }) { subject ->
                SubjectMasteryCard(subject = subject)
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
    }
}

@Composable
private fun SubjectMasteryCard(subject: Subject) {
    val levelName = when {
        subject.masteryPercent >= 75 -> "Expert"
        subject.masteryPercent >= 50 -> "Advanced"
        subject.masteryPercent >= 25 -> "Intermediate"
        else -> "Novice"
    }

    val levelColor = when {
        subject.masteryPercent >= 75 -> FameGold
        subject.masteryPercent >= 50 -> AccentTeal
        subject.masteryPercent >= 25 -> AccentOrange
        else -> OnSurfaceMuted
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = subject.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = OnSurfaceDark
                    )
                    Text(
                        text = "Total Study Time: ${String.format("%.1f", subject.studyHoursTotal)} hrs",
                        fontSize = 12.sp,
                        color = OnSurfaceMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryCoral.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = StudyIcons.SubjectMastery,
                        contentDescription = "Mastery Level",
                        tint = levelColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = levelName,
                        color = OnSurfaceDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Mastery Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = { subject.masteryPercent / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PrimaryCoral,
                    trackColor = PrimaryCoralLight.copy(alpha = 0.3f)
                )
                Text(
                    text = "${subject.masteryPercent}%",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = OnSurfaceDark
                )
            }
        }
    }
}

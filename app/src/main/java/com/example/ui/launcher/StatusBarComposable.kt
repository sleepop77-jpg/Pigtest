package com.example.ui.launcher

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.StudyIcons
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatusBarComposable(
    fame: Int,
    shame: Int,
    streakDays: Int,
    onStatusBarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Current live time
    var currentTimeString by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(10_000L)
        }
    }

    // Dynamic background styling based on Fame/Shame
    val isWarning = shame > fame && shame > 20
    val isHighStreak = streakDays >= 7
    val isHighFame = fame >= 200

    val pillBgColor by animateColorAsState(
        targetValue = when {
            isWarning -> WarningRed.copy(alpha = 0.85f)
            isHighFame -> FameGold.copy(alpha = 0.95f)
            fame >= 50 -> Color(0xFFFFECC8)
            else -> Color.White.copy(alpha = 0.25f)
        },
        animationSpec = tween(500),
        label = "pill_bg"
    )

    val textColor = when {
        isWarning -> Color.White
        isHighFame || fame >= 50 -> OnSurfaceDark
        else -> Color.White
    }

    val shameTextColor = when {
        isWarning -> Color(0xFFFFD4D4)
        else -> ShameDarkRed
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(elevation = if (isHighFame) 6.dp else 2.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = if (isHighFame) {
                    Brush.horizontalGradient(
                        listOf(FameGold, Color(0xFFFFE57F), FameGold)
                    )
                } else {
                    Brush.horizontalGradient(listOf(pillBgColor, pillBgColor))
                }
            )
            .clickable { onStatusBarClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Row 1: Fame (Large), Streak (Center), Time (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fame Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = StudyIcons.FameStar,
                        contentDescription = "Fame Points",
                        tint = if (isHighFame || fame >= 50) FameGoldDark else FameGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "$fame",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = textColor
                    )
                    Text(
                        text = "FAME",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.75f)
                    )
                }

                // Streak Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isHighStreak) AccentOrange.copy(alpha = 0.3f)
                            else Color.Black.copy(alpha = 0.08f)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = StudyIcons.StreakFlame,
                        contentDescription = "Study Streak",
                        tint = if (isHighStreak) AccentOrange else Color(0xFFE65100),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${streakDays}d Streak",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = textColor
                    )
                }

                // Time Indicator
                Text(
                    text = currentTimeString,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.9f)
                )
            }

            // Row 2: Shame Metric (Small, left-aligned)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = StudyIcons.ArrowDown,
                        contentDescription = "Shame Points",
                        tint = shameTextColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "$shame Shame",
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = shameTextColor
                    )
                    if (isWarning) {
                        Text(
                            text = "(Danger! Study now to cancel)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }

                // Net Score Pill
                val netScore = fame - shame
                Text(
                    text = "Net: ${if (netScore >= 0) "+$netScore" else "$netScore"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (netScore >= 0) SuccessGreen else WarningRed
                )
            }
        }
    }
}

package com.example.ui.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.StudyIcons
import com.example.ui.theme.*

@Composable
fun ChallengeWidget(
    challengeTitle: String = "Math Challenge: Complete 50 Flashcards",
    currentProgress: Int = 23,
    targetGoal: Int = 50,
    rewardFame: Int = 200,
    deadlineText: String = "Friday 11:59 PM",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val progressFloat = (currentProgress.toFloat() / targetGoal.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        imageVector = StudyIcons.SubjectMastery,
                        contentDescription = "Active Challenge",
                        tint = FameGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "ACTIVE CHALLENGE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = StudyIcons.FameStar,
                        contentDescription = "Reward",
                        tint = FameGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "+$rewardFame Fame",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = FameGold
                    )
                }
            }

            Text(
                text = challengeTitle,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.White
            )

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progressFloat },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = FameGold,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )
                Text(
                    text = "$currentProgress / $targetGoal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Expires: $deadlineText",
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
                Text(
                    text = "${(progressFloat * 100).toInt()}% Done",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

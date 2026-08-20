package com.example.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom SVG / Vector-rendered illustrations for the Onboarding Tutorial.
 * Built purely with Compose Canvas and vector path primitives for maximum crispness,
 * lightweight loading, and responsive scaling in both Light and Dark themes.
 */

@Composable
fun CircadianWelcomeIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    isDark: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circadian")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f + floatOffset)
            val radius = this.size.width * 0.38f

            // Outer Soft Ambient Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        (if (isDark) FameGold else Color.White).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.5f
                ),
                radius = radius * 1.4f,
                center = center
            )

            // Circadian Orbit Ring
            drawCircle(
                color = if (isDark) SurfaceCream.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.3f),
                radius = radius,
                center = center,
                style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), rotation * 2))
            )

            // Draw Day/Night Hemisphere Base
            val dayArcColor = if (isDark) PrimaryCoralLight else Color(0xFFFFCC80)
            val nightArcColor = if (isDark) PrimaryNightCard else Color(0xFF4A2C2C)
            drawArc(
                color = dayArcColor.copy(alpha = 0.4f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - radius * 0.9f, center.y - radius * 0.9f),
                size = Size(radius * 1.8f, radius * 1.8f),
                style = Stroke(width = 6.dp.toPx())
            )
            drawArc(
                color = nightArcColor.copy(alpha = 0.5f),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - radius * 0.9f, center.y - radius * 0.9f),
                size = Size(radius * 1.8f, radius * 1.8f),
                style = Stroke(width = 6.dp.toPx())
            )

            // Sun Node (Day)
            val sunAngleRad = Math.toRadians((rotation - 90).toDouble())
            val sunX = center.x + (radius * cos(sunAngleRad)).toFloat()
            val sunY = center.y + (radius * sin(sunAngleRad)).toFloat()
            drawCircle(color = FameGold, radius = 14.dp.toPx(), center = Offset(sunX, sunY))
            drawCircle(color = Color.White, radius = 8.dp.toPx(), center = Offset(sunX, sunY))

            // Moon Node (Night)
            val moonAngleRad = Math.toRadians((rotation + 90).toDouble())
            val moonX = center.x + (radius * cos(moonAngleRad)).toFloat()
            val moonY = center.y + (radius * sin(moonAngleRad)).toFloat()
            drawCircle(color = Color(0xFFE0E0E0), radius = 12.dp.toPx(), center = Offset(moonX, moonY))
            // Moon shadow crescent
            drawCircle(
                color = if (isDark) PrimaryNightMaroon else PrimaryCoral,
                radius = 10.dp.toPx(),
                center = Offset(moonX - 4.dp.toPx(), moonY - 3.dp.toPx())
            )

            // Central Friendly Mascot Vector Body
            val mascotRadius = radius * 0.52f
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(PrimaryCoralLight, PrimaryCoralDark),
                    startY = center.y - mascotRadius,
                    endY = center.y + mascotRadius
                ),
                radius = mascotRadius,
                center = center
            )

            // Mascot cute eyes
            val eyeSpacing = mascotRadius * 0.35f
            val eyeY = center.y - mascotRadius * 0.1f
            drawCircle(color = Color(0xFF2B1515), radius = 5.dp.toPx(), center = Offset(center.x - eyeSpacing, eyeY))
            drawCircle(color = Color(0xFF2B1515), radius = 5.dp.toPx(), center = Offset(center.x + eyeSpacing, eyeY))
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(center.x - eyeSpacing - 1.dp.toPx(), eyeY - 2.dp.toPx()))
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(center.x + eyeSpacing - 1.dp.toPx(), eyeY - 2.dp.toPx()))

            // Mascot blush cheeks
            drawCircle(color = Color(0xFFFF8A80).copy(alpha = 0.6f), radius = 6.dp.toPx(), center = Offset(center.x - eyeSpacing - 4.dp.toPx(), eyeY + 7.dp.toPx()))
            drawCircle(color = Color(0xFFFF8A80).copy(alpha = 0.6f), radius = 6.dp.toPx(), center = Offset(center.x + eyeSpacing + 4.dp.toPx(), eyeY + 7.dp.toPx()))

            // Smiling mouth
            val mouthPath = Path().apply {
                moveTo(center.x - 7.dp.toPx(), eyeY + 5.dp.toPx())
                quadraticTo(center.x, eyeY + 12.dp.toPx(), center.x + 7.dp.toPx(), eyeY + 5.dp.toPx())
            }
            drawPath(mouthPath, color = Color(0xFF2B1515), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

            // Green Stem on Top
            val stemPath = Path().apply {
                moveTo(center.x, center.y - mascotRadius)
                cubicTo(
                    center.x - 4.dp.toPx(), center.y - mascotRadius - 12.dp.toPx(),
                    center.x + 8.dp.toPx(), center.y - mascotRadius - 16.dp.toPx(),
                    center.x + 12.dp.toPx(), center.y - mascotRadius - 12.dp.toPx()
                )
            }
            drawPath(stemPath, color = Color(0xFF4CAF50), style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
fun PomodoroStreakIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    isDark: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pomodoro")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.width * 0.36f * pulse

            // Background Clock Ring
            drawCircle(
                color = if (isDark) SurfaceNightCard else Color.White.copy(alpha = 0.2f),
                radius = radius * 1.15f,
                center = center
            )

            // Progress Arc (75% completed)
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(FameGold, PrimaryCoralLight, FameGold),
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )

            // Clock face ticks
            for (i in 0 until 12) {
                val tickAngle = Math.toRadians((i * 30).toDouble())
                val tickStart = Offset(
                    center.x + (radius * 0.82f * cos(tickAngle)).toFloat(),
                    center.y + (radius * 0.82f * sin(tickAngle)).toFloat()
                )
                val tickEnd = Offset(
                    center.x + (radius * 0.92f * cos(tickAngle)).toFloat(),
                    center.y + (radius * 0.92f * sin(tickAngle)).toFloat()
                )
                drawLine(
                    color = Color.White.copy(alpha = if (i % 3 == 0) 0.8f else 0.4f),
                    start = tickStart,
                    end = tickEnd,
                    strokeWidth = if (i % 3 == 0) 3.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Central Flame & Streak Badge
            val flamePath = Path().apply {
                moveTo(center.x, center.y - radius * 0.45f)
                cubicTo(
                    center.x + radius * 0.35f, center.y - radius * 0.2f,
                    center.x + radius * 0.4f, center.y + radius * 0.3f,
                    center.x, center.y + radius * 0.42f
                )
                cubicTo(
                    center.x - radius * 0.4f, center.y + radius * 0.3f,
                    center.x - radius * 0.35f, center.y - radius * 0.2f,
                    center.x, center.y - radius * 0.45f
                )
            }
            drawPath(
                flamePath,
                brush = Brush.verticalGradient(
                    colors = listOf(FameGold, Color(0xFFFF5722)),
                    startY = center.y - radius * 0.5f,
                    endY = center.y + radius * 0.4f
                )
            )

            // Inner core flame
            val innerFlame = Path().apply {
                moveTo(center.x, center.y - radius * 0.25f)
                cubicTo(
                    center.x + radius * 0.18f, center.y - radius * 0.1f,
                    center.x + radius * 0.2f, center.y + radius * 0.2f,
                    center.x, center.y + radius * 0.3f
                )
                cubicTo(
                    center.x - radius * 0.2f, center.y + radius * 0.2f,
                    center.x - radius * 0.18f, center.y - radius * 0.1f,
                    center.x, center.y - radius * 0.25f
                )
            }
            drawPath(innerFlame, color = Color.White.copy(alpha = 0.9f))

            // Study Headphones on Top
            val headphoneArc = Path().apply {
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        center.x - radius * 0.6f,
                        center.y - radius * 0.95f,
                        center.x + radius * 0.6f,
                        center.y + radius * 0.1f
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
            }
            drawPath(headphoneArc, color = Color(0xFF212121), style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
            drawRoundRect(
                color = Color(0xFF212121),
                topLeft = Offset(center.x - radius * 0.65f, center.y - radius * 0.45f),
                size = Size(8.dp.toPx(), 18.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFF212121),
                topLeft = Offset(center.x + radius * 0.65f - 8.dp.toPx(), center.y - radius * 0.45f),
                size = Size(8.dp.toPx(), 18.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}

@Composable
fun EconomyBalanceIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    isDark: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "economy")
    val tilt by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tilt"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val scaleWidth = this.size.width * 0.7f

            // Base stand of the scale
            val standBaseY = center.y + 60.dp.toPx()
            drawLine(
                color = if (isDark) SurfaceCream else Color.White,
                start = Offset(center.x - 40.dp.toPx(), standBaseY),
                end = Offset(center.x + 40.dp.toPx(), standBaseY),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = if (isDark) SurfaceCream else Color.White,
                start = Offset(center.x, standBaseY),
                end = Offset(center.x, center.y - 10.dp.toPx()),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = FameGold, radius = 7.dp.toPx(), center = Offset(center.x, center.y - 10.dp.toPx()))

            // Cross beam tilted
            val beamLength = scaleWidth * 0.75f
            val tiltRad = Math.toRadians(tilt.toDouble())
            val leftEnd = Offset(
                center.x - (beamLength / 2f * cos(tiltRad)).toFloat(),
                center.y - 10.dp.toPx() - (beamLength / 2f * sin(tiltRad)).toFloat()
            )
            val rightEnd = Offset(
                center.x + (beamLength / 2f * cos(tiltRad)).toFloat(),
                center.y - 10.dp.toPx() + (beamLength / 2f * sin(tiltRad)).toFloat()
            )
            drawLine(
                color = FameGold,
                start = leftEnd,
                end = rightEnd,
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Left Pan: FAME (Winning side with glowing gold coins)
            val panDrop = 36.dp.toPx()
            val leftPanCenter = Offset(leftEnd.x, leftEnd.y + panDrop)
            drawLine(color = Color.White.copy(alpha = 0.6f), start = leftEnd, end = Offset(leftPanCenter.x - 18.dp.toPx(), leftPanCenter.y), strokeWidth = 1.5.dp.toPx())
            drawLine(color = Color.White.copy(alpha = 0.6f), start = leftEnd, end = Offset(leftPanCenter.x + 18.dp.toPx(), leftPanCenter.y), strokeWidth = 1.5.dp.toPx())
            drawArc(
                color = FameGold,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(leftPanCenter.x - 22.dp.toPx(), leftPanCenter.y - 6.dp.toPx()),
                size = Size(44.dp.toPx(), 22.dp.toPx())
            )
            // Gold Fame Coins on Left Pan
            drawCircle(color = FameGold, radius = 9.dp.toPx(), center = Offset(leftPanCenter.x - 6.dp.toPx(), leftPanCenter.y - 4.dp.toPx()))
            drawCircle(color = Color(0xFFFFE082), radius = 6.dp.toPx(), center = Offset(leftPanCenter.x - 6.dp.toPx(), leftPanCenter.y - 4.dp.toPx()))
            drawCircle(color = FameGold, radius = 8.dp.toPx(), center = Offset(leftPanCenter.x + 8.dp.toPx(), leftPanCenter.y - 2.dp.toPx()))

            // Right Pan: SHAME (Diminishing side)
            val rightPanCenter = Offset(rightEnd.x, rightEnd.y + panDrop)
            drawLine(color = Color.White.copy(alpha = 0.6f), start = rightEnd, end = Offset(rightPanCenter.x - 18.dp.toPx(), rightPanCenter.y), strokeWidth = 1.5.dp.toPx())
            drawLine(color = Color.White.copy(alpha = 0.6f), start = rightEnd, end = Offset(rightPanCenter.x + 18.dp.toPx(), rightPanCenter.y), strokeWidth = 1.5.dp.toPx())
            drawArc(
                color = ShameDarkRed,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(rightPanCenter.x - 22.dp.toPx(), rightPanCenter.y - 6.dp.toPx()),
                size = Size(44.dp.toPx(), 22.dp.toPx())
            )
            // Warning Shield on Right Pan
            drawCircle(color = ShameWarningRed, radius = 8.dp.toPx(), center = Offset(rightPanCenter.x, rightPanCenter.y - 4.dp.toPx()))

            // Sparkles around Fame
            drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = Offset(leftPanCenter.x - 18.dp.toPx(), leftPanCenter.y - 22.dp.toPx()))
            drawCircle(color = FameGold, radius = 3.dp.toPx(), center = Offset(leftPanCenter.x + 14.dp.toPx(), leftPanCenter.y - 18.dp.toPx()))
        }
    }
}

@Composable
fun StocksSquadsIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    isDark: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stocks")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val chartWidth = this.size.width * 0.78f
            val chartHeight = this.size.height * 0.5f

            // Card background for Stock UI
            val cardLeft = center.x - chartWidth / 2f
            val cardTop = center.y - chartHeight / 2f
            drawRoundRect(
                color = if (isDark) SurfaceNightCard else Color.White.copy(alpha = 0.25f),
                topLeft = Offset(cardLeft, cardTop),
                size = Size(chartWidth, chartHeight),
                cornerRadius = CornerRadius(16.dp.toPx())
            )

            // Grid lines
            for (i in 1..3) {
                val y = cardTop + (chartHeight / 4f) * i
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(cardLeft + 12.dp.toPx(), y),
                    end = Offset(cardLeft + chartWidth - 12.dp.toPx(), y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Green Ascending Stock Curve
            val path = Path().apply {
                moveTo(cardLeft + 16.dp.toPx(), cardTop + chartHeight - 20.dp.toPx())
                cubicTo(
                    cardLeft + chartWidth * 0.3f, cardTop + chartHeight - 30.dp.toPx() - waveOffset,
                    cardLeft + chartWidth * 0.6f, cardTop + chartHeight * 0.4f + waveOffset,
                    cardLeft + chartWidth - 16.dp.toPx(), cardTop + 24.dp.toPx()
                )
            }
            drawPath(
                path,
                brush = Brush.horizontalGradient(listOf(Color(0xFF20B2AA), Color(0xFF4CAF50), FameGold)),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // End point bull dot
            val endX = cardLeft + chartWidth - 16.dp.toPx()
            val endY = cardTop + 24.dp.toPx()
            drawCircle(color = FameGold, radius = 6.dp.toPx(), center = Offset(endX, endY))
            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(endX, endY))

            // Squad Avatars at the bottom
            val squadY = cardTop + chartHeight + 20.dp.toPx()
            val squadColors = listOf(AccentTeal, AccentOrange, AccentPurple, AccentCyan)
            for (idx in squadColors.indices) {
                val avatarX = center.x - 45.dp.toPx() + idx * 30.dp.toPx()
                drawCircle(color = Color.White, radius = 13.dp.toPx(), center = Offset(avatarX, squadY))
                drawCircle(color = squadColors[idx], radius = 11.dp.toPx(), center = Offset(avatarX, squadY))
                // Simple smiling eye marks
                drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = Offset(avatarX - 3.dp.toPx(), squadY - 2.dp.toPx()))
                drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = Offset(avatarX + 3.dp.toPx(), squadY - 2.dp.toPx()))
            }
        }
    }
}

@Composable
fun ProfileThemeIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    isDark: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "profile")
    val togglePos by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "toggle"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Dual split card (Left Day, Right Night)
            val cardW = 160.dp.toPx()
            val cardH = 100.dp.toPx()
            val cardLeft = center.x - cardW / 2f
            val cardTop = center.y - 30.dp.toPx()

            // Left half: Coral Day
            drawRoundRect(
                color = PrimaryCoral,
                topLeft = Offset(cardLeft, cardTop),
                size = Size(cardW / 2f, cardH),
                cornerRadius = CornerRadius(16.dp.toPx())
            )
            // Right half: Night Maroon
            drawRoundRect(
                color = PrimaryNightMaroon,
                topLeft = Offset(center.x, cardTop),
                size = Size(cardW / 2f, cardH),
                cornerRadius = CornerRadius(16.dp.toPx())
            )

            // Sun icon on left
            drawCircle(color = FameGold, radius = 12.dp.toPx(), center = Offset(cardLeft + 40.dp.toPx(), cardTop + 35.dp.toPx()))
            // Moon icon on right
            drawCircle(color = Color(0xFFE0E0E0), radius = 12.dp.toPx(), center = Offset(center.x + 40.dp.toPx(), cardTop + 35.dp.toPx()))
            drawCircle(color = PrimaryNightMaroon, radius = 10.dp.toPx(), center = Offset(center.x + 36.dp.toPx(), cardTop + 32.dp.toPx()))

            // Theme Toggle Pill underneath
            val pillW = 90.dp.toPx()
            val pillH = 36.dp.toPx()
            val pillLeft = center.x - pillW / 2f
            val pillTop = cardTop + cardH + 14.dp.toPx()
            drawRoundRect(
                color = if (isDark) SurfaceNightCard else Color.White,
                topLeft = Offset(pillLeft, pillTop),
                size = Size(pillW, pillH),
                cornerRadius = CornerRadius(18.dp.toPx())
            )

            // Animated Toggle Thumb
            val thumbTravel = pillW - pillH
            val thumbX = pillLeft + (pillH / 2f) + (thumbTravel * togglePos)
            val thumbY = pillTop + pillH / 2f
            drawCircle(
                color = if (togglePos > 0.5f) PrimaryNightCard else PrimaryCoral,
                radius = 14.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
            drawCircle(
                color = FameGold,
                radius = 6.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )

            // Golden Trophy badge above
            val trophyY = cardTop - 32.dp.toPx()
            drawCircle(color = FameGold, radius = 16.dp.toPx(), center = Offset(center.x, trophyY))
            drawCircle(color = Color.White, radius = 12.dp.toPx(), center = Offset(center.x, trophyY))
            drawCircle(color = FameGold, radius = 9.dp.toPx(), center = Offset(center.x, trophyY))
        }
    }
}

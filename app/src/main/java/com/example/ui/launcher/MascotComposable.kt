package com.example.ui.launcher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.MascotState
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val id: Int,
    val xOffset: Float,
    val yOffset: Float,
    val color: Color,
    val size: Float,
    val isHeart: Boolean
)

@Composable
fun InteractiveMascot(
    state: MascotState,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    showArc: Boolean = true,
    progressArc: Float = 1.0f,
    onMascotPetted: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var speechBubbleText by remember { mutableStateOf<String?>(null) }
    var particles by remember { mutableStateOf<List<Particle>>(emptyList()) }
    val scaleAnim = remember { Animatable(1f) }
    val rotationAnim = remember { Animatable(0f) }
    val quotes = remember(state) {
        when (state) {
            MascotState.BURNING -> listOf(
                "I AM ON FIRE! 3+ HOURS UNSTOPPABLE!",
                "SUPERCHARGED OVERDRIVE ACTIVATED! +100 FAME!",
                "MY BRAIN IS RUNNING ON 10,000 DEGREES OF FOCUS!"
            )
            MascotState.STUDYING -> listOf(
                "Typing at 140 WPM! Keep this momentum!",
                "La la la~ Mastering this subject note by note!",
                "Steam is coming out of my ears from intense focus!",
                "Fame is pouring into our account! +2 Fame/min!",
                "Focus locked in! We don't stop now!"
            )
            MascotState.STREAK -> listOf(
                "Our streak is glowing hot!",
                "No stopping us now!",
                "Leaderboards won't know what hit them!"
            )
            MascotState.HIGH_SHAME -> listOf(
                "Shame is rising! Start the timer to cancel it!",
                "Save me! Let's conquer 25 minutes of focus!",
                "Fame cancels Shame! Let's get to work!"
            )
            else -> listOf(
                "Ready to lock in for a 4.0 GPA!",
                "Fame economy is booming!",
                "Study now, thank yourself on exam day!",
                "Petting detected! Motivation restored +100!",
                "I'm keeping your study streak alive!",
                "No procrastination on my watch!",
                "Sphere mode: I glow when you grow!"
            )
        }
    }
    fun handleMascotTap() {
        speechBubbleText = quotes.random()
        val isBurning = state == MascotState.BURNING
        val newParticles = (1..7).map { i ->
            val angle = (i * 51f + Random.nextInt(-15, 15)) * (Math.PI.toFloat() / 180f)
            val dist = Random.nextFloat() * 45f + 35f
            Particle(
                id = Random.nextInt(),
                xOffset = cos(angle) * dist,
                yOffset = sin(angle) * dist - 20f,
                color = when {
                    isBurning -> if (i % 2 == 0) Color(0xFFFF5722) else FameGold
                    i % 2 == 0 -> FameGold
                    else -> Color(0xFFFF4081)
                },
                size = Random.nextFloat() * 6f + 6f,
                isHeart = i % 2 == 0
            )
        }
        particles = newParticles
        coroutineScope.launch {
            scaleAnim.animateTo(1.25f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
            rotationAnim.animateTo(if (Random.nextBoolean()) 8f else -8f, tween(100))
            rotationAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
        }
        coroutineScope.launch {
            delay(2800)
            speechBubbleText = null
            particles = emptyList()
        }
        onMascotPetted?.invoke()
    }
    Box(
        modifier = modifier
            .wrapContentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { handleMascotTap() },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = speechBubbleText != null,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-46).dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (state == MascotState.BURNING) Color(0xFF3E120A) else Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = speechBubbleText ?: "",
                    color = if (state == MascotState.BURNING) FameGold else PrimaryNightMaroon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .scale(scaleAnim.value)
                .graphicsLayer(rotationZ = rotationAnim.value)
        ) {
            MascotComposable(
                state = state,
                size = size,
                showArc = showArc,
                progressArc = progressArc,
                isPetting = speechBubbleText != null
            )
            if (particles.isNotEmpty()) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val cx = size.toPx() / 2f
                    val cy = size.toPx() / 2f
                    particles.forEach { p ->
                        drawCircle(
                            color = p.color,
                            radius = p.size.dp.toPx(),
                            center = Offset(cx + p.xOffset.dp.toPx(), cy + p.yOffset.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MascotComposable(
    state: MascotState,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    showArc: Boolean = true,
    progressArc: Float = 1.0f,
    isPetting: Boolean = false
) {
    val equippedSkin by com.example.core.EquipManager.equippedMascot.collectAsState(initial = null)
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_anim")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )
    val typingStrokeLeft by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typingLeft"
    )
    val typingStrokeRight by infiniteTransition.animateFloat(
        initialValue = 7f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typingRight"
    )
    val musicNotePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "musicFloat"
    )
    val steamPuffPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "steamPuff"
    )
    val fireFlicker by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(160, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fireFlicker"
    )
    val flameGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameGlow"
    )
    val sphereHue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sphereHue"
    )
    val sphereBlink by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sphereBlink"
    )
    val sphereTwinkle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sphereTwinkle"
    )
    var studyMoodCycle by remember { mutableIntStateOf(0) }
    var sphereMood by remember { mutableIntStateOf(0) }
    LaunchedEffect(state) {
        if (state == MascotState.STUDYING) {
            while (true) {
                delay(4500)
                studyMoodCycle = (studyMoodCycle + 1) % 3
            }
        }
    }
    LaunchedEffect(equippedSkin) {
        if (equippedSkin == "item_nyc_sphere") {
            while (true) {
                delay(3800)
                sphereMood = (sphereMood + 1) % 5
            }
        }
    }
    val isStudying = state == MascotState.STUDYING
    val isBurning = state == MascotState.BURNING
    val isFrustrated = (isStudying && studyMoodCycle == 2) || state == MascotState.FRUSTRATED
    val isSinging = (isStudying && studyMoodCycle == 1) || state == MascotState.SINGING
    val isSphere = equippedSkin == "item_nyc_sphere"
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            if (showArc) {
                val arcRadius = w * 0.45f
                val arcRect = Rect(
                    cx - arcRadius,
                    cy - arcRadius - (h * 0.05f),
                    cx + arcRadius,
                    cy + arcRadius - (h * 0.05f)
                )
                drawArc(
                    color = Color.White.copy(alpha = 0.25f),
                    startAngle = 160f,
                    sweepAngle = 220f,
                    useCenter = false,
                    topLeft = arcRect.topLeft,
                    size = arcRect.size,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                if (progressArc > 0f) {
                    drawArc(
                        color = when {
                            isBurning -> Color(0xFFFF5722)
                            state == MascotState.STREAK -> FameGold
                            else -> Color.White
                        },
                        startAngle = 160f,
                        sweepAngle = 220f * progressArc,
                        useCenter = false,
                        topLeft = arcRect.topLeft,
                        size = arcRect.size,
                        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            if (isBurning) {
                drawBurningFireAura(cx, cy, w, h, fireFlicker, flameGlow)
            } else if (state == MascotState.STREAK || isPetting) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(FameGold.copy(alpha = flameGlow * 0.7f), Color.Transparent),
                        center = Offset(cx, cy - (h * 0.04f)),
                        radius = w * 0.48f
                    ),
                    center = Offset(cx, cy - (h * 0.04f)),
                    radius = w * 0.48f
                )
            }
            if (isSinging && !isBurning) {
                drawFloatingMusicalNotes(cx, cy, w, h, musicNotePhase)
            }
            if (isFrustrated && !isBurning) {
                drawFrustrationSteamPuffs(cx, cy, w, h, steamPuffPhase)
            }
            val currentScale = if (isStudying || isBurning) breathScale else 1f
            val headRadiusX = (w * 0.23f) * currentScale
            val headRadiusY = (h * 0.21f) * currentScale
            val headCenter = Offset(cx, cy - (h * 0.04f))
            drawOval(
                color = Color.Black.copy(alpha = 0.18f),
                topLeft = Offset(headCenter.x - headRadiusX, headCenter.y - headRadiusY + 3.dp.toPx()),
                size = Size(headRadiusX * 2, headRadiusY * 2)
            )
            if (isSphere) {
                drawSphereBuddy(headCenter, headRadiusX * 1.32f, headRadiusY * 1.32f, sphereHue, flameGlow)
                drawNycSkyline(cx, cy, w, h, sphereTwinkle)
                drawSphereFace(headCenter, headRadiusX * 1.32f, sphereMood, sphereBlink > 0.93f, state, isPetting)
            } else {
                val headColor = when {
                    isBurning -> Color(0xFFFF5252)
                    state == MascotState.HIGH_SHAME -> Color(0xFFFFEBEE)
                    else -> Color(0xFFFFF7F6)
                }
                drawOval(
                    color = headColor,
                    topLeft = Offset(headCenter.x - headRadiusX, headCenter.y - headRadiusY),
                    size = Size(headRadiusX * 2, headRadiusY * 2)
                )
                drawOval(
                    color = if (isBurning) Color(0xFFB71C1C) else PrimaryCoralDark.copy(alpha = 0.35f),
                    topLeft = Offset(headCenter.x - headRadiusX, headCenter.y - headRadiusY),
                    size = Size(headRadiusX * 2, headRadiusY * 2),
                    style = Stroke(width = 1.8.dp.toPx())
                )
                val blushColor = if (isBurning) FameGold else PrimaryCoral
                drawCircle(
                    color = blushColor.copy(alpha = if (isPetting || isBurning) 0.7f else 0.35f),
                    radius = 4.5.dp.toPx(),
                    center = Offset(headCenter.x - 14.dp.toPx(), headCenter.y + 7.dp.toPx())
                )
                drawCircle(
                    color = blushColor.copy(alpha = if (isPetting || isBurning) 0.7f else 0.35f),
                    radius = 4.5.dp.toPx(),
                    center = Offset(headCenter.x + 14.dp.toPx(), headCenter.y + 7.dp.toPx())
                )
                val leafPath = Path().apply {
                    moveTo(headCenter.x, headCenter.y - headRadiusY)
                    quadraticTo(headCenter.x + 8.dp.toPx(), headCenter.y - headRadiusY - 11.dp.toPx(), headCenter.x + 15.dp.toPx(), headCenter.y - headRadiusY - 9.dp.toPx())
                    quadraticTo(headCenter.x + 5.dp.toPx(), headCenter.y - headRadiusY - 2.dp.toPx(), headCenter.x, headCenter.y - headRadiusY)
                    close()
                }
                drawPath(
                    path = leafPath,
                    color = when {
                        isBurning -> Color(0xFFFF9800)
                        state == MascotState.HIGH_SHAME -> WarningRed
                        else -> SuccessGreen
                    }
                )
                drawFacialExpressions(
                    state = state,
                    isPetting = isPetting,
                    isBurning = isBurning,
                    isSinging = isSinging,
                    isFrustrated = isFrustrated,
                    headCenter = headCenter,
                    headRadiusX = headRadiusX,
                    headRadiusY = headRadiusY,
                    cx = cx
                )
            }
            drawModernLaptopAndPaws(
                cx = cx,
                cy = cy,
                w = w,
                h = h,
                isStudying = isStudying || isBurning,
                isBurning = isBurning,
                typingLeft = typingStrokeLeft,
                typingRight = typingStrokeRight,
                headCenter = headCenter
            )
            drawEquippedSkin(equippedSkin, cx, cy, w, h, headCenter)
        }
    }
}

private fun DrawScope.drawSphereBuddy(
    headCenter: Offset,
    rx: Float,
    ry: Float,
    huePhase: Float,
    glow: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFC400).copy(alpha = 0.40f * glow), Color.Transparent),
            center = headCenter,
            radius = rx * 1.9f
        ),
        center = headCenter,
        radius = rx * 1.9f
    )
    val body = when {
        huePhase < 0.33f -> lerp(Color(0xFFFFD54F), Color(0xFFFFB300), huePhase / 0.33f)
        huePhase < 0.66f -> lerp(Color(0xFFFFB300), Color(0xFFCDDC39), (huePhase - 0.33f) / 0.33f)
        else -> lerp(Color(0xFFCDDC39), Color(0xFFFFD54F), (huePhase - 0.66f) / 0.34f)
    }
    val bodyDark = lerp(body, Color(0xFF8D6E63), 0.35f)
    val rect = Rect(headCenter.x - rx, headCenter.y - ry, headCenter.x + rx, headCenter.y + ry)
    drawOval(
        brush = Brush.verticalGradient(listOf(body, bodyDark), startY = rect.top, endY = rect.bottom),
        topLeft = rect.topLeft,
        size = rect.size
    )
    val spherePath = Path().apply { addOval(rect) }
    clipPath(spherePath) {
        val step = 6.dp.toPx()
        var row = 0
        var y = rect.top + step / 2f
        while (y < rect.bottom) {
            var x = rect.left + (if (row % 2 == 0) 0f else step / 2f)
            while (x < rect.right) {
                drawCircle(Color(0xFF3E2723).copy(alpha = 0.10f), 1.1.dp.toPx(), Offset(x, y))
                x += step
            }
            y += step * 0.9f
            row++
        }
        drawOval(
            color = Color.White.copy(alpha = 0.22f),
            topLeft = Offset(rect.left + rx * 0.35f, rect.top + ry * 0.16f),
            size = Size(rx * 0.75f, ry * 0.42f)
        )
    }
    drawOval(
        color = Color(0xFF5D4037).copy(alpha = 0.45f),
        topLeft = rect.topLeft,
        size = rect.size,
        style = Stroke(width = 1.5.dp.toPx())
    )
}

private fun DrawScope.drawNycSkyline(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    twinkle: Float
) {
    val horizon = cy + h * 0.26f
    for (i in 0 until 8) {
        val sx = w * (((i * 0.117f) + 0.04f) % 1f)
        val sy = h * 0.08f + (i % 4) * h * 0.03f
        val a = 0.3f + 0.5f * kotlin.math.abs(sin(twinkle * 2f * Math.PI.toFloat() + i * 1.7f))
        drawCircle(Color.White.copy(alpha = a), 1.1.dp.toPx(), Offset(sx, sy))
    }
    val buildings = listOf(
        floatArrayOf(0.00f, 0.14f, 0.26f),
        floatArrayOf(0.16f, 0.12f, 0.38f),
        floatArrayOf(0.30f, 0.16f, 0.30f),
        floatArrayOf(0.48f, 0.13f, 0.44f),
        floatArrayOf(0.63f, 0.15f, 0.32f),
        floatArrayOf(0.80f, 0.12f, 0.26f),
        floatArrayOf(0.93f, 0.08f, 0.20f)
    )
    buildings.forEachIndexed { bi, b ->
        val bx = w * b[0]
        val bw = w * b[1]
        val bh = h * b[2]
        val top = horizon - bh
        drawRoundRect(
            Color(0xFF1A2332),
            Offset(bx, top),
            Size(bw, bh),
            androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )
        if (bi == 3) {
            drawLine(Color(0xFF1A2332), Offset(bx + bw / 2f, top), Offset(bx + bw / 2f, top - h * 0.07f), 2.dp.toPx())
            drawCircle(FameGold.copy(alpha = 0.5f + 0.5f * twinkle), 1.6.dp.toPx(), Offset(bx + bw / 2f, top - h * 0.07f))
        }
        val cols = 3
        val rows = (bh / (9.dp.toPx())).toInt().coerceAtMost(5)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val on = sin(twinkle * 2f * Math.PI.toFloat() + (bi * 7 + r * 3 + c) * 1.3f) > -0.1f
                if (on) {
                    drawRoundRect(
                        Color(0xFFFFE082).copy(alpha = 0.75f),
                        Offset(bx + bw * (0.16f + c * 0.28f), top + 3.dp.toPx() + r * 8.dp.toPx()),
                        Size(bw * 0.16f, 3.5.dp.toPx()),
                        androidx.compose.ui.geometry.CornerRadius(1.dp.toPx())
                    )
                }
            }
        }
    }
    drawLine(Color.White.copy(alpha = 0.2f), Offset(0f, horizon + 4.dp.toPx()), Offset(w, horizon + 4.dp.toPx()), 1.2.dp.toPx())
}

private fun DrawScope.drawSphereFace(
    headCenter: Offset,
    rx: Float,
    mood: Int,
    blinking: Boolean,
    state: MascotState,
    isPetting: Boolean
) {
    val ink = Color(0xFF26221B)
    val eyeY = headCenter.y - rx * 0.10f
    val eyeDX = rx * 0.34f
    val leftX = headCenter.x - eyeDX
    val rightX = headCenter.x + eyeDX
    val lw = rx * 0.085f
    val effective = when {
        isPetting -> 3
        state == MascotState.BURNING -> 4
        state == MascotState.HIGH_SHAME -> 2
        state == MascotState.STUDYING -> 1
        else -> mood
    }
    fun lineEye(ex: Float) {
        drawLine(ink, Offset(ex - rx * 0.14f, eyeY), Offset(ex + rx * 0.14f, eyeY), lw, cap = StrokeCap.Round)
    }
    fun roundEye(ex: Float, scale: Float, pupilDy: Float) {
        drawCircle(Color.White.copy(alpha = 0.92f), rx * 0.13f * scale, Offset(ex, eyeY))
        drawCircle(ink, rx * 0.07f * scale, Offset(ex, eyeY + pupilDy))
    }
    fun happyEye(ex: Float) {
        drawArc(
            color = ink,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(ex - rx * 0.14f, eyeY - rx * 0.04f),
            size = Size(rx * 0.28f, rx * 0.22f),
            style = Stroke(width = lw, cap = StrokeCap.Round)
        )
    }
    when (effective) {
        0 -> {
            if (blinking) { lineEye(leftX); lineEye(rightX) } else { lineEye(leftX); lineEye(rightX) }
        }
        1 -> {
            if (blinking) { lineEye(leftX); lineEye(rightX) } else {
                roundEye(leftX, 1f, rx * 0.04f)
                roundEye(rightX, 1f, rx * 0.04f)
            }
        }
        2 -> {
            drawLine(ink, Offset(leftX - rx * 0.14f, eyeY - rx * 0.07f), Offset(leftX + rx * 0.14f, eyeY + rx * 0.03f), lw, cap = StrokeCap.Round)
            drawLine(ink, Offset(rightX + rx * 0.14f, eyeY - rx * 0.07f), Offset(rightX - rx * 0.14f, eyeY + rx * 0.03f), lw, cap = StrokeCap.Round)
        }
        3 -> {
            happyEye(leftX)
            happyEye(rightX)
        }
        else -> {
            if (blinking) { lineEye(leftX); lineEye(rightX) } else {
                roundEye(leftX, 1.25f, 0f)
                roundEye(rightX, 1.25f, 0f)
            }
        }
    }
    val mouthY = headCenter.y + rx * 0.24f
    when {
        effective == 3 -> {
            drawArc(
                color = ink,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(headCenter.x - rx * 0.18f, mouthY - rx * 0.08f),
                size = Size(rx * 0.36f, rx * 0.22f)
            )
        }
        effective == 2 -> {
            drawArc(
                color = ink,
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(headCenter.x - rx * 0.14f, mouthY - rx * 0.06f),
                size = Size(rx * 0.28f, rx * 0.16f),
                style = Stroke(width = lw * 0.8f, cap = StrokeCap.Round)
            )
        }
        effective == 0 || effective == 4 -> {
            drawCircle(ink, rx * 0.055f, Offset(headCenter.x, mouthY))
        }
        else -> {
            drawLine(ink, Offset(headCenter.x - rx * 0.10f, mouthY), Offset(headCenter.x + rx * 0.10f, mouthY), lw * 0.8f, cap = StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawModernLaptopAndPaws(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    isStudying: Boolean,
    isBurning: Boolean,
    typingLeft: Float,
    typingRight: Float,
    headCenter: Offset
) {
    val deskY = cy + (h * 0.20f)
    drawRoundRect(
        color = if (isBurning) Color(0xFF5D1D16) else Color(0xFF4A2B2B).copy(alpha = 0.5f),
        topLeft = Offset(cx - (w * 0.38f), deskY),
        size = Size(w * 0.76f, h * 0.16f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx())
    )
    drawLine(
        color = Color.White.copy(alpha = 0.2f),
        start = Offset(cx - (w * 0.36f), deskY + 1.dp.toPx()),
        end = Offset(cx + (w * 0.36f), deskY + 1.dp.toPx()),
        strokeWidth = 1.5.dp.toPx()
    )
    val laptopW = w * 0.36f
    val laptopH = h * 0.19f
    val laptopLeft = cx - (laptopW / 2f)
    val laptopTop = deskY - (laptopH * 0.78f)
    drawRoundRect(
        color = if (isBurning) Color(0xFF330A0A) else Color(0xFF1E1E24),
        topLeft = Offset(laptopLeft, laptopTop),
        size = Size(laptopW, laptopH * 0.82f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx(), 5.dp.toPx())
    )
    val screenPadding = 2.5.dp.toPx()
    val screenLeft = laptopLeft + screenPadding
    val screenTop = laptopTop + screenPadding
    val screenW = laptopW - (screenPadding * 2)
    val screenH = (laptopH * 0.82f) - (screenPadding * 2)
    drawRoundRect(
        color = if (isBurning) Color(0xFF4E1608) else Color(0xFF0F172A),
        topLeft = Offset(screenLeft, screenTop),
        size = Size(screenW, screenH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    if (isStudying || isBurning) {
        val lineY1 = screenTop + 4.dp.toPx()
        val lineY2 = screenTop + 8.dp.toPx()
        val lineY3 = screenTop + 12.dp.toPx()
        val lineY4 = screenTop + 16.dp.toPx()
        drawLine(color = SuccessGreen, start = Offset(screenLeft + 3.dp.toPx(), lineY1), end = Offset(screenLeft + 12.dp.toPx(), lineY1), strokeWidth = 1.5.dp.toPx())
        drawLine(color = AccentCyan, start = Offset(screenLeft + 14.dp.toPx(), lineY1), end = Offset(screenLeft + 26.dp.toPx(), lineY1), strokeWidth = 1.5.dp.toPx())
        drawLine(color = FameGold, start = Offset(screenLeft + 5.dp.toPx(), lineY2), end = Offset(screenLeft + 20.dp.toPx(), lineY2), strokeWidth = 1.5.dp.toPx())
        drawLine(color = PrimaryCoral, start = Offset(screenLeft + 22.dp.toPx(), lineY2), end = Offset(screenLeft + 32.dp.toPx(), lineY2), strokeWidth = 1.5.dp.toPx())
        drawLine(color = AccentCyan, start = Offset(screenLeft + 5.dp.toPx(), lineY3), end = Offset(screenLeft + 18.dp.toPx(), lineY3), strokeWidth = 1.5.dp.toPx())
        drawLine(color = SuccessGreen, start = Offset(screenLeft + 3.dp.toPx(), lineY4), end = Offset(screenLeft + 8.dp.toPx(), lineY4), strokeWidth = 1.5.dp.toPx())
    } else {
        drawCircle(
            color = PrimaryCoral,
            radius = 3.5.dp.toPx(),
            center = Offset(screenLeft + (screenW / 2f), screenTop + (screenH / 2f))
        )
    }
    val baseLeft = cx - (laptopW * 0.62f)
    val baseTop = laptopTop + (laptopH * 0.78f)
    val baseW = laptopW * 1.24f
    val baseH = h * 0.055f
    drawRoundRect(
        color = if (isBurning) Color(0xFF8B2516) else Color(0xFFD5D8DC),
        topLeft = Offset(baseLeft, baseTop),
        size = Size(baseW, baseH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    val keyBedLeft = baseLeft + 3.dp.toPx()
    val keyBedTop = baseTop + 1.5.dp.toPx()
    val keyBedW = baseW - 6.dp.toPx()
    val keyBedH = baseH * 0.58f
    drawRoundRect(
        color = if (isBurning) Color(0xFF3E120A) else Color(0xFF2C3E50),
        topLeft = Offset(keyBedLeft, keyBedTop),
        size = Size(keyBedW, keyBedH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawLine(color = Color.White.copy(alpha = 0.4f), start = Offset(keyBedLeft + 4.dp.toPx(), keyBedTop + 2.dp.toPx()), end = Offset(keyBedLeft + keyBedW - 4.dp.toPx(), keyBedTop + 2.dp.toPx()), strokeWidth = 1.dp.toPx())
    drawLine(color = Color.White.copy(alpha = 0.4f), start = Offset(keyBedLeft + 4.dp.toPx(), keyBedTop + 5.dp.toPx()), end = Offset(keyBedLeft + keyBedW - 4.dp.toPx(), keyBedTop + 5.dp.toPx()), strokeWidth = 1.dp.toPx())
    val padW = 10.dp.toPx()
    val padH = 3.dp.toPx()
    drawRoundRect(
        color = if (isBurning) FameGold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f),
        topLeft = Offset(cx - (padW / 2f), baseTop + keyBedH + 1.5.dp.toPx()),
        size = Size(padW, padH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
    )
    val pawRadius = 5.5.dp.toPx()
    val pawLeftX = keyBedLeft + 8.dp.toPx()
    val pawLeftY = keyBedTop + 4.dp.toPx() + (if (isStudying) typingLeft.dp.toPx() else 0f)
    val pawRightX = keyBedLeft + keyBedW - 8.dp.toPx()
    val pawRightY = keyBedTop + 4.dp.toPx() + (if (isStudying) typingRight.dp.toPx() else 0f)
    val pawColor = if (isBurning) Color(0xFFFFCC80) else Color(0xFFFFF7F6)
    val pawBorderColor = if (isBurning) Color(0xFFB71C1C) else PrimaryCoralDark
    drawCircle(color = pawColor, radius = pawRadius, center = Offset(pawLeftX, pawLeftY))
    drawCircle(color = pawBorderColor, radius = pawRadius, center = Offset(pawLeftX, pawLeftY), style = Stroke(width = 1.5.dp.toPx()))
    drawCircle(color = pawColor, radius = pawRadius, center = Offset(pawRightX, pawRightY))
    drawCircle(color = pawBorderColor, radius = pawRadius, center = Offset(pawRightX, pawRightY), style = Stroke(width = 1.5.dp.toPx()))
    if (isStudying) {
        val activePawHit = if (typingLeft > 0) Offset(pawLeftX, pawLeftY + 2.dp.toPx()) else Offset(pawRightX, pawRightY + 2.dp.toPx())
        drawCircle(
            color = if (isBurning) FameGold else AccentCyan.copy(alpha = 0.75f),
            radius = 3.dp.toPx(),
            center = activePawHit
        )
    }
}

private fun DrawScope.drawFacialExpressions(
    state: MascotState,
    isPetting: Boolean,
    isBurning: Boolean,
    isSinging: Boolean,
    isFrustrated: Boolean,
    headCenter: Offset,
    headRadiusX: Float,
    headRadiusY: Float,
    cx: Float
) {
    if (isBurning) {
        val sunglassW = 12.dp.toPx()
        val sunglassH = 8.dp.toPx()
        drawRoundRect(
            color = Color(0xFF1E1E24),
            topLeft = Offset(headCenter.x - 14.dp.toPx(), headCenter.y - 4.dp.toPx()),
            size = Size(sunglassW, sunglassH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        drawLine(
            color = FameGold,
            start = Offset(headCenter.x - 12.dp.toPx(), headCenter.y - 2.dp.toPx()),
            end = Offset(headCenter.x - 4.dp.toPx(), headCenter.y + 2.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
        drawRoundRect(
            color = Color(0xFF1E1E24),
            topLeft = Offset(headCenter.x + 2.dp.toPx(), headCenter.y - 4.dp.toPx()),
            size = Size(sunglassW, sunglassH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        drawLine(
            color = FameGold,
            start = Offset(headCenter.x + 4.dp.toPx(), headCenter.y - 2.dp.toPx()),
            end = Offset(headCenter.x + 12.dp.toPx(), headCenter.y + 2.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color(0xFF1E1E24),
            start = Offset(headCenter.x - 2.dp.toPx(), headCenter.y - 1.dp.toPx()),
            end = Offset(headCenter.x + 2.dp.toPx(), headCenter.y - 1.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
        val grin = Path().apply {
            moveTo(headCenter.x - 7.dp.toPx(), headCenter.y + 8.dp.toPx())
            quadraticTo(headCenter.x, headCenter.y + 14.dp.toPx(), headCenter.x + 7.dp.toPx(), headCenter.y + 8.dp.toPx())
            close()
        }
        drawPath(grin, color = FameGold)
    } else if (isPetting) {
        drawArc(
            color = PrimaryCoralDark,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(headCenter.x - 14.dp.toPx(), headCenter.y - 2.dp.toPx()),
            size = Size(8.dp.toPx(), 7.dp.toPx()),
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
            color = PrimaryCoralDark,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(headCenter.x + 6.dp.toPx(), headCenter.y - 2.dp.toPx()),
            size = Size(8.dp.toPx(), 7.dp.toPx()),
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        )
        val happyMouth = Path().apply {
            moveTo(headCenter.x - 6.dp.toPx(), headCenter.y + 7.dp.toPx())
            quadraticTo(headCenter.x, headCenter.y + 16.dp.toPx(), headCenter.x + 6.dp.toPx(), headCenter.y + 7.dp.toPx())
            close()
        }
        drawPath(happyMouth, color = PrimaryCoral)
        drawCrown(headCenter.x, headCenter.y - headRadiusY)
    } else if (isSinging) {
        drawArc(
            color = PrimaryCoralDark,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(headCenter.x - 13.dp.toPx(), headCenter.y - 2.dp.toPx()),
            size = Size(8.dp.toPx(), 6.dp.toPx()),
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        )
        drawCircle(color = PrimaryCoralDark, radius = 3.dp.toPx(), center = Offset(headCenter.x + 10.dp.toPx(), headCenter.y + 1.dp.toPx()))
        drawOval(
            color = PrimaryCoral,
            topLeft = Offset(headCenter.x - 3.5.dp.toPx(), headCenter.y + 7.dp.toPx()),
            size = Size(7.dp.toPx(), 8.dp.toPx())
        )
    } else if (isFrustrated) {
        drawLine(color = PrimaryCoralDark, start = Offset(headCenter.x - 13.dp.toPx(), headCenter.y - 1.dp.toPx()), end = Offset(headCenter.x - 7.dp.toPx(), headCenter.y + 2.dp.toPx()), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = PrimaryCoralDark, start = Offset(headCenter.x - 13.dp.toPx(), headCenter.y + 5.dp.toPx()), end = Offset(headCenter.x - 7.dp.toPx(), headCenter.y + 2.dp.toPx()), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = PrimaryCoralDark, start = Offset(headCenter.x + 13.dp.toPx(), headCenter.y - 1.dp.toPx()), end = Offset(headCenter.x + 7.dp.toPx(), headCenter.y + 2.dp.toPx()), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = PrimaryCoralDark, start = Offset(headCenter.x + 13.dp.toPx(), headCenter.y + 5.dp.toPx()), end = Offset(headCenter.x + 7.dp.toPx(), headCenter.y + 2.dp.toPx()), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
        val clenchedMouth = Path().apply {
            moveTo(headCenter.x - 6.dp.toPx(), headCenter.y + 10.dp.toPx())
            lineTo(headCenter.x + 6.dp.toPx(), headCenter.y + 10.dp.toPx())
        }
        drawPath(clenchedMouth, color = PrimaryCoralDark, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
        val sweatDrop = Path().apply {
            moveTo(headCenter.x + headRadiusX + 2.dp.toPx(), headCenter.y - 2.dp.toPx())
            lineTo(headCenter.x + headRadiusX + 5.dp.toPx(), headCenter.y + 5.dp.toPx())
            lineTo(headCenter.x + headRadiusX - 1.dp.toPx(), headCenter.y + 5.dp.toPx())
            close()
        }
        drawPath(sweatDrop, color = AccentCyan)
    } else {
        drawCircle(color = PrimaryCoralDark, radius = 3.dp.toPx(), center = Offset(headCenter.x - 10.dp.toPx(), headCenter.y + 1.dp.toPx()))
        drawCircle(color = PrimaryCoralDark, radius = 3.dp.toPx(), center = Offset(headCenter.x + 10.dp.toPx(), headCenter.y + 1.dp.toPx()))
        val mouthPath = Path().apply {
            moveTo(headCenter.x - 5.dp.toPx(), headCenter.y + 9.dp.toPx())
            quadraticTo(headCenter.x, headCenter.y + 13.dp.toPx(), headCenter.x + 5.dp.toPx(), headCenter.y + 9.dp.toPx())
        }
        drawPath(mouthPath, color = PrimaryCoralDark, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawBurningFireAura(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    fireFlicker: Float,
    flameGlow: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFF5722).copy(alpha = 0.55f * flameGlow), Color.Transparent),
            center = Offset(cx, cy - (h * 0.04f)),
            radius = w * 0.5f
        ),
        center = Offset(cx, cy - (h * 0.04f)),
        radius = w * 0.5f
    )
    val flameOffsets = listOf(-0.30f, -0.16f, 0f, 0.16f, 0.30f)
    flameOffsets.forEachIndexed { i, off ->
        val flameH = (h * 0.16f + (i % 2) * 4.dp.toPx()) * fireFlicker
        val flameW = 9.dp.toPx()
        val baseX = cx + (w * off)
        val baseY = cy + (h * 0.16f)
        val flamePath = Path().apply {
            moveTo(baseX - flameW / 2f, baseY)
            quadraticTo(baseX - flameW / 2f, baseY - flameH * 0.55f, baseX, baseY - flameH)
            quadraticTo(baseX + flameW / 2f, baseY - flameH * 0.55f, baseX + flameW / 2f, baseY)
            close()
        }
        drawPath(
            path = flamePath,
            brush = Brush.verticalGradient(
                colors = listOf(FameGold, Color(0xFFFF5722)),
                startY = baseY - flameH,
                endY = baseY
            )
        )
    }
}

private fun DrawScope.drawFloatingMusicalNotes(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    musicNotePhase: Float
) {
    val noteConfigs = listOf(
        Triple(-0.34f, 0f, Color(0xFF20B2AA)),
        Triple(0.30f, 0.45f, FameGold),
        Triple(-0.18f, 0.75f, Color(0xFFFF4081))
    )
    noteConfigs.forEach { cfg ->
        val xOff = cfg.first
        val phaseOff = cfg.second
        val noteColor = cfg.third
        val p = (musicNotePhase + phaseOff) % 1f
        val noteX = cx + (w * xOff) + sin(p * 2f * Math.PI.toFloat()) * 3.dp.toPx()
        val noteY = (cy - (h * 0.18f)) - (p * (h * 0.30f))
        val alpha = 1f - p
        drawCircle(
            color = noteColor.copy(alpha = alpha),
            radius = 3.dp.toPx(),
            center = Offset(noteX, noteY)
        )
        drawLine(
            color = noteColor.copy(alpha = alpha),
            start = Offset(noteX + 3.dp.toPx(), noteY),
            end = Offset(noteX + 3.dp.toPx(), noteY - 10.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = noteColor.copy(alpha = alpha),
            start = Offset(noteX + 3.dp.toPx(), noteY - 10.dp.toPx()),
            end = Offset(noteX + 6.dp.toPx(), noteY - 8.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

private fun DrawScope.drawFrustrationSteamPuffs(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    steamPuffPhase: Float
) {
    val puffConfigs = listOf(
        Pair(-0.26f, 0f),
        Pair(0.26f, 0.5f)
    )
    puffConfigs.forEach { cfg ->
        val xOff = cfg.first
        val phaseOff = cfg.second
        val p = (steamPuffPhase + phaseOff) % 1f
        val puffX = cx + (w * xOff)
        val puffY = (cy - (h * 0.22f)) - (p * (h * 0.18f))
        val alpha = (1f - p) * 0.8f
        val radius = 3.dp.toPx() + (p * 3.dp.toPx())
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = radius,
            center = Offset(puffX, puffY)
        )
    }
    drawLine(
        color = PrimaryCoralDark,
        start = Offset(cx - 16.dp.toPx(), cy - (h * 0.16f)),
        end = Offset(cx - 10.dp.toPx(), cy - (h * 0.14f)),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = PrimaryCoralDark,
        start = Offset(cx + 16.dp.toPx(), cy - (h * 0.16f)),
        end = Offset(cx + 10.dp.toPx(), cy - (h * 0.14f)),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawCrown(x: Float, y: Float) {
    val crownPath = Path().apply {
        moveTo(x - 10.dp.toPx(), y - 2.dp.toPx())
        lineTo(x - 10.dp.toPx(), y - 12.dp.toPx())
        lineTo(x - 5.dp.toPx(), y - 7.dp.toPx())
        lineTo(x, y - 14.dp.toPx())
        lineTo(x + 5.dp.toPx(), y - 7.dp.toPx())
        lineTo(x + 10.dp.toPx(), y - 12.dp.toPx())
        lineTo(x + 10.dp.toPx(), y - 2.dp.toPx())
        close()
    }
    drawPath(crownPath, color = FameGold)
}

private fun DrawScope.drawEquippedSkin(
    skin: String?,
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    headCenter: Offset
) {
    when (skin) {
        "item_cyberpunk" -> {
            drawRoundRect(
                color = Color(0xFF00E5FF),
                topLeft = Offset(headCenter.x - 16.dp.toPx(), headCenter.y - 6.dp.toPx()),
                size = Size(32.dp.toPx(), 9.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
            drawLine(
                color = Color(0xFF00E5FF),
                start = Offset(headCenter.x + 12.dp.toPx(), headCenter.y - 6.dp.toPx()),
                end = Offset(headCenter.x + 18.dp.toPx(), headCenter.y - 16.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(
                color = Color(0xFFFF4081),
                radius = 2.5.dp.toPx(),
                center = Offset(headCenter.x + 18.dp.toPx(), headCenter.y - 17.dp.toPx())
            )
        }
        "item_night_owl_skin" -> {
            val capPath = Path().apply {
                moveTo(headCenter.x - 14.dp.toPx(), headCenter.y - 14.dp.toPx())
                quadraticTo(headCenter.x, headCenter.y - 30.dp.toPx(), headCenter.x + 16.dp.toPx(), headCenter.y - 18.dp.toPx())
                lineTo(headCenter.x + 20.dp.toPx(), headCenter.y - 10.dp.toPx())
                close()
            }
            drawPath(capPath, color = Color(0xFF3F51B5))
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = Offset(headCenter.x + 20.dp.toPx(), headCenter.y - 9.dp.toPx())
            )
        }
        "item_golden_desk" -> {
            drawLine(
                color = FameGold,
                start = Offset(cx - (w * 0.38f), cy + (h * 0.20f)),
                end = Offset(cx + (w * 0.38f), cy + (h * 0.20f)),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

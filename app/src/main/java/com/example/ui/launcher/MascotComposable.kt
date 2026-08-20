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

    // Tap scale animation
    val scaleAnim = remember { Animatable(1f) }
    val rotationAnim = remember { Animatable(0f) }

    val quotes = remember(state) {
        when (state) {
            MascotState.BURNING -> listOf(
                "I AM ON FIRE! 3+ HOURS UNSTOPPABLE! 🔥",
                "SUPERCHARGED OVERDRIVE ACTIVATED! +100 FAME! 🔥",
                "MY BRAIN IS RUNNING ON 10,000 DEGREES OF FOCUS! ⚡"
            )
            MascotState.STUDYING -> listOf(
                "Typing at 140 WPM! Keep this momentum! 💻",
                "La la la~ Mastering this subject note by note! 🎵",
                "Steam is coming out of my ears from intense focus! 💨",
                "Fame is pouring into our account! +2 Fame/min! 📈",
                "Focus locked in! We don't stop now! 🎯"
            )
            MascotState.STREAK -> listOf(
                "Our streak is glowing hot! 🔥",
                "No stopping us now! 🏆",
                "Leaderboards won't know what hit them! 📈"
            )
            MascotState.HIGH_SHAME -> listOf(
                "Shame is rising! Start the timer to cancel it! ⏳",
                "Save me! Let's conquer 25 minutes of focus! 🍅",
                "Fame cancels Shame! Let's get to work! 💪"
            )
            else -> listOf(
                "Ready to lock in for a 4.0 GPA! 🎯",
                "Fame economy is booming! 📈",
                "Study now, thank yourself on exam day! 💡",
                "Petting detected! Motivation restored +100! ✨",
                "I'm keeping your study streak alive! 🔥",
                "No procrastination on my watch! 🍅"
            )
        }
    }

    fun handleMascotTap() {
        speechBubbleText = quotes.random()

        // Generate burst particles (stars, hearts, flames)
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
        // Floating Speech Bubble
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

        // Animated Mascot Core
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

            // Canvas for floating burst particles
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

    // Breathing scale
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Rigorous Typing Rapid Alternating Hands (Fast stroke animation)
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

    // Singing / Musical Notes vertical wave float (0 to 1)
    val musicNotePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "musicFloat"
    )

    // Frustration steam puff expansion (0 to 1)
    val steamPuffPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "steamPuff"
    )

    // Burning Fire tongues flicker (0 to 1)
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

    // Cyclic study behavior mood timer (Rigorous typing -> Singing -> Frustration)
    var studyMoodCycle by remember { mutableIntStateOf(0) }
    LaunchedEffect(state) {
        if (state == MascotState.STUDYING) {
            while (true) {
                delay(4500)
                studyMoodCycle = (studyMoodCycle + 1) % 3
            }
        }
    }

    val isStudying = state == MascotState.STUDYING
    val isBurning = state == MascotState.BURNING
    val isFrustrated = (isStudying && studyMoodCycle == 2) || state == MascotState.FRUSTRATED
    val isSinging = (isStudying && studyMoodCycle == 1) || state == MascotState.SINGING
    val isRigorousTyping = (isStudying && studyMoodCycle == 0) || isBurning

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            // 1. Progress Arc Ring
            if (showArc) {
                val arcRadius = w * 0.45f
                val arcRect = androidx.compose.ui.geometry.Rect(
                    cx - arcRadius,
                    cy - arcRadius - (h * 0.05f),
                    cx + arcRadius,
                    cy + arcRadius - (h * 0.05f)
                )

                // Background track
                drawArc(
                    color = Color.White.copy(alpha = 0.25f),
                    startAngle = 160f,
                    sweepAngle = 220f,
                    useCenter = false,
                    topLeft = arcRect.topLeft,
                    size = arcRect.size,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Active foreground arc
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

            // 2. BURNING FIRE AURA (If >= 3 hours continuous study)
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

            // 3. SINGING FLOATING MUSICAL NOTES (♪ ♫ ♬)
            if (isSinging && !isBurning) {
                drawFloatingMusicalNotes(cx, cy, w, h, musicNotePhase)
            }

            // 4. FRUSTRATION STEAM PUFFS (💨)
            if (isFrustrated && !isBurning) {
                drawFrustrationSteamPuffs(cx, cy, w, h, steamPuffPhase)
            }

            // 5. Tomato Head Body
            val currentScale = if (isStudying || isBurning) breathScale else 1f
            val headRadiusX = (w * 0.23f) * currentScale
            val headRadiusY = (h * 0.21f) * currentScale
            val headCenter = Offset(cx, cy - (h * 0.04f))

            // Body shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.18f),
                topLeft = Offset(headCenter.x - headRadiusX, headCenter.y - headRadiusY + 3.dp.toPx()),
                size = Size(headRadiusX * 2, headRadiusY * 2)
            )

            // Tomato Head Base (Fiery Orange/Red when Burning, Peach/Cream otherwise)
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

            // Head Outline
            drawOval(
                color = if (isBurning) Color(0xFFB71C1C) else PrimaryCoralDark.copy(alpha = 0.35f),
                topLeft = Offset(headCenter.x - headRadiusX, headCenter.y - headRadiusY),
                size = Size(headRadiusX * 2, headRadiusY * 2),
                style = Stroke(width = 1.8.dp.toPx())
            )

            // Blush Cheeks
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

            // Green Stem Top
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

            // 6. DYNAMIC FACIAL EXPRESSIONS
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

            // 7. HIGH-VISIBILITY SLEEK ALUMINUM LAPTOP & RIGOROUS TYPING PAWS
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

    // 1. Sleek Modern Desk Mat / Surface
    drawRoundRect(
        color = if (isBurning) Color(0xFF5D1D16) else Color(0xFF4A2B2B).copy(alpha = 0.5f),
        topLeft = Offset(cx - (w * 0.38f), deskY),
        size = Size(w * 0.76f, h * 0.16f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx())
    )

    // Desk Edge highlight
    drawLine(
        color = Color.White.copy(alpha = 0.2f),
        start = Offset(cx - (w * 0.36f), deskY + 1.dp.toPx()),
        end = Offset(cx + (w * 0.36f), deskY + 1.dp.toPx()),
        strokeWidth = 1.5.dp.toPx()
    )

    // 2. High-Visibility Sleek Laptop Display (Open Screen facing mascot/user)
    val laptopW = w * 0.36f
    val laptopH = h * 0.19f
    val laptopLeft = cx - (laptopW / 2f)
    val laptopTop = deskY - (laptopH * 0.78f)

    // Laptop Screen Outer Shell (Dark Titanium Grey)
    drawRoundRect(
        color = if (isBurning) Color(0xFF330A0A) else Color(0xFF1E1E24),
        topLeft = Offset(laptopLeft, laptopTop),
        size = Size(laptopW, laptopH * 0.82f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx(), 5.dp.toPx())
    )

    // Glowing IPS Display Screen
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

    // Syntax Highlighted Code Lines on Screen (Cyan, FameGold, Coral, Green)
    if (isStudying || isBurning) {
        val lineY1 = screenTop + 4.dp.toPx()
        val lineY2 = screenTop + 8.dp.toPx()
        val lineY3 = screenTop + 12.dp.toPx()
        val lineY4 = screenTop + 16.dp.toPx()

        // Code Line 1 (Green keyword + Cyan variable)
        drawLine(color = SuccessGreen, start = Offset(screenLeft + 3.dp.toPx(), lineY1), end = Offset(screenLeft + 12.dp.toPx(), lineY1), strokeWidth = 1.5.dp.toPx())
        drawLine(color = AccentCyan, start = Offset(screenLeft + 14.dp.toPx(), lineY1), end = Offset(screenLeft + 26.dp.toPx(), lineY1), strokeWidth = 1.5.dp.toPx())

        // Code Line 2 (FameGold function)
        drawLine(color = FameGold, start = Offset(screenLeft + 5.dp.toPx(), lineY2), end = Offset(screenLeft + 20.dp.toPx(), lineY2), strokeWidth = 1.5.dp.toPx())
        drawLine(color = PrimaryCoral, start = Offset(screenLeft + 22.dp.toPx(), lineY2), end = Offset(screenLeft + 32.dp.toPx(), lineY2), strokeWidth = 1.5.dp.toPx())

        // Code Line 3 (Cyan return statement)
        drawLine(color = AccentCyan, start = Offset(screenLeft + 5.dp.toPx(), lineY3), end = Offset(screenLeft + 18.dp.toPx(), lineY3), strokeWidth = 1.5.dp.toPx())

        // Code Line 4 (SuccessGreen bracket)
        drawLine(color = SuccessGreen, start = Offset(screenLeft + 3.dp.toPx(), lineY4), end = Offset(screenLeft + 8.dp.toPx(), lineY4), strokeWidth = 1.5.dp.toPx())
    } else {
        // Glowing Tomato Logo on Idle Display
        drawCircle(
            color = PrimaryCoral,
            radius = 3.5.dp.toPx(),
            center = Offset(screenLeft + (screenW / 2f), screenTop + (screenH / 2f))
        )
    }

    // 3. Laptop Keyboard Base (Forward inclined with key matrix)
    val baseLeft = cx - (laptopW * 0.62f)
    val baseTop = laptopTop + (laptopH * 0.78f)
    val baseW = laptopW * 1.24f
    val baseH = h * 0.055f

    // Aluminum Keyboard Deck
    drawRoundRect(
        color = if (isBurning) Color(0xFF8B2516) else Color(0xFFD5D8DC),
        topLeft = Offset(baseLeft, baseTop),
        size = Size(baseW, baseH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )

    // Keyboard Key Bed Matrix (Dark Keys)
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

    // Individual glowing key lines
    drawLine(color = Color.White.copy(alpha = 0.4f), start = Offset(keyBedLeft + 4.dp.toPx(), keyBedTop + 2.dp.toPx()), end = Offset(keyBedLeft + keyBedW - 4.dp.toPx(), keyBedTop + 2.dp.toPx()), strokeWidth = 1.dp.toPx())
    drawLine(color = Color.White.copy(alpha = 0.4f), start = Offset(keyBedLeft + 4.dp.toPx(), keyBedTop + 5.dp.toPx()), end = Offset(keyBedLeft + keyBedW - 4.dp.toPx(), keyBedTop + 5.dp.toPx()), strokeWidth = 1.dp.toPx())

    // Trackpad
    val padW = 10.dp.toPx()
    val padH = 3.dp.toPx()
    drawRoundRect(
        color = if (isBurning) FameGold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f),
        topLeft = Offset(cx - (padW / 2f), baseTop + keyBedH + 1.5.dp.toPx()),
        size = Size(padW, padH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
    )

    // 4. RIGOROUS TYPING PAWS (Highly visible, distinct peach paws striking keys)
    val pawRadius = 5.5.dp.toPx()
    val pawLeftX = keyBedLeft + 8.dp.toPx()
    val pawLeftY = keyBedTop + 4.dp.toPx() + (if (isStudying) typingLeft.dp.toPx() else 0f)

    val pawRightX = keyBedLeft + keyBedW - 8.dp.toPx()
    val pawRightY = keyBedTop + 4.dp.toPx() + (if (isStudying) typingRight.dp.toPx() else 0f)

    val pawColor = if (isBurning) Color(0xFFFFCC80) else Color(0xFFFFF7F6)
    val pawBorderColor = if (isBurning) Color(0xFFB71C1C) else PrimaryCoralDark

    // Left Paw
    drawCircle(
        color = pawColor,
        radius = pawRadius,
        center = Offset(pawLeftX, pawLeftY)
    )
    drawCircle(
        color = pawBorderColor,
        radius = pawRadius,
        center = Offset(pawLeftX, pawLeftY),
        style = Stroke(width = 1.5.dp.toPx())
    )

    // Right Paw
    drawCircle(
        color = pawColor,
        radius = pawRadius,
        center = Offset(pawRightX, pawRightY)
    )
    drawCircle(
        color = pawBorderColor,
        radius = pawRadius,
        center = Offset(pawRightX, pawRightY),
        style = Stroke(width = 1.5.dp.toPx())
    )

    // Typing Hit Sparks / Ripples when actively studying
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
        // Fiery Glowing Sunglasses / Supercharged Fiery Eyes
        val sunglassW = 12.dp.toPx()
        val sunglassH = 8.dp.toPx()

        // Left Fiery Lens
        drawRoundRect(
            color = Color(0xFF1E1E24),
            topLeft = Offset(headCenter.x - 14.dp.toPx(), headCenter.y - 4.dp.toPx()),
            size = Size(sunglassW, sunglassH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        // Fire reflection inside sunglasses
        drawLine(
            color = FameGold,
            start = Offset(headCenter.x - 12.dp.toPx(), headCenter.y - 2.dp.toPx()),
            end = Offset(headCenter.x - 4.dp.toPx(), headCenter.y + 2.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )

        // Right Fiery Lens
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

        // Bridge between glasses
        drawLine(
            color = Color(0xFF1E1E24),
            start = Offset(headCenter.x - 2.dp.toPx(), headCenter.y - 1.dp.toPx()),
            end = Offset(headCenter.x + 2.dp.toPx(), headCenter.y - 1.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )

        // Fiery Grin
        val grin = Path().apply {
            moveTo(headCenter.x - 7.dp.toPx(), headCenter.y + 8.dp.toPx())
            quadraticTo(headCenter.x, headCenter.y + 14.dp.toPx(), headCenter.x + 7.dp.toPx(), headCenter.y + 8.dp.toPx())
            close()
        }
        drawPath(grin, color = FameGold)
    } else if (isPetting) {
        // Heart eyes / Ultra happy
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
        // Big Happy Open Smile
        val happyMouth = Path().apply {
            moveTo(headCenter.x - 6.dp.toPx(), headCenter.y + 7.dp.toPx())
            quadraticTo(headCenter.x, headCenter.y + 16.dp.toPx(), headCenter.x + 6.dp.toPx(), headCenter.y + 7.dp.toPx())
            close()
        }
        drawPath(happyMouth, color = PrimaryCoral)
        drawCrown(headCenter.x, headCenter.y - headRadiusY)
    } else if (isSinging) {
        // Singing Joyful Winking Eye & Open Round Singing Mouth
        // Left Eye (Winking arc)
        drawArc(
            color = PrimaryCoralDark,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(headCenter.x - 13.dp.toPx(), headCenter.y - 2.dp.toPx()),
            size = Size(8.dp.toPx(), 6.dp.toPx()),
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        )
        // Right Eye (Joyful dot)
        drawCircle(color = PrimaryCoralDark, radius = 3.dp.toPx(), center = Offset(headCenter.x + 10.dp.toPx(), headCenter.y + 1.dp.toPx()))

        // Singing Open Mouth (Singing 'o' shape)
        drawOval(
            color = PrimaryCoral,
            topLeft = Offset(headCenter.x - 3.5.dp.toPx(), headCenter.y + 7.dp.toPx()),
            size = Size(7.dp.toPx(), 8.dp.toPx())
        )
    } else if (isFrustrated) {
        // Frustration Focused Furrowed Brows (> <)
        // Left Angled Eye
        drawLine(color = PrimaryCoralDark, start = Offset(headCenter.x - 13.dp.toPx(), headCenter.y - 1.dp.toPx()), end = Offset(headCenter.x - 7.dp.toPx(), headCenter.y + 2.dp.toPx()), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = PrimaryCoralDark, start = Offset(headCenter.x - 13.dp.toPx(), headCenter.y + 5.dp.toPx()), end = Offset(headCenter.x - 7.dp.toPx(), headCenter.y + 2.dp.toPx()), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)

        // Right Angled Eye
        drawLine(color = PrimaryCoralDark, start = Offset(headCenter.x + 13.dp.toPx(), headCenter.y - 1.dp.toPx()), end = Offset(headCenter.x + 7.dp.toPx(), headCenter.y + 2.dp.toPx()), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = PrimaryCoralDark, start = Offset(headCenter.x + 13.dp.toPx(), headCenter.y + 5.dp.toPx()), end = Offset(headCenter.x + 7.dp.toPx(), headCenter.y + 2.dp.toPx()), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)

        // Clenched determined mouth
        val clenchedMouth = Path().apply {
            moveTo(headCenter.x - 6.dp.toPx(), headCenter.y + 10.dp.toPx())
            lineTo(headCenter.x + 6.dp.toPx(), headCenter.y + 10.dp.toPx())
        }
        drawPath(clenchedMouth, color = PrimaryCoralDark, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))

        // Sweat Drop
        val sweatDrop = Path().apply {
            moveTo(headCenter.x + headRadiusX + 2.dp.toPx(), headCenter.y - 2.dp.toPx())
            lineTo(headCenter.x + headRadiusX + 5.dp.toPx(), headCenter.y + 5.dp.toPx())
            lineTo(headCenter.x + headRadiusX - 1.dp.toPx(), headCenter.y + 5.dp.toPx())
            close()
        }
        drawPath(sweatDrop, color = AccentCyan)
    } else {
        // Standard Focused / Idle
        drawCircle(color = PrimaryCoralDark, radius = 3.dp.toPx(), center = Offset(headCenter.x - 10.dp.toPx(), headCenter.y + 1.dp.toPx()))
        drawCircle(color = PrimaryCoralDark, radius = 3.dp.toPx(), center = Offset(headCenter.x + 10.dp.toPx(), headCenter.y + 1.dp.toPx()))

        val mouthPath = Path().apply {
            moveTo(headCenter.x - 5.dp.toPx(), headCenter.y + 9.dp.toPx())
            quadraticTo(headCenter.x, headCenter.y + 13.dp.toPx(), headCenter.x + 5.dp.toPx(), headCenter.y + 9.dp.toPx())
        }
        drawPath(mouthPath, color = PrimaryCoralDark, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawBurningFireAura(cx: Float, cy: Float, w: Float, h: Float, fireFlicker: Float, flameGlow: Float) {
    val auraRadius = w * 0.46f * fireFlicker

    // Outer Red Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFF3D00).copy(alpha = flameGlow * 0.85f), Color(0xFFFF9100).copy(alpha = flameGlow * 0.5f), Color.Transparent),
            center = Offset(cx, cy - (h * 0.05f)),
            radius = auraRadius
        ),
        center = Offset(cx, cy - (h * 0.05f)),
        radius = auraRadius
    )

    // Dynamic Flame Tongues leaping upwards
    val flame1 = Path().apply {
        moveTo(cx - 24.dp.toPx(), cy - (h * 0.05f))
        quadraticTo(cx - 30.dp.toPx(), cy - (h * 0.28f) * fireFlicker, cx - 18.dp.toPx(), cy - (h * 0.35f) * fireFlicker)
        quadraticTo(cx - 10.dp.toPx(), cy - (h * 0.22f), cx - 6.dp.toPx(), cy - (h * 0.05f))
        close()
    }
    drawPath(flame1, color = Color(0xFFFF5722).copy(alpha = 0.85f))

    val flame2 = Path().apply {
        moveTo(cx + 6.dp.toPx(), cy - (h * 0.05f))
        quadraticTo(cx + 12.dp.toPx(), cy - (h * 0.32f) * fireFlicker, cx + 20.dp.toPx(), cy - (h * 0.38f) * fireFlicker)
        quadraticTo(cx + 28.dp.toPx(), cy - (h * 0.24f), cx + 24.dp.toPx(), cy - (h * 0.05f))
        close()
    }
    drawPath(flame2, color = Color(0xFFFF9800).copy(alpha = 0.85f))

    // Core Golden Flame Tongue
    val coreFlame = Path().apply {
        moveTo(cx - 10.dp.toPx(), cy - (h * 0.08f))
        quadraticTo(cx, cy - (h * 0.38f) * fireFlicker, cx, cy - (h * 0.42f) * fireFlicker)
        quadraticTo(cx + 6.dp.toPx(), cy - (h * 0.28f), cx + 10.dp.toPx(), cy - (h * 0.08f))
        close()
    }
    drawPath(coreFlame, color = FameGold.copy(alpha = 0.95f))
}

private fun DrawScope.drawFloatingMusicalNotes(cx: Float, cy: Float, w: Float, h: Float, phase: Float) {
    // Musical Note 1 (♪) floating left
    val note1Y = (cy - (h * 0.15f)) - (phase * 40.dp.toPx())
    val note1X = (cx - 24.dp.toPx()) + (sin(phase * Math.PI.toFloat() * 2f) * 6.dp.toPx())
    val alpha1 = (1f - phase).coerceIn(0f, 1f)

    drawCircle(color = FameGold.copy(alpha = alpha1), radius = 2.8.dp.toPx(), center = Offset(note1X, note1Y))
    drawLine(color = FameGold.copy(alpha = alpha1), start = Offset(note1X + 2.5.dp.toPx(), note1Y), end = Offset(note1X + 2.5.dp.toPx(), note1Y - 8.dp.toPx()), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = FameGold.copy(alpha = alpha1), start = Offset(note1X + 2.5.dp.toPx(), note1Y - 8.dp.toPx()), end = Offset(note1X + 6.dp.toPx(), note1Y - 6.dp.toPx()), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)

    // Musical Note 2 (♫) floating right (offset phase)
    val phase2 = (phase + 0.5f) % 1f
    val note2Y = (cy - (h * 0.15f)) - (phase2 * 42.dp.toPx())
    val note2X = (cx + 20.dp.toPx()) - (cos(phase2 * Math.PI.toFloat() * 2f) * 6.dp.toPx())
    val alpha2 = (1f - phase2).coerceIn(0f, 1f)

    drawCircle(color = AccentCyan.copy(alpha = alpha2), radius = 2.4.dp.toPx(), center = Offset(note2X, note2Y))
    drawCircle(color = AccentCyan.copy(alpha = alpha2), radius = 2.4.dp.toPx(), center = Offset(note2X + 6.dp.toPx(), note2Y - 2.dp.toPx()))
    drawLine(color = AccentCyan.copy(alpha = alpha2), start = Offset(note2X + 2.2.dp.toPx(), note2Y), end = Offset(note2X + 2.2.dp.toPx(), note2Y - 7.dp.toPx()), strokeWidth = 1.5.dp.toPx())
    drawLine(color = AccentCyan.copy(alpha = alpha2), start = Offset(note2X + 8.2.dp.toPx(), note2Y - 2.dp.toPx()), end = Offset(note2X + 8.2.dp.toPx(), note2Y - 9.dp.toPx()), strokeWidth = 1.5.dp.toPx())
    drawLine(color = AccentCyan.copy(alpha = alpha2), start = Offset(note2X + 2.2.dp.toPx(), note2Y - 7.dp.toPx()), end = Offset(note2X + 8.2.dp.toPx(), note2Y - 9.dp.toPx()), strokeWidth = 2.dp.toPx())
}

private fun DrawScope.drawFrustrationSteamPuffs(cx: Float, cy: Float, w: Float, h: Float, phase: Float) {
    val steamAlpha = (1f - phase).coerceIn(0f, 0.85f)
    val steamDist = phase * 16.dp.toPx()
    val steamSize = (4.dp.toPx() + (phase * 6.dp.toPx()))

    // Left Steam Puff (💨)
    val leftX = cx - (w * 0.22f) - steamDist
    val leftY = cy - (h * 0.12f) - (steamDist * 0.5f)
    drawCircle(color = Color.White.copy(alpha = steamAlpha), radius = steamSize, center = Offset(leftX, leftY))
    drawCircle(color = Color.White.copy(alpha = steamAlpha * 0.7f), radius = steamSize * 0.7f, center = Offset(leftX + 4.dp.toPx(), leftY + 2.dp.toPx()))

    // Right Steam Puff
    val rightX = cx + (w * 0.22f) + steamDist
    val rightY = cy - (h * 0.12f) - (steamDist * 0.5f)
    drawCircle(color = Color.White.copy(alpha = steamAlpha), radius = steamSize, center = Offset(rightX, rightY))
    drawCircle(color = Color.White.copy(alpha = steamAlpha * 0.7f), radius = steamSize * 0.7f, center = Offset(rightX - 4.dp.toPx(), rightY + 2.dp.toPx()))
}

private fun DrawScope.drawCrown(x: Float, y: Float) {
    val crownPath = Path().apply {
        moveTo(x - 12.dp.toPx(), y)
        lineTo(x - 14.dp.toPx(), y - 10.dp.toPx())
        lineTo(x - 6.dp.toPx(), y - 4.dp.toPx())
        lineTo(x, y - 13.dp.toPx())
        lineTo(x + 6.dp.toPx(), y - 4.dp.toPx())
        lineTo(x + 14.dp.toPx(), y - 10.dp.toPx())
        lineTo(x + 12.dp.toPx(), y)
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
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            drawLine(Color(0xFFFF00FF), Offset(headCenter.x - 16.dp.toPx(), headCenter.y - 6.dp.toPx()), Offset(headCenter.x - 21.dp.toPx(), headCenter.y - 13.dp.toPx()), 2.dp.toPx())
            drawLine(Color(0xFFFF00FF), Offset(headCenter.x + 16.dp.toPx(), headCenter.y - 6.dp.toPx()), Offset(headCenter.x + 21.dp.toPx(), headCenter.y - 13.dp.toPx()), 2.dp.toPx())
            drawCircle(Color(0xFFFF00FF), 2.5.dp.toPx(), Offset(headCenter.x - 21.dp.toPx(), headCenter.y - 13.dp.toPx()))
            drawCircle(Color(0xFFFF00FF), 2.5.dp.toPx(), Offset(headCenter.x + 21.dp.toPx(), headCenter.y - 13.dp.toPx()))
        }
        "item_night_owl_skin" -> {
            val cap = Path().apply {
                moveTo(headCenter.x - 15.dp.toPx(), headCenter.y - 12.dp.toPx())
                quadraticTo(headCenter.x - 2.dp.toPx(), headCenter.y - 34.dp.toPx(), headCenter.x + 14.dp.toPx(), headCenter.y - 20.dp.toPx())
                quadraticTo(headCenter.x + 22.dp.toPx(), headCenter.y - 12.dp.toPx(), headCenter.x + 26.dp.toPx(), headCenter.y - 4.dp.toPx())
                close()
            }
            drawPath(cap, Color(0xFF3F51B5))
            drawCircle(FameGold, 3.5.dp.toPx(), Offset(headCenter.x + 26.dp.toPx(), headCenter.y - 4.dp.toPx()))
        }
        "item_golden_desk" -> {
            drawRoundRect(
                color = FameGold,
                topLeft = Offset(cx - (w * 0.38f), cy + (h * 0.20f)),
                size = Size(w * 0.76f, 3.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            drawCircle(FameGold, 4.dp.toPx(), Offset(cx - (w * 0.34f), cy + (h * 0.28f)))
            drawCircle(FameGold, 4.dp.toPx(), Offset(cx + (w * 0.34f), cy + (h * 0.28f)))
        }
    }
}

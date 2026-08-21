package com.example.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.MascotState
import com.example.core.TimeBasedThemeManager
import com.example.data.repository.StudyRepository
import com.example.ui.common.StudyIcons
import com.example.ui.launcher.InteractiveMascot
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingStep(
    val title: String,
    val tagline: String,
    val badge: String,
    val facts: List<String>,
    val art: Int,
    val accent: Color
)

@Composable
fun OnboardingScreen(
    repository: StudyRepository,
    themeManager: TimeBasedThemeManager,
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val isDark = themeManager.isDarkThemeActive()
    val steps = remember {
        listOf(
            OnboardingStep("StudyOS", "Your phone is now a campus.", "FOCUS OS", listOf("Circadian themes", "Reactive mascot", "100% offline"), 0, Color(0xFFD9534F)),
            OnboardingStep("Lock In", "25 minutes. Zero excuses.", "+2 FAME / MIN", listOf("1-480 min timers", "Loop Mode", "3h = BURNING +100"), 1, Color(0xFFFF5722)),
            OnboardingStep("Fame vs Shame", "Study = Fame. Slack = Shame.", "ZERO-SUM ECONOMY", listOf("Fame cancels Shame", "4-6 PM = x3 Shame", "Savage alerts"), 2, Color(0xFFC7A600)),
            OnboardingStep("Knowledge Market", "Your grades have a stock price.", "LIVE TICKERS", listOf("Trade \$MATH & \$CS", "Squads & goals", "Scholar tiers"), 3, Color(0xFF20B2AA)),
            OnboardingStep("Make It Yours", "Flex your focus.", "EQUIP & FLEX", listOf("Animated skins", "Launcher themes", "Night Maroon"), 4, Color(0xFF9C27B0))
        )
    }
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val page = pagerState.currentPage
    val bgTop by animateColorAsState(targetValue = steps[page].accent, animationSpec = tween(700), label = "bg_top")
    val bgBottom by animateColorAsState(targetValue = if (isDark) Color(0xFF1B0F0F) else Color(0xFF4A2C2C), animationSpec = tween(700), label = "bg_bottom")
    val finish: () -> Unit = {
        coroutineScope.launch {
            repository.setOnboardingCompleted(true)
            onFinishOnboarding()
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(32.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(StudyIcons.StreakFlame, contentDescription = null, tint = FameGold, modifier = Modifier.size(18.dp))
                        }
                    }
                    Text("StudyOS", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White, letterSpacing = 0.5.sp)
                }
                if (page < steps.size - 1) {
                    TextButton(onClick = finish) {
                        Text("Skip", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { p ->
                OnboardingPage(step = steps[p], active = p == page)
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    repeat(steps.size) { index ->
                        val isSelected = page == index
                        val dotW by animateFloatAsState(if (isSelected) 24f else 6f, tween(300), label = "dot$index")
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(dotW.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) FameGold else Color.White.copy(alpha = 0.35f))
                        )
                    }
                }
                val isLast = page == steps.size - 1
                val pulseT = rememberInfiniteTransition(label = "btn_pulse")
                val pulse by pulseT.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.04f,
                    animationSpec = infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "pulse"
                )
                Button(
                    onClick = {
                        if (isLast) finish()
                        else coroutineScope.launch { pagerState.animateScrollToPage(page + 1) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) FameGold else Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .graphicsLayer {
                            val s = if (isLast) pulse else 1f
                            scaleX = s
                            scaleY = s
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(
                            text = if (isLast) "Get Started" else "Continue",
                            color = if (isDark) OnSurfaceDark else PrimaryCoralDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (isLast) StudyIcons.Check else StudyIcons.ChevronRight,
                            contentDescription = null,
                            tint = if (isDark) OnSurfaceDark else PrimaryCoralDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(step: OnboardingStep, active: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.18f),
            modifier = stagger(active, 0)
        ) {
            Text(
                text = step.badge,
                color = FameGold,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = stagger(active, 1).size(210.dp), contentAlignment = Alignment.Center) {
            if (step.art == 0) {
                InteractiveMascot(state = MascotState.IDLE, size = 200.dp, showArc = false)
            } else {
                when (step.art) {
                    1 -> PomodoroStreakIllustration(size = 200.dp, isDark = false)
                    2 -> EconomyBalanceIllustration(size = 200.dp, isDark = false)
                    3 -> StocksSquadsIllustration(size = 200.dp, isDark = false)
                    else -> ProfileThemeIllustration(size = 200.dp, isDark = false)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        TypewriterTitle(text = step.title, active = active)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = step.tagline,
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.92f),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = stagger(active, 3)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = stagger(active, 4)) {
            step.facts.forEach { fact ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(StudyIcons.FameStar, contentDescription = null, tint = FameGold, modifier = Modifier.size(11.dp))
                    Text(fact, fontSize = 12.sp, color = Color.White.copy(alpha = 0.95f), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TypewriterTitle(text: String, active: Boolean) {
    var shown by remember { mutableIntStateOf(0) }
    LaunchedEffect(active, text) {
        if (active) {
            shown = 0
            delay(300)
            while (shown < text.length) {
                delay(45)
                shown++
            }
        }
    }
    Text(
        text = if (shown < text.length) text.take(shown) + "|" else text,
        fontWeight = FontWeight.Black,
        fontSize = 30.sp,
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier.height(40.dp)
    )
}

@Composable
private fun stagger(active: Boolean, index: Int): Modifier {
    val t by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(450, delayMillis = if (active) 120 + index * 110 else 0, easing = FastOutSlowInEasing),
        label = "stagger$index"
    )
    return Modifier.graphicsLayer {
        alpha = t
        translationY = (1f - t) * 36f
        scaleX = 0.94f + 0.06f * t
        scaleY = 0.94f + 0.06f * t
    }
}

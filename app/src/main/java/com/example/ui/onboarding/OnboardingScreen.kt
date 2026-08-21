package com.example.ui.onboarding

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.TimeBasedThemeManager
import com.example.data.repository.StudyRepository
import com.example.ui.common.StudyIcons
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingStep(
    val title: String,
    val description: String,
    val badge: String,
    val illustrationType: Int,
    val keyFacts: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
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
            OnboardingStep(
                title = "Welcome to StudyOS",
                description = "A full study operating system that lives on your phone and reacts to your day.",
                badge = "Circadian OS",
                illustrationType = 0,
                keyFacts = listOf(
                    "Theme auto-shifts: Sunrise, Coral Day, Sunset, Night Maroon",
                    "Your StudyBuddy mascot reacts live to every session",
                    "Works 100% offline - your data stays on your phone"
                )
            ),
            OnboardingStep(
                title = "Pomodoro & Exam Prep",
                description = "Run focus timers while your mascot types, sings, sweats and burns with you.",
                badge = "Custom 1-480 min",
                illustrationType = 1,
                keyFacts = listOf(
                    "Standard 25m or any custom duration",
                    "Loop Mode = auto-restarting continuous cycles",
                    "1h+ loops boost to 2.5 Fame/min",
                    "3h straight = BURNING mode (+100 Fame)"
                )
            ),
            OnboardingStep(
                title = "Fame vs Shame Economy",
                description = "Study earns Fame. Idling during study hours earns Shame. Fame cancels Shame.",
                badge = "Zero-Sum Balance",
                illustrationType = 2,
                keyFacts = listOf(
                    "+2 Fame every minute you study",
                    "+1 Shame every idle minute (5 AM - 10 PM)",
                    "DANGER HOURS 4-6 PM: +3 Shame per minute",
                    "Active study cancels Shame 1:1 while running"
                )
            ),
            OnboardingStep(
                title = "Danger Hours: 4-6 PM",
                description = "Every day from 4 to 6 PM the OS catches fire and Shame triples.",
                badge = "Fire Protocol",
                illustrationType = 1,
                keyFacts = listOf(
                    "Launcher turns into a burning fire theme",
                    "Mascot panics until you start a session",
                    "+3 Shame per minute if idle",
                    "Starting a timer instantly stops the bleeding"
                )
            ),
            OnboardingStep(
                title = "Study Stocks & Squads",
                description = "Invest Fame in subject stocks that rise when you log study hours.",
                badge = "Live Market",
                illustrationType = 3,
                keyFacts = listOf(
                    "Trade \$MATH, \$CS, \$PHYS, \$SPAN, \$HIST",
                    "Prices move with your weekly study volume",
                    "Create squads and hit group Pomodoro goals"
                )
            ),
            OnboardingStep(
                title = "Fame Store & Cosmetics",
                description = "Spend Fame on animated mascot skins, launcher themes and perks.",
                badge = "Equip & Flex",
                illustrationType = 4,
                keyFacts = listOf(
                    "Animated skins: halo, ninja, confetti and more",
                    "Launcher themes: Matrix, Fiesta, Aurora",
                    "Some items lock behind subject mastery"
                )
            )
        )
    }
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val bgBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF4A2C2C), Color(0xFF241515)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFD9534F), Color(0xFFC94440)))
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = StudyIcons.StreakFlame,
                                    contentDescription = null,
                                    tint = FameGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "StudyOS",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                    if (pagerState.currentPage < steps.size - 1) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    repository.setOnboardingCompleted(true)
                                    onFinishOnboarding()
                                }
                            }
                        ) {
                            Text(
                                text = "Skip",
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    val step = steps[page]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (step.illustrationType) {
                                0 -> CircadianWelcomeIllustration(size = 180.dp, isDark = isDark)
                                1 -> PomodoroStreakIllustration(size = 180.dp, isDark = isDark)
                                2 -> EconomyBalanceIllustration(size = 180.dp, isDark = isDark)
                                3 -> StocksSquadsIllustration(size = 180.dp, isDark = isDark)
                                else -> ProfileThemeIllustration(size = 180.dp, isDark = isDark)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) SurfaceNightCard else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = step.badge.uppercase(),
                                color = FameGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = step.title,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = step.description,
                            fontSize = 13.sp,
                            color = if (isDark) OnSurfaceNightMuted else Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            step.keyFacts.forEach { fact ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = StudyIcons.FameStar,
                                        contentDescription = null,
                                        tint = FameGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = fact,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.95f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(steps.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(if (isSelected) 24.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) FameGold else Color.White.copy(alpha = 0.35f)
                                    )
                            )
                        }
                    }
                    val isLastPage = pagerState.currentPage == steps.size - 1
                    Button(
                        onClick = {
                            if (isLastPage) {
                                coroutineScope.launch {
                                    repository.setOnboardingCompleted(true)
                                    onFinishOnboarding()
                                }
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) FameGold else Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isLastPage) "Get Started with StudyOS" else "Continue",
                                color = if (isDark) OnSurfaceDark else PrimaryCoralDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (isLastPage) StudyIcons.Check else StudyIcons.ChevronRight,
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
}

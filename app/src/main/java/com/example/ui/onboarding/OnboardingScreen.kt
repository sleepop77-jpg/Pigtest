package com.example.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    val subtitle: String,
    val description: String,
    val badge: String,
    val illustrationType: Int
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
                subtitle = "CIRCADIAN FOCUS OPERATING SYSTEM",
                description = "Master your academic routine with a dynamic study OS that transitions naturally between Day Coral focus and Night Maroon calm.",
                badge = "v2.0 Architecture",
                illustrationType = 0
            ),
            OnboardingStep(
                title = "Pomodoro & Reactive Mascot",
                subtitle = "DEEP FOCUS & EXAM PREP",
                description = "Power through 25-minute Pomodoros and 50-minute Exam Prep sprints. Your StudyBuddy mascot reacts with fire auras during streaks!",
                badge = "+2 Fame / Minute",
                illustrationType = 1
            ),
            OnboardingStep(
                title = "Fame vs. Shame Economy",
                subtitle = "GAMIFIED BEHAVIOR PROTOCOL",
                description = "Active study generates Fame tokens. Idling during study hours incurs Shame penalties. Studying actively cancels Shame directly!",
                badge = "Zero-Sum Balance",
                illustrationType = 2
            ),
            OnboardingStep(
                title = "Study Stocks & Squads",
                subtitle = "KNOWLEDGE MARKET INTELLIGENCE",
                description = "Invest your Fame in subject stocks (\$MATH, \$CS, \$PHYS, \$SPAN) whose valuations appreciate as you and your squad log study hours.",
                badge = "Live Market Tickers",
                illustrationType = 3
            ),
            OnboardingStep(
                title = "Personalize & Dark Theme",
                subtitle = "SCHOLAR PROFILE & CONTROLS",
                description = "Switch between Coral Day and Velvet Maroon Dark themes anytime. Customize your scholar profile, avatars, and daily GPA targets.",
                badge = "Customizable Experience",
                illustrationType = 4
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
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header: App Branding + Skip Button
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

                // Center Content: Horizontal Pager
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
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // SVG Illustration
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (step.illustrationType) {
                                0 -> CircadianWelcomeIllustration(size = 220.dp, isDark = isDark)
                                1 -> PomodoroStreakIllustration(size = 220.dp, isDark = isDark)
                                2 -> EconomyBalanceIllustration(size = 220.dp, isDark = isDark)
                                3 -> StocksSquadsIllustration(size = 220.dp, isDark = isDark)
                                else -> ProfileThemeIllustration(size = 220.dp, isDark = isDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category pill badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) SurfaceNightCard else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.padding(bottom = 8.dp)
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
                            fontSize = 24.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = step.description,
                            fontSize = 14.sp,
                            color = if (isDark) OnSurfaceNightMuted else Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                // Bottom Controls: Page Indicator Dots & Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Dot indicators
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

                    // Main Action Button
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

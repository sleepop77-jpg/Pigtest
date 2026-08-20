package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.core.EconomyManager
import com.example.core.FocusTimerManager
import com.example.core.NotificationHelper
import com.example.core.ThemeMode
import com.example.core.TimeBasedThemeManager
import com.example.core.TimeOfDayPhase
import com.example.data.local.AppDatabase
import com.example.data.repository.StudyRepository
import com.example.ui.analytics.AnalyticsScreen
import com.example.ui.flashcards.FlashcardsScreen
import com.example.ui.launcher.LauncherScreen
import com.example.ui.leaderboard.LeaderboardScreen
import com.example.ui.notes.NotesScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.pomodoro.PomodoroScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.store.StoreScreen
import com.example.ui.studygroups.StudyGroupsScreen
import com.example.ui.studystocks.StockMarketScreen
import com.example.ui.taskgoals.TasksGoalsScreen
import com.example.ui.theme.StudyOSTheme

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var repository: StudyRepository
    private lateinit var themeManager: TimeBasedThemeManager
    private lateinit var economyManager: EconomyManager
    private lateinit var timerManager: FocusTimerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.initChannels(applicationContext)

        database = AppDatabase.getDatabase(applicationContext)
        repository = StudyRepository(database)
        themeManager = TimeBasedThemeManager()
        economyManager = EconomyManager(repository)
        timerManager = FocusTimerManager(applicationContext, repository, economyManager)

        setContent {
            val themeMode by themeManager.themeMode.collectAsState()
            val isDarkTheme = when (themeMode) {
                ThemeMode.FORCE_NIGHT_MAROON -> true
                ThemeMode.FORCE_CORAL_DAY -> false
                ThemeMode.AUTO_TIME_BASED -> themeManager.getCurrentPhase() == TimeOfDayPhase.NIGHT_MAROON
            }

            // Notification permission request for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Permission granted or denied */ }

                LaunchedEffect(Unit) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!hasPermission) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            StudyOSTheme(darkTheme = isDarkTheme) {
                StudyOSApp(
                    repository = repository,
                    economyManager = economyManager,
                    timerManager = timerManager,
                    themeManager = themeManager,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        economyManager.cleanup()
        timerManager.cleanup()
    }
}

@Composable
fun StudyOSApp(
    repository: StudyRepository,
    economyManager: EconomyManager,
    timerManager: FocusTimerManager,
    themeManager: TimeBasedThemeManager,
    isDarkTheme: Boolean
) {
    val navController = rememberNavController()
    val userProfile by repository.userProfile.collectAsState(initial = null)

    // First time launch: If user hasn't completed onboarding, direct to onboarding first!
    val startDestination = if (userProfile != null && !userProfile!!.hasCompletedOnboarding) {
        "onboarding"
    } else {
        "launcher"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize(),
        enterTransition = { fadeIn() + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
        exitTransition = { fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
        popEnterTransition = { fadeIn() + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End) },
        popExitTransition = { fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) }
    ) {
        composable("onboarding") {
            OnboardingScreen(
                repository = repository,
                themeManager = themeManager,
                onFinishOnboarding = {
                    navController.navigate("launcher") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("launcher") {
            LauncherScreen(
                repository = repository,
                economyManager = economyManager,
                themeManager = themeManager,
                onNavigateToRoute = { route -> navController.navigate(route) }
            )
        }

        composable("profile") {
            ProfileScreen(
                repository = repository,
                themeManager = themeManager,
                economyManager = economyManager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOnboarding = { navController.navigate("onboarding") }
            )
        }

        composable("pomodoro") {
            PomodoroScreen(
                repository = repository,
                economyManager = economyManager,
                timerManager = timerManager,
                themeManager = themeManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("tasks_goals") {
            TasksGoalsScreen(
                repository = repository,
                themeManager = themeManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("stocks") {
            StockMarketScreen(
                repository = repository,
                themeManager = themeManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("flashcards") {
            FlashcardsScreen(
                repository = repository,
                themeManager = themeManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("notes") {
            NotesScreen(
                repository = repository,
                themeManager = themeManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("analytics") {
            AnalyticsScreen(
                repository = repository,
                themeManager = themeManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("store") {
            StoreScreen(
                repository = repository,
                themeManager = themeManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("leaderboard") {
            LeaderboardScreen(
                repository = repository,
                themeManager = themeManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("groups") {
            StudyGroupsScreen(
                repository = repository,
                themeManager = themeManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                themeManager = themeManager,
                economyManager = economyManager,
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToOnboarding = { navController.navigate("onboarding") }
            )
        }
    }
}

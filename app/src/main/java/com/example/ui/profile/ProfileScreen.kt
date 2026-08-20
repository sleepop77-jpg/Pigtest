package com.example.ui.profile

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.EconomyManager
import com.example.core.ThemeMode
import com.example.core.TimeBasedThemeManager
import com.example.data.local.entities.UserProfile
import com.example.data.repository.StudyRepository
import com.example.ui.common.StudyIcons
import com.example.ui.launcher.MascotComposable
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class AvatarOption(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: StudyRepository,
    themeManager: TimeBasedThemeManager,
    economyManager: EconomyManager,
    onNavigateBack: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val userProfileState by repository.userProfile.collectAsState(initial = UserProfile())
    val profile = userProfileState ?: UserProfile()

    val totalFame by economyManager.totalFame.collectAsState()
    val totalShame by economyManager.totalShame.collectAsState()
    val streakDays by economyManager.currentStreakDays.collectAsState()
    val allSessions by repository.allSessions.collectAsState(initial = emptyList())
    val portfolio by repository.portfolio.collectAsState(initial = emptyList())
    val stocks by repository.allStocks.collectAsState(initial = emptyList())

    val currentThemeMode by themeManager.themeMode.collectAsState()
    val isDarkMode = themeManager.isDarkThemeActive()
    val notificationIntensity by themeManager.notificationIntensity.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }

    // Calculate Lifetime Stats
    val totalStudyMinutes = allSessions.sumOf { it.durationMinutes }
    val totalStudyHours = (totalStudyMinutes / 60f)
    val portfolioValue = portfolio.sumOf { pos ->
        val price = stocks.find { it.id == pos.stockId }?.currentPrice ?: 10f
        (pos.sharesOwned * price * 10).toInt()
    }

    val scholarRankTitle = when {
        totalFame >= 500 -> "Grand Scholar Master"
        totalFame >= 250 -> "Dean's Honor List"
        totalFame >= 100 -> "Senior Focus Apprentice"
        else -> "Focus Novice"
    }

    val avatarOptions = remember {
        listOf(
            AvatarOption("mascot_headphones", "Headphones Scholar", StudyIcons.PomodoroTimer, AccentTeal),
            AvatarOption("mascot_crown", "Honor Valedictorian", StudyIcons.Crown, FameGold),
            AvatarOption("mascot_flame", "Streak Champion", StudyIcons.StreakFlame, Color(0xFFFF5722)),
            AvatarOption("mascot_sleepy", "Night Owl Grinder", StudyIcons.DarkMode, AccentPurple),
            AvatarOption("mascot_glasses", "STEM Researcher", StudyIcons.School, AccentCyan)
        )
    }

    val currentAvatar = avatarOptions.find { it.id == profile.avatarId } ?: avatarOptions.first()

    val screenBg = if (isDarkMode) PrimaryNightMaroon else PrimaryCoral
    val cardBg = if (isDarkMode) SurfaceNightCard else SurfaceCream
    val textColor = if (isDarkMode) OnSurfaceNight else OnSurfaceDark
    val textMuted = if (isDarkMode) OnSurfaceNightMuted else OnSurfaceMuted

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = screenBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scholar Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Black
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
                actions = {
                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(
                            imageVector = StudyIcons.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = screenBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Profile Header Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar with badge
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(currentAvatar.color.copy(alpha = 0.2f))
                            .border(3.dp, currentAvatar.color, CircleShape)
                            .clickable { showAvatarPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = currentAvatar.icon,
                            contentDescription = currentAvatar.name,
                            tint = currentAvatar.color,
                            modifier = Modifier.size(48.dp)
                        )

                        // Edit badge
                        Surface(
                            shape = CircleShape,
                            color = FameGold,
                            modifier = Modifier
                                .size(26.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = StudyIcons.Edit,
                                    contentDescription = "Change Avatar",
                                    tint = OnSurfaceDark,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = profile.fullName,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = textColor
                        )
                        Text(
                            text = "@${profile.username} • ${profile.email}",
                            fontSize = 12.sp,
                            color = textMuted
                        )
                    }

                    // Rank Pill Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FameGold.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, FameGold.copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = StudyIcons.Trophy,
                                contentDescription = null,
                                tint = FameGoldDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = scholarRankTitle.uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = if (isDarkMode) FameGold else FameGoldDark,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Bio & Major
                    Text(
                        text = profile.bio,
                        fontSize = 13.sp,
                        color = textColor,
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDarkMode) PrimaryNightMaroon else Color.White.copy(alpha = 0.5f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = StudyIcons.School,
                                contentDescription = null,
                                tint = AccentTeal,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = profile.major,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                        }
                    }
                }
            }

            // 2. Lifetime Scholar Metrics (4-stat grid)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "LIFETIME PERFORMANCE METRICS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isDarkMode) FameGold else PrimaryCoralDark,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Total Hours
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(StudyIcons.PomodoroTimer, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "%.1f hrs".format(totalStudyHours), fontWeight = FontWeight.Black, fontSize = 16.sp, color = textColor)
                            Text(text = "Time Studied", fontSize = 10.sp, color = textMuted)
                        }

                        // Total Fame
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(StudyIcons.FameStar, contentDescription = null, tint = FameGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$totalFame", fontWeight = FontWeight.Black, fontSize = 16.sp, color = textColor)
                            Text(text = "Fame Points", fontSize = 10.sp, color = textMuted)
                        }

                        // Streak
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(StudyIcons.StreakFlame, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$streakDays Days", fontWeight = FontWeight.Black, fontSize = 16.sp, color = textColor)
                            Text(text = "Streak Record", fontSize = 10.sp, color = textMuted)
                        }

                        // Portfolio
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(StudyIcons.StudyStocks, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "+$portfolioValue", fontWeight = FontWeight.Black, fontSize = 16.sp, color = textColor)
                            Text(text = "Stock Assets", fontSize = 10.sp, color = textMuted)
                        }
                    }
                }
            }

            // 3. Visual Theme & Dark Mode Preference Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "THEME & DISPLAY PREFERENCES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isDarkMode) FameGold else PrimaryCoralDark,
                        letterSpacing = 1.sp
                    )

                    // Dedicated Dark Mode Toggle Switch Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDarkMode) PrimaryNightMaroon else PrimaryCoral.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isDarkMode) StudyIcons.DarkMode else StudyIcons.LightMode,
                                        contentDescription = null,
                                        tint = if (isDarkMode) FameGold else PrimaryCoral,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Dark Mode",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textColor
                                )
                                Text(
                                    text = if (isDarkMode) "Deep Velvet Maroon Night" else "Warm Daylight Coral",
                                    fontSize = 12.sp,
                                    color = textMuted
                                )
                            }
                        }

                        // Switch control
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    themeManager.setThemeMode(ThemeMode.FORCE_NIGHT_MAROON)
                                    coroutineScope.launch { repository.setPreferredTheme("DARK") }
                                } else {
                                    themeManager.setThemeMode(ThemeMode.FORCE_CORAL_DAY)
                                    coroutineScope.launch { repository.setPreferredTheme("LIGHT") }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = FameGold,
                                checkedTrackColor = PrimaryNightCard,
                                uncheckedThumbColor = PrimaryCoral,
                                uncheckedTrackColor = SurfaceCreamLight
                            )
                        )
                    }

                    Divider(color = (if (isDarkMode) OnSurfaceNightMuted else OnSurfaceMuted).copy(alpha = 0.2f))

                    // 3-Way Mode Chips (Auto Circadian, Light, Dark)
                    Text(
                        text = "Theme Schedule Mode",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = textColor
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentThemeMode == ThemeMode.AUTO_TIME_BASED,
                            onClick = {
                                themeManager.setThemeMode(ThemeMode.AUTO_TIME_BASED)
                                coroutineScope.launch { repository.setPreferredTheme("AUTO") }
                            },
                            label = { Text("Auto Circadian", fontSize = 11.sp) },
                            leadingIcon = { Icon(StudyIcons.AutoMode, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )

                        FilterChip(
                            selected = currentThemeMode == ThemeMode.FORCE_CORAL_DAY,
                            onClick = {
                                themeManager.setThemeMode(ThemeMode.FORCE_CORAL_DAY)
                                coroutineScope.launch { repository.setPreferredTheme("LIGHT") }
                            },
                            label = { Text("Day Coral", fontSize = 11.sp) },
                            leadingIcon = { Icon(StudyIcons.LightMode, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )

                        FilterChip(
                            selected = currentThemeMode == ThemeMode.FORCE_NIGHT_MAROON,
                            onClick = {
                                themeManager.setThemeMode(ThemeMode.FORCE_NIGHT_MAROON)
                                coroutineScope.launch { repository.setPreferredTheme("DARK") }
                            },
                            label = { Text("Night Maroon", fontSize = 11.sp) },
                            leadingIcon = { Icon(StudyIcons.DarkMode, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }
            }

            // 4. Study Preferences & Target Hours
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "DAILY FOCUS TARGETS & NOTIFICATIONS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isDarkMode) FameGold else PrimaryCoralDark,
                        letterSpacing = 1.sp
                    )

                    // Daily Target Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Daily Study Target", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = textColor)
                            Text(text = "${profile.dailyStudyTargetHours.roundToInt()} Hours / Day", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = FameGold)
                        }

                        Slider(
                            value = profile.dailyStudyTargetHours,
                            onValueChange = { newValue ->
                                coroutineScope.launch {
                                    repository.updateUserProfile(profile.copy(dailyStudyTargetHours = newValue))
                                }
                            },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = if (isDarkMode) FameGold else PrimaryCoral,
                                activeTrackColor = if (isDarkMode) FameGold else PrimaryCoral
                            )
                        )
                    }

                    // Sarcastic Notification Intensity
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Idle Reminder Sarcasm", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = textColor)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Gentle", "Normal", "Savage").forEach { intensity ->
                                FilterChip(
                                    selected = notificationIntensity == intensity,
                                    onClick = { themeManager.setNotificationIntensity(intensity) },
                                    label = { Text(intensity, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Notification & Vibration switches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Sound & Vibration Cues", fontSize = 13.sp, color = textColor)
                        Switch(
                            checked = profile.soundVibrationEnabled,
                            onCheckedChange = { checked ->
                                coroutineScope.launch {
                                    repository.updateUserProfile(profile.copy(soundVibrationEnabled = checked))
                                }
                            }
                        )
                    }
                }
            }

            // 5. Onboarding & Tutorial Replay Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "TUTORIAL & DISCOVERY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isDarkMode) FameGold else PrimaryCoralDark,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Need a refresher on Pomodoros, the Fame/Shame economy, or Study Stocks?",
                        fontSize = 12.sp,
                        color = textMuted
                    )

                    OutlinedButton(
                        onClick = onNavigateToOnboarding,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isDarkMode) FameGold else PrimaryCoralDark
                        ),
                        border = BorderStroke(1.dp, if (isDarkMode) FameGold else PrimaryCoral)
                    ) {
                        Icon(StudyIcons.Help, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Replay Onboarding Tutorial", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Edit Profile Modal Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(profile.fullName) }
        var editUsername by remember { mutableStateOf(profile.username) }
        var editEmail by remember { mutableStateOf(profile.email) }
        var editMajor by remember { mutableStateOf(profile.major) }
        var editBio by remember { mutableStateOf(profile.bio) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Edit Scholar Profile",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editMajor,
                        onValueChange = { editMajor = it },
                        label = { Text("Academic Major / Target") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio / Scholar Moto") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.updateUserProfile(
                                profile.copy(
                                    fullName = editName.ifBlank { profile.fullName },
                                    username = editUsername.ifBlank { profile.username },
                                    email = editEmail.ifBlank { profile.email },
                                    major = editMajor.ifBlank { profile.major },
                                    bio = editBio.ifBlank { profile.bio }
                                )
                            )
                            showEditProfileDialog = false
                            snackbarHostState.showSnackbar("Profile updated successfully!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Avatar Picker Modal
    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            title = {
                Text(
                    text = "Select Avatar Icon",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    avatarOptions.forEach { opt ->
                        val isSelected = opt.id == profile.avatarId
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) opt.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) BorderStroke(2.dp, opt.color) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        repository.updateUserProfile(profile.copy(avatarId = opt.id))
                                        showAvatarPicker = false
                                        snackbarHostState.showSnackbar("Avatar set to ${opt.name}")
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = opt.color,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(opt.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }

                                Text(
                                    text = opt.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAvatarPicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}

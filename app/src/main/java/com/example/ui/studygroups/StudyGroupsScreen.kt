package com.example.ui.studygroups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.TimeBasedThemeManager
import com.example.data.local.entities.StudyGroup
import com.example.data.repository.StudyRepository
import com.example.ui.common.StudyIcons
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyGroupsScreen(
    repository: StudyRepository,
    themeManager: TimeBasedThemeManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val groups by repository.allStudyGroups.collectAsState(initial = emptyList())
    var showCreateGroupDialog by remember { mutableStateOf(false) }

    val isNightMode = themeManager.isDarkThemeActive()

    val backgroundBrush = remember(isNightMode) {
        if (isNightMode) Brush.verticalGradient(listOf(Color(0xFF4A2C2C), Color(0xFF241515)))
        else Brush.verticalGradient(listOf(Color(0xFFD9534F), Color(0xFFC94440)))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Study Squads & Clubs",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateGroupDialog = true },
                containerColor = FameGold,
                contentColor = OnSurfaceDark,
                shape = CircleShape
            ) {
                Icon(imageVector = StudyIcons.Add, contentDescription = "Create Group")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Active Squad Goals & Collective Focus",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (groups.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = StudyIcons.StudyGroups,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "No Study Squads Yet",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Create your first squad or study club with friends to pool study hours and earn Fame multipliers!",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Button(
                                    onClick = { showCreateGroupDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Create a Squad", color = PrimaryCoralDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(groups, key = { it.id }) { group ->
                        StudyGroupCard(
                            group = group,
                            onToggleJoin = {
                                coroutineScope.launch {
                                    if (group.userJoined) repository.leaveStudyGroup(group)
                                    else repository.joinStudyGroup(group)
                                }
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(70.dp)) }
            }
        }
    }

    if (showCreateGroupDialog) {
        var groupName by remember { mutableStateOf("") }
        var inviteCode by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            title = { Text("Create Study Squad", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Squad Name (e.g. Physics Prep)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = { inviteCode = it.uppercase() },
                        label = { Text("Squad Code (e.g. PHYS26)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (groupName.isNotBlank() && inviteCode.isNotBlank()) {
                            coroutineScope.launch {
                                repository.createStudyGroup(
                                    StudyGroup(
                                        id = java.util.UUID.randomUUID().toString(),
                                        name = groupName,
                                        inviteCode = inviteCode.uppercase(),
                                        memberCount = 1,
                                        currentPomodoros = 0,
                                        targetPomodoros = 100,
                                        isPremium = false,
                                        weeklyFee = 0,
                                        userJoined = true
                                    )
                                )
                                showCreateGroupDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
                ) {
                    Text("Create Squad")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGroupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StudyGroupCard(
    group: StudyGroup,
    onToggleJoin: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PrimaryCoralLight,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = StudyIcons.StudyGroups,
                                contentDescription = null,
                                tint = OnPrimaryWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = group.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = OnSurfaceDark
                        )
                        Text(
                            text = "Squad Code: ${group.inviteCode} · ${group.memberCount} members",
                            fontSize = 11.sp,
                            color = OnSurfaceMuted
                        )
                    }
                }

                Button(
                    onClick = onToggleJoin,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (group.userJoined) Color.Gray.copy(alpha = 0.2f) else PrimaryCoral
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (group.userJoined) "Joined" else "Join Squad",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (group.userJoined) OnSurfaceDark else Color.White
                    )
                }
            }

            // Progress towards Target Pomodoros
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val progress = if (group.targetPomodoros > 0) (group.currentPomodoros.toFloat() / group.targetPomodoros.toFloat()).coerceIn(0f, 1f) else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = FameGold,
                    trackColor = Color.Black.copy(alpha = 0.08f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${group.currentPomodoros} Pomodoros / ${group.targetPomodoros} goal",
                        fontSize = 10.sp,
                        color = OnSurfaceMuted
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryCoralDark
                    )
                }
            }
        }
    }
}

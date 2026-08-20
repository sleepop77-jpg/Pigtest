package com.example.ui.taskgoals

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.TimeBasedThemeManager
import com.example.data.local.entities.StudyGoal
import com.example.data.local.entities.Subject
import com.example.data.local.entities.Task
import com.example.data.repository.StudyRepository
import com.example.ui.common.StudyIcons
import com.example.ui.pomodoro.AddSubjectDialog
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksGoalsScreen(
    repository: StudyRepository,
    themeManager: TimeBasedThemeManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Tasks, 1 = Goals

    val tasks by repository.allTasks.collectAsState(initial = emptyList())
    val goals by repository.allGoals.collectAsState(initial = emptyList())
    val subjects by repository.allSubjects.collectAsState(initial = emptyList())

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showCreateSubjectDialog by remember { mutableStateOf(false) }
    var onSubjectCreatedCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }

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
                        text = if (selectedTab == 0) "Tasks" else "Study Goals",
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
                onClick = {
                    if (selectedTab == 0) showAddTaskDialog = true
                    else showAddGoalDialog = true
                },
                containerColor = FameGold,
                contentColor = OnSurfaceDark,
                shape = CircleShape
            ) {
                Icon(imageVector = StudyIcons.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
            // Tab Switcher Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedTab == 0) Color.White else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Daily Tasks (${tasks.count { !it.completed }})",
                        color = if (selectedTab == 0) PrimaryCoral else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedTab == 1) Color.White else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Weekly Goals (${goals.size})",
                        color = if (selectedTab == 1) PrimaryCoral else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Tasks List
                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tasks yet! Tap + to add a study task.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            TaskItemCard(
                                task = task,
                                onToggle = {
                                    coroutineScope.launch {
                                        repository.updateTask(task.copy(completed = !task.completed))
                                    }
                                },
                                onDelete = {
                                    coroutineScope.launch {
                                        repository.deleteTask(task)
                                    }
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            } else {
                // Goals List
                if (goals.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No study goals yet! Set a weekly goal to earn Fame.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(goals, key = { it.id }) { goal ->
                            GoalItemCard(
                                goal = goal,
                                onClaimReward = {
                                    coroutineScope.launch {
                                        repository.claimGoalReward(goal)
                                    }
                                },
                                onDelete = {
                                    coroutineScope.launch {
                                        repository.deleteGoal(goal)
                                    }
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var taskSubject by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "Mathematics") }
        var taskPriority by remember { mutableStateOf("Medium") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Study Task", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Subject", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val availableSubjects = if (subjects.isNotEmpty()) subjects.map { it.name } else listOf("Mathematics", "Computer Science", "Spanish")
                        availableSubjects.forEach { subjName ->
                            FilterChip(
                                selected = taskSubject == subjName,
                                onClick = { taskSubject = subjName },
                                label = { Text(subjName, fontSize = 11.sp) }
                            )
                        }

                        // "+ New Subject" Button Chip
                        SuggestionChip(
                            onClick = {
                                onSubjectCreatedCallback = { newName ->
                                    taskSubject = newName
                                }
                                showCreateSubjectDialog = true
                            },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = StudyIcons.Add,
                                        contentDescription = "Add Subject",
                                        tint = PrimaryCoral,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text("+ New", color = PrimaryCoral, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }

                    Text("Priority", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("High", "Medium", "Low").forEach { p ->
                            FilterChip(
                                selected = taskPriority == p,
                                onClick = { taskPriority = p },
                                label = { Text(p) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            coroutineScope.launch {
                                repository.insertTask(
                                    Task(
                                        title = taskTitle,
                                        subject = taskSubject,
                                        priority = taskPriority
                                    )
                                )
                            }
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
                ) {
                    Text("Add Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        var goalTitle by remember { mutableStateOf("") }
        var goalType by remember { mutableStateOf("time") }
        var targetValueStr by remember { mutableStateOf("10") }
        var goalSubject by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "Mathematics") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("Set Study Goal", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text("Goal Title (e.g. Study 15h Math)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Subject", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val availableSubjects = if (subjects.isNotEmpty()) subjects.map { it.name } else listOf("Mathematics", "Computer Science", "Spanish")
                        availableSubjects.forEach { subjName ->
                            FilterChip(
                                selected = goalSubject == subjName,
                                onClick = { goalSubject = subjName },
                                label = { Text(subjName, fontSize = 11.sp) }
                            )
                        }

                        // "+ New Subject" Button Chip
                        SuggestionChip(
                            onClick = {
                                onSubjectCreatedCallback = { newName ->
                                    goalSubject = newName
                                }
                                showCreateSubjectDialog = true
                            },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = StudyIcons.Add,
                                        contentDescription = "Add Subject",
                                        tint = PrimaryCoral,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text("+ New", color = PrimaryCoral, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }

                    Text("Goal Type", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("time" to "Hours", "flashcard" to "Cards", "streak" to "Streak").forEach { (type, label) ->
                            FilterChip(
                                selected = goalType == type,
                                onClick = { goalType = type },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = targetValueStr,
                        onValueChange = { targetValueStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Target Quantity") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetVal = targetValueStr.toIntOrNull() ?: 10
                        if (goalTitle.isNotBlank()) {
                            coroutineScope.launch {
                                repository.insertGoal(
                                    StudyGoal(
                                        title = goalTitle,
                                        subject = goalSubject,
                                        goalType = goalType,
                                        targetValue = targetVal,
                                        currentValue = 0,
                                        rewardFame = 100
                                    )
                                )
                            }
                            showAddGoalDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
                ) {
                    Text("Save Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reusable Custom Subject Creation Dialog
    if (showCreateSubjectDialog) {
        AddSubjectDialog(
            onDismiss = { showCreateSubjectDialog = false },
            onAddSubject = { name, colorHex ->
                coroutineScope.launch {
                    val newSubject = Subject(
                        id = name.lowercase().replace(" ", "_"),
                        name = name,
                        masteryPercent = 0,
                        studyHoursTotal = 0f,
                        cardsReviewed = 0,
                        cardsCorrect = 0,
                        colorHex = colorHex
                    )
                    repository.insertSubject(newSubject)
                    onSubjectCreatedCallback?.invoke(name)
                    showCreateSubjectDialog = false
                }
            }
        )
    }
}

@Composable
private fun TaskItemCard(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = SuccessGreen)
                )
                Column {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (task.completed) OnSurfaceMuted else OnSurfaceDark,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = task.subject,
                            fontSize = 11.sp,
                            color = PrimaryCoralDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "•  Priority: ${task.priority}",
                            fontSize = 11.sp,
                            color = when (task.priority) {
                                "High" -> WarningRed
                                "Medium" -> AccentOrange
                                else -> OnSurfaceMuted
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = StudyIcons.Delete,
                    contentDescription = "Delete",
                    tint = OnSurfaceMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun GoalItemCard(
    goal: StudyGoal,
    onClaimReward: () -> Unit,
    onDelete: () -> Unit
) {
    val progressFloat = (goal.currentValue.toFloat() / goal.targetValue.toFloat()).coerceIn(0f, 1f)
    val isComplete = goal.currentValue >= goal.targetValue

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = goal.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = OnSurfaceDark
                    )
                    Text(
                        text = "Deadline: ${goal.deadlineText}",
                        fontSize = 11.sp,
                        color = OnSurfaceMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = StudyIcons.FameStar,
                        contentDescription = "Reward",
                        tint = FameGoldDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "+${goal.rewardFame} Fame",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = FameGoldDark
                    )
                }
            }

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
                    color = if (isComplete) SuccessGreen else PrimaryCoral,
                    trackColor = PrimaryCoralLight.copy(alpha = 0.3f)
                )
                Text(
                    text = "${goal.currentValue} / ${goal.targetValue}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = OnSurfaceDark
                )
            }

            // Claim Reward or Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isComplete && !goal.claimedReward) {
                    Button(
                        onClick = onClaimReward,
                        colors = ButtonDefaults.buttonColors(containerColor = FameGold),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(imageVector = StudyIcons.Sparkle, contentDescription = null, tint = OnSurfaceDark, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Claim +${goal.rewardFame} Fame", color = OnSurfaceDark, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                } else if (goal.claimedReward) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = StudyIcons.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                        Text("Reward Claimed", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "In Progress (${(progressFloat * 100).toInt()}%)",
                        fontSize = 12.sp,
                        color = OnSurfaceMuted,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = StudyIcons.Delete, contentDescription = "Delete", tint = OnSurfaceMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

package com.example.ui.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.core.EconomyManager
import com.example.core.MascotState
import com.example.core.TimeBasedThemeManager
import com.example.data.repository.StudyRepository
import com.example.ui.common.StudyIcons
import com.example.ui.launcher.InteractiveMascot
import com.example.ui.theme.FameGold
import com.example.ui.theme.OnSurfaceDark
import com.example.ui.theme.PrimaryCoral
import com.example.ui.theme.PrimaryCoralDark
import com.example.ui.theme.SurfaceCream
import com.example.ui.theme.WarningRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

object LibraryIcons {
    val Library: ImageVector by lazy {
        ImageVector.Builder(
            name = "Library",
            defaultViewportSize = Size(24f, 24f)
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 5f)
                curveTo(9f, 3f, 5f, 3f, 3f, 4f)
                lineTo(3f, 19f)
                curveTo(5f, 18f, 9f, 18f, 12f, 20f)
                curveTo(15f, 18f, 19f, 18f, 21f, 19f)
                lineTo(21f, 4f)
                curveTo(19f, 3f, 15f, 3f, 12f, 5f)
                close()
            }
        }.build()
    }
    val Video: ImageVector by lazy {
        ImageVector.Builder(
            name = "Video",
            defaultViewportSize = Size(24f, 24f)
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 7f)
                lineTo(3f, 17f)
                lineTo(13f, 17f)
                lineTo(13f, 7f)
                close()
                moveTo(14.5f, 10f)
                lineTo(21f, 6.5f)
                lineTo(21f, 17.5f)
                lineTo(14.5f, 14f)
                close()
            }
        }.build()
    }
}

data class LibraryRoom(
    val id: String,
    val name: String,
    val zoomLink: String,
    val host: String
)

object LibraryRoomStore {
    val rooms = MutableStateFlow<List<LibraryRoom>>(emptyList())
    fun add(room: LibraryRoom) {
        rooms.value = rooms.value + room
    }
}

enum class LibraryScene(val label: String) {
    RAIN("Rain"),
    STARS("Stars"),
    FIRE("Fire")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    repository: StudyRepository,
    economyManager: EconomyManager,
    themeManager: TimeBasedThemeManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var view by remember { mutableStateOf("menu") }
    var scene by remember { mutableStateOf(LibraryScene.RAIN) }
    var minutes by remember { mutableIntStateOf(25) }
    var showCreateRoom by remember { mutableStateOf(false) }
    val rooms by LibraryRoomStore.rooms.collectAsState()
    val isNightMode = themeManager.isDarkThemeActive()
    val backgroundBrush = remember(isNightMode) {
        if (isNightMode) Brush.verticalGradient(listOf(Color(0xFF4A2C2C), Color(0xFF241515)))
        else Brush.verticalGradient(listOf(Color(0xFFD9534F), Color(0xFFC94440)))
    }
    when (view) {
        "menu" -> Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("The Library", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(StudyIcons.Back, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
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
                            text = "Choose your study room",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(LibraryIcons.Library, contentDescription = null, tint = PrimaryCoralDark, modifier = Modifier.size(26.dp))
                                    Text("Alone Library", color = OnSurfaceDark, fontWeight = FontWeight.Black, fontSize = 17.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Total isolation. Just you, the app, and the work. Leaving the app mid-session costs +5 Shame.",
                                    color = OnSurfaceDark.copy(alpha = 0.75f),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(25, 50, 90).forEach { m ->
                                        Button(
                                            onClick = { minutes = m },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (minutes == m) PrimaryCoral else Color(0xFFE8E0DC)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("$m m", color = if (minutes == m) Color.White else OnSurfaceDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    LibraryScene.values().forEach { s ->
                                        Button(
                                            onClick = { scene = s },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (scene == s) PrimaryCoralDark else Color(0xFFE8E0DC)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(s.label, color = if (scene == s) Color.White else OnSurfaceDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { view = "alone" },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().height(46.dp)
                                ) {
                                    Text("Enter Alone Library", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(LibraryIcons.Video, contentDescription = null, tint = PrimaryCoralDark, modifier = Modifier.size(26.dp))
                                    Text("Group Library", color = OnSurfaceDark, fontWeight = FontWeight.Black, fontSize = 17.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Study alongside real people. Create a room, share the Zoom link, and suffer through finals together.",
                                    color = OnSurfaceDark.copy(alpha = 0.75f),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { showCreateRoom = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoralDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().height(46.dp)
                                ) {
                                    Text("Create a Room", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    if (rooms.isNotEmpty()) {
                        item {
                            Text("Open rooms", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        items(rooms, key = { it.id }) { room ->
                            RoomCard(room = room)
                        }
                    }
                    item {
                        Text(
                            text = "Live shared rooms arrive with the backend. For now, send your Zoom link to friends and join together.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }
            }
        }
        "alone" -> AloneLibrarySession(
            repository = repository,
            economyManager = economyManager,
            minutes = minutes,
            scene = scene,
            onExit = { view = "menu" }
        )
    }
    if (showCreateRoom) {
        var roomName by remember { mutableStateOf("") }
        var zoomLink by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateRoom = false },
            title = { Text("Create a Group Library", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text("Room name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = zoomLink,
                        onValueChange = { zoomLink = it },
                        label = { Text("Zoom link") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (roomName.isNotBlank()) {
                            LibraryRoomStore.add(
                                LibraryRoom(
                                    id = "room_${System.currentTimeMillis()}",
                                    name = roomName,
                                    zoomLink = zoomLink.trim(),
                                    host = "You"
                                )
                            )
                            showCreateRoom = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
                ) { Text("Open Room") }
            },
            dismissButton = { TextButton(onClick = { showCreateRoom = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun RoomCard(room: LibraryRoom) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(room.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Hosted by ${room.host}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
            Button(
                onClick = {
                    if (room.zoomLink.isNotBlank()) {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(room.zoomLink)))
                        } catch (_: Exception) { }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FameGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Join on Zoom", color = OnSurfaceDark, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun AloneLibrarySession(
    repository: StudyRepository,
    economyManager: EconomyManager,
    minutes: Int,
    scene: LibraryScene,
    onExit: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var secondsLeft by remember { mutableIntStateOf(minutes * 60) }
    var distractions by remember { mutableIntStateOf(0) }
    var showLeftDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var leftApp by remember { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                leftApp = true
            }
            if (event == Lifecycle.Event.ON_START && leftApp) {
                leftApp = false
                if (!finished) {
                    distractions++
                    showLeftDialog = true
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) {
        economyManager.startStudySession("Library")
    }
    LaunchedEffect(finished) {
        if (finished) return@LaunchedEffect
        while (secondsLeft > 0 && !finished) {
            delay(1000L)
            secondsLeft--
        }
        if (secondsLeft == 0 && !finished) {
            finished = true
            economyManager.stopStudySession()
            repository.recordStudySession(
                sessionType = "Library",
                subject = "Deep Work",
                durationMinutes = minutes,
                isExamPrep = false,
                customFameEarned = 0
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "library_ambient")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "ambient"
    )
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1B1210))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(
                brush = Brush.verticalGradient(listOf(Color(0xFF2A1B16), Color(0xFF170E0B))),
                size = size
            )
            val winW = w * 0.42f
            val winH = h * 0.24f
            val winX = w * 0.08f
            val winY = h * 0.08f
            drawRoundRect(
                color = Color(0xFF3B2A24),
                topLeft = Offset(winX - 4.dp.toPx(), winY - 4.dp.toPx()),
                size = Size(winW + 8.dp.toPx(), winH + 8.dp.toPx()),
                cornerRadius = CornerRadius(10.dp.toPx())
            )
            drawRoundRect(
                color = if (scene == LibraryScene.STARS) Color(0xFF0B1026) else Color(0xFF14213D),
                topLeft = Offset(winX, winY),
                size = Size(winW, winH),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            if (scene == LibraryScene.RAIN) {
                for (i in 0 until 14) {
                    val rx = winX + (winW / 14f) * i + 4.dp.toPx()
                    val ry = winY + ((phase * 1.6f + i * 0.07f) % 1f) * winH
                    drawLine(
                        color = Color(0xFF9EC9FF).copy(alpha = 0.5f),
                        start = Offset(rx, ry),
                        end = Offset(rx - 2.dp.toPx(), ry + 7.dp.toPx()),
                        strokeWidth = 1.4.dp.toPx()
                    )
                }
            } else if (scene == LibraryScene.STARS) {
                for (i in 0 until 12) {
                    val sx = winX + (winW / 12f) * i + 5.dp.toPx()
                    val sy = winY + (i % 4 + 1) * winH / 5f
                    val a = 0.3f + 0.6f * abs(sin(phase * 6.28f + i * 1.7f))
                    drawCircle(Color.White.copy(alpha = a), 1.2.dp.toPx(), Offset(sx, sy))
                }
                drawCircle(FameGold.copy(alpha = 0.85f), 6.dp.toPx(), Offset(winX + winW * 0.78f, winY + winH * 0.3f))
            } else {
                val glow = 0.5f + 0.4f * abs(sin(phase * 6.28f))
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFFF7043).copy(alpha = glow), Color.Transparent),
                        center = Offset(w * 0.82f, h * 0.30f),
                        radius = w * 0.22f
                    ),
                    center = Offset(w * 0.82f, h * 0.30f),
                    radius = w * 0.22f
                )
                for (i in 0 until 3) {
                    val fx = w * 0.78f + i * 12.dp.toPx()
                    val fh = (14 + i * 5).dp.toPx() * (0.8f + 0.3f * abs(sin(phase * 6.28f + i)))
                    val p = Path().apply {
                        moveTo(fx - 6.dp.toPx(), h * 0.36f)
                        quadraticTo(fx, h * 0.36f - fh, fx + 6.dp.toPx(), h * 0.36f)
                        close()
                    }
                    drawPath(p, Color(0xFFFF9100).copy(alpha = 0.8f))
                }
            }
            val deskY = h * 0.72f
            drawRoundRect(
                color = Color(0xFF4A2B2B),
                topLeft = Offset(w * 0.06f, deskY),
                size = Size(w * 0.88f, h * 0.05f),
                cornerRadius = CornerRadius(8.dp.toPx())
            )
            val lampX = w * 0.80f
            drawLine(Color(0xFF223047), Offset(lampX, deskY), Offset(lampX, deskY - h * 0.12f), 3.dp.toPx())
            drawCircle(Color(0xFF223047), 7.dp.toPx(), Offset(lampX, deskY - h * 0.12f))
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(FameGold.copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(lampX, deskY - h * 0.10f),
                    radius = w * 0.20f
                ),
                center = Offset(lampX, deskY - h * 0.10f),
                radius = w * 0.20f
            )
            for (i in 0 until 3) {
                drawRoundRect(
                    color = listOf(Color(0xFFD9534F), Color(0xFF20B2AA), Color(0xFFF5A623))[i],
                    topLeft = Offset(w * 0.12f + i * 5.dp.toPx(), deskY - 6.dp.toPx() - i * 6.dp.toPx()),
                    size = Size(w * 0.14f, 5.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
            for (i in 0 until 8) {
                val dx = (w / 8f) * i + 10.dp.toPx()
                val dy = h * 0.5f + sin(phase * 6.28f + i * 2.1f) * 12.dp.toPx()
                drawCircle(Color.White.copy(alpha = 0.10f), 1.5.dp.toPx(), Offset(dx, dy))
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = if (finished) "Session complete. The library approves." else "ALONE LIBRARY",
                color = FameGold,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = 2.sp
            )
            Text(
                text = String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 52.sp
            )
            Text(
                text = "Distractions: $distractions (leaving costs +5 Shame)",
                color = if (distractions > 0) WarningRed else Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                InteractiveMascot(state = MascotState.STUDYING, size = 150.dp, showArc = false)
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (finished) {
                        onExit()
                    } else {
                        showExitDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (finished) FameGold else Color(0xFF3B2A24)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = if (finished) "Leave the Library" else "Leave early (+5 Shame)",
                    color = if (finished) OnSurfaceDark else Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    if (showLeftDialog) {
        AlertDialog(
            onDismissRequest = { showLeftDialog = false },
            title = { Text("You left the library.", fontWeight = FontWeight.Black) },
            text = { Text("The books saw everything. +5 Shame for abandoning your desk mid-session.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeftDialog = false
                        coroutineScope.launch {
                            repository.addShame(5, "Left the Alone Library mid-session")
                        }
                    }
                ) { Text("Accept my Shame", color = WarningRed, fontWeight = FontWeight.Bold) }
            }
        )
    }
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Abandon your desk?", fontWeight = FontWeight.Black) },
            text = { Text("Leaving early burns +5 Shame and ends the session. The mascot will remember this.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        finished = true
                        economyManager.stopStudySession()
                        coroutineScope.launch {
                            repository.addShame(5, "Abandoned the Alone Library early")
                        }
                        onExit()
                    }
                ) { Text("Leave anyway", color = WarningRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Stay and study", color = FameGold, fontWeight = FontWeight.Bold) }
            }
        )
    }
}

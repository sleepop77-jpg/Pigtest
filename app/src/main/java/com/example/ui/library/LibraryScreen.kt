package com.example.ui.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
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
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.sin

object LibraryIcons {
    val Library: ImageVector by lazy {
        ImageVector.Builder(
            name = "Library",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
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
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
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

data class StickyNote(val id: Long, val text: String, val color: Long)
data class PdfItem(val name: String, val uri: String)

object LibraryStore {
    private const val PREFS = "studyos_library"

    fun loadNotes(ctx: Context): List<StickyNote> {
        val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("notes", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                StickyNote(o.getLong("id"), o.getString("text"), o.getLong("color"))
            }
        } catch (_: Exception) { emptyList() }
    }

    fun saveNotes(ctx: Context, notes: List<StickyNote>) {
        val arr = JSONArray()
        notes.forEach { n ->
            arr.put(JSONObject().put("id", n.id).put("text", n.text).put("color", n.color))
        }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("notes", arr.toString()).apply()
    }

    fun loadPdfs(ctx: Context): List<PdfItem> {
        val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("pdfs", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                PdfItem(o.getString("name"), o.getString("uri"))
            }
        } catch (_: Exception) { emptyList() }
    }

    fun savePdfs(ctx: Context, pdfs: List<PdfItem>) {
        val arr = JSONArray()
        pdfs.forEach { p ->
            arr.put(JSONObject().put("name", p.name).put("uri", p.uri))
        }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("pdfs", arr.toString()).apply()
    }
}

private fun queryDisplayName(ctx: Context, uri: Uri): String? {
    return try {
        ctx.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
        }
    } catch (_: Exception) { null }
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
                    title = { Text("The Library", color = Color.White, fontWeight = FontWeight.Black) },
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(FameGold, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(LibraryIcons.Library, contentDescription = null, tint = Color(0xFF2B0503), modifier = Modifier.size(28.dp))
                        }
                        Column {
                            Text("THE LIBRARY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 2.sp)
                            Text("Your quiet place. Sealed phones only.", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(Brush.horizontalGradient(listOf(PrimaryCoral, FameGold)), RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Alone Library", color = OnSurfaceDark, fontWeight = FontWeight.Black, fontSize = 17.sp)
                            Text(
                                "Total isolation with your sticky notes and PDFs on the desk. Leaving mid-session costs +5 Shame, except when shelving a PDF.",
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
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(Brush.horizontalGradient(listOf(FameGold, PrimaryCoral)), RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Group Library", color = OnSurfaceDark, fontWeight = FontWeight.Black, fontSize = 17.sp)
                            Text(
                                "Study alongside real people. Create a room, share the Zoom link, and suffer through finals together.",
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
                    if (rooms.isNotEmpty()) {
                        Text("Open rooms", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        rooms.forEach { room ->
                            RoomCard(room = room)
                        }
                    }
                    Text(
                        text = "Live shared rooms arrive with the backend. For now, send your Zoom link to friends and join together.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var secondsLeft by remember { mutableIntStateOf(minutes * 60) }
    var distractions by remember { mutableIntStateOf(0) }
    var showLeftDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var leftApp by remember { mutableStateOf(false) }
    var excused by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(LibraryStore.loadNotes(context)) }
    var pdfs by remember { mutableStateOf(LibraryStore.loadPdfs(context)) }
    var showAddNote by remember { mutableStateOf(false) }
    var viewNote by remember { mutableStateOf<StickyNote?>(null) }

    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) { }
            val name = queryDisplayName(context, uri) ?: "PDF"
            pdfs = pdfs + PdfItem(name, uri.toString())
            LibraryStore.savePdfs(context, pdfs)
        }
        excused = false
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                leftApp = true
            }
            if (event == Lifecycle.Event.ON_START && leftApp) {
                leftApp = false
                if (!finished) {
                    if (excused) {
                        excused = false
                    } else {
                        distractions++
                        showLeftDialog = true
                    }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B1210))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.16f)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (finished) "Session complete" else "ALONE LIBRARY",
                color = FameGold,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 2.sp
            )
            Text(
                text = String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 44.sp
            )
            Text(
                text = "Distractions: $distractions",
                color = if (distractions > 0) WarningRed else Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.62f)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFF2A1B16), Color(0xFF170E0B))),
                    size = size
                )
                val winW = w * 0.35f
                val winH = h * 0.40f
                val winX = w * 0.06f
                val winY = h * 0.04f
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
                    for (i in 0 until 12) {
                        val rx = winX + (winW / 12f) * i + 4.dp.toPx()
                        val ry = winY + ((phase * 1.6f + i * 0.07f) % 1f) * winH
                        drawLine(
                            color = Color(0xFF9EC9FF).copy(alpha = 0.5f),
                            start = Offset(rx, ry),
                            end = Offset(rx - 2.dp.toPx(), ry + 7.dp.toPx()),
                            strokeWidth = 1.4.dp.toPx()
                        )
                    }
                } else if (scene == LibraryScene.STARS) {
                    for (i in 0 until 10) {
                        val sx = winX + (winW / 10f) * i + 5.dp.toPx()
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
                            center = Offset(w * 0.78f, h * 0.22f),
                            radius = w * 0.22f
                        ),
                        center = Offset(w * 0.78f, h * 0.22f),
                        radius = w * 0.22f
                    )
                    for (i in 0 until 3) {
                        val fx = w * 0.74f + i * 12.dp.toPx()
                        val fh = (14 + i * 5).dp.toPx() * (0.8f + 0.3f * abs(sin(phase * 6.28f + i)))
                        val p = Path().apply {
                            moveTo(fx - 6.dp.toPx(), h * 0.32f)
                            quadraticTo(fx, h * 0.32f - fh, fx + 6.dp.toPx(), h * 0.32f)
                            close()
                        }
                        drawPath(p, Color(0xFFFF9100).copy(alpha = 0.8f))
                    }
                }
                val deskY = h * 0.78f
                drawRoundRect(
                    color = Color(0xFF4A2B2B),
                    topLeft = Offset(w * 0.06f, deskY),
                    size = Size(w * 0.88f, h * 0.04f),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
                val lampX = w * 0.86f
                drawLine(Color(0xFF223047), Offset(lampX, deskY), Offset(lampX, deskY - h * 0.10f), 3.dp.toPx())
                drawCircle(Color(0xFF223047), 6.dp.toPx(), Offset(lampX, deskY - h * 0.10f))
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(FameGold.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(lampX, deskY - h * 0.08f),
                        radius = w * 0.16f
                    ),
                    center = Offset(lampX, deskY - h * 0.08f),
                    radius = w * 0.16f
                )
                for (i in 0 until 6) {
                    val dx = (w / 6f) * i + 10.dp.toPx()
                    val dy = h * 0.55f + sin(phase * 6.28f + i * 2.1f) * 8.dp.toPx()
                    drawCircle(Color.White.copy(alpha = 0.10f), 1.5.dp.toPx(), Offset(dx, dy))
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 46.dp),
                contentAlignment = Alignment.Center
            ) {
                InteractiveMascot(state = MascotState.STUDYING, size = 120.dp, showArc = false)
            }
            LazyRow(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp, start = 8.dp, end = 56.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .rotate(if (note.id % 2 == 0L) -2.5f else 2.5f)
                            .shadow(6.dp, RoundedCornerShape(6.dp))
                            .background(Color(note.color), RoundedCornerShape(6.dp))
                            .clickable { viewNote = note }
                            .padding(8.dp)
                    ) {
                        Text(
                            note.text,
                            color = Color(0xFF3E2723),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, RoundedCornerShape(22.dp))
                        .background(FameGold, RoundedCornerShape(22.dp))
                        .clickable { showAddNote = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color(0xFF2B0503), fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, RoundedCornerShape(22.dp))
                        .background(Color(0xFF20B2AA), RoundedCornerShape(22.dp))
                        .clickable {
                            excused = true
                            pdfPicker.launch(arrayOf("application/pdf"))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }
            LazyRow(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, end = 60.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pdfs, key = { it.uri }) { pdf ->
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF3B2A24), RoundedCornerShape(10.dp))
                            .clickable {
                                excused = true
                                try {
                                    val uri = Uri.parse(pdf.uri)
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/pdf")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                    )
                                } catch (_: Exception) { excused = false }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("PDF", color = Color(0xFF9EC9FF), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        Text(
                            pdf.name.take(14),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "x",
                            color = WarningRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.clickable {
                                pdfs = pdfs.filterNot { it.uri == pdf.uri }
                                LibraryStore.savePdfs(context, pdfs)
                            }
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.22f)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
                    .height(48.dp)
            ) {
                Text(
                    text = if (finished) "Leave the Library" else "Leave early (+5 Shame)",
                    color = if (finished) OnSurfaceDark else Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }
    }
    if (showAddNote) {
        var noteText by remember { mutableStateOf("") }
        var noteColor by remember { mutableStateOf(0xFFFFF9C4L) }
        AlertDialog(
            onDismissRequest = { showAddNote = false },
            title = { Text("Pin a sticky note", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(0xFFFFF9C4L, 0xFFF8BBD0L, 0xFFC8E6C9L, 0xFFBBDEFBL).forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(c), RoundedCornerShape(6.dp))
                                    .clickable { noteColor = c }
                            ) {
                                if (noteColor == c) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(12.dp)
                                            .background(Color(0xFF3E2723), RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            notes = notes + StickyNote(System.currentTimeMillis(), noteText.take(60), noteColor)
                            LibraryStore.saveNotes(context, notes)
                            showAddNote = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
                ) { Text("Pin it") }
            },
            dismissButton = { TextButton(onClick = { showAddNote = false }) { Text("Cancel") } }
        )
    }
    viewNote?.let { note ->
        AlertDialog(
            onDismissRequest = { viewNote = null },
            title = { Text("Sticky note", fontWeight = FontWeight.Black) },
            text = { Text(note.text) },
            confirmButton = {
                TextButton(
                    onClick = {
                        notes = notes.filterNot { it.id == note.id }
                        LibraryStore.saveNotes(context, notes)
                        viewNote = null
                    }
                ) { Text("Tear it off", color = WarningRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { viewNote = null }) { Text("Keep it") } }
        )
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

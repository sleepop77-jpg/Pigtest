package com.example.ui.lockdown

import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.core.LockdownBustedState
import com.example.core.LockdownManager
import com.example.core.MascotState
import com.example.ui.common.StudyIcons
import com.example.ui.launcher.InteractiveMascot
import com.example.ui.theme.FameGold
import com.example.ui.theme.OnSurfaceDark
import com.example.ui.theme.PrimaryCoral
import com.example.ui.theme.SurfaceCream
import com.example.ui.theme.WarningRed
import androidx.compose.ui.graphics.asImageBitmap

object LockdownIcons {
    val Lock: ImageVector by lazy {
        ImageVector.Builder(
            name = "Lock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(7f, 10f)
                lineTo(7f, 7f)
                curveTo(7f, 4.2f, 9.2f, 2f, 12f, 2f)
                curveTo(14.8f, 2f, 17f, 4.2f, 17f, 7f)
                lineTo(17f, 10f)
                lineTo(19f, 10f)
                lineTo(19f, 21f)
                lineTo(5f, 21f)
                lineTo(5f, 10f)
                close()
            }
        }.build()
    }
}

data class AppEntry(
    val pkg: String,
    val label: String,
    val icon: Drawable
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockdownScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(LockdownManager.isEnabled(context)) }
    var hasAccess by remember { mutableStateOf(LockdownManager.hasUsageAccess(context)) }
    var blocked by remember { mutableStateOf(LockdownManager.blockedPackages(context)) }
    val apps = remember {
        val pm = context.packageManager
        pm.getInstalledApplications(0)
            .mapNotNull { info ->
                if (info.packageName == context.packageName) return@mapNotNull null
                if (pm.getLaunchIntentForPackage(info.packageName) == null) return@mapNotNull null
                AppEntry(info.packageName, pm.getApplicationLabel(info).toString(), pm.getApplicationIcon(info))
            }
            .sortedBy { it.label.lowercase() }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Lockdown", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(StudyIcons.Back, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF241515))
                .padding(pad)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lockdown Mode", color = OnSurfaceDark, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text(
                                "During any focus session, opening a blocked app yanks you back and burns Shame.",
                                color = OnSurfaceDark.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = enabled,
                            onCheckedChange = {
                                enabled = it
                                LockdownManager.setEnabled(context, it)
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = PrimaryCoral)
                        )
                    }
                }
            }
            if (enabled && !hasAccess) {
                item {
                    Button(
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            } catch (_: Exception) { }
                            hasAccess = LockdownManager.hasUsageAccess(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarningRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("Grant usage access (required)", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
            item {
                Text(
                    "Sealed apps (${blocked.size})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            items(apps, key = { it.pkg }) { app ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = app.icon.toBitmap(48, 48).asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                        Text(
                            app.label,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                        )
                        Checkbox(
                            checked = blocked.contains(app.pkg),
                            onCheckedChange = { add ->
                                blocked = if (add) blocked + app.pkg else blocked - app.pkg
                                LockdownManager.setBlockedPackages(context, blocked)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = PrimaryCoral)
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun BustedScreen(onReturn: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF241515)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(28.dp)
        ) {
            Text(
                "BUSTED.",
                color = FameGold,
                fontWeight = FontWeight.Black,
                fontSize = 44.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "You opened ${LockdownBustedState.lastApp} during Lockdown.",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Text(
                "+${LockdownBustedState.lastPenalty} Shame added. The mascot is disappointed.",
                color = WarningRed,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            InteractiveMascot(state = MascotState.FRUSTRATED, size = 170.dp, showArc = false)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onReturn,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Back to my session", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
    }
}

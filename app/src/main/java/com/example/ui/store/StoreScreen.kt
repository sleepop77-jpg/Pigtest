            package com.example.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.core.EquipManager
import com.example.core.TimeBasedThemeManager
import com.example.data.local.entities.StoreItem
import com.example.data.local.entities.Subject
import com.example.data.repository.StudyRepository
import com.example.ui.common.StudyIcons
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    repository: StudyRepository,
    themeManager: TimeBasedThemeManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val totalFame by repository.totalFame.collectAsState(initial = 100)
    val storeItems by repository.allStoreItems.collectAsState(initial = emptyList())
    val subjects by repository.allSubjects.collectAsState(initial = emptyList())
    var snackbarHostState = remember { SnackbarHostState() }
    val isNightMode = themeManager.isDarkThemeActive()
    val backgroundBrush = remember(isNightMode) {
        if (isNightMode) Brush.verticalGradient(listOf(Color(0xFF4A2C2C), Color(0xFF241515)))
        else Brush.verticalGradient(listOf(Color(0xFFD9534F), Color(0xFFC94440)))
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Fame Store", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = StudyIcons.Back, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(FameGold)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = StudyIcons.FameStar, contentDescription = "Fame", tint = OnSurfaceDark, modifier = Modifier.size(16.dp))
                        Text(text = "$totalFame", color = OnSurfaceDark, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(backgroundBrush).padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text(text = "Cosmetics & Perks", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                items(storeItems, key = { it.id }) { item ->
                    StoreItemCard(
                        item = item,
                        userFame = totalFame,
                        subjects = subjects,
                        onBuy = {
                            coroutineScope.launch {
                                val success = repository.purchaseStoreItem(item)
                                if (success) snackbarHostState.showSnackbar("Unlocked ${item.name}! Now tap Equip to wear it.")
                                else snackbarHostState.showSnackbar("Insufficient Fame! Study more to earn Fame.")
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun StoreItemCard(
    item: StoreItem,
    userFame: Int,
    subjects: List<Subject>,
    onBuy: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val canAfford = userFame >= item.costFame
    val equippedMascot by EquipManager.equippedMascot.collectAsState(initial = null)
    val equippedTheme by EquipManager.equippedTheme.collectAsState(initial = null)
    val isEquippable = item.category == "Mascot" || item.category == "Theme"
    val isEquipped = (item.category == "Mascot" && equippedMascot == item.id) ||
            (item.category == "Theme" && equippedTheme == item.id)
    val reqSubject = subjects.firstOrNull { it.id == item.requiredMasterySubject }
    val masteryLocked = item.requiredMasterySubject != null &&
            (reqSubject?.masteryPercent ?: 0) < item.requiredMasteryLevel

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurfaceDark)
                    Text(text = "• ${item.category}", fontSize = 12.sp, color = PrimaryCoralDark, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.description, fontSize = 12.sp, color = OnSurfaceMuted)
                if (masteryLocked) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                        Icon(imageVector = StudyIcons.Lock, contentDescription = null, tint = WarningRed, modifier = Modifier.size(12.dp))
                        Text(text = "Requires ${reqSubject?.name ?: item.requiredMasterySubject} ${item.requiredMasteryLevel}% mastery", fontSize = 11.sp, color = WarningRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            when {
                !item.unlocked -> Button(
                    onClick = onBuy,
                    enabled = canAfford && !masteryLocked,
                    colors = ButtonDefaults.buttonColors(containerColor = FameGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = StudyIcons.FameStar, contentDescription = null, tint = OnSurfaceDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${item.costFame}", color = OnSurfaceDark, fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
                isEquippable -> Button(
                    onClick = { scope.launch { EquipManager.toggle(item.category, item.id) } },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isEquipped) SuccessGreen else AccentTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isEquipped) Icon(imageVector = StudyIcons.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isEquipped) "Equipped" else "Equip", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                else -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(SuccessGreen.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = StudyIcons.Check, contentDescription = "Unlocked", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Text(text = "Active", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}    

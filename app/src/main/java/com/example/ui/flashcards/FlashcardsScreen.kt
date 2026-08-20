package com.example.ui.flashcards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.TimeBasedThemeManager
import com.example.data.local.entities.Flashcard
import com.example.data.local.entities.FlashcardDeck
import com.example.data.repository.StudyRepository
import com.example.ui.common.StudyIcons
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    repository: StudyRepository,
    themeManager: TimeBasedThemeManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val decks by repository.allDecks.collectAsState(initial = emptyList())
    var activeStudyDeck by remember { mutableStateOf<FlashcardDeck?>(null) }
    var showCreateDeckDialog by remember { mutableStateOf(false) }

    val isNightMode = themeManager.isDarkThemeActive()
    val backgroundBrush = remember(isNightMode) {
        if (isNightMode) Brush.verticalGradient(listOf(Color(0xFF4A2C2C), Color(0xFF241515)))
        else Brush.verticalGradient(listOf(Color(0xFFD9534F), Color(0xFFC94440)))
    }

    if (activeStudyDeck != null) {
        FlashcardStudyView(
            deck = activeStudyDeck!!,
            repository = repository,
            onClose = { activeStudyDeck = null }
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Flashcard Mastery",
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
                    onClick = { showCreateDeckDialog = true },
                    containerColor = FameGold,
                    contentColor = OnSurfaceDark,
                    shape = CircleShape
                ) {
                    Icon(imageVector = StudyIcons.Add, contentDescription = "New Deck")
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Subject Decks",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(decks, key = { it.id }) { deck ->
                        DeckCard(
                            deck = deck,
                            onStudy = { activeStudyDeck = deck }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            }
        }
    }

    if (showCreateDeckDialog) {
        var deckTitle by remember { mutableStateOf("") }
        var deckSubject by remember { mutableStateOf("Mathematics") }

        AlertDialog(
            onDismissRequest = { showCreateDeckDialog = false },
            title = { Text("Create Flashcard Deck", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = deckTitle,
                        onValueChange = { deckTitle = it },
                        label = { Text("Deck Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deckSubject,
                        onValueChange = { deckSubject = it },
                        label = { Text("Subject (e.g. Physics)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deckTitle.isNotBlank()) {
                            coroutineScope.launch {
                                val newDeckId = "deck_${System.currentTimeMillis()}"
                                repository.insertDeck(
                                    FlashcardDeck(
                                        id = newDeckId,
                                        title = deckTitle,
                                        subject = deckSubject,
                                        totalCards = 0,
                                        masteryRate = 0
                                    )
                                )
                            }
                            showCreateDeckDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
                ) {
                    Text("Create Deck")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDeckDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DeckCard(
    deck: FlashcardDeck,
    onStudy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStudy() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deck.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = OnSurfaceDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = deck.subject,
                        color = PrimaryCoralDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "•  Mastery: ${deck.masteryRate}%",
                        color = OnSurfaceMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Button(
                onClick = onStudy,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Study", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FlashcardStudyView(
    deck: FlashcardDeck,
    repository: StudyRepository,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val cards by repository.getCardsForDeck(deck.id).collectAsState(initial = emptyList())
    var currentCardIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400),
        label = "card_flip"
    )

    val currentCard = cards.getOrNull(currentCardIndex)

    Scaffold(
        containerColor = PrimaryCoral,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(imageVector = StudyIcons.Close, contentDescription = "Close", tint = Color.White)
                }
                Text(
                    text = "${deck.title} (${currentCardIndex + 1}/${cards.size.coerceAtLeast(1)})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = StudyIcons.FameStar, contentDescription = null, tint = FameGold, modifier = Modifier.size(16.dp))
                    Text("+2/min", color = FameGold, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (cards.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No cards in this deck yet!", color = Color.White, fontSize = 16.sp)
                }
            } else if (currentCard != null) {
                // Interactive 3D Flip Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceCream)
                        .clickable { isFlipped = !isFlipped }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        // Front (Question)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "QUESTION",
                                color = PrimaryCoralDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = currentCard.question,
                                color = OnSurfaceDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "(Tap to flip)",
                                color = OnSurfaceMuted,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        // Back (Answer - rotated 180 to remain readable)
                        Column(
                            modifier = Modifier.graphicsLayer { rotationY = 180f },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "ANSWER",
                                color = SuccessGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = currentCard.answer,
                                color = OnSurfaceDark,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Response Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                repository.recordFlashcardAnswer(currentCard, false, deck.subject)
                            }
                            isFlipped = false
                            if (currentCardIndex < cards.size - 1) currentCardIndex += 1
                            else currentCardIndex = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarningRed),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Need Practice", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                repository.recordFlashcardAnswer(currentCard, true, deck.subject)
                                repository.addFame(2, "Mastered flashcard in ${deck.subject}")
                            }
                            isFlipped = false
                            if (currentCardIndex < cards.size - 1) currentCardIndex += 1
                            else currentCardIndex = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Mastered (+2)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

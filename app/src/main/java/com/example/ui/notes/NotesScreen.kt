package com.example.ui.notes

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.TimeBasedThemeManager
import com.example.data.local.entities.Note
import com.example.data.repository.StudyRepository
import com.example.ui.common.StudyIcons
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    repository: StudyRepository,
    themeManager: TimeBasedThemeManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val notes by repository.allNotes.collectAsState(initial = emptyList())
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    val isNightMode = themeManager.isDarkThemeActive()
    val backgroundBrush = remember(isNightMode) {
        if (isNightMode) Brush.verticalGradient(listOf(Color(0xFF4A2C2C), Color(0xFF241515)))
        else Brush.verticalGradient(listOf(Color(0xFFD9534F), Color(0xFFC94440)))
    }

    if (editingNote != null || isCreatingNew) {
        NoteEditorView(
            initialNote = editingNote,
            onSave = { noteToSave ->
                coroutineScope.launch {
                    if (noteToSave.id == 0L) {
                        repository.insertNote(noteToSave)
                    } else {
                        repository.updateNote(noteToSave)
                    }
                }
                editingNote = null
                isCreatingNew = false
            },
            onDelete = { noteToDelete ->
                coroutineScope.launch {
                    if (noteToDelete.id != 0L) {
                        repository.deleteNote(noteToDelete)
                    }
                }
                editingNote = null
                isCreatingNew = false
            },
            onCancel = {
                editingNote = null
                isCreatingNew = false
            }
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Notes OS",
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
                    onClick = { isCreatingNew = true },
                    containerColor = FameGold,
                    contentColor = OnSurfaceDark,
                    shape = CircleShape
                ) {
                    Icon(imageVector = StudyIcons.Add, contentDescription = "New Note")
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
                if (notes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No notes yet! Tap + to write subject notes.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { editingNote = note }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
        }
    }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = OnSurfaceDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = note.subject,
                    color = PrimaryCoralDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryCoralLight.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Text(
                text = note.content,
                color = OnSurfaceMuted,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditorView(
    initialNote: Note?,
    onSave: (Note) -> Unit,
    onDelete: (Note) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var subject by remember { mutableStateOf(initialNote?.subject ?: "Mathematics") }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }

    Scaffold(
        containerColor = PrimaryCoral,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (initialNote == null) "New Note" else "Edit Note",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(imageVector = StudyIcons.Close, contentDescription = "Cancel", tint = Color.White)
                    }
                },
                actions = {
                    if (initialNote != null) {
                        IconButton(onClick = { onDelete(initialNote) }) {
                            Icon(imageVector = StudyIcons.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    Note(
                                        id = initialNote?.id ?: 0L,
                                        title = title,
                                        subject = subject,
                                        content = content,
                                        lastModified = System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FameGold),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save", color = OnSurfaceDark, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryCoral)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Note Title") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCream,
                    unfocusedContainerColor = SurfaceCream,
                    focusedTextColor = OnSurfaceDark,
                    unfocusedTextColor = OnSurfaceDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject (e.g. Mathematics)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCream,
                    unfocusedContainerColor = SurfaceCream,
                    focusedTextColor = OnSurfaceDark,
                    unfocusedTextColor = OnSurfaceDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Write your notes, formulas, summaries...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCream,
                    unfocusedContainerColor = SurfaceCream,
                    focusedTextColor = OnSurfaceDark,
                    unfocusedTextColor = OnSurfaceDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

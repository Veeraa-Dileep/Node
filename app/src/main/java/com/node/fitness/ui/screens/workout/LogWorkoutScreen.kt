package com.node.fitness.ui.screens.workout

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.node.fitness.ui.components.GlassSurface
import com.node.fitness.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWorkoutScreen(
    onWorkoutComplete: () -> Unit,
    viewModel: LogWorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onWorkoutComplete()
    }

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var showPRCelebration by remember { mutableStateOf(false) }
    var prExerciseName by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.prEvent) {
        viewModel.prEvent.collect {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            prExerciseName = uiState.newPRExercise ?: "Exercise"
            showPRCelebration = true
            delay(2500)
            showPRCelebration = false
        }
    }

    Box {
        if (uiState.showMuscleGroupSelector) {
            MuscleGroupSelector(
                muscleGroups = uiState.muscleGroups,
                catalogExercises = uiState.catalogExercises,
                selectedMuscleGroupId = uiState.selectedMuscleGroupId,
                onSelectMuscleGroup = { viewModel.selectMuscleGroup(it) },
                onSelectExercise = { viewModel.addExerciseFromCatalog(it) },
                onStartWorkout = { viewModel.startWorkout() },
                onCancel = onWorkoutComplete,
                selectedExercises = uiState.exercises
            )
        } else {
            WorkoutLoggingScreen(
                uiState = uiState,
                viewModel = viewModel,
                onWorkoutComplete = onWorkoutComplete
            )
        }

        // PR celebration overlay
        AnimatedVisibility(
            visible = showPRCelebration,
            enter = fadeIn(tween(300)) + slideInVertically(tween(400)) { -it },
            exit = fadeOut(tween(500)),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp)
        ) {
            PRCelebrationBanner(exerciseName = prExerciseName)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MuscleGroupSelector(
    muscleGroups: List<com.node.fitness.data.database.entity.MuscleGroup>,
    catalogExercises: List<com.node.fitness.data.database.entity.ExerciseCatalog>,
    selectedMuscleGroupId: Long?,
    onSelectMuscleGroup: (Long) -> Unit,
    onSelectExercise: (com.node.fitness.data.database.entity.ExerciseCatalog) -> Unit,
    onStartWorkout: () -> Unit,
    onCancel: () -> Unit,
    selectedExercises: List<ExerciseUiState>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Exercises", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Close, "Cancel", tint = TextSecondary)
                    }
                },
                actions = {
                    if (selectedExercises.isNotEmpty()) {
                        Button(
                            onClick = onStartWorkout,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentOrange,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Start (${selectedExercises.size})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Muscle group chips
            Text(
                "MUSCLE GROUP",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(muscleGroups) { group ->
                    FilterChip(
                        selected = group.id == selectedMuscleGroupId,
                        onClick = { onSelectMuscleGroup(group.id) },
                        label = { Text(group.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentOrange,
                            selectedLabelColor = Color.Black,
                            containerColor = SurfaceDark,
                            labelColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Exercise list
            if (selectedMuscleGroupId != null) {
                Text(
                    "EXERCISES",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(catalogExercises) { exercise ->
                        val isSelected = selectedExercises.any { it.name == exercise.name }
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) AccentOrange.copy(alpha = 0.15f) else SurfaceDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isSelected) onSelectExercise(exercise)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    exercise.name,
                                    color = if (isSelected) AccentOrange else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = AccentOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.FitnessCenter,
                            contentDescription = null,
                            tint = TextDisabled,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Select a muscle group to see exercises",
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutLoggingScreen(
    uiState: WorkoutUiState,
    viewModel: LogWorkoutViewModel,
    onWorkoutComplete: () -> Unit
) {
    var showDiscardDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Timer,
                            contentDescription = "Timer",
                            tint = AccentOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = formatDuration(uiState.elapsedSeconds),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showDiscardDialog = true }) {
                        Icon(Icons.Filled.Close, "Discard", tint = TextSecondary)
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.finishWorkout() },
                        enabled = !uiState.isSaving && uiState.exercises.any { it.name.isNotBlank() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentOrange,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.Black
                            )
                        } else {
                            Text("Finish", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.addExercise() },
                containerColor = AccentOrange,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add Exercise")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = TextDisabled,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Tap + to add your first exercise",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                itemsIndexed(
                    items = uiState.exercises,
                    key = { index, _ -> index }
                ) { exerciseIndex, exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        exerciseIndex = exerciseIndex,
                        onNameChange = { viewModel.updateExerciseName(exerciseIndex, it) },
                        onAddSet = { viewModel.addSet(exerciseIndex) },
                        onWeightChange = { setIdx, w -> viewModel.updateSetWeight(exerciseIndex, setIdx, w) },
                        onRepsChange = { setIdx, r -> viewModel.updateSetReps(exerciseIndex, setIdx, r) },
                        onRemoveSet = { setIdx -> viewModel.removeSet(exerciseIndex, setIdx) },
                        onRemoveExercise = { viewModel.removeExercise(exerciseIndex) },
                        onToggleExpand = { viewModel.toggleExerciseExpanded(exerciseIndex) }
                    )
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Workout?", color = TextPrimary) },
            text = { Text("This workout will not be saved.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    viewModel.discardWorkout()
                }) {
                    Text("Discard", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Going", color = AccentOrange)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun ExerciseCard(
    exercise: ExerciseUiState,
    exerciseIndex: Int,
    onNameChange: (String) -> Unit,
    onAddSet: () -> Unit,
    onWeightChange: (Int, String) -> Unit,
    onRepsChange: (Int, String) -> Unit,
    onRemoveSet: (Int) -> Unit,
    onRemoveExercise: () -> Unit,
    onToggleExpand: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = exercise.name,
                    onValueChange = onNameChange,
                    placeholder = {
                        Text("Exercise name", color = TextDisabled)
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentOrange,
                        unfocusedBorderColor = CardBorder,
                        cursorColor = AccentOrange,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        if (exercise.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Toggle",
                        tint = TextSecondary
                    )
                }
                IconButton(onClick = onRemoveExercise) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = ErrorRed)
                }
            }

            AnimatedVisibility(visible = exercise.isExpanded) {
                Column {
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("SET", style = MaterialTheme.typography.labelSmall, color = TextDisabled, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        Text("KG", style = MaterialTheme.typography.labelSmall, color = TextDisabled, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("REPS", style = MaterialTheme.typography.labelSmall, color = TextDisabled, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Spacer(Modifier.width(40.dp))
                    }

                    Spacer(Modifier.height(8.dp))

                    exercise.sets.forEachIndexed { setIndex, set ->
                        SetRow(
                            set = set,
                            onWeightChange = { onWeightChange(setIndex, it) },
                            onRepsChange = { onRepsChange(setIndex, it) },
                            onRemove = { onRemoveSet(setIndex) }
                        )
                        Spacer(Modifier.height(6.dp))
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick = onAddSet,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Set", color = AccentOrange)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetRow(
    set: SetUiState,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                SurfaceVariantDark,
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = set.setNumber.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = AccentOrange,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = set.weight,
            onValueChange = onWeightChange,
            placeholder = { Text("0", color = TextDisabled, fontSize = 14.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .semantics { contentDescription = "Set ${set.setNumber} weight" },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentOrange,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = AccentOrange,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            shape = RoundedCornerShape(8.dp)
        )

        OutlinedTextField(
            value = set.reps,
            onValueChange = onRepsChange,
            placeholder = { Text("0", color = TextDisabled, fontSize = 14.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .semantics { contentDescription = "Set ${set.setNumber} reps" },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentOrange,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = AccentOrange,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            shape = RoundedCornerShape(8.dp)
        )

        if (set.isPersonalRecord) {
            Icon(
                Icons.Filled.EmojiEvents,
                contentDescription = "PR!",
                tint = AccentOrangeEnd,
                modifier = Modifier.size(24.dp)
            )
        } else {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove set",
                    tint = TextDisabled,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format("%d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}

@Composable
private fun PRCelebrationBanner(exerciseName: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pr_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    GlassSurface(
        cornerRadius = 20.dp,
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .scale(pulseScale)
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            AccentOrangeStart.copy(alpha = 0.3f * glowAlpha),
                            AccentOrangeEnd.copy(alpha = 0.3f * glowAlpha)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("🏆", fontSize = 28.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "NEW PR!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange
                )
                Text(
                    exerciseName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

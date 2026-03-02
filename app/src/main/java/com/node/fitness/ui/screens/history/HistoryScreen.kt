package com.node.fitness.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.node.fitness.data.database.dao.WorkoutWithExercises
import com.node.fitness.data.database.entity.WorkoutSession
import com.node.fitness.ui.components.GlassSurface
import com.node.fitness.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Workout History",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            IconButton(onClick = { viewModel.toggleCalendarView() }) {
                Icon(
                    if (uiState.isCalendarView) Icons.Filled.ViewList else Icons.Filled.CalendarMonth,
                    contentDescription = "Toggle view",
                    tint = AccentOrange
                )
            }
        }

        // ── Search & Calendar ──
        AnimatedVisibility(visible = !uiState.isCalendarView) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by exercise...", color = TextDisabled) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentOrange,
                    unfocusedBorderColor = CardBorder,
                    cursorColor = AccentOrange,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
        AnimatedVisibility(visible = uiState.isCalendarView) {
            HistoryCalendar(
                workoutDates = uiState.workoutDates,
                onDateSelected = { viewModel.selectDate(it) }
            )
        }
        Spacer(Modifier.height(16.dp))

        // ── Content ──
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentOrange)
            }
        } else if (uiState.sessions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        tint = TextDisabled,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (uiState.isCalendarView && uiState.selectedDate != null) "No workouts on this day" else "No workouts found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(
                    items = uiState.sessions,
                    key = { it.session.id }
                ) { workoutWithExercises ->
                    WorkoutHistoryCard(
                        data = workoutWithExercises,
                        isSelected = uiState.selectedSession?.id == workoutWithExercises.session.id,
                        onClick = {
                            if (uiState.selectedSession?.id == workoutWithExercises.session.id) {
                                viewModel.clearSelection()
                            } else {
                                viewModel.selectSession(workoutWithExercises.session)
                            }
                        }
                    )
                }
            }
        }
    }

    // ── Detail Bottom Sheet ──
    if (uiState.selectedSession != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelection() },
            containerColor = SurfaceDark,
            contentColor = TextPrimary
        ) {
            val selectedSession = uiState.selectedSession
            if (selectedSession != null) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        SimpleDateFormat("EEEE, MMM d yyyy", Locale.getDefault())
                            .format(Date(selectedSession.date)),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        formatDuration(selectedSession.durationSeconds),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(20.dp))

                    uiState.selectedExercisesWithSets.forEach { exerciseWithSets ->
                        Text(
                            exerciseWithSets.exercise.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = AccentOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        exerciseWithSets.sets.forEach { set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, bottom = 4.dp)
                            ) {
                                Text(
                                    "Set ${set.setNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextDisabled,
                                    modifier = Modifier.width(56.dp)
                                )
                                Text(
                                    "${set.weight} kg × ${set.reps}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (set.isPersonalRecord) {
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        Icons.Filled.EmojiEvents,
                                        contentDescription = "PR",
                                        tint = AccentOrangeEnd,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryCard(
    data: WorkoutWithExercises,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val session = data.session
    val dateFormatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceVariantDark else SurfaceDark
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(
                        if (isSelected) AccentOrange.copy(alpha = 0.15f) else SurfaceVariantDark,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                val cal = Calendar.getInstance().apply { timeInMillis = session.date }
                Text(
                    SimpleDateFormat("MMM", Locale.getDefault()).format(Date(session.date)).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentOrange
                )
                Text(
                    cal.get(Calendar.DAY_OF_MONTH).toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    data.exercises.joinToString(", ") { it.name }.ifBlank { "Workout" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Row {
                    Text(
                        "${data.exercises.size} exercises",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Text(" · ", color = TextDisabled)
                    Text(
                        formatDuration(session.durationSeconds),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextDisabled
            )
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    return if (m >= 60) "${m / 60}h ${m % 60}m" else "${m}m"
}

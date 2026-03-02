package com.node.fitness.ui.screens.analytics

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.node.fitness.data.database.entity.PersonalRecord
import com.node.fitness.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentOrange)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // ── Streaks Row ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Filled.LocalFireDepartment,
                    value = "${uiState.currentStreak}",
                    label = "Day Streak",
                    color = AccentOrange,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Filled.EmojiEvents,
                    value = "${uiState.prStreak}",
                    label = "PR Streak",
                    color = AccentOrangeEnd,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Filled.FitnessCenter,
                    value = "${uiState.totalWorkouts}",
                    label = "Workouts",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Weekly Volume by Muscle ──
        if (uiState.weeklyVolumeByMuscle.isNotEmpty()) {
            item {
                SectionHeader("Weekly Volume by Muscle")
            }
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val maxVolume = uiState.weeklyVolumeByMuscle.maxOfOrNull { it.totalVolume } ?: 1.0
                        uiState.weeklyVolumeByMuscle.forEach { entry ->
                            VolumeBar(
                                label = entry.muscleGroup,
                                value = entry.totalVolume,
                                maxValue = maxVolume
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // ── Muscle Frequency (Last 30 Days) ──
        if (uiState.muscleFrequency.isNotEmpty()) {
            item {
                SectionHeader("Muscle Frequency (30 Days)")
            }
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        uiState.muscleFrequency.forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(entry.muscleGroup, color = TextPrimary)
                                Text(
                                    "${entry.sessionCount} sessions",
                                    color = AccentOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Body Composition ──
        if (uiState.latestWeight != null || uiState.leanBodyMass != null) {
            item {
                SectionHeader("Body Composition")
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.latestWeight?.let { weight ->
                        StatCard(
                            icon = Icons.Filled.Scale,
                            value = String.format("%.1f", weight),
                            label = "Body Weight (kg)",
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    uiState.leanBodyMass?.let { lbm ->
                        StatCard(
                            icon = Icons.Filled.Person,
                            value = String.format("%.1f", lbm),
                            label = "Lean Mass (kg)",
                            color = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ── Recent PRs ──
        if (uiState.recentPRs.isNotEmpty()) {
            item {
                SectionHeader("Recent PRs")
            }
            items(uiState.recentPRs) { pr ->
                PRCard(pr)
            }
        }

        // ── Workout Heatmap ──
        if (uiState.workoutDates.isNotEmpty()) {
            item {
                SectionHeader("Activity (Last 12 Weeks)")
            }
            item {
                WorkoutHeatmap(dates = uiState.workoutDates)
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun VolumeBar(label: String, value: Double, maxValue: Double) {
    val fraction = (value / maxValue).toFloat().coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(
                String.format("%.0f kg", value),
                style = MaterialTheme.typography.bodyMedium,
                color = AccentOrange,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(SurfaceVariantDark, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .background(AccentOrange, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun PRCard(pr: PersonalRecord) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = AccentOrangeEnd,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pr.exerciseName, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    buildString {
                        append(pr.prType)
                        append(" • ")
                        append(SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(pr.date)))
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDisabled
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${pr.weight} kg × ${pr.reps}",
                    color = AccentOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (pr.estimated1RM > 0) {
                    Text(
                        "Est. 1RM: ${String.format("%.1f", pr.estimated1RM)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutHeatmap(dates: List<Long>) {
    val cal = Calendar.getInstance()
    val today = cal.timeInMillis
    cal.add(Calendar.WEEK_OF_YEAR, -12)
    val startDate = cal.timeInMillis

    // Count workouts per day
    val dayCounts = mutableMapOf<Long, Int>()
    dates.filter { it >= startDate }.forEach { date ->
        cal.timeInMillis = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val dayKey = cal.timeInMillis
        dayCounts[dayKey] = (dayCounts[dayKey] ?: 0) + 1
    }

    // Build 12 weeks × 7 days grid
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Day labels
            val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

            Row(modifier = Modifier.fillMaxWidth()) {
                // Day label column
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    dayLabels.forEach { label ->
                        Box(
                            modifier = Modifier.size(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, fontSize = 8.sp, color = TextDisabled)
                        }
                    }
                }
                Spacer(Modifier.width(4.dp))
                // Weeks
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    cal.timeInMillis = startDate
                    // Align to Monday
                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    val mondayOffset = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek
                    cal.add(Calendar.DAY_OF_YEAR, mondayOffset)

                    for (week in 0 until 12) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (day in 0 until 7) {
                                val dayTime = cal.timeInMillis
                                cal.set(Calendar.HOUR_OF_DAY, 0)
                                cal.set(Calendar.MINUTE, 0)
                                cal.set(Calendar.SECOND, 0)
                                cal.set(Calendar.MILLISECOND, 0)
                                val count = dayCounts[cal.timeInMillis] ?: 0
                                val color = when {
                                    dayTime > today -> Color.Transparent
                                    count >= 2 -> AccentOrange
                                    count == 1 -> AccentOrange.copy(alpha = 0.4f)
                                    else -> SurfaceVariantDark
                                }
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(color, RoundedCornerShape(3.dp))
                                )
                                cal.add(Calendar.DAY_OF_YEAR, 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = AccentOrange,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
    )
}

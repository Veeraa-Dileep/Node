package com.node.fitness.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.node.fitness.ui.components.GlassSurface
import com.node.fitness.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    onNavigateToLogWorkout: () -> Unit,
    onNavigateToProgress: (Int) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Greeting Header ──
        item {
            Text(
                text = uiState.greeting,
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Text(
                text = "Let's crush it today 💪",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // ── Stats Summary Card ──
        item {
            GlassSurface(
                cornerRadius = 20.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "This Week",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            value = uiState.workoutsThisWeek.toString(),
                            label = "Workouts",
                            icon = Icons.Filled.FitnessCenter
                        )
                        StatItem(
                            value = formatVolume(uiState.totalVolumeThisWeek),
                            label = "Volume",
                            icon = Icons.Filled.Scale
                        )
                        StatItem(
                            value = formatMinutes(uiState.totalDurationThisWeek),
                            label = "Time",
                            icon = Icons.Filled.Timer
                        )
                    }
                }
            }
        }

        // ── Quick Actions ──
        item {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    label = "Log Workout",
                    icon = Icons.Filled.Add,
                    gradient = listOf(AccentOrangeStart, AccentOrangeEnd),
                    onClick = onNavigateToLogWorkout,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    label = "Add Weight",
                    icon = Icons.Filled.MonitorWeight,
                    gradient = listOf(SuccessGreen, Color(0xFF66BB6A)),
                    onClick = { onNavigateToProgress(1) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    label = "Add Photo",
                    icon = Icons.Filled.CameraAlt,
                    gradient = listOf(Color(0xFF42A5F5), Color(0xFF1E88E5)),
                    onClick = { onNavigateToProgress(3) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Latest Body Weight ──
        uiState.latestWeight?.let { weight ->
            item {
                GlassSurface(
                    cornerRadius = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.MonitorWeight,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "Current Weight",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Text(
                                "${weight.weight} kg",
                                style = MaterialTheme.typography.displayMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ── Recent PRs ──
        if (uiState.recentPRs.isNotEmpty()) {
            item {
                Text(
                    "Recent PRs 🏆",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.recentPRs) { pr ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                            modifier = Modifier.width(160.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Icon(
                                    Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    tint = AccentOrangeEnd,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    pr.exerciseName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Text(
                                    "${pr.weight} kg × ${pr.reps}",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = AccentOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Text(
                                    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(pr.date)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextDisabled
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Last Workout ──
        uiState.lastWorkout?.let { session ->
            item {
                Text(
                    "Last Workout",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))
                GlassSurface(
                    cornerRadius = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    brush = Brush.linearGradient(listOf(AccentOrangeStart, AccentOrangeEnd)),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.FitnessCenter,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date(session.date)),
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                formatMinutes(session.durationSeconds) + " workout",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Bottom spacer
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun StatItem(value: String, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.displayMedium,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(gradient),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(6.dp))
                Text(label, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

private fun formatVolume(volume: Double): String {
    return when {
        volume >= 1000 -> String.format("%.1fk", volume / 1000)
        else -> String.format("%.0f", volume)
    }
}

private fun formatMinutes(seconds: Int): String {
    val m = seconds / 60
    return if (m >= 60) "${m / 60}h ${m % 60}m" else "${m}m"
}

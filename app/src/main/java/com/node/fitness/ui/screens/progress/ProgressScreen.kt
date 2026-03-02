package com.node.fitness.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.node.fitness.data.database.entity.BodyMeasurement
import com.node.fitness.data.database.entity.BodyWeight
import com.node.fitness.data.database.entity.PersonalRecord
import com.node.fitness.data.database.entity.ProgressPhoto
import com.node.fitness.ui.screens.analytics.AnalyticsScreen
import com.node.fitness.data.repository.BodyDataRepository
import com.node.fitness.data.repository.PRRepository
import com.node.fitness.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ── ViewModel ──

data class ProgressUiState(
    val prs: List<PersonalRecord> = emptyList(),
    val weights: List<BodyWeight> = emptyList(),
    val measurements: List<BodyMeasurement> = emptyList(),
    val photos: List<ProgressPhoto> = emptyList(),
    val selectedTab: Int = 0
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val prRepo: PRRepository,
    private val bodyDataRepo: BodyDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { prRepo.getAllRecords().collect { prs -> _uiState.update { it.copy(prs = prs) } } }
        viewModelScope.launch { bodyDataRepo.getAllWeights().collect { w -> _uiState.update { it.copy(weights = w) } } }
        viewModelScope.launch { bodyDataRepo.getAllMeasurements().collect { m -> _uiState.update { it.copy(measurements = m) } } }
        viewModelScope.launch { bodyDataRepo.getAllPhotos().collect { p -> _uiState.update { it.copy(photos = p) } } }
    }

    fun selectTab(index: Int) { _uiState.update { it.copy(selectedTab = index) } }

    // Body weight entry
    fun addWeight(value: Double) {
        viewModelScope.launch { bodyDataRepo.addWeight(BodyWeight(weight = value)) }
    }

    fun addMeasurement(chest: Double, arms: Double, waist: Double, thighs: Double) {
        viewModelScope.launch {
            bodyDataRepo.addMeasurement(BodyMeasurement(chest = chest, arms = arms, waist = waist, thighs = thighs))
        }
    }
}

// ── Screen ──

@Composable
fun ProgressScreen(
    initialTab: Int = 0,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("PRs", "Weight", "Measurements", "Photos", "Analytics")

    LaunchedEffect(initialTab) {
        if (initialTab != uiState.selectedTab) {
            viewModel.selectTab(initialTab)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Text(
            "Progress",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
        )

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = uiState.selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = AccentOrange,
            edgePadding = 20.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                    color = AccentOrange
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedTab == index,
                    onClick = { viewModel.selectTab(index) },
                    text = {
                        Text(
                            title,
                            fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.selectedTab == index) AccentOrange else TextSecondary
                        )
                    }
                )
            }
        }

        // Tab content
        when (uiState.selectedTab) {
            0 -> PRsTab(uiState.prs)
            1 -> WeightTab(uiState.weights, onAdd = { viewModel.addWeight(it) })
            2 -> MeasurementsTab(uiState.measurements, onAdd = { c, a, w, t -> viewModel.addMeasurement(c, a, w, t) })
            3 -> ProgressPhotosRoute()
            4 -> AnalyticsScreen()
        }
    }
}

@Composable
private fun PRsTab(prs: List<PersonalRecord>) {
    if (prs.isEmpty()) {
        EmptyState("No PRs yet", "Complete workouts to see your records here")
    } else {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(prs) { pr ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = AccentOrangeEnd,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pr.exerciseName, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(
                                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(pr.date)),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextDisabled
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${pr.weight} kg × ${pr.reps}",
                                color = AccentOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Vol: ${String.format("%.0f", pr.maxVolume)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightTab(weights: List<BodyWeight>, onAdd: (Double) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Add button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            FilledTonalButton(
                onClick = { showDialog = true },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = SurfaceVariantDark,
                    contentColor = AccentOrange
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Log Weight")
            }
        }

        if (weights.isEmpty()) {
            EmptyState("No weight entries", "Start tracking your body weight")
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(weights) { w ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${w.weight} kg",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(w.date)),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Log Weight", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentOrange,
                        unfocusedBorderColor = CardBorder,
                        cursorColor = AccentOrange,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    weightInput.toDoubleOrNull()?.let { onAdd(it) }
                    weightInput = ""
                    showDialog = false
                }) { Text("Save", color = AccentOrange) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun MeasurementsTab(
    measurements: List<BodyMeasurement>,
    onAdd: (Double, Double, Double, Double) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var chest by remember { mutableStateOf("") }
    var arms by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var thighs by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            FilledTonalButton(
                onClick = { showDialog = true },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = SurfaceVariantDark,
                    contentColor = AccentOrange
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Log Measurements")
            }
        }

        if (measurements.isEmpty()) {
            EmptyState("No measurements yet", "Track your body measurements monthly")
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(measurements) { m ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(m.date)),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MeasurementValue("Chest", m.chest)
                                MeasurementValue("Arms", m.arms)
                                MeasurementValue("Waist", m.waist)
                                MeasurementValue("Thighs", m.thighs)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Log Measurements (cm)", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Chest" to chest, "Arms" to arms, "Waist" to waist, "Thighs" to thighs).forEach { (label, value) ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = {
                                when (label) {
                                    "Chest" -> chest = it; "Arms" -> arms = it
                                    "Waist" -> waist = it; "Thighs" -> thighs = it
                                }
                            },
                            label = { Text(label) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentOrange,
                                unfocusedBorderColor = CardBorder,
                                cursorColor = AccentOrange,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onAdd(
                        chest.toDoubleOrNull() ?: 0.0,
                        arms.toDoubleOrNull() ?: 0.0,
                        waist.toDoubleOrNull() ?: 0.0,
                        thighs.toDoubleOrNull() ?: 0.0
                    )
                    chest = ""; arms = ""; waist = ""; thighs = ""
                    showDialog = false
                }) { Text("Save", color = AccentOrange) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = SurfaceDark
        )
    }
}



@Composable
private fun MeasurementValue(label: String, value: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            String.format("%.1f", value),
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = TextDisabled,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextDisabled)
        }
    }
}

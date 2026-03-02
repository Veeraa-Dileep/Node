package com.node.fitness.ui.screens.body

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.node.fitness.data.database.entity.BodyMeasurement
import com.node.fitness.ui.theme.*
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.FloatEntry

import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMeasurementsScreen(
    viewModel: BodyMeasurementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEntryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Body Measurements", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showEntryDialog = true },
                containerColor = AccentOrange,
                contentColor = Color.Black
            ) {
                Icon(Icons.Filled.Add, "Add Measurements")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentOrange)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Summary card
                item {
                    MeasurementsSummaryCard(latest = uiState.latestMeasurement)
                }

                // Graphs for each measurement
                if (uiState.measurements.isNotEmpty()) {
                    item {
                        MeasurementGraphCard(
                            title = "Chest",
                            measurements = uiState.measurements,
                            selector = { it.chest }
                        )
                    }
                    item {
                        MeasurementGraphCard(
                            title = "Arms",
                            measurements = uiState.measurements,
                            selector = { it.arms }
                        )
                    }
                    item {
                        MeasurementGraphCard(
                            title = "Waist",
                            measurements = uiState.measurements,
                            selector = { it.waist }
                        )
                    }
                    item {
                        MeasurementGraphCard(
                            title = "Thighs",
                            measurements = uiState.measurements,
                            selector = { it.thighs }
                        )
                    }
                }

                // History
                item {
                    Text(
                        "History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(items = uiState.measurements, key = { it.id }) { measurement ->
                    MeasurementHistoryItem(
                        measurement = measurement,
                        onDelete = { viewModel.deleteMeasurement(measurement) }
                    )
                }
            }
        }
    }

    if (showEntryDialog) {
        MeasurementEntryDialog(
            chest = uiState.entryChest,
            arms = uiState.entryArms,
            waist = uiState.entryWaist,
            thighs = uiState.entryThighs,
            onChestChange = { viewModel.setEntryChest(it) },
            onArmsChange = { viewModel.setEntryArms(it) },
            onWaistChange = { viewModel.setEntryWaist(it) },
            onThighsChange = { viewModel.setEntryThighs(it) },
            isSaving = uiState.isSaving,
            onSave = {
                viewModel.saveMeasurement()
                showEntryDialog = false
            },
            onDismiss = { showEntryDialog = false }
        )
    }
}

@Composable
private fun MeasurementsSummaryCard(latest: BodyMeasurement?) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Latest Measurements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (latest != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MeasurementItem("Chest", latest.chest)
                    MeasurementItem("Arms", latest.arms)
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MeasurementItem("Waist", latest.waist)
                    MeasurementItem("Thighs", latest.thighs)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(latest.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDisabled,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Text(
                    "No measurements yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun MeasurementItem(label: String, value: Double) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            "${value.takeIf { it > 0.0 }?.let { "%.1f cm".format(it) } ?: "—"}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AccentOrange
        )
    }
}

@Composable
private fun MeasurementGraphCard(
    title: String,
    measurements: List<BodyMeasurement>,
    selector: (BodyMeasurement) -> Double
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val chartEntries = measurements.reversed()
                .mapIndexed { index, measurement ->
                    val value = selector(measurement)
                    if (value > 0.0) FloatEntry(index.toFloat(), value.toFloat()) else null
                }
                .filterNotNull()

            if (chartEntries.isNotEmpty()) {
                val chartModel = entryModelOf(chartEntries)
                Chart(
                    chart = lineChart(),
                    model = chartModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data", color = TextDisabled)
                }
            }
        }
    }
}

@Composable
private fun MeasurementHistoryItem(
    measurement: BodyMeasurement,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (measurement.chest > 0) Text("Chest: ${measurement.chest} cm", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    if (measurement.arms > 0) Text("Arms: ${measurement.arms} cm", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (measurement.waist > 0) Text("Waist: ${measurement.waist} cm", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    if (measurement.thighs > 0) Text("Thighs: ${measurement.thighs} cm", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(measurement.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "Delete", tint = ErrorRed)
            }
        }
    }
}

@Composable
private fun MeasurementEntryDialog(
    chest: String,
    arms: String,
    waist: String,
    thighs: String,
    onChestChange: (String) -> Unit,
    onArmsChange: (String) -> Unit,
    onWaistChange: (String) -> Unit,
    onThighsChange: (String) -> Unit,
    isSaving: Boolean,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Measurements", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = chest,
                    onValueChange = onChestChange,
                    label = { Text("Chest (cm)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentOrange,
                        unfocusedBorderColor = CardBorder,
                        cursorColor = AccentOrange,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = arms,
                    onValueChange = onArmsChange,
                    label = { Text("Arms (cm)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentOrange,
                        unfocusedBorderColor = CardBorder,
                        cursorColor = AccentOrange,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = waist,
                    onValueChange = onWaistChange,
                    label = { Text("Waist (cm)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentOrange,
                        unfocusedBorderColor = CardBorder,
                        cursorColor = AccentOrange,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = thighs,
                    onValueChange = onThighsChange,
                    label = { Text("Thighs (cm)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentOrange,
                        unfocusedBorderColor = CardBorder,
                        cursorColor = AccentOrange,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                } else {
                    Text("Save", color = Color.Black)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}

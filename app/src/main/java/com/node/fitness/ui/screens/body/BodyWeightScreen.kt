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
import com.node.fitness.data.database.entity.BodyWeight
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
fun BodyWeightScreen(
    viewModel: BodyWeightViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEntryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Body Weight", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showEntryDialog = true },
                containerColor = AccentOrange,
                contentColor = Color.Black
            ) {
                Icon(Icons.Filled.Add, "Add Weight")
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
                // Current weight card
                item {
                    WeightSummaryCard(latestWeight = uiState.latestWeight)
                }

                // Line graph
                if (uiState.weights.isNotEmpty()) {
                    item {
                        WeightGraphCard(weights = uiState.weights)
                    }
                }

                // Weight history
                item {
                    Text(
                        "History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(items = uiState.weights, key = { it.id }) { weight ->
                    WeightHistoryItem(
                        weight = weight,
                        onDelete = { viewModel.deleteWeight(weight) }
                    )
                }
            }
        }
    }

    if (showEntryDialog) {
        WeightEntryDialog(
            currentWeight = uiState.entryWeight,
            onWeightChange = { viewModel.setEntryWeight(it) },
            isSaving = uiState.isSaving,
            onSave = {
                viewModel.saveWeight()
                showEntryDialog = false
            },
            onDismiss = { showEntryDialog = false }
        )
    }
}

@Composable
private fun WeightSummaryCard(latestWeight: BodyWeight?) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Current Weight",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                latestWeight?.let { "${it.weight} kg" } ?: "— kg",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = AccentOrange
            )
            latestWeight?.let {
                Text(
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDisabled
                )
            }
        }
    }
}

@Composable
private fun WeightGraphCard(weights: List<BodyWeight>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val chartEntries = weights.reversed().mapIndexed { index, weight ->
                FloatEntry(index.toFloat(), weight.weight.toFloat())
            }

            if (chartEntries.isNotEmpty()) {
                val chartModel = entryModelOf(chartEntries)
                Chart(
                    chart = lineChart(),
                    model = chartModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

@Composable
private fun WeightHistoryItem(
    weight: BodyWeight,
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
            Column {
                Text(
                    "${weight.weight} kg",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(weight.date)),
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
private fun WeightEntryDialog(
    currentWeight: String,
    onWeightChange: (String) -> Unit,
    isSaving: Boolean,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Weight Entry", color = TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = currentWeight,
                    onValueChange = onWeightChange,
                    label = { Text("Weight (kg)", color = TextSecondary) },
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
                enabled = !isSaving && currentWeight.toDoubleOrNull() != null,
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

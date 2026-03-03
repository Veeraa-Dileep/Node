package com.node.fitness.ui.screens.settings

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.node.fitness.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current as Activity

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            val stream = context.contentResolver.openOutputStream(it)
            if (stream != null) {
                viewModel.exportData(stream)
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val stream = context.contentResolver.openInputStream(it)
            if (stream != null) {
                viewModel.importData(stream)
            }
        }
    }

    // Show success/error snackbar
    LaunchedEffect(uiState.exportSuccess) {
        uiState.exportSuccess?.let { success ->
            val msg = if (success) "Data exported successfully" else (uiState.statusMessage ?: "Export failed")
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearExportStatus()
        }
    }

    LaunchedEffect(uiState.importSuccess) {
        uiState.importSuccess?.let { success ->
            val msg = if (success) "Data restored successfully" else (uiState.statusMessage ?: "Import failed")
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearImportStatus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(24.dp))

        // ── Appearance ──
        SectionHeader("Appearance")
        SettingsToggle(
            icon = Icons.Filled.DarkMode,
            title = "Dark Mode",
            subtitle = "Optimized for gym use",
            checked = uiState.isDarkMode,
            onCheckedChange = { viewModel.toggleDarkMode() }
        )

        Spacer(Modifier.height(24.dp))

        // ── Reminders ──
        SectionHeader("Reminders")
        SettingsToggle(
            icon = Icons.Filled.Scale,
            title = "Weekly Weigh-In",
            subtitle = "Remind every 7 days",
            checked = uiState.isWeightReminderEnabled,
            onCheckedChange = { viewModel.toggleWeightReminder() }
        )
        SettingsToggle(
            icon = Icons.Filled.Straighten,
            title = "Monthly Measurements",
            subtitle = "Remind every 30 days",
            checked = uiState.isMeasurementsReminderEnabled,
            onCheckedChange = { viewModel.toggleMeasurementsReminder() }
        )
        SettingsToggle(
            icon = Icons.Filled.CameraAlt,
            title = "Progress Photos",
            subtitle = "Remind every 4 weeks",
            checked = uiState.isPhotosReminderEnabled,
            onCheckedChange = { viewModel.togglePhotosReminder() }
        )

        Spacer(Modifier.height(24.dp))

        // ── Data ──
        SectionHeader("Data")
        SettingsAction(
            icon = Icons.Filled.Upload,
            title = "Backup Data",
            subtitle = "Export as JSON file",
            onClick = { exportLauncher.launch("node_backup.json") },
            isLoading = uiState.isExporting
        )
        SettingsAction(
            icon = Icons.Filled.Download,
            title = "Restore Data",
            subtitle = "Import from backup file",
            onClick = { importLauncher.launch(arrayOf("application/json")) },
            isLoading = uiState.isImporting
        )

        Spacer(Modifier.height(24.dp))

        // ── About ──
        SectionHeader("About")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Node", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = TextDisabled)
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = AccentOrange,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = TextSecondary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextDisabled)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentOrange,
                checkedTrackColor = AccentOrangeStart.copy(alpha = 0.3f),
                uncheckedThumbColor = TextDisabled,
                uncheckedTrackColor = SurfaceVariantDark
            )
        )
    }
}

@Composable
private fun SettingsAction(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = TextSecondary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextDisabled)
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = AccentOrange
            )
        } else {
            IconButton(onClick = onClick) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Open", tint = TextDisabled)
            }
        }
    }
}

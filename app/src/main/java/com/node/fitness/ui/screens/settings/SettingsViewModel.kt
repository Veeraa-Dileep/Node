package com.node.fitness.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.node.fitness.data.backup.BackupManager
import com.node.fitness.data.preferences.PreferencesDataStore
import com.node.fitness.util.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val isWeightReminderEnabled: Boolean = true,
    val isMeasurementsReminderEnabled: Boolean = true,
    val isPhotosReminderEnabled: Boolean = true,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportSuccess: Boolean? = null,
    val importSuccess: Boolean? = null,
    val statusMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesDataStore: PreferencesDataStore,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            combine(
                preferencesDataStore.darkModeFlow,
                preferencesDataStore.reminderWeightFlow,
                preferencesDataStore.reminderMeasurementsFlow,
                preferencesDataStore.reminderPhotosFlow
            ) { darkMode, weight, measurements, photos ->
                SettingsUiState(
                    isDarkMode = darkMode,
                    isWeightReminderEnabled = weight,
                    isMeasurementsReminderEnabled = measurements,
                    isPhotosReminderEnabled = photos
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isDarkMode
            preferencesDataStore.setDarkMode(newValue)
        }
    }

    fun toggleWeightReminder() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isWeightReminderEnabled
            preferencesDataStore.setReminderWeight(newValue)
            if (newValue) ReminderScheduler.scheduleWeeklyWeightReminder(context)
            else ReminderScheduler.cancelWeightReminder(context)
        }
    }

    fun toggleMeasurementsReminder() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isMeasurementsReminderEnabled
            preferencesDataStore.setReminderMeasurements(newValue)
            if (newValue) ReminderScheduler.scheduleMonthlyMeasurementsReminder(context)
            else ReminderScheduler.cancelMeasurementsReminder(context)
        }
    }

    fun togglePhotosReminder() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isPhotosReminderEnabled
            preferencesDataStore.setReminderPhotos(newValue)
            if (newValue) ReminderScheduler.schedule4WeekPhotoReminder(context)
            else ReminderScheduler.cancelPhotoReminder(context)
        }
    }

    fun exportData(outputStream: OutputStream) {
        _uiState.update { it.copy(isExporting = true, exportSuccess = null, statusMessage = null) }
        viewModelScope.launch {
            val result = backupManager.exportToStream(outputStream)
            _uiState.update {
                it.copy(
                    isExporting = false,
                    exportSuccess = result.success,
                    statusMessage = result.message
                )
            }
        }
    }

    fun importData(inputStream: InputStream) {
        _uiState.update { it.copy(isImporting = true, importSuccess = null, statusMessage = null) }
        viewModelScope.launch {
            val result = backupManager.importFromStream(inputStream)
            _uiState.update {
                it.copy(
                    isImporting = false,
                    importSuccess = result.success,
                    statusMessage = result.message
                )
            }
        }
    }

    fun clearExportStatus() {
        _uiState.update { it.copy(exportSuccess = null, statusMessage = null) }
    }

    fun clearImportStatus() {
        _uiState.update { it.copy(importSuccess = null, statusMessage = null) }
    }
}

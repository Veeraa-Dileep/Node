package com.node.fitness.ui.screens.body

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.node.fitness.data.database.entity.BodyMeasurement
import com.node.fitness.data.repository.BodyDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BodyMeasurementUiState(
    val measurements: List<BodyMeasurement> = emptyList(),
    val latestMeasurement: BodyMeasurement? = null,
    val entryChest: String = "",
    val entryArms: String = "",
    val entryWaist: String = "",
    val entryThighs: String = "",
    val entryDate: Long = System.currentTimeMillis(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

@HiltViewModel
class BodyMeasurementViewModel @Inject constructor(
    private val bodyDataRepo: BodyDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BodyMeasurementUiState())
    val uiState: StateFlow<BodyMeasurementUiState> = _uiState.asStateFlow()

    init {
        loadMeasurements()
    }

    private fun loadMeasurements() {
        viewModelScope.launch {
            bodyDataRepo.getAllMeasurements().collect { measurements ->
                _uiState.update {
                    it.copy(
                        measurements = measurements,
                        latestMeasurement = measurements.firstOrNull(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setEntryChest(value: String) {
        _uiState.update { it.copy(entryChest = value) }
    }

    fun setEntryArms(value: String) {
        _uiState.update { it.copy(entryArms = value) }
    }

    fun setEntryWaist(value: String) {
        _uiState.update { it.copy(entryWaist = value) }
    }

    fun setEntryThighs(value: String) {
        _uiState.update { it.copy(entryThighs = value) }
    }

    fun setEntryDate(date: Long) {
        _uiState.update { it.copy(entryDate = date) }
    }

    fun saveMeasurement() {
        val chest = _uiState.value.entryChest.toDoubleOrNull()
        val arms = _uiState.value.entryArms.toDoubleOrNull()
        val waist = _uiState.value.entryWaist.toDoubleOrNull()
        val thighs = _uiState.value.entryThighs.toDoubleOrNull()

        if (chest == null && arms == null && waist == null && thighs == null) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            bodyDataRepo.addMeasurement(
                BodyMeasurement(
                    chest = chest ?: 0.0,
                    arms = arms ?: 0.0,
                    waist = waist ?:0.0,
                    thighs = thighs ?: 0.0,
                    date = _uiState.value.entryDate
                )
            )
            _uiState.update {
                it.copy(
                    entryChest = "",
                    entryArms = "",
                    entryWaist = "",
                    entryThighs = "",
                    entryDate = System.currentTimeMillis(),
                    isSaving = false
                )
            }
        }
    }

    fun deleteMeasurement(measurement: BodyMeasurement) {
        viewModelScope.launch {
            bodyDataRepo.deleteMeasurement(measurement)
        }
    }
}

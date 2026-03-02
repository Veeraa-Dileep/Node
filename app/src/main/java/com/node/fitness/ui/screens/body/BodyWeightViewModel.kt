package com.node.fitness.ui.screens.body

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.node.fitness.data.database.entity.BodyWeight
import com.node.fitness.data.repository.BodyDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class BodyWeightUiState(
    val weights: List<BodyWeight> = emptyList(),
    val latestWeight: BodyWeight? = null,
    val entryWeight: String = "",
    val entryDate: Long = System.currentTimeMillis(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

@HiltViewModel
class BodyWeightViewModel @Inject constructor(
    private val bodyDataRepo: BodyDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BodyWeightUiState())
    val uiState: StateFlow<BodyWeightUiState> = _uiState.asStateFlow()

    init {
        loadWeights()
    }

    private fun loadWeights() {
        viewModelScope.launch {
            bodyDataRepo.getAllWeights().collect { weights ->
                _uiState.update {
                    it.copy(
                        weights = weights,
                        latestWeight = weights.firstOrNull(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setEntryWeight(weight: String) {
        _uiState.update { it.copy(entryWeight = weight) }
    }

    fun setEntryDate(date: Long) {
        _uiState.update { it.copy(entryDate = date) }
    }

    fun saveWeight() {
        val weight = _uiState.value.entryWeight.toDoubleOrNull() ?: return
        if (weight <= 0.0) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            bodyDataRepo.addWeight(
                BodyWeight(
                    weight = weight,
                    date = _uiState.value.entryDate
                )
            )
            _uiState.update {
                it.copy(
                    entryWeight = "",
                    entryDate = System.currentTimeMillis(),
                    isSaving = false
                )
            }
        }
    }

    fun deleteWeight(weight: BodyWeight) {
        viewModelScope.launch {
            bodyDataRepo.deleteWeight(weight)
        }
    }
}

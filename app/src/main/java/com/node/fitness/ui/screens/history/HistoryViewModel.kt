package com.node.fitness.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.node.fitness.data.database.dao.ExerciseWithSets
import com.node.fitness.data.database.dao.WorkoutWithExercises
import com.node.fitness.data.database.entity.WorkoutSession
import com.node.fitness.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val sessions: List<WorkoutWithExercises> = emptyList(),
    val workoutDates: Set<java.time.LocalDate> = emptySet(),
    val selectedDate: java.time.LocalDate? = null,
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val isCalendarView: Boolean = false,
    val selectedSession: WorkoutSession? = null,
    val selectedExercisesWithSets: List<ExerciseWithSets> = emptyList()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var allSessions: List<WorkoutWithExercises> = emptyList()

    init {
        viewModelScope.launch {
            workoutRepo.getAllSessionsWithExercises().collect { sessions ->
                allSessions = sessions.filter { it.session.isCompleted }
                
                // Extract dates for calendar
                val dates = allSessions.map { 
                    java.time.Instant.ofEpochMilli(it.session.date)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                }.toSet()

                applyFilter()
                _uiState.update { it.copy(isLoading = false, workoutDates = dates) }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilter()
    }

    fun toggleCalendarView() {
        _uiState.update { it.copy(isCalendarView = !uiState.value.isCalendarView) }
        applyFilter()
    }

    fun selectDate(date: java.time.LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        applyFilter()
    }

    fun selectSession(session: WorkoutSession) {
        _uiState.update { it.copy(selectedSession = session) }
        viewModelScope.launch {
            workoutRepo.getExercisesWithSets(session.id).collect { exercises ->
                _uiState.update { it.copy(selectedExercisesWithSets = exercises) }
            }
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedSession = null, selectedExercisesWithSets = emptyList()) }
    }

    private fun applyFilter() {
        val state = _uiState.value
        val query = state.searchQuery.lowercase()
        
        var filtered = allSessions
        
        // Filter by Calendar Date if in calendar view and date is selected
        if (state.isCalendarView && state.selectedDate != null) {
            filtered = filtered.filter { 
                java.time.Instant.ofEpochMilli(it.session.date)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate() == state.selectedDate
            }
        }

        // Filter by Search Query
        if (query.isNotBlank()) {
            filtered = filtered.filter { ws ->
                ws.exercises.any { it.name.lowercase().contains(query) }
            }
        }
        
        _uiState.update { it.copy(sessions = filtered) }
    }
}

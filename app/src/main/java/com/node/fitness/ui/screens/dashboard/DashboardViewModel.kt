package com.node.fitness.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.node.fitness.data.database.entity.BodyWeight
import com.node.fitness.data.database.entity.PersonalRecord
import com.node.fitness.data.database.entity.WorkoutSession
import com.node.fitness.data.repository.BodyDataRepository
import com.node.fitness.data.repository.PRRepository
import com.node.fitness.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val greeting: String = "",
    val workoutsThisWeek: Int = 0,
    val totalVolumeThisWeek: Double = 0.0,
    val totalDurationThisWeek: Int = 0,
    val latestWeight: BodyWeight? = null,
    val recentPRs: List<PersonalRecord> = emptyList(),
    val lastWorkout: WorkoutSession? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val prRepo: PRRepository,
    private val bodyDataRepo: BodyDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        val greeting = getGreeting()
        _uiState.update { it.copy(greeting = greeting) }

        val (weekStart, weekEnd) = getWeekRange()

        // Workout count
        viewModelScope.launch {
            workoutRepo.getCompletedCount(weekStart, weekEnd).collect { count ->
                _uiState.update { it.copy(workoutsThisWeek = count, isLoading = false) }
            }
        }

        // Total volume
        viewModelScope.launch {
            workoutRepo.getTotalVolume(weekStart, weekEnd).collect { vol ->
                _uiState.update { it.copy(totalVolumeThisWeek = vol ?: 0.0) }
            }
        }

        // Total duration
        viewModelScope.launch {
            workoutRepo.getTotalDuration(weekStart, weekEnd).collect { dur ->
                _uiState.update { it.copy(totalDurationThisWeek = dur ?: 0) }
            }
        }

        // Latest body weight
        viewModelScope.launch {
            bodyDataRepo.getLatestWeight().collect { w ->
                _uiState.update { it.copy(latestWeight = w) }
            }
        }

        // Recent PRs
        viewModelScope.launch {
            prRepo.getRecentRecords(5).collect { prs ->
                _uiState.update { it.copy(recentPRs = prs) }
            }
        }

        // Last workout
        viewModelScope.launch {
            workoutRepo.getAllSessions().collect { sessions ->
                _uiState.update { it.copy(lastWorkout = sessions.firstOrNull()) }
            }
        }
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    private fun getWeekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val weekStart = cal.timeInMillis

        cal.add(Calendar.WEEK_OF_YEAR, 1)
        val weekEnd = cal.timeInMillis

        return weekStart to weekEnd
    }
}

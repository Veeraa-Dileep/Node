package com.node.fitness.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.node.fitness.data.database.dao.MuscleFrequencyResult
import com.node.fitness.data.database.dao.MuscleVolumeResult
import com.node.fitness.data.database.entity.BodyWeight
import com.node.fitness.data.database.entity.PersonalRecord
import com.node.fitness.data.database.entity.WorkoutSession
import com.node.fitness.data.repository.BodyDataRepository
import com.node.fitness.data.repository.PRRepository
import com.node.fitness.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AnalyticsUiState(
    val weeklyVolumeByMuscle: List<MuscleVolumeResult> = emptyList(),
    val muscleFrequency: List<MuscleFrequencyResult> = emptyList(),
    val totalWorkouts: Int = 0,
    val currentStreak: Int = 0,
    val prStreak: Int = 0,
    val totalPRs: Int = 0,
    val latestWeight: Double? = null,
    val leanBodyMass: Double? = null,
    val ffmi: Double? = null,
    val workoutDates: List<Long> = emptyList(),
    val recentPRs: List<PersonalRecord> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val prRepo: PRRepository,
    private val bodyDataRepo: BodyDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
        observeRealtimeData()
    }

    private fun observeRealtimeData() {
        viewModelScope.launch {
            bodyDataRepo.getLatestWeight().collect { w ->
                _uiState.update { it.copy(latestWeight = w?.weight) }
                recalculateBodyComposition(w)
            }
        }
        viewModelScope.launch {
            prRepo.getRecentRecords(10).collect { prs ->
                _uiState.update { it.copy(recentPRs = prs, totalPRs = prs.size) }
            }
        }
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            try {
                val cal = Calendar.getInstance()
                val now = cal.timeInMillis

                cal.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = cal.timeInMillis
                cal.timeInMillis = now

                cal.add(Calendar.DAY_OF_YEAR, -30)
                val monthAgo = cal.timeInMillis

                val volumeByMuscle = workoutRepo.getVolumeByMuscleGroup(weekAgo, now)
                val frequency = workoutRepo.getMuscleFrequency(monthAgo, now)
                val totalWorkouts = workoutRepo.getTotalCompletedWorkouts()
                val allSessions = workoutRepo.getAllCompletedSessions()
                val streak = calculateWorkoutStreak(allSessions)

                _uiState.update {
                    it.copy(
                        weeklyVolumeByMuscle = volumeByMuscle,
                        muscleFrequency = frequency,
                        totalWorkouts = totalWorkouts,
                        currentStreak = streak,
                        workoutDates = allSessions.map { s -> s.date },
                        isLoading = false
                    )
                }

                // PR streak (async — requires DAO calls per session)
                calculatePRStreak(allSessions)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun recalculateBodyComposition(latestWeight: BodyWeight?) {
        if (latestWeight == null) return
        viewModelScope.launch {
            bodyDataRepo.getLatestMeasurement().firstOrNull()?.let { measurement ->
                val bf = measurement.bodyFatPercentage
                if (bf != null && bf > 0) {
                    val weight = latestWeight.weight
                    val lbm = weight * (1 - bf / 100.0)
                    _uiState.update { it.copy(leanBodyMass = lbm) }
                }
            }
        }
    }

    private fun calculateWorkoutStreak(sessions: List<WorkoutSession>): Int {
        if (sessions.isEmpty()) return 0

        val cal = Calendar.getInstance()
        val today = clearTime(cal)

        val workoutDays = sessions.map { session ->
            cal.timeInMillis = session.date
            clearTime(cal)
        }.toSet().sortedDescending()

        if (workoutDays.isEmpty()) return 0

        val mostRecent = workoutDays.first()
        val dayDiff = (today - mostRecent) / (24 * 60 * 60 * 1000L)
        if (dayDiff > 1) return 0

        var streak = 1
        for (i in 1 until workoutDays.size) {
            val diff = (workoutDays[i - 1] - workoutDays[i]) / (24 * 60 * 60 * 1000L)
            if (diff == 1L) streak++ else break
        }
        return streak
    }

    private fun calculatePRStreak(sessions: List<WorkoutSession>) {
        viewModelScope.launch {
            var streak = 0
            for (session in sessions.sortedByDescending { it.date }) {
                val prCount = prRepo.getPRCountForSession(session.id)
                if (prCount > 0) streak++ else break
            }
            _uiState.update { it.copy(prStreak = streak) }
        }
    }

    private fun clearTime(cal: Calendar): Long {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

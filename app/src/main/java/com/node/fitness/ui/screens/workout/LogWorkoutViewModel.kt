package com.node.fitness.ui.screens.workout

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.node.fitness.data.database.entity.Exercise
import com.node.fitness.data.database.entity.ExerciseCatalog
import com.node.fitness.data.database.entity.MuscleGroup
import com.node.fitness.data.database.entity.SetEntry
import com.node.fitness.data.database.entity.WorkoutSession
import com.node.fitness.data.repository.ExerciseCatalogRepository
import com.node.fitness.data.repository.PRRepository
import com.node.fitness.data.repository.WorkoutRepository
import com.node.fitness.service.WorkoutForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State models ──

data class SetUiState(
    val id: Long = 0,
    val setNumber: Int,
    val weight: String = "",
    val reps: String = "",
    val isPersonalRecord: Boolean = false,
    val prTypes: List<String> = emptyList(),
    val notes: String = ""
)

data class ExerciseUiState(
    val id: Long = 0,
    val name: String = "",
    val exerciseCatalogId: Long? = null,
    val sets: List<SetUiState> = listOf(SetUiState(setNumber = 1)),
    val isExpanded: Boolean = true
)

data class WorkoutUiState(
    val sessionId: Long = 0,
    val exercises: List<ExerciseUiState> = emptyList(),
    val elapsedSeconds: Int = 0,
    val isSaving: Boolean = false,
    val isComplete: Boolean = false,
    val newPRExercise: String? = null,
    // Muscle group selection
    val muscleGroups: List<MuscleGroup> = emptyList(),
    val selectedMuscleGroupId: Long? = null,
    val catalogExercises: List<ExerciseCatalog> = emptyList(),
    val showMuscleGroupSelector: Boolean = true
)

@HiltViewModel
class LogWorkoutViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val workoutRepo: WorkoutRepository,
    private val prRepo: PRRepository,
    private val catalogRepo: ExerciseCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private val _timerRunning = MutableStateFlow(false)
    private var startRealtimeMillis: Long = 0L
    private var accumulatedSeconds: Int = 0

    private val _prEvent = MutableSharedFlow<Unit>()
    val prEvent = _prEvent.asSharedFlow()

    init {
        loadMuscleGroups()
    }

    private fun loadMuscleGroups() {
        viewModelScope.launch {
            catalogRepo.getAllMuscleGroups().collect { groups ->
                _uiState.update { it.copy(muscleGroups = groups) }
            }
        }
    }

    fun selectMuscleGroup(muscleGroupId: Long) {
        _uiState.update { it.copy(selectedMuscleGroupId = muscleGroupId) }
        viewModelScope.launch {
            catalogRepo.getExercisesForMuscleGroup(muscleGroupId).collect { exercises ->
                _uiState.update { it.copy(catalogExercises = exercises) }
            }
        }
    }

    fun startWorkout() {
        _uiState.update { it.copy(showMuscleGroupSelector = false) }
        viewModelScope.launch {
            val session = WorkoutSession(startTimeMillis = System.currentTimeMillis())
            val sessionId = workoutRepo.createSession(session)
            _uiState.update { it.copy(sessionId = sessionId) }
            WorkoutForegroundService.start(appContext, sessionId)
            startTimer()
        }
    }

    private fun startTimer() {
        startRealtimeMillis = SystemClock.elapsedRealtime()
        accumulatedSeconds = 0
        _timerRunning.value = true
        viewModelScope.launch {
            while (_timerRunning.value) {
                kotlinx.coroutines.delay(500) // Update display at 2Hz for responsive UI
                if (_timerRunning.value) {
                    val elapsedMs = SystemClock.elapsedRealtime() - startRealtimeMillis
                    _uiState.update {
                        it.copy(elapsedSeconds = accumulatedSeconds + (elapsedMs / 1000).toInt())
                    }
                }
            }
        }
    }

    fun addExerciseFromCatalog(catalog: ExerciseCatalog) {
        _uiState.update { state ->
            state.copy(
                exercises = state.exercises + ExerciseUiState(
                    name = catalog.name,
                    exerciseCatalogId = catalog.id,
                    sets = listOf(SetUiState(setNumber = 1))
                )
            )
        }
    }

    fun addExercise() {
        _uiState.update { state ->
            state.copy(
                exercises = state.exercises + ExerciseUiState(
                    sets = listOf(SetUiState(setNumber = 1))
                )
            )
        }
    }

    fun updateExerciseName(exerciseIndex: Int, name: String) {
        _uiState.update { state ->
            val updated = state.exercises.toMutableList()
            updated[exerciseIndex] = updated[exerciseIndex].copy(name = name)
            state.copy(exercises = updated)
        }
    }

    fun addSet(exerciseIndex: Int) {
        _uiState.update { state ->
            val updated = state.exercises.toMutableList()
            val exercise = updated[exerciseIndex]
            val newSetNumber = exercise.sets.size + 1
            updated[exerciseIndex] = exercise.copy(
                sets = exercise.sets + SetUiState(setNumber = newSetNumber)
            )
            state.copy(exercises = updated)
        }
    }

    fun updateSetWeight(exerciseIndex: Int, setIndex: Int, weight: String) {
        _uiState.update { state ->
            val exercises = state.exercises.toMutableList()
            val sets = exercises[exerciseIndex].sets.toMutableList()
            sets[setIndex] = sets[setIndex].copy(weight = weight)
            exercises[exerciseIndex] = exercises[exerciseIndex].copy(sets = sets)
            state.copy(exercises = exercises)
        }
    }

    fun updateSetReps(exerciseIndex: Int, setIndex: Int, reps: String) {
        _uiState.update { state ->
            val exercises = state.exercises.toMutableList()
            val sets = exercises[exerciseIndex].sets.toMutableList()
            sets[setIndex] = sets[setIndex].copy(reps = reps)
            exercises[exerciseIndex] = exercises[exerciseIndex].copy(sets = sets)
            state.copy(exercises = exercises)
        }
    }

    fun removeSet(exerciseIndex: Int, setIndex: Int) {
        _uiState.update { state ->
            val exercises = state.exercises.toMutableList()
            val sets = exercises[exerciseIndex].sets.toMutableList()
            sets.removeAt(setIndex)
            val renumbered = sets.mapIndexed { i, s -> s.copy(setNumber = i + 1) }
            exercises[exerciseIndex] = exercises[exerciseIndex].copy(sets = renumbered)
            state.copy(exercises = exercises)
        }
    }

    fun removeExercise(exerciseIndex: Int) {
        _uiState.update { state ->
            val exercises = state.exercises.toMutableList()
            exercises.removeAt(exerciseIndex)
            state.copy(exercises = exercises)
        }
    }

    fun toggleExerciseExpanded(exerciseIndex: Int) {
        _uiState.update { state ->
            val exercises = state.exercises.toMutableList()
            val ex = exercises[exerciseIndex]
            exercises[exerciseIndex] = ex.copy(isExpanded = !ex.isExpanded)
            state.copy(exercises = exercises)
        }
    }

    fun finishWorkout() {
        _timerRunning.value = false
        WorkoutForegroundService.stop(appContext)
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val state = _uiState.value

            val session = workoutRepo.getSessionById(state.sessionId)
            session?.let {
                workoutRepo.updateSession(
                    it.copy(
                        durationSeconds = state.elapsedSeconds,
                        isCompleted = true
                    )
                )
            }

            state.exercises.forEachIndexed { exIndex, exerciseUi ->
                if (exerciseUi.name.isNotBlank()) {
                    // Ensure exercise exists in catalog
                    val catalogExercise = catalogRepo.getOrCreateExercise(
                        name = exerciseUi.name.trim(),
                        muscleGroupId = _uiState.value.selectedMuscleGroupId
                    )

                    val exerciseId = workoutRepo.addExercise(
                        Exercise(
                            sessionId = state.sessionId,
                            name = exerciseUi.name.trim(),
                            order = exIndex,
                            exerciseCatalogId = catalogExercise.id
                        )
                    )

                    exerciseUi.sets.forEach { setUi ->
                        val weight = setUi.weight.toDoubleOrNull() ?: 0.0
                        val reps = setUi.reps.toIntOrNull() ?: 0

                        if (weight > 0 || reps > 0) {
                            val volume = weight * reps
                            val est1RM = if (reps > 0) weight * (1 + reps / 30.0) else 0.0

                            val prTypes = prRepo.checkAndRecordAllPRs(
                                exerciseName = exerciseUi.name.trim(),
                                weight = weight,
                                reps = reps,
                                sessionId = state.sessionId,
                                exerciseCatalogId = catalogExercise.id
                            )

                            workoutRepo.addSet(
                                SetEntry(
                                    exerciseId = exerciseId,
                                    setNumber = setUi.setNumber,
                                    weight = weight,
                                    reps = reps,
                                    volume = volume,
                                    estimated1RM = est1RM,
                                    isPersonalRecord = prTypes.isNotEmpty(),
                                    notes = setUi.notes
                                )
                            )

                            if (prTypes.isNotEmpty()) {
                                _prEvent.emit(Unit)
                            }
                        }
                    }
                }
            }

            _uiState.update { it.copy(isSaving = false, isComplete = true) }
        }
    }

    fun discardWorkout() {
        _timerRunning.value = false
        WorkoutForegroundService.stop(appContext)
        viewModelScope.launch {
            val session = workoutRepo.getSessionById(_uiState.value.sessionId)
            session?.let { workoutRepo.deleteSession(it) }
            _uiState.update { it.copy(isComplete = true) }
        }
    }

    fun clearPRNotification() {
        _uiState.update { it.copy(newPRExercise = null) }
    }
}

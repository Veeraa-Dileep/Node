package com.node.fitness.data.repository

import com.node.fitness.data.database.dao.WorkoutDao
import com.node.fitness.data.database.dao.ExerciseWithSets
import com.node.fitness.data.database.dao.WorkoutWithExercises
import com.node.fitness.data.database.entity.Exercise
import com.node.fitness.data.database.entity.SetEntry
import com.node.fitness.data.database.entity.WorkoutSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao
) {
    // ── Sessions ──
    suspend fun createSession(session: WorkoutSession): Long = workoutDao.insertSessionRecord(session)
    suspend fun updateSession(session: WorkoutSession) = workoutDao.updateSession(session)
    suspend fun deleteSession(session: WorkoutSession) = workoutDao.deleteSession(session)
    suspend fun getSessionById(id: Long): WorkoutSession? = workoutDao.getSessionById(id)
    fun getAllSessions(): Flow<List<WorkoutSession>> = workoutDao.getAllSessions()
    fun getSessionsBetween(start: Long, end: Long): Flow<List<WorkoutSession>> = workoutDao.getSessionsBetween(start, end)
    fun getCompletedCount(start: Long, end: Long): Flow<Int> = workoutDao.getCompletedWorkoutCount(start, end)
    suspend fun getSessionWithExercises(id: Long): WorkoutWithExercises? = workoutDao.getSessionWithExercises(id)
    fun getAllSessionsWithExercises(): Flow<List<WorkoutWithExercises>> = workoutDao.getAllSessionsWithExercises()

    // ── Exercises ──
    suspend fun addExercise(exercise: Exercise): Long = workoutDao.insertExerciseRecord(exercise)
    suspend fun updateExercise(exercise: Exercise) = workoutDao.updateExercise(exercise)
    suspend fun deleteExercise(exercise: Exercise) = workoutDao.deleteExercise(exercise)
    fun getExercisesForSession(sessionId: Long): Flow<List<Exercise>> = workoutDao.getExercisesForSession(sessionId)
    fun getAllExerciseNames(): Flow<List<String>> = workoutDao.getAllExerciseNames()

    // ── Sets ──
    suspend fun addSet(set: SetEntry): Long = workoutDao.insertSetRecord(set)
    suspend fun updateSet(set: SetEntry) = workoutDao.updateSet(set)
    suspend fun deleteSet(set: SetEntry) = workoutDao.deleteSet(set)
    fun getSetsForExercise(exerciseId: Long): Flow<List<SetEntry>> = workoutDao.getSetsForExercise(exerciseId)
    fun getExercisesWithSets(sessionId: Long): Flow<List<ExerciseWithSets>> = workoutDao.getExercisesWithSets(sessionId)

    // ── Aggregates ──
    fun getTotalVolume(start: Long, end: Long): Flow<Double?> = workoutDao.getTotalVolume(start, end)
    fun getTotalDuration(start: Long, end: Long): Flow<Int?> = workoutDao.getTotalDuration(start, end)
    suspend fun getTotalCompletedWorkouts(): Int = workoutDao.getTotalCompletedWorkouts()
    suspend fun getAllCompletedSessions(): List<WorkoutSession> = workoutDao.getAllCompletedSessionsSync()
    suspend fun getVolumeByMuscleGroup(start: Long, end: Long) = workoutDao.getVolumeByMuscleGroup(start, end)
    suspend fun getMuscleFrequency(start: Long, end: Long) = workoutDao.getMuscleFrequency(start, end)
}

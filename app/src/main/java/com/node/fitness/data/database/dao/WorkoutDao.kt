package com.node.fitness.data.database.dao

import androidx.room.*
import com.node.fitness.data.database.entity.Exercise
import com.node.fitness.data.database.entity.SetEntry
import com.node.fitness.data.database.entity.WorkoutSession
import kotlinx.coroutines.flow.Flow

data class WorkoutWithExercises(
    @Embedded val session: WorkoutSession,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val exercises: List<Exercise>
)

data class ExerciseWithSets(
    @Embedded val exercise: Exercise,
    @Relation(parentColumn = "id", entityColumn = "exerciseId")
    val sets: List<SetEntry>
)

data class MuscleVolumeResult(
    val muscleGroup: String,
    val totalVolume: Double
)

data class MuscleFrequencyResult(
    val muscleGroup: String,
    val sessionCount: Int
)

@Dao
interface WorkoutDao {

    // ── Sessions ──

    @Insert
    suspend fun insertSessionRecord(session: WorkoutSession): Long

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): WorkoutSession?

    @Query("SELECT * FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getSessionsBetween(startDate: Long, endDate: Long): Flow<List<WorkoutSession>>

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate AND isCompleted = 1")
    fun getCompletedWorkoutCount(startDate: Long, endDate: Long): Flow<Int>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionWithExercises(sessionId: Long): WorkoutWithExercises?

    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun getAllSessionsWithExercises(): Flow<List<WorkoutWithExercises>>

    // ── Exercises ──

    @Insert
    suspend fun insertExerciseRecord(exercise: Exercise): Long

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("SELECT * FROM exercises WHERE sessionId = :sessionId ORDER BY `order`")
    fun getExercisesForSession(sessionId: Long): Flow<List<Exercise>>

    @Query("SELECT DISTINCT name FROM exercises ORDER BY name")
    fun getAllExerciseNames(): Flow<List<String>>

    // ── Sets ──

    @Insert
    suspend fun insertSetRecord(set: SetEntry): Long

    @Update
    suspend fun updateSet(set: SetEntry)

    @Delete
    suspend fun deleteSet(set: SetEntry)

    @Query("SELECT * FROM set_entries WHERE exerciseId = :exerciseId ORDER BY setNumber")
    fun getSetsForExercise(exerciseId: Long): Flow<List<SetEntry>>

    @Transaction
    @Query("SELECT * FROM exercises WHERE sessionId = :sessionId ORDER BY `order`")
    fun getExercisesWithSets(sessionId: Long): Flow<List<ExerciseWithSets>>

    // ── Aggregates ──

    @Query("SELECT SUM(s.weight * s.reps) FROM set_entries s INNER JOIN exercises e ON s.exerciseId = e.id INNER JOIN workout_sessions w ON e.sessionId = w.id WHERE w.date BETWEEN :startDate AND :endDate")
    fun getTotalVolume(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(durationSeconds) FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate AND isCompleted = 1")
    fun getTotalDuration(startDate: Long, endDate: Long): Flow<Int?>

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE isCompleted = 1")
    suspend fun getTotalCompletedWorkouts(): Int

    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 1 ORDER BY date DESC")
    suspend fun getAllCompletedSessionsSync(): List<WorkoutSession>

    @Query("""
        SELECT mg.name as muscleGroup, SUM(se.volume) as totalVolume
        FROM set_entries se
        INNER JOIN exercises e ON se.exerciseId = e.id
        INNER JOIN exercise_catalog ec ON e.exerciseCatalogId = ec.id
        INNER JOIN exercise_muscle_map emm ON ec.id = emm.exerciseCatalogId
        INNER JOIN muscle_groups mg ON emm.muscleGroupId = mg.id
        INNER JOIN workout_sessions ws ON e.sessionId = ws.id
        WHERE ws.date BETWEEN :startDate AND :endDate AND ws.isCompleted = 1
        GROUP BY mg.name
        ORDER BY totalVolume DESC
    """)
    suspend fun getVolumeByMuscleGroup(startDate: Long, endDate: Long): List<MuscleVolumeResult>

    @Query("""
        SELECT DISTINCT mg.name as muscleGroup, COUNT(DISTINCT ws.id) as sessionCount
        FROM workout_sessions ws
        INNER JOIN exercises e ON e.sessionId = ws.id
        INNER JOIN exercise_catalog ec ON e.exerciseCatalogId = ec.id
        INNER JOIN exercise_muscle_map emm ON ec.id = emm.exerciseCatalogId
        INNER JOIN muscle_groups mg ON emm.muscleGroupId = mg.id
        WHERE ws.date BETWEEN :startDate AND :endDate AND ws.isCompleted = 1
        GROUP BY mg.name
        ORDER BY sessionCount DESC
    """)
    suspend fun getMuscleFrequency(startDate: Long, endDate: Long): List<MuscleFrequencyResult>

    // ── Sync methods for backup ──

    @Query("SELECT * FROM workout_sessions")
    suspend fun getAllSessionsSync(): List<WorkoutSession>

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercisesSync(): List<Exercise>

    @Query("SELECT * FROM set_entries")
    suspend fun getAllSetsSync(): List<SetEntry>
}

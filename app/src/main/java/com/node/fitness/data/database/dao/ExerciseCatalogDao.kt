package com.node.fitness.data.database.dao

import androidx.room.*
import com.node.fitness.data.database.entity.ExerciseCatalog
import com.node.fitness.data.database.entity.ExerciseMuscleMap
import com.node.fitness.data.database.entity.MuscleGroup
import kotlinx.coroutines.flow.Flow

data class ExerciseWithMuscleGroups(
    @Embedded val exercise: ExerciseCatalog,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            ExerciseMuscleMap::class,
            parentColumn = "exerciseCatalogId",
            entityColumn = "muscleGroupId"
        )
    )
    val muscleGroups: List<MuscleGroup>
)

@Dao
interface ExerciseCatalogDao {

    // ── Muscle Groups ──

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMuscleGroup(group: MuscleGroup): Long

    @Query("SELECT * FROM muscle_groups ORDER BY name")
    fun getAllMuscleGroups(): Flow<List<MuscleGroup>>

    @Query("SELECT * FROM muscle_groups ORDER BY name")
    suspend fun getAllMuscleGroupsSync(): List<MuscleGroup>

    // ── Exercise Catalog ──

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExerciseCatalog(exercise: ExerciseCatalog): Long

    @Query("SELECT * FROM exercise_catalog ORDER BY name")
    fun getAllExercises(): Flow<List<ExerciseCatalog>>

    @Query("SELECT * FROM exercise_catalog WHERE id = :id")
    suspend fun getExerciseById(id: Long): ExerciseCatalog?

    @Query("SELECT * FROM exercise_catalog WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getExerciseByName(name: String): ExerciseCatalog?

    @Query("""
        SELECT ec.* FROM exercise_catalog ec
        INNER JOIN exercise_muscle_map emm ON ec.id = emm.exerciseCatalogId
        WHERE emm.muscleGroupId = :muscleGroupId
        ORDER BY ec.name
    """)
    fun getExercisesForMuscleGroup(muscleGroupId: Long): Flow<List<ExerciseCatalog>>

    @Transaction
    @Query("SELECT * FROM exercise_catalog ORDER BY name")
    fun getAllExercisesWithMuscleGroups(): Flow<List<ExerciseWithMuscleGroups>>

    // ── Mapping ──

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExerciseMuscleMap(map: ExerciseMuscleMap)

    // ── Sync methods for backup ──

    @Query("SELECT * FROM exercise_catalog")
    suspend fun getAllExercisesSync(): List<ExerciseCatalog>

    @Query("SELECT * FROM exercise_muscle_map")
    suspend fun getAllMappingsSync(): List<ExerciseMuscleMap>
}

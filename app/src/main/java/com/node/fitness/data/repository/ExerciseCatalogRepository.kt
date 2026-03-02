package com.node.fitness.data.repository

import com.node.fitness.data.database.dao.ExerciseCatalogDao
import com.node.fitness.data.database.entity.ExerciseCatalog
import com.node.fitness.data.database.entity.ExerciseMuscleMap
import com.node.fitness.data.database.entity.MuscleGroup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseCatalogRepository @Inject constructor(
    private val dao: ExerciseCatalogDao
) {
    fun getAllMuscleGroups(): Flow<List<MuscleGroup>> = dao.getAllMuscleGroups()

    fun getAllExercises(): Flow<List<ExerciseCatalog>> = dao.getAllExercises()

    fun getExercisesForMuscleGroup(muscleGroupId: Long): Flow<List<ExerciseCatalog>> =
        dao.getExercisesForMuscleGroup(muscleGroupId)

    suspend fun getExerciseByName(name: String): ExerciseCatalog? =
        dao.getExerciseByName(name)

    suspend fun getOrCreateExercise(name: String, muscleGroupId: Long? = null): ExerciseCatalog {
        val existing = dao.getExerciseByName(name)
        if (existing != null) return existing

        val id = dao.insertExerciseCatalog(
            ExerciseCatalog(name = name, isUserCreated = true)
        )
        if (muscleGroupId != null && id > 0) {
            dao.insertExerciseMuscleMap(
                ExerciseMuscleMap(exerciseCatalogId = id, muscleGroupId = muscleGroupId)
            )
        }
        return dao.getExerciseByName(name)!!
    }
}

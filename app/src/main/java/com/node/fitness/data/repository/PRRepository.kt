package com.node.fitness.data.repository

import com.node.fitness.data.database.dao.PersonalRecordDao
import com.node.fitness.data.database.entity.PersonalRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PRRepository @Inject constructor(
    private val prDao: PersonalRecordDao
) {
    suspend fun getBestForExercise(name: String): PersonalRecord? = prDao.getBestForExercise(name)
    suspend fun insertRecord(record: PersonalRecord): Long = prDao.insertRecord(record)
    fun getAllRecords(): Flow<List<PersonalRecord>> = prDao.getAllRecords()
    fun getRecordsForExercise(name: String): Flow<List<PersonalRecord>> = prDao.getRecordsForExercise(name)
    fun getRecentRecords(limit: Int = 5): Flow<List<PersonalRecord>> = prDao.getRecentRecords(limit)

    suspend fun getBestWeightForExercise(name: String): PersonalRecord? =
        prDao.getBestByType(name, PersonalRecord.PR_TYPE_WEIGHT)

    suspend fun getBest1RMForExercise(name: String): PersonalRecord? =
        prDao.getBestByType(name, PersonalRecord.PR_TYPE_1RM)

    /**
     * Check all PR types for the given set and record any new PRs.
     * Returns list of PR types that were achieved.
     */
    suspend fun checkAndRecordAllPRs(
        exerciseName: String,
        weight: Double,
        reps: Int,
        sessionId: Long,
        exerciseCatalogId: Long? = null
    ): List<String> {
        val achievedPRs = mutableListOf<String>()
        val volume = weight * reps
        val est1RM = if (reps > 0) weight * (1 + reps / 30.0) else 0.0

        // Volume PR
        val currentBestVolume = getBestForExercise(exerciseName)
        if (currentBestVolume == null || volume > currentBestVolume.maxVolume) {
            insertRecord(
                PersonalRecord(
                    exerciseName = exerciseName,
                    prType = PersonalRecord.PR_TYPE_VOLUME,
                    maxVolume = volume,
                    weight = weight,
                    reps = reps,
                    estimated1RM = est1RM,
                    sessionId = sessionId,
                    exerciseCatalogId = exerciseCatalogId
                )
            )
            achievedPRs.add(PersonalRecord.PR_TYPE_VOLUME)
        }

        // Weight PR (heaviest single lift)
        val currentBestWeight = getBestWeightForExercise(exerciseName)
        if (currentBestWeight == null || weight > currentBestWeight.weight) {
            insertRecord(
                PersonalRecord(
                    exerciseName = exerciseName,
                    prType = PersonalRecord.PR_TYPE_WEIGHT,
                    maxVolume = volume,
                    weight = weight,
                    reps = reps,
                    estimated1RM = est1RM,
                    sessionId = sessionId,
                    exerciseCatalogId = exerciseCatalogId
                )
            )
            achievedPRs.add(PersonalRecord.PR_TYPE_WEIGHT)
        }

        // Estimated 1RM PR
        val currentBest1RM = getBest1RMForExercise(exerciseName)
        if (est1RM > 0 && (currentBest1RM == null || est1RM > currentBest1RM.estimated1RM)) {
            insertRecord(
                PersonalRecord(
                    exerciseName = exerciseName,
                    prType = PersonalRecord.PR_TYPE_1RM,
                    maxVolume = volume,
                    weight = weight,
                    reps = reps,
                    estimated1RM = est1RM,
                    sessionId = sessionId,
                    exerciseCatalogId = exerciseCatalogId
                )
            )
            achievedPRs.add(PersonalRecord.PR_TYPE_1RM)
        }

        return achievedPRs
    }

    /** Legacy compatibility wrapper */
    suspend fun checkAndRecordPR(
        exerciseName: String,
        weight: Double,
        reps: Int,
        sessionId: Long
    ): Boolean {
        return checkAndRecordAllPRs(exerciseName, weight, reps, sessionId).isNotEmpty()
    }

    suspend fun getPRCountForSession(sessionId: Long): Int =
        prDao.getPRCountForSession(sessionId)
}

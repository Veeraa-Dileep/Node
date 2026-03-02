package com.node.fitness.data.database.dao

import androidx.room.*
import com.node.fitness.data.database.entity.PersonalRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {

    @Insert
    suspend fun insertRecord(record: PersonalRecord): Long

    @Update
    suspend fun update(record: PersonalRecord)

    @Query("SELECT * FROM personal_records WHERE exerciseName = :exerciseName AND prType = 'VOLUME' ORDER BY maxVolume DESC LIMIT 1")
    suspend fun getBestForExercise(exerciseName: String): PersonalRecord?

    @Query("SELECT * FROM personal_records WHERE exerciseName = :exerciseName AND prType = :prType ORDER BY CASE :prType WHEN 'WEIGHT' THEN weight WHEN '1RM' THEN estimated1RM ELSE maxVolume END DESC LIMIT 1")
    suspend fun getBestByType(exerciseName: String, prType: String): PersonalRecord?

    @Query("SELECT * FROM personal_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<PersonalRecord>>

    @Query("SELECT * FROM personal_records WHERE exerciseName = :exerciseName ORDER BY date DESC")
    fun getRecordsForExercise(exerciseName: String): Flow<List<PersonalRecord>>

    @Query("SELECT * FROM personal_records ORDER BY date DESC LIMIT :limit")
    fun getRecentRecords(limit: Int = 5): Flow<List<PersonalRecord>>

    // ── Sync methods for backup ──

    @Query("SELECT * FROM personal_records")
    suspend fun getAllRecordsSync(): List<PersonalRecord>

    @Query("SELECT COUNT(*) FROM personal_records WHERE sessionId = :sessionId")
    suspend fun getPRCountForSession(sessionId: Long): Int

    @Query("SELECT COUNT(DISTINCT sessionId) FROM personal_records")
    suspend fun getSessionsWithPRs(): Int
}

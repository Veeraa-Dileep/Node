package com.node.fitness.data.database.dao

import androidx.room.*
import com.node.fitness.data.database.entity.BodyWeight
import com.node.fitness.data.database.entity.BodyMeasurement
import com.node.fitness.data.database.entity.ProgressPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyDataDao {

    // ── Body Weight ──

    @Insert
    suspend fun insertWeightRecord(weight: BodyWeight): Long

    @Update
    suspend fun updateWeight(weight: BodyWeight)

    @Delete
    suspend fun deleteWeight(weight: BodyWeight)

    @Query("SELECT * FROM body_weight ORDER BY date DESC")
    fun getAllWeights(): Flow<List<BodyWeight>>

    @Query("SELECT * FROM body_weight ORDER BY date DESC LIMIT 1")
    fun getLatestWeight(): Flow<BodyWeight?>

    // ── Body Measurements ──

    @Insert
    suspend fun insertMeasurementRecord(measurement: BodyMeasurement): Long

    @Update
    suspend fun updateMeasurement(measurement: BodyMeasurement)

    @Delete
    suspend fun deleteMeasurement(measurement: BodyMeasurement)

    @Query("SELECT * FROM body_measurements ORDER BY date DESC")
    fun getAllMeasurements(): Flow<List<BodyMeasurement>>

    @Query("SELECT * FROM body_measurements ORDER BY date DESC LIMIT 1")
    fun getLatestMeasurement(): Flow<BodyMeasurement?>

    // ── Progress Photos ──

    @Insert
    suspend fun insertPhotoRecord(photo: ProgressPhoto): Long

    @Delete
    suspend fun deletePhoto(photo: ProgressPhoto)

    @Query("SELECT * FROM progress_photos ORDER BY date DESC")
    fun getAllPhotos(): Flow<List<ProgressPhoto>>

    @Query("SELECT * FROM progress_photos ORDER BY date ASC")
    fun getAllPhotosChronological(): Flow<List<ProgressPhoto>>

    // ── Sync methods for backup ──

    @Query("SELECT * FROM body_weight")
    suspend fun getAllWeightsSync(): List<BodyWeight>

    @Query("SELECT * FROM body_measurements")
    suspend fun getAllMeasurementsSync(): List<BodyMeasurement>

    @Query("SELECT * FROM progress_photos")
    suspend fun getAllPhotosSync(): List<ProgressPhoto>
}

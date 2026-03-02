package com.node.fitness.data.repository

import com.node.fitness.data.database.dao.BodyDataDao
import com.node.fitness.data.database.entity.BodyMeasurement
import com.node.fitness.data.database.entity.BodyWeight
import com.node.fitness.data.database.entity.ProgressPhoto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyDataRepository @Inject constructor(
    private val bodyDataDao: BodyDataDao
) {
    // ── Weight ──
    suspend fun addWeight(weight: BodyWeight): Long = bodyDataDao.insertWeightRecord(weight)
    suspend fun updateWeight(weight: BodyWeight) = bodyDataDao.updateWeight(weight)
    suspend fun deleteWeight(weight: BodyWeight) = bodyDataDao.deleteWeight(weight)
    fun getAllWeights(): Flow<List<BodyWeight>> = bodyDataDao.getAllWeights()
    fun getLatestWeight(): Flow<BodyWeight?> = bodyDataDao.getLatestWeight()

    // ── Measurements ──
    suspend fun addMeasurement(measurement: BodyMeasurement): Long = bodyDataDao.insertMeasurementRecord(measurement)
    suspend fun updateMeasurement(measurement: BodyMeasurement) = bodyDataDao.updateMeasurement(measurement)
    suspend fun deleteMeasurement(measurement: BodyMeasurement) = bodyDataDao.deleteMeasurement(measurement)
    fun getAllMeasurements(): Flow<List<BodyMeasurement>> = bodyDataDao.getAllMeasurements()
    fun getLatestMeasurement(): Flow<BodyMeasurement?> = bodyDataDao.getLatestMeasurement()

    // ── Photos ──
    suspend fun addPhoto(photo: ProgressPhoto): Long = bodyDataDao.insertPhotoRecord(photo)
    suspend fun deletePhoto(photo: ProgressPhoto) = bodyDataDao.deletePhoto(photo)
    fun getAllPhotos(): Flow<List<ProgressPhoto>> = bodyDataDao.getAllPhotos()
    fun getAllPhotosChronological(): Flow<List<ProgressPhoto>> = bodyDataDao.getAllPhotosChronological()
}

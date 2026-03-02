package com.node.fitness.data.repository

import com.node.fitness.data.database.dao.BodyDataDao
import com.node.fitness.data.database.entity.ProgressPhoto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    private val bodyDataDao: BodyDataDao
) {
    fun getAllPhotos(): Flow<List<ProgressPhoto>> =
        bodyDataDao.getAllPhotos()

    fun getAllPhotosChronological(): Flow<List<ProgressPhoto>> =
        bodyDataDao.getAllPhotosChronological()

    suspend fun insertPhoto(photo: ProgressPhoto): Long =
        bodyDataDao.insertPhotoRecord(photo)

    suspend fun deletePhoto(photo: ProgressPhoto) =
        bodyDataDao.deletePhoto(photo)
}

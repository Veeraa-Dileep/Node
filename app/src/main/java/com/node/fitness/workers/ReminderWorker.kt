package com.node.fitness.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.node.fitness.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_TYPE_KEY = "work_type"
        const val TYPE_WEIGHT = "weight"
        const val TYPE_MEASUREMENTS = "measurements"
        const val TYPE_PHOTOS = "photos"
    }

    override suspend fun doWork(): Result {
        val reminderType = inputData.getString(WORK_TYPE_KEY) ?: return Result.failure()

        return try {
            when (reminderType) {
                TYPE_WEIGHT -> NotificationHelper.showWeightReminder(applicationContext)
                TYPE_MEASUREMENTS -> NotificationHelper.showMeasurementsReminder(applicationContext)
                TYPE_PHOTOS -> NotificationHelper.showPhotoReminder(applicationContext)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

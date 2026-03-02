package com.node.fitness.util

import android.content.Context
import androidx.work.*
import com.node.fitness.workers.ReminderWorker
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val WORK_NAME_WEIGHT = "weight_reminder"
    private const val WORK_NAME_MEASUREMENTS = "measurements_reminder"
    private const val WORK_NAME_PHOTOS = "photo_reminder"

    fun scheduleWeeklyWeightReminder(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            7, TimeUnit.DAYS
        ).setInputData(
            workDataOf(ReminderWorker.WORK_TYPE_KEY to ReminderWorker.TYPE_WEIGHT)
        ).setInitialDelay(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_WEIGHT,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun scheduleMonthlyMeasurementsReminder(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            30, TimeUnit.DAYS
        ).setInputData(
            workDataOf(ReminderWorker.WORK_TYPE_KEY to ReminderWorker.TYPE_MEASUREMENTS)
        ).setInitialDelay(7, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_MEASUREMENTS,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun schedule4WeekPhotoReminder(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            28, TimeUnit.DAYS
        ).setInputData(
            workDataOf(ReminderWorker.WORK_TYPE_KEY to ReminderWorker.TYPE_PHOTOS)
        ).setInitialDelay(14, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_PHOTOS,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelWeightReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_WEIGHT)
    }

    fun cancelMeasurementsReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_MEASUREMENTS)
    }

    fun cancelPhotoReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PHOTOS)
    }
}

package com.node.fitness

import android.app.Application
import com.node.fitness.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NodeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}

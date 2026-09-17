package com.taskflow

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.taskflow.core.notification.REMINDER_CHANNEL_ID
import com.taskflow.worker.CLEANUP_WORK_NAME
import com.taskflow.worker.CleanupWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class TaskFlowApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        createReminderNotificationChannel()
        schedulePeriodicCleanup()
    }

    private fun createReminderNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            getString(R.string.notification_channel_reminders_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.notification_channel_reminders_description)
        }
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    private fun schedulePeriodicCleanup() {
        val request = PeriodicWorkRequestBuilder<CleanupWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(CLEANUP_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}

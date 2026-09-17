package com.taskflow

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.taskflow.core.notification.REMINDER_CHANNEL_ID
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TaskFlowApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createReminderNotificationChannel()
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
}

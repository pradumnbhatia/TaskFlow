package com.taskflow.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.taskflow.core.datastore.UserPreferencesRepository
import com.taskflow.core.model.Task
import com.taskflow.core.model.TaskStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class AlarmReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ReminderScheduler {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override suspend fun schedule(task: Task) {
        cancel(task.id)

        val dueDate = task.dueDate
        val dueTime = task.dueTime
        if (task.status != TaskStatus.PENDING || !task.reminderEnabled || dueDate == null || dueTime == null) {
            return
        }

        val preferences = userPreferencesRepository.userPreferences.first()
        if (!preferences.taskRemindersEnabled) {
            return
        }

        val triggerAtMillis = LocalDateTime.of(dueDate, dueTime)
            .minusMinutes(preferences.defaultReminderMinutesBefore.toLong())
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerAtMillis <= System.currentTimeMillis()) {
            return
        }

        val pendingIntent = requireNotNull(
            reminderPendingIntent(task.id, task.title, PendingIntent.FLAG_UPDATE_CURRENT),
        )
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    override fun cancel(taskId: String) {
        val pendingIntent = reminderPendingIntent(taskId, taskTitle = null, flags = PendingIntent.FLAG_NO_CREATE)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun reminderPendingIntent(taskId: String, taskTitle: String?, flags: Int): PendingIntent? {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

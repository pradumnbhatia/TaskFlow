package com.taskflow.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.taskflow.core.data.TaskRepository
import com.taskflow.core.datastore.UserPreferencesRepository
import com.taskflow.core.model.TaskStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var taskRepository: TaskRepository

    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject lateinit var reminderNotifier: ReminderNotifier

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = taskRepository.getTaskById(taskId)
                val remindersEnabled = userPreferencesRepository.userPreferences.first().taskRemindersEnabled
                if (task != null && task.status == TaskStatus.PENDING && task.reminderEnabled && remindersEnabled) {
                    reminderNotifier.showReminder(task.id, task.title)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

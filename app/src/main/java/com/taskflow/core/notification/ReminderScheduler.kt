package com.taskflow.core.notification

import com.taskflow.core.model.Task

interface ReminderScheduler {

    suspend fun schedule(task: Task)

    fun cancel(taskId: String)
}

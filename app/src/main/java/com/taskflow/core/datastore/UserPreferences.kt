package com.taskflow.core.datastore

import com.taskflow.core.model.Priority
import com.taskflow.core.model.ThemeMode

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultPriority: Priority = Priority.MEDIUM,
    val defaultReminderMinutesBefore: Int = 10,
    val weekStartsOnMonday: Boolean = true,
    val taskRemindersEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = true,
    val completedTaskSoundEnabled: Boolean = false,
)

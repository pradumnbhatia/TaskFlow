package com.taskflow.core.datastore

import com.taskflow.core.model.Priority
import com.taskflow.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    val userPreferences: Flow<UserPreferences>

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setDefaultPriority(priority: Priority)

    suspend fun setDefaultReminderMinutesBefore(minutes: Int)

    suspend fun setWeekStartsOnMonday(startsOnMonday: Boolean)

    suspend fun setTaskRemindersEnabled(enabled: Boolean)

    suspend fun setDailySummaryEnabled(enabled: Boolean)

    suspend fun setCompletedTaskSoundEnabled(enabled: Boolean)
}

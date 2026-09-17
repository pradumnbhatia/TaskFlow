package com.taskflow.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.taskflow.core.model.Priority
import com.taskflow.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private object PreferencesKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val DEFAULT_PRIORITY = stringPreferencesKey("default_priority")
    val DEFAULT_REMINDER_MINUTES_BEFORE = intPreferencesKey("default_reminder_minutes_before")
    val WEEK_STARTS_ON_MONDAY = booleanPreferencesKey("week_starts_on_monday")
    val TASK_REMINDERS_ENABLED = booleanPreferencesKey("task_reminders_enabled")
    val DAILY_SUMMARY_ENABLED = booleanPreferencesKey("daily_summary_enabled")
    val COMPLETED_TASK_SOUND_ENABLED = booleanPreferencesKey("completed_task_sound_enabled")
}

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            themeMode = prefs[PreferencesKeys.THEME_MODE]?.let(ThemeMode::valueOf) ?: ThemeMode.SYSTEM,
            defaultPriority = prefs[PreferencesKeys.DEFAULT_PRIORITY]?.let(Priority::valueOf) ?: Priority.MEDIUM,
            defaultReminderMinutesBefore = prefs[PreferencesKeys.DEFAULT_REMINDER_MINUTES_BEFORE] ?: 10,
            weekStartsOnMonday = prefs[PreferencesKeys.WEEK_STARTS_ON_MONDAY] ?: true,
            taskRemindersEnabled = prefs[PreferencesKeys.TASK_REMINDERS_ENABLED] ?: true,
            dailySummaryEnabled = prefs[PreferencesKeys.DAILY_SUMMARY_ENABLED] ?: true,
            completedTaskSoundEnabled = prefs[PreferencesKeys.COMPLETED_TASK_SOUND_ENABLED] ?: false,
        )
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { it[PreferencesKeys.THEME_MODE] = themeMode.name }
    }

    override suspend fun setDefaultPriority(priority: Priority) {
        dataStore.edit { it[PreferencesKeys.DEFAULT_PRIORITY] = priority.name }
    }

    override suspend fun setDefaultReminderMinutesBefore(minutes: Int) {
        dataStore.edit { it[PreferencesKeys.DEFAULT_REMINDER_MINUTES_BEFORE] = minutes }
    }

    override suspend fun setWeekStartsOnMonday(startsOnMonday: Boolean) {
        dataStore.edit { it[PreferencesKeys.WEEK_STARTS_ON_MONDAY] = startsOnMonday }
    }

    override suspend fun setTaskRemindersEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.TASK_REMINDERS_ENABLED] = enabled }
    }

    override suspend fun setDailySummaryEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.DAILY_SUMMARY_ENABLED] = enabled }
    }

    override suspend fun setCompletedTaskSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.COMPLETED_TASK_SOUND_ENABLED] = enabled }
    }
}

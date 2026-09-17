package com.taskflow.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.core.datastore.UserPreferences
import com.taskflow.core.datastore.UserPreferencesRepository
import com.taskflow.core.model.Priority
import com.taskflow.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences?> = userPreferencesRepository.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { userPreferencesRepository.setThemeMode(mode) }
    }

    fun setDefaultPriority(priority: Priority) {
        viewModelScope.launch { userPreferencesRepository.setDefaultPriority(priority) }
    }

    fun setDefaultReminderMinutesBefore(minutes: Int) {
        viewModelScope.launch { userPreferencesRepository.setDefaultReminderMinutesBefore(minutes) }
    }

    fun setWeekStartsOnMonday(startsOnMonday: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setWeekStartsOnMonday(startsOnMonday) }
    }

    fun setTaskRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setTaskRemindersEnabled(enabled) }
    }

    fun setDailySummaryEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setDailySummaryEnabled(enabled) }
    }

    fun setCompletedTaskSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setCompletedTaskSoundEnabled(enabled) }
    }
}

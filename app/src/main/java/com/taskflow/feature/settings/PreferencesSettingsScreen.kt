package com.taskflow.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskflow.R
import com.taskflow.core.designsystem.component.PrioritySelector

private val reminderPresetsMinutes = listOf(5, 10, 15, 30)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferencesSettingsRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    SettingsScaffold(
        title = stringResource(R.string.settings_preferences_title),
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    ) { padding ->
        val prefs = preferences ?: return@SettingsScaffold
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            PreferenceSection(title = stringResource(R.string.settings_preferences_default_priority)) {
                PrioritySelector(selected = prefs.defaultPriority, onSelected = viewModel::setDefaultPriority)
            }
            PreferenceSection(title = stringResource(R.string.settings_preferences_default_reminder)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    reminderPresetsMinutes.forEach { minutes ->
                        FilterChip(
                            selected = prefs.defaultReminderMinutesBefore == minutes,
                            onClick = { viewModel.setDefaultReminderMinutesBefore(minutes) },
                            label = { Text(stringResource(R.string.settings_preferences_reminder_minutes, minutes)) },
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.settings_preferences_week_starts_monday),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(checked = prefs.weekStartsOnMonday, onCheckedChange = viewModel::setWeekStartsOnMonday)
            }
        }
    }
}

@Composable
private fun PreferenceSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        content()
    }
}

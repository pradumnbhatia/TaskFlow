package com.taskflow.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

@Composable
fun NotificationsSettingsRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    SettingsScaffold(
        title = stringResource(R.string.settings_notifications_title),
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    ) { padding ->
        val prefs = preferences ?: return@SettingsScaffold
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            SwitchRow(
                label = stringResource(R.string.settings_notifications_task_reminders),
                checked = prefs.taskRemindersEnabled,
                onCheckedChange = viewModel::setTaskRemindersEnabled,
            )
            SwitchRow(
                label = stringResource(R.string.settings_notifications_daily_summary),
                checked = prefs.dailySummaryEnabled,
                onCheckedChange = viewModel::setDailySummaryEnabled,
            )
            SwitchRow(
                label = stringResource(R.string.settings_notifications_completed_sound),
                checked = prefs.completedTaskSoundEnabled,
                onCheckedChange = viewModel::setCompletedTaskSoundEnabled,
            )
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

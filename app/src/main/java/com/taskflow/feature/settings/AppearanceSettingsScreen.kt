package com.taskflow.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
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
import com.taskflow.core.model.ThemeMode

@Composable
fun AppearanceSettingsRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    SettingsScaffold(
        title = stringResource(R.string.settings_appearance_title),
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    ) { padding ->
        val prefs = preferences ?: return@SettingsScaffold
        Column(modifier = Modifier.padding(padding)) {
            ThemeOptionRow(
                mode = ThemeMode.SYSTEM,
                label = stringResource(R.string.settings_appearance_system),
                selected = prefs.themeMode,
                onSelected = viewModel::setThemeMode,
            )
            ThemeOptionRow(
                mode = ThemeMode.LIGHT,
                label = stringResource(R.string.settings_appearance_light),
                selected = prefs.themeMode,
                onSelected = viewModel::setThemeMode,
            )
            ThemeOptionRow(
                mode = ThemeMode.DARK,
                label = stringResource(R.string.settings_appearance_dark),
                selected = prefs.themeMode,
                onSelected = viewModel::setThemeMode,
            )
        }
    }
}

@Composable
private fun ThemeOptionRow(
    mode: ThemeMode,
    label: String,
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(mode) }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected == mode, onClick = { onSelected(mode) })
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

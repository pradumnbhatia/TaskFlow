package com.taskflow.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskflow.R
import com.taskflow.core.common.DEFAULT_USER_NAME
import com.taskflow.core.designsystem.component.InitialsAvatar
import com.taskflow.core.designsystem.component.StatColumn
import com.taskflow.core.domain.ProductivityStats

@Composable
fun ProfileRoute(
    onNotificationsClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(
        uiState = uiState,
        onNotificationsClick = onNotificationsClick,
        onAppearanceClick = onAppearanceClick,
        onPreferencesClick = onPreferencesClick,
        onAboutClick = onAboutClick,
        modifier = modifier,
    )
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onNotificationsClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        when (uiState) {
            is ProfileUiState.Loading -> Box(modifier = Modifier.padding(innerPadding).fillMaxSize())
            is ProfileUiState.Loaded -> ProfileContent(
                stats = uiState.stats,
                onNotificationsClick = onNotificationsClick,
                onAppearanceClick = onAppearanceClick,
                onPreferencesClick = onPreferencesClick,
                onAboutClick = onAboutClick,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ProfileContent(
    stats: ProductivityStats,
    onNotificationsClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            InitialsAvatar(name = DEFAULT_USER_NAME, size = 72.dp)
            Text(
                text = DEFAULT_USER_NAME,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = stringResource(R.string.profile_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatColumn(value = stats.tasksDone.toString(), label = stringResource(R.string.profile_stat_tasks_done))
                StatColumn(
                    value = "${stats.completionRatePercent}%",
                    label = stringResource(R.string.profile_stat_completion_rate),
                )
                StatColumn(
                    value = stats.currentStreakDays.toString(),
                    label = stringResource(R.string.profile_stat_streak),
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

        Text(
            text = stringResource(R.string.profile_settings_section),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        SettingsRow(
            icon = Icons.Filled.Notifications,
            label = stringResource(R.string.profile_notifications),
            onClick = onNotificationsClick,
        )
        SettingsRow(
            icon = Icons.Filled.Palette,
            label = stringResource(R.string.profile_appearance),
            onClick = onAppearanceClick,
        )
        SettingsRow(
            icon = Icons.Filled.Tune,
            label = stringResource(R.string.profile_preferences),
            onClick = onPreferencesClick,
        )
        SettingsRow(
            icon = Icons.Filled.Info,
            label = stringResource(R.string.profile_about),
            onClick = onAboutClick,
        )
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.padding(4.dp),
            )
        },
    )
}

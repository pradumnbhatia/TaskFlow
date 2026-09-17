package com.taskflow.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskflow.R
import com.taskflow.core.common.DEFAULT_USER_NAME
import com.taskflow.core.designsystem.component.EmptyState
import com.taskflow.core.designsystem.component.InitialsAvatar
import com.taskflow.core.designsystem.component.TaskListItem
import com.taskflow.core.model.Task

@Composable
fun HomeRoute(
    onSeeAllTasks: () -> Unit,
    onAddTask: () -> Unit,
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onSeeAllTasks = onSeeAllTasks,
        onAddTask = onAddTask,
        onTaskClick = onTaskClick,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSeeAllTasks: () -> Unit,
    onAddTask: () -> Unit,
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.home_add_task_cd))
            }
        },
    ) { innerPadding ->
        when (uiState) {
            is HomeUiState.Loading -> Box(modifier = Modifier.padding(innerPadding).fillMaxSize())
            is HomeUiState.Loaded -> HomeContent(
                uiState = uiState,
                onSeeAllTasks = onSeeAllTasks,
                onAddTask = onAddTask,
                onTaskClick = onTaskClick,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState.Loaded,
    onSeeAllTasks: () -> Unit,
    onAddTask: () -> Unit,
    onTaskClick: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    if (uiState.totalTasksToday == 0) {
        EmptyState(
            icon = Icons.Filled.Checklist,
            title = stringResource(R.string.home_empty_title),
            message = stringResource(R.string.home_empty_message),
            actionLabel = stringResource(R.string.home_empty_cta),
            onAction = onAddTask,
            modifier = Modifier.padding(contentPadding),
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(greetingRes(uiState.greetingPeriod), uiState.userName),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = stringResource(R.string.home_stay_focused),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                InitialsAvatar(name = DEFAULT_USER_NAME)
            }
        }
        item {
            TodayProgressCard(
                totalTasks = uiState.totalTasksToday,
                completedTasks = uiState.completedTasksToday,
                completionPercent = uiState.completionPercent,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.home_today_tasks_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = onSeeAllTasks) {
                    Text(stringResource(R.string.home_see_all))
                }
            }
        }
        items(items = uiState.todayTasks, key = Task::id) { task ->
            TaskListItem(task = task, onClick = { onTaskClick(task.id) })
        }
    }
}

@Composable
private fun TodayProgressCard(
    totalTasks: Int,
    completedTasks: Int,
    completionPercent: Int,
    modifier: Modifier = Modifier,
) {
    val pendingTasks = totalTasks - completedTasks
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.home_progress_label),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.home_progress_completed, completedTasks, totalTasks),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { completionPercent / 100f },
                        modifier = Modifier.size(56.dp),
                    )
                    Text(
                        text = "$completionPercent%",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { completionPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatColumn(value = totalTasks, label = stringResource(R.string.home_stat_total))
                StatColumn(value = completedTasks, label = stringResource(R.string.home_stat_completed))
                StatColumn(value = pendingTasks, label = stringResource(R.string.home_stat_pending))
            }
        }
    }
}

@Composable
private fun StatColumn(value: Int, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun greetingRes(period: GreetingPeriod): Int = when (period) {
    GreetingPeriod.MORNING -> R.string.home_greeting_morning
    GreetingPeriod.AFTERNOON -> R.string.home_greeting_afternoon
    GreetingPeriod.EVENING -> R.string.home_greeting_evening
}

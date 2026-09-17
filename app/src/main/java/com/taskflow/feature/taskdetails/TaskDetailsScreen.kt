package com.taskflow.feature.taskdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskflow.R
import com.taskflow.core.common.label
import com.taskflow.core.common.toDisplayDateString
import com.taskflow.core.common.toDisplayString
import com.taskflow.core.common.toFullDisplayString
import com.taskflow.core.designsystem.theme.indicatorColor
import com.taskflow.core.model.Priority
import com.taskflow.core.model.TaskStatus

@Composable
fun TaskDetailsRoute(
    onNavigateBack: () -> Unit,
    onEditTask: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.deletedEvents.collect { onNavigateBack() }
    }
    TaskDetailsScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onEditTask = onEditTask,
        onToggleStatus = viewModel::onToggleStatus,
        onDeleteConfirmed = viewModel::onDeleteConfirmed,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    uiState: TaskDetailsUiState,
    onNavigateBack: () -> Unit,
    onEditTask: (String) -> Unit,
    onToggleStatus: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.task_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.task_details_back_cd),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            is TaskDetailsUiState.Loading -> Box(modifier = Modifier.padding(innerPadding).fillMaxSize())
            is TaskDetailsUiState.NotFound -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.task_details_not_found))
            }
            is TaskDetailsUiState.Loaded -> TaskDetailsContent(
                uiState = uiState,
                onToggleStatus = onToggleStatus,
                onEditTask = { onEditTask(uiState.task.id) },
                onDeleteClick = { showDeleteDialog = true },
                contentPadding = innerPadding,
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.task_details_delete_dialog_title)) },
            text = { Text(stringResource(R.string.task_details_delete_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteConfirmed()
                }) {
                    Text(
                        text = stringResource(R.string.task_details_delete_dialog_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.task_details_delete_dialog_cancel))
                }
            },
        )
    }
}

@Composable
private fun TaskDetailsContent(
    uiState: TaskDetailsUiState.Loaded,
    onToggleStatus: () -> Unit,
    onEditTask: () -> Unit,
    onDeleteClick: () -> Unit,
    contentPadding: PaddingValues,
) {
    val task = uiState.task
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(text = task.title, style = MaterialTheme.typography.headlineSmall)

        PriorityBadge(priority = task.priority, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))

        if (task.description.isNotBlank()) {
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }

        DetailRow(
            label = stringResource(R.string.task_details_due_label),
            value = task.dueDate?.let { date ->
                listOfNotNull(date.toFullDisplayString(), task.dueTime?.toDisplayString()).joinToString(" · ")
            } ?: stringResource(R.string.task_details_due_no_date),
        )
        DetailRow(
            label = stringResource(R.string.task_details_reminder_label),
            value = if (task.reminderEnabled) {
                stringResource(R.string.task_details_reminder_on, uiState.reminderMinutesBefore)
            } else {
                stringResource(R.string.task_details_reminder_off)
            },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        DetailRow(
            label = stringResource(R.string.task_details_created_label),
            value = task.createdAt.toDisplayDateString(),
        )
        DetailRow(
            label = stringResource(R.string.task_details_status_label),
            value = if (task.status == TaskStatus.COMPLETED) {
                stringResource(R.string.task_details_status_completed)
            } else {
                stringResource(R.string.task_details_status_pending)
            },
        )
        DetailRow(
            label = stringResource(R.string.task_details_updated_label),
            value = task.updatedAt.toDisplayDateString(),
        )

        Button(
            onClick = onToggleStatus,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        ) {
            Text(
                stringResource(
                    if (task.status == TaskStatus.COMPLETED) {
                        R.string.task_details_mark_pending
                    } else {
                        R.string.task_details_mark_completed
                    },
                ),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TextButton(onClick = onEditTask) {
                Text(stringResource(R.string.task_details_edit))
            }
            TextButton(onClick = onDeleteClick) {
                Text(
                    text = stringResource(R.string.task_details_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(priority.indicatorColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = priority.label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}

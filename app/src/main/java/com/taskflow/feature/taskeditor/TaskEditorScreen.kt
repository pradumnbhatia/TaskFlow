package com.taskflow.feature.taskeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskflow.R
import com.taskflow.core.common.label
import com.taskflow.core.common.toDisplayString
import com.taskflow.core.common.toFullDisplayString
import com.taskflow.core.designsystem.theme.indicatorColor
import com.taskflow.core.model.Priority
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@Composable
fun TaskEditorRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.saveCompletedEvents.collect { onNavigateBack() }
    }
    TaskEditorScreen(
        uiState = uiState,
        onTitleChanged = viewModel::onTitleChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onPrioritySelected = viewModel::onPrioritySelected,
        onDueDateSelected = viewModel::onDueDateSelected,
        onDueTimeSelected = viewModel::onDueTimeSelected,
        onReminderToggled = viewModel::onReminderToggled,
        onSaveClicked = viewModel::onSaveClicked,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(
    uiState: TaskEditorUiState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onPrioritySelected: (Priority) -> Unit,
    onDueDateSelected: (LocalDate?) -> Unit,
    onDueTimeSelected: (LocalTime?) -> Unit,
    onReminderToggled: (Boolean) -> Unit,
    onSaveClicked: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (uiState.mode == TaskEditorMode.EDIT) {
                                R.string.task_editor_title_edit
                            } else {
                                R.string.task_editor_title_create
                            },
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.task_editor_back_cd),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize())
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                EditorField(label = stringResource(R.string.task_editor_field_title)) {
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = onTitleChanged,
                        placeholder = { Text(stringResource(R.string.task_editor_field_title_placeholder)) },
                        isError = uiState.titleError,
                        supportingText = if (uiState.titleError) {
                            { Text(stringResource(R.string.task_editor_field_title_error)) }
                        } else {
                            null
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                EditorField(label = stringResource(R.string.task_editor_field_description)) {
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = onDescriptionChanged,
                        placeholder = { Text(stringResource(R.string.task_editor_field_description_placeholder)) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                EditorField(label = stringResource(R.string.task_editor_priority_label)) {
                    PrioritySelector(selected = uiState.priority, onSelected = onPrioritySelected)
                }

                EditorField(label = stringResource(R.string.task_editor_due_date_label)) {
                    EditorPickerRow(
                        icon = Icons.Filled.CalendarToday,
                        text = uiState.dueDate?.toFullDisplayString()
                            ?: stringResource(R.string.task_editor_due_date_placeholder),
                        onClick = { showDatePicker = true },
                    )
                }

                EditorField(label = stringResource(R.string.task_editor_due_time_label)) {
                    EditorPickerRow(
                        icon = Icons.Filled.AccessTime,
                        text = uiState.dueTime?.toDisplayString()
                            ?: stringResource(R.string.task_editor_due_time_placeholder),
                        onClick = { showTimePicker = true },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.task_editor_reminder_label),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = stringResource(R.string.task_editor_reminder_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = uiState.reminderEnabled, onCheckedChange = onReminderToggled)
                }

                Button(
                    onClick = onSaveClicked,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text(
                        stringResource(
                            if (uiState.mode == TaskEditorMode.EDIT) {
                                R.string.task_editor_save_edit
                            } else {
                                R.string.task_editor_save_create
                            },
                        ),
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DueDatePickerDialog(
            initialDate = uiState.dueDate,
            onConfirm = onDueDateSelected,
            onDismiss = { showDatePicker = false },
        )
    }
    if (showTimePicker) {
        DueTimePickerDialog(
            initialTime = uiState.dueTime,
            onConfirm = onDueTimeSelected,
            onDismiss = { showTimePicker = false },
        )
    }
}

@Composable
private fun EditorField(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        content()
    }
}

@Composable
private fun EditorPickerRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text)
        }
    }
}

@Composable
private fun PrioritySelector(selected: Priority, onSelected: (Priority) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Priority.entries.forEach { priority ->
            FilterChip(
                selected = selected == priority,
                onClick = { onSelected(priority) },
                label = { Text(priority.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = priority.indicatorColor,
                    selectedLabelColor = Color.White,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDatePickerDialog(
    initialDate: LocalDate?,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialMillis = (initialDate ?: LocalDate.now())
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                }
                onDismiss()
            }) {
                Text(stringResource(R.string.task_editor_date_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.task_editor_date_picker_cancel))
            }
        },
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueTimePickerDialog(
    initialTime: LocalTime?,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val time = initialTime ?: LocalTime.of(9, 0)
    val state = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(state.hour, state.minute))
                onDismiss()
            }) {
                Text(stringResource(R.string.task_editor_date_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.task_editor_date_picker_cancel))
            }
        },
        text = { TimePicker(state = state) },
    )
}

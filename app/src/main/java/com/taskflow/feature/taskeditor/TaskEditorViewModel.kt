package com.taskflow.feature.taskeditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.core.data.TaskRepository
import com.taskflow.core.datastore.UserPreferencesRepository
import com.taskflow.core.model.Priority
import com.taskflow.core.model.Task
import com.taskflow.core.model.TaskStatus
import com.taskflow.core.navigation.TASK_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val editingTaskId: String? = savedStateHandle[TASK_ID_ARG]

    private var originalTask: Task? = null

    private val _uiState = MutableStateFlow(
        TaskEditorUiState(
            mode = if (editingTaskId != null) TaskEditorMode.EDIT else TaskEditorMode.CREATE,
            isLoading = editingTaskId != null,
        ),
    )
    val uiState: StateFlow<TaskEditorUiState> = _uiState.asStateFlow()

    private val saveCompletedChannel = Channel<Unit>(Channel.CONFLATED)
    val saveCompletedEvents: Flow<Unit> = saveCompletedChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            if (editingTaskId != null) {
                val task = taskRepository.getTaskById(editingTaskId)
                if (task != null) {
                    originalTask = task
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = task.title,
                            description = task.description,
                            priority = task.priority,
                            dueDate = task.dueDate,
                            dueTime = task.dueTime,
                            reminderEnabled = task.reminderEnabled,
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } else {
                val defaultPriority = userPreferencesRepository.userPreferences.first().defaultPriority
                _uiState.update { it.copy(priority = defaultPriority) }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title, titleError = false) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onPrioritySelected(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onDueDateSelected(dueDate: LocalDate?) {
        _uiState.update { it.copy(dueDate = dueDate) }
    }

    fun onDueTimeSelected(dueTime: LocalTime?) {
        _uiState.update { it.copy(dueTime = dueTime) }
    }

    fun onReminderToggled(enabled: Boolean) {
        _uiState.update { it.copy(reminderEnabled = enabled) }
    }

    fun onSaveClicked() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val now = Instant.now()
            val task = Task(
                id = originalTask?.id ?: UUID.randomUUID().toString(),
                title = state.title.trim(),
                description = state.description.trim(),
                priority = state.priority,
                status = originalTask?.status ?: TaskStatus.PENDING,
                dueDate = state.dueDate,
                dueTime = state.dueTime,
                reminderEnabled = state.reminderEnabled,
                createdAt = originalTask?.createdAt ?: now,
                completedAt = originalTask?.completedAt,
                updatedAt = now,
            )
            taskRepository.upsertTask(task)
            _uiState.update { it.copy(isSaving = false) }
            saveCompletedChannel.trySend(Unit)
        }
    }
}

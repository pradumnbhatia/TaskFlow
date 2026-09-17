package com.taskflow.feature.taskdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.core.data.TaskRepository
import com.taskflow.core.datastore.UserPreferencesRepository
import com.taskflow.core.model.TaskStatus
import com.taskflow.core.navigation.TASK_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val taskId: String = checkNotNull(savedStateHandle[TASK_ID_ARG])

    val uiState: StateFlow<TaskDetailsUiState> = combine(
        taskRepository.observeTaskById(taskId),
        userPreferencesRepository.userPreferences,
    ) { task, prefs ->
        if (task != null) {
            TaskDetailsUiState.Loaded(task = task, reminderMinutesBefore = prefs.defaultReminderMinutesBefore)
        } else {
            TaskDetailsUiState.NotFound
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskDetailsUiState.Loading,
    )

    private val deletedChannel = Channel<Unit>(Channel.CONFLATED)
    val deletedEvents: Flow<Unit> = deletedChannel.receiveAsFlow()

    private val errorChannel = Channel<Unit>(Channel.CONFLATED)
    val errorEvents: Flow<Unit> = errorChannel.receiveAsFlow()

    fun onToggleStatus() {
        val loaded = uiState.value as? TaskDetailsUiState.Loaded ?: return
        viewModelScope.launch {
            try {
                val newStatus = if (loaded.task.status == TaskStatus.COMPLETED) {
                    TaskStatus.PENDING
                } else {
                    TaskStatus.COMPLETED
                }
                taskRepository.setTaskStatus(loaded.task.id, newStatus)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorChannel.trySend(Unit)
            }
        }
    }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(taskId)
                deletedChannel.trySend(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorChannel.trySend(Unit)
            }
        }
    }
}

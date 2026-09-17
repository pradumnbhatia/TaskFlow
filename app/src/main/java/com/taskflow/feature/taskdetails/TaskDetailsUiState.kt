package com.taskflow.feature.taskdetails

import com.taskflow.core.model.Task

sealed interface TaskDetailsUiState {

    data object Loading : TaskDetailsUiState

    data object NotFound : TaskDetailsUiState

    data class Loaded(
        val task: Task,
        val reminderMinutesBefore: Int,
    ) : TaskDetailsUiState
}

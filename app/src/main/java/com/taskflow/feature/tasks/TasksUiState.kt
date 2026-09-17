package com.taskflow.feature.tasks

import com.taskflow.core.model.Task

sealed interface TasksUiState {

    data object Loading : TasksUiState

    data class Loaded(
        val filter: TasksFilterState,
        val tasks: List<Task>,
        val isSearchActive: Boolean,
    ) : TasksUiState
}

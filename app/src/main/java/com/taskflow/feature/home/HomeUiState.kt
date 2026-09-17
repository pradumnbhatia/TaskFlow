package com.taskflow.feature.home

import com.taskflow.core.model.Task

enum class GreetingPeriod {
    MORNING,
    AFTERNOON,
    EVENING,
}

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Loaded(
        val userName: String,
        val greetingPeriod: GreetingPeriod,
        val totalTasksToday: Int,
        val completedTasksToday: Int,
        val completionPercent: Int,
        val todayTasks: List<Task>,
    ) : HomeUiState
}

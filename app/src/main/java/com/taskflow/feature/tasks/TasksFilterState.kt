package com.taskflow.feature.tasks

import com.taskflow.core.model.Priority

enum class TaskListFilter {
    ALL,
    TODAY,
    UPCOMING,
    COMPLETED,
}

enum class TaskSortOrder {
    DUE_DATE_EARLIEST,
    DUE_DATE_LATEST,
    PRIORITY_HIGH_FIRST,
}

data class TasksFilterState(
    val listFilter: TaskListFilter = TaskListFilter.ALL,
    val priorityFilter: Priority? = null,
    val searchQuery: String = "",
    val sortOrder: TaskSortOrder = TaskSortOrder.DUE_DATE_EARLIEST,
)

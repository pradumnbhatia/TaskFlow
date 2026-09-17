package com.taskflow.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.core.data.TaskRepository
import com.taskflow.core.model.Priority
import com.taskflow.core.model.Task
import com.taskflow.core.model.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val filterState = MutableStateFlow(TasksFilterState())
    private val isSearchActive = MutableStateFlow(false)

    val uiState: StateFlow<TasksUiState> = combine(
        taskRepository.observeTasks(),
        filterState,
        isSearchActive,
    ) { tasks, filter, searchActive ->
        TasksUiState.Loaded(
            filter = filter,
            tasks = tasks.applyFilter(filter),
            isSearchActive = searchActive,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TasksUiState.Loading,
    )

    fun onListFilterSelected(filter: TaskListFilter) {
        filterState.update { it.copy(listFilter = filter) }
    }

    fun onPriorityFilterSelected(priority: Priority?) {
        filterState.update { it.copy(priorityFilter = priority) }
    }

    fun onSortOrderSelected(sortOrder: TaskSortOrder) {
        filterState.update { it.copy(sortOrder = sortOrder) }
    }

    fun onClearFilters() {
        filterState.update { it.copy(priorityFilter = null, sortOrder = TaskSortOrder.DUE_DATE_EARLIEST) }
    }

    fun onSearchQueryChanged(query: String) {
        filterState.update { it.copy(searchQuery = query) }
    }

    fun onSearchActiveChanged(active: Boolean) {
        isSearchActive.value = active
        if (!active) {
            filterState.update { it.copy(searchQuery = "") }
        }
    }

    fun onToggleTaskStatus(task: Task) {
        viewModelScope.launch {
            val newStatus = if (task.status == TaskStatus.COMPLETED) TaskStatus.PENDING else TaskStatus.COMPLETED
            taskRepository.setTaskStatus(task.id, newStatus)
        }
    }
}

private fun List<Task>.applyFilter(filter: TasksFilterState): List<Task> {
    val today = LocalDate.now()

    val byListFilter = when (filter.listFilter) {
        TaskListFilter.ALL -> filter { it.status == TaskStatus.PENDING }
        TaskListFilter.TODAY -> filter { it.status == TaskStatus.PENDING && it.dueDate == today }
        TaskListFilter.UPCOMING -> filter {
            it.status == TaskStatus.PENDING && it.dueDate != null && it.dueDate.isAfter(today)
        }
        TaskListFilter.COMPLETED -> filter { it.status == TaskStatus.COMPLETED }
    }

    val byPriority = filter.priorityFilter?.let { priority ->
        byListFilter.filter { it.priority == priority }
    } ?: byListFilter

    val bySearch = if (filter.searchQuery.isBlank()) {
        byPriority
    } else {
        byPriority.filter { it.title.contains(filter.searchQuery, ignoreCase = true) }
    }

    return when (filter.sortOrder) {
        TaskSortOrder.DUE_DATE_EARLIEST -> bySearch.sortedWith(
            compareBy(nullsLast<LocalDate>()) { it.dueDate },
        )
        TaskSortOrder.DUE_DATE_LATEST -> bySearch.sortedWith(
            compareByDescending(nullsFirst<LocalDate>()) { it.dueDate },
        )
        TaskSortOrder.PRIORITY_HIGH_FIRST -> bySearch.sortedByDescending { it.priority.ordinal }
    }
}

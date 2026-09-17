package com.taskflow.feature.tasks

import com.taskflow.MainDispatcherRule
import com.taskflow.core.data.TaskRepository
import com.taskflow.core.model.Priority
import com.taskflow.core.model.Task
import com.taskflow.core.model.TaskStatus
import com.taskflow.core.model.testTask
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TasksViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.now()

    private val pendingToday = testTask(title = "Due today", status = TaskStatus.PENDING, dueDate = today)
    private val pendingUpcoming = testTask(
        title = "Due next week",
        status = TaskStatus.PENDING,
        dueDate = today.plusDays(7),
        priority = Priority.HIGH,
    )
    private val completed = testTask(title = "Done", status = TaskStatus.COMPLETED)

    private fun viewModel(tasks: List<Task> = listOf(pendingToday, pendingUpcoming, completed)): TasksViewModel {
        val repository = mockk<TaskRepository>()
        every { repository.observeTasks() } returns flowOf(tasks)
        return TasksViewModel(repository)
    }

    @Test
    fun `ALL filter excludes completed tasks`() = runTest {
        val viewModel = viewModel()
        val job = launch { viewModel.uiState.collect {} }
        runCurrent()

        val state = viewModel.uiState.value as TasksUiState.Loaded
        assertEquals(listOf(pendingToday, pendingUpcoming), state.tasks)

        job.cancel()
    }

    @Test
    fun `TODAY filter keeps only tasks due today`() = runTest {
        val viewModel = viewModel()
        val job = launch { viewModel.uiState.collect {} }
        runCurrent()

        viewModel.onListFilterSelected(TaskListFilter.TODAY)

        val state = viewModel.uiState.value as TasksUiState.Loaded
        assertEquals(listOf(pendingToday), state.tasks)

        job.cancel()
    }

    @Test
    fun `COMPLETED filter shows only completed tasks`() = runTest {
        val viewModel = viewModel()
        val job = launch { viewModel.uiState.collect {} }
        runCurrent()

        viewModel.onListFilterSelected(TaskListFilter.COMPLETED)

        val state = viewModel.uiState.value as TasksUiState.Loaded
        assertEquals(listOf(completed), state.tasks)

        job.cancel()
    }

    @Test
    fun `priority filter narrows the list`() = runTest {
        val viewModel = viewModel()
        val job = launch { viewModel.uiState.collect {} }
        runCurrent()

        viewModel.onPriorityFilterSelected(Priority.HIGH)

        val state = viewModel.uiState.value as TasksUiState.Loaded
        assertEquals(listOf(pendingUpcoming), state.tasks)

        job.cancel()
    }

    @Test
    fun `search query matches title case-insensitively`() = runTest {
        val viewModel = viewModel()
        val job = launch { viewModel.uiState.collect {} }
        runCurrent()

        viewModel.onSearchQueryChanged("due TODAY")

        val state = viewModel.uiState.value as TasksUiState.Loaded
        assertEquals(listOf(pendingToday), state.tasks)

        job.cancel()
    }

    @Test
    fun `sorting by priority puts HIGH before MEDIUM`() = runTest {
        val viewModel = viewModel()
        val job = launch { viewModel.uiState.collect {} }
        runCurrent()

        viewModel.onSortOrderSelected(TaskSortOrder.PRIORITY_HIGH_FIRST)

        val state = viewModel.uiState.value as TasksUiState.Loaded
        assertEquals(listOf(pendingUpcoming, pendingToday), state.tasks)

        job.cancel()
    }

    @Test
    fun `clearing filters resets priority and sort order but keeps the list tab`() = runTest {
        val viewModel = viewModel()
        val job = launch { viewModel.uiState.collect {} }
        runCurrent()

        viewModel.onListFilterSelected(TaskListFilter.TODAY)
        viewModel.onPriorityFilterSelected(Priority.HIGH)
        viewModel.onSortOrderSelected(TaskSortOrder.PRIORITY_HIGH_FIRST)
        viewModel.onClearFilters()

        val state = viewModel.uiState.value as TasksUiState.Loaded
        assertEquals(TaskListFilter.TODAY, state.filter.listFilter)
        assertEquals(null, state.filter.priorityFilter)
        assertEquals(TaskSortOrder.DUE_DATE_EARLIEST, state.filter.sortOrder)

        job.cancel()
    }
}

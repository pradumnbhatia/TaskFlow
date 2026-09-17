package com.taskflow.feature.taskeditor

import androidx.lifecycle.SavedStateHandle
import com.taskflow.MainDispatcherRule
import com.taskflow.core.data.TaskRepository
import com.taskflow.core.datastore.UserPreferences
import com.taskflow.core.datastore.UserPreferencesRepository
import com.taskflow.core.model.Priority
import com.taskflow.core.model.Task
import com.taskflow.core.model.testTask
import com.taskflow.core.navigation.TASK_ID_ARG
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TaskEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val taskRepository = mockk<TaskRepository>(relaxUnitFun = true)
    private val userPreferencesRepository = mockk<UserPreferencesRepository> {
        every { userPreferences } returns flowOf(UserPreferences(defaultPriority = Priority.LOW))
    }

    private fun viewModel(taskId: String? = null) = TaskEditorViewModel(
        savedStateHandle = if (taskId != null) SavedStateHandle(mapOf(TASK_ID_ARG to taskId)) else SavedStateHandle(),
        taskRepository = taskRepository,
        userPreferencesRepository = userPreferencesRepository,
    )

    @Test
    fun `blank title is rejected without saving`() = runTest {
        val viewModel = viewModel()

        viewModel.onSaveClicked()

        assertTrue(viewModel.uiState.value.titleError)
        coVerify(exactly = 0) { taskRepository.upsertTask(any()) }
    }

    @Test
    fun `valid title is trimmed and saved`() = runTest {
        val viewModel = viewModel()
        val events = mutableListOf<Unit>()
        val job = launch { viewModel.saveCompletedEvents.collect { events.add(it) } }
        val savedTask = slot<Task>()
        coEvery { taskRepository.upsertTask(capture(savedTask)) } returns Unit

        viewModel.onTitleChanged("  Buy milk  ")
        viewModel.onSaveClicked()
        runCurrent()

        assertEquals("Buy milk", savedTask.captured.title)
        assertEquals(1, events.size)
        assertTrue(!viewModel.uiState.value.isSaving)

        job.cancel()
    }

    @Test
    fun `create mode defaults to the user's preferred priority`() = runTest {
        val viewModel = viewModel()

        assertEquals(Priority.LOW, viewModel.uiState.value.priority)
    }

    @Test
    fun `edit mode loads the existing task into the form`() = runTest {
        val existing = testTask(id = "task-1", title = "Existing task", priority = Priority.HIGH)
        coEvery { taskRepository.getTaskById("task-1") } returns existing

        val viewModel = viewModel(taskId = "task-1")

        assertEquals(TaskEditorMode.EDIT, viewModel.uiState.value.mode)
        assertEquals("Existing task", viewModel.uiState.value.title)
        assertEquals(Priority.HIGH, viewModel.uiState.value.priority)
    }

    @Test
    fun `a failed save surfaces an error instead of navigating away`() = runTest {
        coEvery { taskRepository.upsertTask(any()) } throws RuntimeException("boom")
        val viewModel = viewModel()
        val saveEvents = mutableListOf<Unit>()
        val errorEvents = mutableListOf<Unit>()
        val saveJob = launch { viewModel.saveCompletedEvents.collect { saveEvents.add(it) } }
        val errorJob = launch { viewModel.errorEvents.collect { errorEvents.add(it) } }

        viewModel.onTitleChanged("Task title")
        viewModel.onSaveClicked()
        runCurrent()

        assertEquals(0, saveEvents.size)
        assertEquals(1, errorEvents.size)
        assertTrue(!viewModel.uiState.value.isSaving)

        saveJob.cancel()
        errorJob.cancel()
    }
}

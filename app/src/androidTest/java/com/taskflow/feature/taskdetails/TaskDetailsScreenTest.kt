package com.taskflow.feature.taskdetails

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.taskflow.R
import com.taskflow.core.designsystem.theme.TaskFlowTheme
import com.taskflow.core.model.Priority
import com.taskflow.core.model.Task
import com.taskflow.core.model.TaskStatus
import java.time.Instant
import org.junit.Rule
import org.junit.Test

class TaskDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val pendingTask = Task(
        id = "task-1",
        title = "Finish report",
        priority = Priority.HIGH,
        status = TaskStatus.PENDING,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    private fun setContent(task: Task, onToggleStatus: () -> Unit = {}) {
        composeTestRule.setContent {
            TaskFlowTheme {
                TaskDetailsScreen(
                    uiState = TaskDetailsUiState.Loaded(task = task, reminderMinutesBefore = 10),
                    onNavigateBack = {},
                    onEditTask = {},
                    onToggleStatus = onToggleStatus,
                    onDeleteConfirmed = {},
                )
            }
        }
    }

    @Test
    fun markingAPendingTaskComplete_invokesOnToggleStatus() {
        var toggled = false
        setContent(task = pendingTask, onToggleStatus = { toggled = true })

        composeTestRule
            .onNodeWithText(context.getString(R.string.task_details_mark_completed))
            .performClick()

        assert(toggled)
    }

    @Test
    fun completedTask_showsMarkAsPendingAction() {
        setContent(task = pendingTask.copy(status = TaskStatus.COMPLETED))

        composeTestRule
            .onNodeWithText(context.getString(R.string.task_details_mark_pending))
            .assertIsDisplayed()
    }
}

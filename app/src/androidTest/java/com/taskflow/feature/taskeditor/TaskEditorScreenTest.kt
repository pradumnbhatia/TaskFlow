package com.taskflow.feature.taskeditor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.taskflow.R
import com.taskflow.core.designsystem.theme.TaskFlowTheme
import org.junit.Rule
import org.junit.Test

class TaskEditorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun setContent(
        uiState: TaskEditorUiState = TaskEditorUiState(),
        onSaveClicked: () -> Unit = {},
        onTitleChanged: (String) -> Unit = {},
    ) {
        composeTestRule.setContent {
            TaskFlowTheme {
                TaskEditorScreen(
                    uiState = uiState,
                    onTitleChanged = onTitleChanged,
                    onDescriptionChanged = {},
                    onPrioritySelected = {},
                    onDueDateSelected = {},
                    onDueTimeSelected = {},
                    onReminderToggled = {},
                    onSaveClicked = onSaveClicked,
                    onNavigateBack = {},
                )
            }
        }
    }

    @Test
    fun titleErrorMessage_isShown_whenTitleIsInvalid() {
        setContent(uiState = TaskEditorUiState(titleError = true))

        composeTestRule
            .onNodeWithText(context.getString(R.string.task_editor_field_title_error))
            .assertIsDisplayed()
    }

    @Test
    fun savingTask_invokesOnSaveClicked() {
        var saveClicked = false
        setContent(onSaveClicked = { saveClicked = true })

        composeTestRule
            .onNodeWithText(context.getString(R.string.task_editor_save_create))
            .performClick()

        assert(saveClicked)
    }

    @Test
    fun typingInTitleField_invokesOnTitleChanged() {
        var latestTitle = ""
        setContent(onTitleChanged = { latestTitle = it })

        composeTestRule
            .onNodeWithText(context.getString(R.string.task_editor_field_title_placeholder))
            .performTextInput("Buy milk")

        assert(latestTitle == "Buy milk")
    }
}

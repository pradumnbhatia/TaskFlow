package com.taskflow.feature.taskeditor

import com.taskflow.core.model.Priority
import java.time.LocalDate
import java.time.LocalTime

enum class TaskEditorMode {
    CREATE,
    EDIT,
}

data class TaskEditorUiState(
    val mode: TaskEditorMode = TaskEditorMode.CREATE,
    val isLoading: Boolean = false,
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val reminderEnabled: Boolean = false,
    val titleError: Boolean = false,
    val isSaving: Boolean = false,
)

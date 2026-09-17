package com.taskflow.core.navigation

const val TASK_ID_ARG = "taskId"
const val TASK_EDITOR_ROUTE = "task_editor"
const val TASK_EDITOR_ROUTE_PATTERN = "$TASK_EDITOR_ROUTE?$TASK_ID_ARG={$TASK_ID_ARG}"

fun taskEditorRoute(taskId: String? = null): String =
    if (taskId != null) "$TASK_EDITOR_ROUTE?$TASK_ID_ARG=$taskId" else TASK_EDITOR_ROUTE

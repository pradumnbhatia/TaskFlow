package com.taskflow.core.navigation

const val TASK_DETAILS_ROUTE = "task_details"
const val TASK_DETAILS_ROUTE_PATTERN = "$TASK_DETAILS_ROUTE/{$TASK_ID_ARG}"

fun taskDetailsRoute(taskId: String): String = "$TASK_DETAILS_ROUTE/$taskId"

package com.taskflow.core.navigation

const val TASK_DETAILS_ROUTE = "task_details"
const val TASK_DETAILS_ROUTE_PATTERN = "$TASK_DETAILS_ROUTE/{$TASK_ID_ARG}"

const val TASK_DETAILS_DEEP_LINK_SCHEME = "taskflow"
const val TASK_DETAILS_DEEP_LINK_URI_PATTERN = "$TASK_DETAILS_DEEP_LINK_SCHEME://$TASK_DETAILS_ROUTE/{$TASK_ID_ARG}"

fun taskDetailsRoute(taskId: String): String = "$TASK_DETAILS_ROUTE/$taskId"

fun taskDetailsDeepLinkUri(taskId: String): String = "$TASK_DETAILS_DEEP_LINK_SCHEME://$TASK_DETAILS_ROUTE/$taskId"

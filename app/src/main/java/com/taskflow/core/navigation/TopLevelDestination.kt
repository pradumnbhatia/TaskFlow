package com.taskflow.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    HOME(route = "home", label = "Home", icon = Icons.Filled.Home),
    TASKS(route = "tasks", label = "Tasks", icon = Icons.AutoMirrored.Filled.List),
    PROFILE(route = "profile", label = "Profile", icon = Icons.Filled.Person),
}

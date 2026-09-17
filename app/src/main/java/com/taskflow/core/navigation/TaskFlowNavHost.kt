package com.taskflow.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.taskflow.feature.home.HomeRoute
import com.taskflow.feature.profile.ProfileScreen
import com.taskflow.feature.tasks.TasksScreen

@Composable
fun TaskFlowNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.HOME.route,
        modifier = modifier,
    ) {
        composable(TopLevelDestination.HOME.route) {
            HomeRoute(
                onSeeAllTasks = { navController.navigateToTopLevelDestination(TopLevelDestination.TASKS) },
                // TODO(Phase 5): navigate to the Create Task screen once it exists.
                onAddTask = {},
            )
        }
        composable(TopLevelDestination.TASKS.route) { TasksScreen() }
        composable(TopLevelDestination.PROFILE.route) { ProfileScreen() }
    }
}

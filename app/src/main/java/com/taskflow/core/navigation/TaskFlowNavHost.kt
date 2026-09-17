package com.taskflow.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.taskflow.feature.home.HomeRoute
import com.taskflow.feature.profile.ProfileScreen
import com.taskflow.feature.taskeditor.TaskEditorRoute
import com.taskflow.feature.tasks.TasksRoute

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
                onAddTask = { navController.navigate(taskEditorRoute()) },
            )
        }
        composable(TopLevelDestination.TASKS.route) {
            TasksRoute(
                // TODO(Phase 6): navigate to Task Details once it exists.
                onTaskClick = {},
                onAddTask = { navController.navigate(taskEditorRoute()) },
            )
        }
        composable(TopLevelDestination.PROFILE.route) { ProfileScreen() }
        composable(
            route = TASK_EDITOR_ROUTE_PATTERN,
            arguments = listOf(
                navArgument(TASK_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            TaskEditorRoute(onNavigateBack = { navController.popBackStack() })
        }
    }
}

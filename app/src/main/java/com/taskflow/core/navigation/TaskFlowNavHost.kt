package com.taskflow.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.taskflow.feature.home.HomeRoute
import com.taskflow.feature.profile.ProfileRoute
import com.taskflow.feature.settings.AboutRoute
import com.taskflow.feature.settings.AppearanceSettingsRoute
import com.taskflow.feature.settings.NotificationsSettingsRoute
import com.taskflow.feature.settings.PreferencesSettingsRoute
import com.taskflow.feature.taskdetails.TaskDetailsRoute
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
                onTaskClick = { taskId -> navController.navigate(taskDetailsRoute(taskId)) },
            )
        }
        composable(TopLevelDestination.TASKS.route) {
            TasksRoute(
                onTaskClick = { taskId -> navController.navigate(taskDetailsRoute(taskId)) },
                onAddTask = { navController.navigate(taskEditorRoute()) },
            )
        }
        composable(TopLevelDestination.PROFILE.route) {
            ProfileRoute(
                onNotificationsClick = { navController.navigate(SETTINGS_NOTIFICATIONS_ROUTE) },
                onAppearanceClick = { navController.navigate(SETTINGS_APPEARANCE_ROUTE) },
                onPreferencesClick = { navController.navigate(SETTINGS_PREFERENCES_ROUTE) },
                onAboutClick = { navController.navigate(SETTINGS_ABOUT_ROUTE) },
            )
        }
        composable(SETTINGS_NOTIFICATIONS_ROUTE) {
            NotificationsSettingsRoute(onNavigateBack = { navController.popBackStack() })
        }
        composable(SETTINGS_APPEARANCE_ROUTE) {
            AppearanceSettingsRoute(onNavigateBack = { navController.popBackStack() })
        }
        composable(SETTINGS_PREFERENCES_ROUTE) {
            PreferencesSettingsRoute(onNavigateBack = { navController.popBackStack() })
        }
        composable(SETTINGS_ABOUT_ROUTE) {
            AboutRoute(onNavigateBack = { navController.popBackStack() })
        }
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
        composable(
            route = TASK_DETAILS_ROUTE_PATTERN,
            arguments = listOf(navArgument(TASK_ID_ARG) { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = TASK_DETAILS_DEEP_LINK_URI_PATTERN }),
        ) {
            TaskDetailsRoute(
                onNavigateBack = { navController.popBackStack() },
                onEditTask = { taskId -> navController.navigate(taskEditorRoute(taskId)) },
            )
        }
    }
}

package com.taskflow.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskflow.R
import com.taskflow.core.common.label
import com.taskflow.core.designsystem.component.EmptyState
import com.taskflow.core.designsystem.component.TaskListItem
import com.taskflow.core.model.Priority
import com.taskflow.core.model.Task

@Composable
fun TasksRoute(
    onTaskClick: (String) -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TasksScreen(
        uiState = uiState,
        onTaskClick = onTaskClick,
        onAddTask = onAddTask,
        onListFilterSelected = viewModel::onListFilterSelected,
        onPriorityFilterSelected = viewModel::onPriorityFilterSelected,
        onSortOrderSelected = viewModel::onSortOrderSelected,
        onClearFilters = viewModel::onClearFilters,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSearchActiveChanged = viewModel::onSearchActiveChanged,
        onToggleTaskStatus = viewModel::onToggleTaskStatus,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    uiState: TasksUiState,
    onTaskClick: (String) -> Unit,
    onAddTask: () -> Unit,
    onListFilterSelected: (TaskListFilter) -> Unit,
    onPriorityFilterSelected: (Priority?) -> Unit,
    onSortOrderSelected: (TaskSortOrder) -> Unit,
    onClearFilters: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSearchActiveChanged: (Boolean) -> Unit,
    onToggleTaskStatus: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TasksTopBar(
                uiState = uiState,
                onSearchQueryChanged = onSearchQueryChanged,
                onSearchActiveChanged = onSearchActiveChanged,
                onFilterClick = { showFilterSheet = true },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.home_add_task_cd))
            }
        },
    ) { innerPadding ->
        when (uiState) {
            is TasksUiState.Loading -> Box(modifier = Modifier.padding(innerPadding).fillMaxSize())
            is TasksUiState.Loaded -> TasksContent(
                uiState = uiState,
                onListFilterSelected = onListFilterSelected,
                onTaskClick = onTaskClick,
                onToggleTaskStatus = onToggleTaskStatus,
                contentPadding = innerPadding,
            )
        }
    }

    if (showFilterSheet) {
        val filter = (uiState as? TasksUiState.Loaded)?.filter ?: TasksFilterState()
        FilterBottomSheet(
            filter = filter,
            onPrioritySelected = onPriorityFilterSelected,
            onSortOrderSelected = onSortOrderSelected,
            onClear = onClearFilters,
            onDismiss = { showFilterSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksTopBar(
    uiState: TasksUiState,
    onSearchQueryChanged: (String) -> Unit,
    onSearchActiveChanged: (Boolean) -> Unit,
    onFilterClick: () -> Unit,
) {
    val isSearchActive = (uiState as? TasksUiState.Loaded)?.isSearchActive == true
    val searchQuery = (uiState as? TasksUiState.Loaded)?.filter?.searchQuery.orEmpty()

    TopAppBar(
        title = {
            if (isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text(stringResource(R.string.tasks_search_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(stringResource(R.string.tasks_title))
            }
        },
        actions = {
            if (isSearchActive) {
                IconButton(onClick = { onSearchActiveChanged(false) }) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.tasks_search_cd))
                }
            } else {
                IconButton(onClick = { onSearchActiveChanged(true) }) {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.tasks_search_cd))
                }
                IconButton(onClick = onFilterClick) {
                    Icon(Icons.Filled.FilterList, contentDescription = stringResource(R.string.tasks_filter_cd))
                }
            }
        },
    )
}

@Composable
private fun TasksContent(
    uiState: TasksUiState.Loaded,
    onListFilterSelected: (TaskListFilter) -> Unit,
    onTaskClick: (String) -> Unit,
    onToggleTaskStatus: (Task) -> Unit,
    contentPadding: PaddingValues,
) {
    Column(modifier = Modifier.padding(top = contentPadding.calculateTopPadding())) {
        ListFilterTabs(
            selected = uiState.filter.listFilter,
            onSelected = onListFilterSelected,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        if (uiState.tasks.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.FilterList,
                title = stringResource(R.string.tasks_empty_title),
                message = stringResource(R.string.tasks_empty_message),
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = uiState.tasks, key = Task::id) { task ->
                    TaskListItem(
                        task = task,
                        onClick = { onTaskClick(task.id) },
                        onToggleComplete = { onToggleTaskStatus(task) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListFilterTabs(
    selected: TaskListFilter,
    onSelected: (TaskListFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = TaskListFilter.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, filter ->
            SegmentedButton(
                selected = filter == selected,
                onClick = { onSelected(filter) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
            ) {
                Text(stringResource(filter.labelRes()))
            }
        }
    }
}

private fun TaskListFilter.labelRes(): Int = when (this) {
    TaskListFilter.ALL -> R.string.tasks_tab_all
    TaskListFilter.TODAY -> R.string.tasks_tab_today
    TaskListFilter.UPCOMING -> R.string.tasks_tab_upcoming
    TaskListFilter.COMPLETED -> R.string.tasks_tab_completed
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    filter: TasksFilterState,
    onPrioritySelected: (Priority?) -> Unit,
    onSortOrderSelected: (TaskSortOrder) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.tasks_filters_title), style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.tasks_clear))
                }
            }

            Text(
                text = stringResource(R.string.tasks_priority_label),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filter.priorityFilter == null,
                    onClick = { onPrioritySelected(null) },
                    label = { Text(stringResource(R.string.tasks_priority_all)) },
                )
                Priority.entries.forEach { priority ->
                    FilterChip(
                        selected = filter.priorityFilter == priority,
                        onClick = { onPrioritySelected(priority) },
                        label = { Text(priority.label) },
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = stringResource(R.string.tasks_sort_by_label),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Column {
                SortOptionRow(
                    label = stringResource(R.string.tasks_sort_due_earliest),
                    selected = filter.sortOrder == TaskSortOrder.DUE_DATE_EARLIEST,
                    onClick = { onSortOrderSelected(TaskSortOrder.DUE_DATE_EARLIEST) },
                )
                SortOptionRow(
                    label = stringResource(R.string.tasks_sort_due_latest),
                    selected = filter.sortOrder == TaskSortOrder.DUE_DATE_LATEST,
                    onClick = { onSortOrderSelected(TaskSortOrder.DUE_DATE_LATEST) },
                )
                SortOptionRow(
                    label = stringResource(R.string.tasks_sort_priority),
                    selected = filter.sortOrder == TaskSortOrder.PRIORITY_HIGH_FIRST,
                    onClick = { onSortOrderSelected(TaskSortOrder.PRIORITY_HIGH_FIRST) },
                )
            }
        }
    }
}

@Composable
private fun SortOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

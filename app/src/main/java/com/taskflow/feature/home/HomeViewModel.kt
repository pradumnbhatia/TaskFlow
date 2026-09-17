package com.taskflow.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.core.common.DEFAULT_USER_NAME
import com.taskflow.core.domain.GetTodayTaskSummaryUseCase
import com.taskflow.core.domain.TodayTaskSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getTodayTaskSummary: GetTodayTaskSummaryUseCase,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getTodayTaskSummary()
        .map { summary -> summary.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )
}

private fun TodayTaskSummary.toUiState(): HomeUiState.Loaded = HomeUiState.Loaded(
    userName = DEFAULT_USER_NAME,
    greetingPeriod = greetingPeriodForTime(LocalTime.now()),
    totalTasksToday = totalCount,
    completedTasksToday = completedCount,
    completionPercent = completionPercent,
    todayTasks = tasks,
)

private fun greetingPeriodForTime(time: LocalTime): GreetingPeriod = when (time.hour) {
    in 5..11 -> GreetingPeriod.MORNING
    in 12..16 -> GreetingPeriod.AFTERNOON
    else -> GreetingPeriod.EVENING
}

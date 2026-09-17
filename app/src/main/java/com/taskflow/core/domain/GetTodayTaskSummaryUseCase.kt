package com.taskflow.core.domain

import com.taskflow.core.data.TaskRepository
import com.taskflow.core.model.Task
import com.taskflow.core.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

data class TodayTaskSummary(
    val tasks: List<Task>,
    val completedCount: Int,
) {
    val totalCount: Int get() = tasks.size

    val completionPercent: Int
        get() = if (totalCount == 0) 0 else (completedCount * 100) / totalCount
}

class GetTodayTaskSummaryUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    operator fun invoke(): Flow<TodayTaskSummary> =
        taskRepository.observeTasks().map { tasks ->
            val todayTasks = tasks.filter { it.dueDate == LocalDate.now() }
            TodayTaskSummary(
                tasks = todayTasks,
                completedCount = todayTasks.count { it.status == TaskStatus.COMPLETED },
            )
        }
}

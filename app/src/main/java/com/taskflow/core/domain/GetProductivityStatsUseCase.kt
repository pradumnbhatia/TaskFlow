package com.taskflow.core.domain

import com.taskflow.core.data.TaskRepository
import com.taskflow.core.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class ProductivityStats(
    val tasksDone: Int,
    val completionRatePercent: Int,
    val currentStreakDays: Int,
)

class GetProductivityStatsUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    operator fun invoke(): Flow<ProductivityStats> =
        taskRepository.observeTasks().map { tasks ->
            val tasksDone = tasks.count { it.status == TaskStatus.COMPLETED }
            val completionRate = if (tasks.isEmpty()) 0 else (tasksDone * 100) / tasks.size
            val completedDates = tasks.mapNotNull { task ->
                task.completedAt?.atZone(ZoneId.systemDefault())?.toLocalDate()
            }.toSet()
            ProductivityStats(
                tasksDone = tasksDone,
                completionRatePercent = completionRate,
                currentStreakDays = currentStreak(completedDates, LocalDate.now()),
            )
        }
}

/** Consecutive days (ending today, or yesterday if nothing's been completed yet today) with at least one completed task. */
private fun currentStreak(completedDates: Set<LocalDate>, today: LocalDate): Int {
    var streak = 0
    var day = if (completedDates.contains(today)) today else today.minusDays(1)
    while (completedDates.contains(day)) {
        streak++
        day = day.minusDays(1)
    }
    return streak
}

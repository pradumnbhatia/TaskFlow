package com.taskflow.core.domain

import com.taskflow.core.data.TaskRepository
import com.taskflow.core.model.TaskStatus
import com.taskflow.core.model.testTask
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetProductivityStatsUseCaseTest {

    private val taskRepository = mockk<TaskRepository>()

    private fun useCase() = GetProductivityStatsUseCase(taskRepository)

    private fun completedOn(date: LocalDate) =
        testTask(status = TaskStatus.COMPLETED, completedAt = date.atStartOfDay(ZoneId.systemDefault()).toInstant())

    @Test
    fun `tasksDone and completion rate reflect all tasks, not just today`() = runTest {
        val today = LocalDate.now()
        every { taskRepository.observeTasks() } returns flowOf(
            listOf(
                completedOn(today),
                testTask(status = TaskStatus.PENDING),
                testTask(status = TaskStatus.PENDING),
            ),
        )

        val stats = useCase().invoke().first()

        assertEquals(1, stats.tasksDone)
        assertEquals(33, stats.completionRatePercent)
    }

    @Test
    fun `completion rate is zero when there are no tasks to avoid dividing by zero`() = runTest {
        every { taskRepository.observeTasks() } returns flowOf(emptyList())

        val stats = useCase().invoke().first()

        assertEquals(0, stats.completionRatePercent)
    }

    @Test
    fun `streak counts consecutive days ending today`() = runTest {
        val today = LocalDate.now()
        every { taskRepository.observeTasks() } returns flowOf(
            listOf(
                completedOn(today),
                completedOn(today.minusDays(1)),
                completedOn(today.minusDays(2)),
            ),
        )

        assertEquals(3, useCase().invoke().first().currentStreakDays)
    }

    @Test
    fun `streak continues from yesterday if nothing has been completed yet today`() = runTest {
        val today = LocalDate.now()
        every { taskRepository.observeTasks() } returns flowOf(
            listOf(completedOn(today.minusDays(1)), completedOn(today.minusDays(2))),
        )

        assertEquals(2, useCase().invoke().first().currentStreakDays)
    }

    @Test
    fun `streak is zero when there is a gap before today`() = runTest {
        val today = LocalDate.now()
        every { taskRepository.observeTasks() } returns flowOf(listOf(completedOn(today.minusDays(2))))

        assertEquals(0, useCase().invoke().first().currentStreakDays)
    }
}

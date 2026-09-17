package com.taskflow.core.domain

import com.taskflow.core.data.TaskRepository
import com.taskflow.core.model.TaskStatus
import com.taskflow.core.model.testTask
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTodayTaskSummaryUseCaseTest {

    private val taskRepository = mockk<TaskRepository>()

    private fun useCase() = GetTodayTaskSummaryUseCase(taskRepository)

    @Test
    fun `only tasks due today are included`() = runTest {
        val today = LocalDate.now()
        val tasks = listOf(
            testTask(dueDate = today, status = TaskStatus.PENDING),
            testTask(dueDate = today, status = TaskStatus.COMPLETED),
            testTask(dueDate = today.plusDays(1), status = TaskStatus.PENDING),
            testTask(dueDate = null, status = TaskStatus.PENDING),
        )
        every { taskRepository.observeTasks() } returns flowOf(tasks)

        val summary = useCase().invoke().first()

        assertEquals(2, summary.totalCount)
        assertEquals(1, summary.completedCount)
        assertEquals(50, summary.completionPercent)
    }

    @Test
    fun `completion percent is zero when there are no tasks today to avoid dividing by zero`() = runTest {
        every { taskRepository.observeTasks() } returns flowOf(emptyList())

        val summary = useCase().invoke().first()

        assertEquals(0, summary.totalCount)
        assertEquals(0, summary.completionPercent)
    }
}

package com.taskflow.core.data

import com.taskflow.core.database.TaskDao
import com.taskflow.core.database.TaskEntity
import com.taskflow.core.model.Task
import com.taskflow.core.model.TaskStatus
import com.taskflow.core.notification.ReminderScheduler
import com.taskflow.worker.SyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val reminderScheduler: ReminderScheduler,
    private val syncScheduler: SyncScheduler,
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> =
        taskDao.observeTasks().map { entities -> entities.map(TaskEntity::toDomain) }

    override fun observeTaskById(id: String): Flow<Task?> =
        taskDao.observeTaskById(id).map { it?.toDomain() }

    override suspend fun getTaskById(id: String): Task? =
        taskDao.getTaskById(id)?.toDomain()

    override suspend fun upsertTask(task: Task) {
        val updated = task.copy(updatedAt = Instant.now())
        taskDao.upsert(updated.toEntity())
        reminderScheduler.schedule(updated)
        syncScheduler.scheduleSync()
    }

    override suspend fun deleteTask(id: String) {
        reminderScheduler.cancel(id)
        taskDao.markDeleted(id, Instant.now())
        syncScheduler.scheduleSync()
    }

    override suspend fun setTaskStatus(id: String, status: TaskStatus) {
        val now = Instant.now()
        taskDao.updateStatus(
            id = id,
            status = status,
            completedAt = if (status == TaskStatus.COMPLETED) now else null,
            updatedAt = now,
        )
        if (status == TaskStatus.COMPLETED) {
            reminderScheduler.cancel(id)
        } else {
            taskDao.getTaskById(id)?.toDomain()?.let { reminderScheduler.schedule(it) }
        }
        syncScheduler.scheduleSync()
    }

    override suspend fun clearCompletedTasks() {
        taskDao.markDeletedByStatus(TaskStatus.COMPLETED, Instant.now())
        syncScheduler.scheduleSync()
    }
}

package com.taskflow.core.data

import com.taskflow.core.database.TaskDao
import com.taskflow.core.database.TaskEntity
import com.taskflow.core.model.Task
import com.taskflow.core.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> =
        taskDao.observeTasks().map { entities -> entities.map(TaskEntity::toDomain) }

    override fun observeTaskById(id: String): Flow<Task?> =
        taskDao.observeTaskById(id).map { it?.toDomain() }

    override suspend fun getTaskById(id: String): Task? =
        taskDao.getTaskById(id)?.toDomain()

    override suspend fun upsertTask(task: Task) {
        taskDao.upsert(task.copy(updatedAt = Instant.now()).toEntity())
    }

    override suspend fun deleteTask(id: String) {
        taskDao.deleteById(id)
    }

    override suspend fun setTaskStatus(id: String, status: TaskStatus) {
        val now = Instant.now()
        taskDao.updateStatus(
            id = id,
            status = status,
            completedAt = if (status == TaskStatus.COMPLETED) now else null,
            updatedAt = now,
        )
    }

    override suspend fun clearCompletedTasks() {
        taskDao.deleteByStatus(TaskStatus.COMPLETED)
    }
}

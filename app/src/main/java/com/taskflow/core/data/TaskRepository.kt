package com.taskflow.core.data

import com.taskflow.core.model.Task
import com.taskflow.core.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    fun observeTasks(): Flow<List<Task>>

    fun observeTaskById(id: String): Flow<Task?>

    suspend fun getTaskById(id: String): Task?

    suspend fun upsertTask(task: Task)

    suspend fun deleteTask(id: String)

    suspend fun setTaskStatus(id: String, status: TaskStatus)

    suspend fun clearCompletedTasks()
}

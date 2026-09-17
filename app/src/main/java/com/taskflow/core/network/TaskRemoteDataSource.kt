package com.taskflow.core.network

import com.taskflow.core.model.Task

interface TaskRemoteDataSource {

    suspend fun upsertTask(task: Task)

    suspend fun deleteTask(taskId: String)
}

package com.taskflow.core.network

import com.taskflow.core.model.Task
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

/**
 * Stands in for a real Supabase-backed [TaskRemoteDataSource] until the project is provisioned.
 * Conforms to the same contract, so swapping in the real implementation later is a one-class change.
 */
@Singleton
class FakeTaskRemoteDataSource @Inject constructor() : TaskRemoteDataSource {

    private val remoteTasks = ConcurrentHashMap<String, Task>()

    override suspend fun upsertTask(task: Task) {
        delay(SIMULATED_NETWORK_LATENCY_MILLIS)
        remoteTasks[task.id] = task
    }

    override suspend fun deleteTask(taskId: String) {
        delay(SIMULATED_NETWORK_LATENCY_MILLIS)
        remoteTasks.remove(taskId)
    }

    private companion object {
        const val SIMULATED_NETWORK_LATENCY_MILLIS = 300L
    }
}

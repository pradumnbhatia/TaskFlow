package com.taskflow.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.taskflow.core.data.toDomain
import com.taskflow.core.database.TaskDao
import com.taskflow.core.network.TaskRemoteDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val taskDao: TaskDao,
    private val taskRemoteDataSource: TaskRemoteDataSource,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pendingTasks = taskDao.getPendingSyncTasks()
        for (entity in pendingTasks) {
            try {
                if (entity.isDeleted) {
                    taskRemoteDataSource.deleteTask(entity.id)
                    taskDao.deleteById(entity.id)
                } else {
                    taskRemoteDataSource.upsertTask(entity.toDomain())
                    taskDao.markSynced(entity.id)
                }
            } catch (e: IOException) {
                return Result.retry()
            }
        }
        return Result.success()
    }
}

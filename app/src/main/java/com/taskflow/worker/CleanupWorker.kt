package com.taskflow.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.taskflow.core.database.TaskDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.temporal.ChronoUnit

@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val taskDao: TaskDao,
    private val syncScheduler: SyncScheduler,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = Instant.now()
        val cutoff = now.minus(COMPLETED_TASK_RETENTION_DAYS, ChronoUnit.DAYS)
        val archivedCount = taskDao.archiveCompletedBefore(cutoff, now)
        if (archivedCount > 0) {
            syncScheduler.scheduleSync()
        }
        return Result.success()
    }

    private companion object {
        const val COMPLETED_TASK_RETENTION_DAYS = 30L
    }
}

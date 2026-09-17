package com.taskflow.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.taskflow.core.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY (dueDate IS NULL), dueDate, (dueTime IS NULL), dueTime")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id AND isDeleted = 0")
    fun observeTaskById(id: String): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :id AND isDeleted = 0")
    suspend fun getTaskById(id: String): TaskEntity?

    @Upsert
    suspend fun upsert(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE tasks SET status = :status, completedAt = :completedAt, updatedAt = :updatedAt, isPendingSync = 1 WHERE id = :id")
    suspend fun updateStatus(id: String, status: TaskStatus, completedAt: Instant?, updatedAt: Instant)

    @Query("UPDATE tasks SET isDeleted = 1, isPendingSync = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markDeleted(id: String, updatedAt: Instant)

    @Query("UPDATE tasks SET isDeleted = 1, isPendingSync = 1, updatedAt = :updatedAt WHERE status = :status AND isDeleted = 0")
    suspend fun markDeletedByStatus(status: TaskStatus, updatedAt: Instant)

    @Query("SELECT * FROM tasks WHERE isPendingSync = 1")
    suspend fun getPendingSyncTasks(): List<TaskEntity>

    @Query("UPDATE tasks SET isPendingSync = 0 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("UPDATE tasks SET isDeleted = 1, isPendingSync = 1, updatedAt = :updatedAt WHERE status = 'COMPLETED' AND isDeleted = 0 AND completedAt < :cutoff")
    suspend fun archiveCompletedBefore(cutoff: Instant, updatedAt: Instant): Int
}

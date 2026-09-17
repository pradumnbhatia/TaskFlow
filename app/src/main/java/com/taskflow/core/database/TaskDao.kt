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

    @Query("SELECT * FROM tasks ORDER BY (dueDate IS NULL), dueDate, (dueTime IS NULL), dueTime")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeTaskById(id: String): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    @Upsert
    suspend fun upsert(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE tasks SET status = :status, completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: TaskStatus, completedAt: Instant?, updatedAt: Instant)

    @Query("DELETE FROM tasks WHERE status = :status")
    suspend fun deleteByStatus(status: TaskStatus)
}

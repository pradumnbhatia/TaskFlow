package com.taskflow.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.taskflow.core.model.Priority
import com.taskflow.core.model.TaskStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priority: Priority,
    val status: TaskStatus,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val reminderEnabled: Boolean,
    val createdAt: Instant,
    val completedAt: Instant?,
    val updatedAt: Instant,
    val isPendingSync: Boolean = true,
    val isDeleted: Boolean = false,
)

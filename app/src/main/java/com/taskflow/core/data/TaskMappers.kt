package com.taskflow.core.data

import com.taskflow.core.database.TaskEntity
import com.taskflow.core.model.Task

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    priority = priority,
    status = status,
    dueDate = dueDate,
    dueTime = dueTime,
    reminderEnabled = reminderEnabled,
    createdAt = createdAt,
    completedAt = completedAt,
    updatedAt = updatedAt,
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    priority = priority,
    status = status,
    dueDate = dueDate,
    dueTime = dueTime,
    reminderEnabled = reminderEnabled,
    createdAt = createdAt,
    completedAt = completedAt,
    updatedAt = updatedAt,
)

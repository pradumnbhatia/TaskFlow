package com.taskflow.core.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

fun testTask(
    id: String = UUID.randomUUID().toString(),
    title: String = "Task",
    priority: Priority = Priority.MEDIUM,
    status: TaskStatus = TaskStatus.PENDING,
    dueDate: LocalDate? = null,
    dueTime: LocalTime? = null,
    completedAt: Instant? = null,
): Task = Task(
    id = id,
    title = title,
    priority = priority,
    status = status,
    dueDate = dueDate,
    dueTime = dueTime,
    createdAt = Instant.now(),
    completedAt = completedAt,
    updatedAt = Instant.now(),
)

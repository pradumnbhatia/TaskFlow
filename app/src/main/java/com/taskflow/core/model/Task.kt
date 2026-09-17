package com.taskflow.core.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val reminderEnabled: Boolean = false,
    val createdAt: Instant,
    val completedAt: Instant? = null,
    val updatedAt: Instant,
)

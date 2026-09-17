package com.taskflow.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
private val dateFormatter = DateTimeFormatter.ofPattern("d MMM")
private val fullDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

fun LocalTime.toDisplayString(): String = format(timeFormatter)

fun LocalDate.toRelativeDisplayString(today: LocalDate = LocalDate.now()): String = when (this) {
    today -> "Today"
    today.plusDays(1) -> "Tomorrow"
    else -> format(dateFormatter)
}

fun LocalDate.toFullDisplayString(): String = format(fullDateFormatter)

fun Instant.toDisplayDateString(): String = atZone(ZoneId.systemDefault()).toLocalDate().toFullDisplayString()

/** Whole days between this instant's local date and [today] (0 = same day, 1 = yesterday, ...). */
fun Instant.daysAgo(today: LocalDate = LocalDate.now()): Long {
    val completedDate = atZone(ZoneId.systemDefault()).toLocalDate()
    return ChronoUnit.DAYS.between(completedDate, today).coerceAtLeast(0L)
}

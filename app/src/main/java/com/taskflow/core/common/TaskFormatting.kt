package com.taskflow.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

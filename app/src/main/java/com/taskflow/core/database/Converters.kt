package com.taskflow.core.database

import androidx.room.TypeConverter
import com.taskflow.core.model.Priority
import com.taskflow.core.model.TaskStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun epochDayToLocalDate(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun nanoOfDayToLocalTime(value: Long?): LocalTime? = value?.let(LocalTime::ofNanoOfDay)

    @TypeConverter
    fun localTimeToNanoOfDay(time: LocalTime?): Long? = time?.toNanoOfDay()

    @TypeConverter
    fun epochMilliToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun instantToEpochMilli(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun nameToPriority(value: String): Priority = Priority.valueOf(value)

    @TypeConverter
    fun priorityToName(priority: Priority): String = priority.name

    @TypeConverter
    fun nameToTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun taskStatusToName(status: TaskStatus): String = status.name
}

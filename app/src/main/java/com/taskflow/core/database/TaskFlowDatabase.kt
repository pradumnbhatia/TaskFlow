package com.taskflow.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TaskEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TaskFlowDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

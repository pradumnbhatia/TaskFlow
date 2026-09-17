package com.taskflow.core.database.di

import android.content.Context
import androidx.room.Room
import com.taskflow.core.database.MIGRATION_1_2
import com.taskflow.core.database.TaskDao
import com.taskflow.core.database.TaskFlowDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTaskFlowDatabase(@ApplicationContext context: Context): TaskFlowDatabase =
        Room.databaseBuilder(
            context,
            TaskFlowDatabase::class.java,
            "taskflow.db",
        ).addMigrations(MIGRATION_1_2).build()

    @Provides
    fun provideTaskDao(database: TaskFlowDatabase): TaskDao = database.taskDao()
}

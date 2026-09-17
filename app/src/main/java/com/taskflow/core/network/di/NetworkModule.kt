package com.taskflow.core.network.di

import com.taskflow.core.network.FakeTaskRemoteDataSource
import com.taskflow.core.network.TaskRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindTaskRemoteDataSource(impl: FakeTaskRemoteDataSource): TaskRemoteDataSource
}

package com.mirage.todolist.di

import android.content.Context
import com.mirage.todolist.App
import com.mirage.todolist.model.room.DatabaseModel
import com.mirage.todolist.model.tasks.TodolistModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: App) {

    @Provides
    @Singleton
    fun provideApplication(): App = application

    @Provides
    @Singleton
    @ApplicationContext
    fun provideApplicationContext(): Context = application

    @Provides
    @Singleton
    fun provideTodolistModel() = TodolistModel()

    @Provides
    @Singleton
    fun provideDatabaseModel() = DatabaseModel()

}
package com.mirage.todolist.model.dagger

import com.mirage.todolist.model.tasks.TodolistModel
import com.mirage.todolist.model.tasks.TodolistModelImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun todolistModel(): TodolistModel = TodolistModelImpl()

}
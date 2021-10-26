package com.mirage.todolist.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mirage.todolist.ui.lockscreen.LockScreenViewModel
import com.mirage.todolist.ui.todolist.TodolistViewModel
import com.mirage.todolist.ui.todolist.tags.TagsViewModel
import com.mirage.todolist.ui.todolist.tasks.TaskRecyclerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(LockScreenViewModel::class)
    abstract fun bindLockScreenViewModel(lockScreenViewModel: LockScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TagsViewModel::class)
    abstract fun bindTagsViewModel(tagsViewModel: TagsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TaskRecyclerViewModel::class)
    abstract fun bindTaskRecyclerViewModel(taskRecyclerViewModel: TaskRecyclerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TodolistViewModel::class)
    abstract fun bindTodolistViewModel(todolistViewModel: TodolistViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
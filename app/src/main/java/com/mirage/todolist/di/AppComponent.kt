package com.mirage.todolist.di

import android.content.Context
import com.mirage.todolist.model.googledrive.GoogleDriveModel
import com.mirage.todolist.model.room.DatabaseModel
import com.mirage.todolist.view.edittask.EditTaskActivity
import com.mirage.todolist.view.lockscreen.LockScreenActivity
import com.mirage.todolist.view.settings.SettingsActivity
import com.mirage.todolist.view.settings.SettingsFragment
import com.mirage.todolist.view.todolist.TodolistActivity
import com.mirage.todolist.view.todolist.tags.TagsFragment
import com.mirage.todolist.view.todolist.tasks.TaskRecyclerFragment
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ViewModelModule::class, DatabaseModule::class, AndroidInjectionModule::class])
interface AppComponent : AndroidInjector<App> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun withApplication(application: App): Builder

        fun build(): AppComponent
    }

    @ApplicationContext
    fun getContext(): Context

    fun getApplication(): App

    @DatabaseInfo
    fun getDatabaseName(): String

    fun inject(lockScreenActivity: LockScreenActivity)

    fun inject(todolistActivity: TodolistActivity)

    fun inject(editTaskActivity: EditTaskActivity)

    fun inject(settingsActivity: SettingsActivity)

    fun inject(tagsFragment: TagsFragment)

    fun inject(settingsFragment: SettingsFragment)

    fun inject(tagsRecyclerFragment: TaskRecyclerFragment)

    fun inject(databaseModel: DatabaseModel)

    fun inject(googleDriveModel: GoogleDriveModel)
}
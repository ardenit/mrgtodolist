package com.mirage.todolist.di

import android.content.Context
import com.mirage.todolist.model.database.DatabaseModel
import com.mirage.todolist.model.googledrive.GoogleDriveModel
import com.mirage.todolist.model.repository.TodoRepository
import com.mirage.todolist.ui.edittask.EditTaskActivity
import com.mirage.todolist.ui.lockscreen.*
import com.mirage.todolist.ui.settings.AddGraphicalKeyFragment
import com.mirage.todolist.ui.settings.SettingsActivity
import com.mirage.todolist.ui.settings.SettingsFragment
import com.mirage.todolist.ui.todolist.TodolistActivity
import com.mirage.todolist.ui.todolist.tags.TagsFragment
import com.mirage.todolist.ui.todolist.tasks.TaskRecyclerFragment
import com.mirage.todolist.ui.todolist.tasks.TasksFragment
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

    fun inject(tasksFragment: TasksFragment)

    fun inject(settingsFragment: SettingsFragment)

    fun inject(tapFragment: TapFragment)

    fun inject(graphicalKeyFragment: GraphicalKeyFragment)

    fun inject(passwordFragment: PasswordFragment)

    fun inject(fingerprintFragment: FingerprintFragment)

    fun inject(tasksRecyclerFragment: TaskRecyclerFragment)

    fun inject(addGraphicalKeyFragment: AddGraphicalKeyFragment)

    fun inject(databaseModel: DatabaseModel)

    fun inject(googleDriveModel: GoogleDriveModel)

    fun inject(todoRepository: TodoRepository)
}
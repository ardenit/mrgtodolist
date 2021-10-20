package com.mirage.todolist.model.dagger

import android.app.Application
import com.mirage.todolist.view.edittask.EditTaskActivity
import com.mirage.todolist.view.lockscreen.LockScreenActivity
import com.mirage.todolist.view.settings.SettingsFragment
import com.mirage.todolist.view.todolist.TodolistActivity
import com.mirage.todolist.view.todolist.tags.TagsFragment
import com.mirage.todolist.view.todolist.tasks.TaskRecyclerFragment
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ViewModelModule::class, AndroidInjectionModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun withApplication(application: App): Builder

        fun build(): AppComponent
    }

    fun inject(lockScreenActivity: LockScreenActivity)

    fun inject(todolistActivity: TodolistActivity)

    fun inject(editTaskActivity: EditTaskActivity)

    fun inject(tagsFragment: TagsFragment)

    fun inject(settingsFragment: SettingsFragment)

    fun inject(tagsRecyclerFragment: TaskRecyclerFragment)
}
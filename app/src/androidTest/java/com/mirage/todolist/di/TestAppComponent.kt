package com.mirage.todolist.di

import com.mirage.todolist.model.database.DatabaseModelTest
import com.mirage.todolist.model.database.TaskDaoTest
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ViewModelModule::class, TestDatabaseModule::class, AndroidInjectionModule::class])
interface TestAppComponent : AppComponent {

    fun inject(test: DatabaseModelTest)

    fun inject(test: TaskDaoTest)
}
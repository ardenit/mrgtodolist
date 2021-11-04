package com.mirage.todolist.di

import com.mirage.todolist.model.database.*
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    TestApplicationModule::class,
    ViewModelModule::class,
    TestDatabaseModule::class,
    AndroidInjectionModule::class
])
interface TestAppComponent : AppComponent {

    fun inject(test: DatabaseModelTest)

    fun inject(test: TaskDaoTest)

    fun inject(test: TagDaoTest)

    fun inject(test: VersionDaoTest)

    fun inject(test: RelationDaoTest)
}
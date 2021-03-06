package com.mirage.todolist.di

import com.mirage.todolist.model.database.*
import com.mirage.todolist.model.googledrive.SnapshotMergerTest
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    TestApplicationModule::class,
    ViewModelModule::class,
    WorkerModule::class,
    TestDatabaseModule::class,
    AndroidInjectionModule::class
])
interface TestAppComponent : AppComponent {

    fun inject(test: DatabaseModelTest)

    fun inject(test: AccountSwitchingTest)

    fun inject(test: TaskDaoTest)

    fun inject(test: TagDaoTest)

    fun inject(test: VersionDaoTest)

    fun inject(test: RelationDaoTest)

    fun inject(test: SnapshotMergerTest)
}
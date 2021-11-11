package com.mirage.todolist.di

import com.mirage.todolist.model.workers.ChildWorkerFactory
import com.mirage.todolist.model.workers.SyncWorker
import com.mirage.todolist.model.workers.SyncWorkerFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class WorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(SyncWorker::class)
    abstract fun bindHelloWorldWorker(factory: SyncWorkerFactory): ChildWorkerFactory
}
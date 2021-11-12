package com.mirage.todolist.di

import com.mirage.todolist.model.workers.*
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class WorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(SyncWorker::class)
    abstract fun bindSyncWorker(factory: SyncWorkerFactory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(NotificationWorker::class)
    abstract fun bindNotificationWorker(factory: NotificationWorkerFactory): ChildWorkerFactory
}
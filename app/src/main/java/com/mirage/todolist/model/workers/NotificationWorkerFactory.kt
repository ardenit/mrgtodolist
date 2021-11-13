package com.mirage.todolist.model.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import javax.inject.Inject

class NotificationWorkerFactory @Inject constructor(): ChildWorkerFactory {

    override fun create(appContext: Context, params: WorkerParameters): ListenableWorker =
        NotificationWorker(appContext, params)
}
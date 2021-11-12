package com.mirage.todolist.model.workers

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.mirage.todolist.model.database.DatabaseModel
import com.mirage.todolist.model.googledrive.GoogleDriveModel
import com.mirage.todolist.model.googledrive.SnapshotMerger
import javax.inject.Inject

class NotificationWorkerFactory @Inject constructor(): ChildWorkerFactory {

    override fun create(appContext: Context, params: WorkerParameters): ListenableWorker =
        NotificationWorker(appContext, params)
}
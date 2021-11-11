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

class SyncWorkerFactory @Inject constructor(
    private val googleDriveModel: GoogleDriveModel,
    private val databaseModel: DatabaseModel,
    private val gson: Gson,
    private val sharedPreferences: SharedPreferences,
    private val resources: Resources,
    private val snapshotMerger: SnapshotMerger,
    private val workManager: WorkManager
) : ChildWorkerFactory {

    override fun create(appContext: Context, params: WorkerParameters): ListenableWorker =
        SyncWorker(
            appContext,
            params,
            googleDriveModel,
            databaseModel,
            gson,
            sharedPreferences,
            resources,
            snapshotMerger,
            workManager
        )
}
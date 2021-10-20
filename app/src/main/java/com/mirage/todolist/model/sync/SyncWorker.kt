package com.mirage.todolist.model.sync

import android.content.Context
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.mirage.todolist.R
import com.mirage.todolist.model.tasks.TodolistModelImpl
import kotlinx.coroutines.*

class SyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val accNameKey = TodolistModelImpl.ACC_NAME_KEY
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val email = prefs.getString(accNameKey, "")
        if (email.isNullOrBlank()) return Result.success()
        val gDriveApi = GDriveRestApi()
        gDriveApi.init(applicationContext, email)
        val connectionCompleted = CompletableDeferred<Boolean>()
        val gDriveConnectExceptionHandler = object : GDriveConnectExceptionHandler {

            override suspend fun onSuccessfulConnect() {
                println("SERVICE - GDRIVE_CONNECT_SUCCESSFUL")
                connectionCompleted.complete(true)
            }

            override suspend fun onUserRecoverableFailure(ex: UserRecoverableAuthIOException) {
                println("SERVICE - USER_RECOVERABLE")
                connectionCompleted.complete(false)
            }

            override suspend fun onGoogleAuthFailure(ex: GoogleAuthIOException) {
                println("SERVICE - GOOGLE_AUTH_FAIL")
                println(ex.message)
                connectionCompleted.complete(false)
            }

            override suspend fun onUnspecifiedFailure(ex: Exception) {
                println("SERVICE - UNSPECIFIED_GDRIVE_CONNECT_FAILURE")
                println(ex.message)
                connectionCompleted.complete(false)
            }
        }
        gDriveApi.connect(gDriveConnectExceptionHandler)
        runBlocking(Dispatchers.IO) {
            loadFromGDrive(gDriveApi)
        }
        return Result.success()
    }

    private suspend fun loadFromGDrive(gDriveApi: GDriveRestApi) {
        var lockFileId = gDriveApi.getFileId(LOCKFILE_NAME)
        if (lockFileId == null) {
            lockFileId = gDriveApi.createFile(LOCKFILE_NAME)
        }

    }

    companion object {
        private const val LOCKFILE_NAME = "com-mirage-todolist-lockfile.json"
        private const val DATAFILE_NAME = "com-mirage-todolist-datafile.json"
    }
}
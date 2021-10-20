package com.mirage.todolist.model.sync

import android.content.Context
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.mirage.todolist.R
import com.mirage.todolist.model.room.DatabaseModel
import com.mirage.todolist.model.room.DatabaseModelImpl
import com.mirage.todolist.model.room.DatabaseSnapshot
import com.mirage.todolist.model.tasks.TodolistModelImpl
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.abs

class SyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val gDriveApi = GDriveRestApi()
    private val gson = Gson()
    private val identity = UUID.randomUUID()

    override fun doWork(): Result {
        val accNameKey = TodolistModelImpl.ACC_NAME_KEY
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val email = prefs.getString(accNameKey, "")
        if (email.isNullOrBlank()) return Result.success()
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
        val syncCompleted = runBlocking(Dispatchers.IO) {
            val lockSuccessful = tryLockGDrive()
            if (!lockSuccessful) return@runBlocking false
            val syncStartTime = System.currentTimeMillis()
            val dataFromDriveAsync = async {
                loadDataFromGDrive()
            }
            val dataFromDatabaseAsync = async {
                loadDataFromDatabase()
            }
            val dataFromDrive = dataFromDriveAsync.await()
            val dataFromDatabase = dataFromDatabaseAsync.await()
            val databaseVersion = dataFromDatabase.dataVersion
            val mergedSnapshot = mergeSnapshots(dataFromDatabase, dataFromDrive)
            val syncEndTime = System.currentTimeMillis()
            // If sync took too long, abort and retry later
            if (syncEndTime - syncStartTime > SYNC_TIMEOUT_TIME_MILLIS) return@runBlocking false
            writeDataToGDrive(mergedSnapshot)
            val databaseModel: DatabaseModel = DatabaseModelImpl()
            databaseModel.init(applicationContext) { }
            val databaseWriteSuccessful = databaseModel.updateDatabaseAfterSync(mergedSnapshot, databaseVersion)
            databaseWriteSuccessful
        }
        return if (syncCompleted) Result.success() else Result.retry()
    }

    /**
     * Tries to take lock of Google Drive.
     * @return true if successful, false if should be retried later
     */
    private suspend fun tryLockGDrive(): Boolean {
        var lockFileId = gDriveApi.getFileId(LOCKFILE_NAME)
        if (lockFileId == null) {
            lockFileId = gDriveApi.createFile(LOCKFILE_NAME)
        }
        val lockFileData = readLockFile(lockFileId)
        val currentTime = System.currentTimeMillis()
        if (abs(lockFileData.lockStartMillis - currentTime) < MAX_LOCK_TIME_MILLIS) {
            return false
        }
        val newLockFileData = LockFileData(currentTime, identity, lockFileData.dataVersion)
        gDriveApi.updateFile(lockFileId, gson.toJson(newLockFileData).encodeToByteArray())
        // Waiting a bit and reading the file again to prevent concurrent lock acquisition
        delay(LOCK_CONFIRM_WAIT_TIME_MILLIS)
        val updatedLockFileData = readLockFile(lockFileId)
        if (updatedLockFileData.lockOwner != identity) {
            return false
        }
        return true
    }

    private suspend fun loadDataFromGDrive(): DatabaseSnapshot {
        var dataFileId = gDriveApi.getFileId(DATAFILE_NAME)
        if (dataFileId == null) {
            dataFileId = gDriveApi.createFile(DATAFILE_NAME)
        }
        val dataFile = gDriveApi.downloadFile(dataFileId)
        val dataFileData = try {
            gson.fromJson(dataFile.decodeToString(), DatabaseSnapshot::class.java)
        } catch (ex: JsonSyntaxException) {
            DatabaseSnapshot(emptySet(), emptySet(), emptySet(), emptySet())
        }
        return dataFileData
    }

    private suspend fun readLockFile(lockFileId: String): LockFileData {
        val lockFile = gDriveApi.downloadFile(lockFileId)
        val lockFileData = try {
            gson.fromJson(lockFile.decodeToString(), LockFileData::class.java)
        } catch (ex: JsonSyntaxException) {
            LockFileData(-1L, null, null)
        }
        return lockFileData
    }

    private suspend fun loadDataFromDatabase(): DatabaseSnapshot {
        val databaseModel: DatabaseModel = DatabaseModelImpl()
        val snapshot = CompletableDeferred<DatabaseSnapshot>()
        databaseModel.init(applicationContext) { snapshot.complete(it) }
        return snapshot.await()
    }

    private suspend fun writeDataToGDrive(newSnapshot: DatabaseSnapshot) {
        val newVersion = newSnapshot.dataVersion
        val newLockFileData = LockFileData(-1L, identity, newVersion)
        val newData = gson.toJson(newSnapshot).encodeToByteArray()
        val newLock = gson.toJson(newLockFileData).encodeToByteArray()
        val lockFileId = gDriveApi.getFileId(LOCKFILE_NAME)!!
        val dataFileId = gDriveApi.getFileId(DATAFILE_NAME)!!
        gDriveApi.updateFile(dataFileId, newData)
        gDriveApi.updateFile(lockFileId, newLock)
    }

    companion object {
        private const val LOCKFILE_NAME = "com-mirage-todolist-lockfile.json"
        private const val DATAFILE_NAME = "com-mirage-todolist-datafile.json"
        private const val MAX_LOCK_TIME_MILLIS = 60 * 1000L
        private const val LOCK_CONFIRM_WAIT_TIME_MILLIS = 5 * 1000L
        private const val SYNC_TIMEOUT_TIME_MILLIS = 40 * 1000L
    }
}
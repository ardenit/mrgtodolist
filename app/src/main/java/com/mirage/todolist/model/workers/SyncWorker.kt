package com.mirage.todolist.model.workers

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.work.*
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.mirage.todolist.R
import com.mirage.todolist.model.database.AccountSnapshot
import com.mirage.todolist.model.database.DatabaseModel
import com.mirage.todolist.model.database.VersionEntity
import com.mirage.todolist.model.googledrive.GoogleDriveModel
import com.mirage.todolist.model.googledrive.LockFileData
import com.mirage.todolist.model.googledrive.SnapshotMerger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.Exception
import kotlin.math.abs

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val googleDriveModel: GoogleDriveModel,
    private val databaseModel: DatabaseModel,
    private val gson: Gson,
    private val sharedPreferences: SharedPreferences,
    private val resources: Resources,
    private val snapshotMerger: SnapshotMerger,
    private val workManager: WorkManager
) : Worker(context, workerParams) {

    /** Worker's identity used to acquire a lock file in Google Drive */
    private val identity = UUID.randomUUID()

    override fun doWork(): Result {
        val emailKey = resources.getString(R.string.key_sync_select_acc)
        val email = sharedPreferences.getString(emailKey, "")
        Timber.v("Performing sync work with email $email")
        if (email.isNullOrBlank()) return Result.success()
        val connectionCompleted = googleDriveModel.connectBlocking(email)
        if (!connectionCompleted) {
            Timber.v("Connection to Google Drive from SyncWorker failed")
            return Result.retry()
        }
        Timber.v("Locking Google Drive....")
        val lockSuccessful = tryLockGDrive()
        if (!lockSuccessful) return Result.retry()
        Timber.v("Google Drive lock successfully taken")
        val syncStartTime = System.currentTimeMillis()
        val (databaseData, driveData) = runBlocking(Dispatchers.IO) {
            val driveDataAsync = async {
                loadDataFromGDrive(email)
            }
            val databaseDataAsync = async {
                databaseModel.getAccountSnapshot()
            }
            Pair(databaseDataAsync.await(), driveDataAsync.await())
        }
        Timber.v("Database data: $databaseData")
        val databaseVersion = databaseData.version.dataVersion
        Timber.v("Database version: ${databaseData.version}")
        val mergedSnapshot = snapshotMerger.mergeSnapshots(databaseData, driveData)
        val syncEndTime = System.currentTimeMillis()
        // If sync took too long, abort and retry later
        if (syncEndTime - syncStartTime > SYNC_TIMEOUT_TIME_MILLIS) {
            Timber.e("Sync took too long, retrying later")
            return Result.retry()
        }
        writeDataToGDrive(mergedSnapshot)
        databaseModel.updateDatabaseAfterSync(mergedSnapshot, databaseVersion)
        val databaseWriteSuccessful =
            databaseModel.updateDatabaseAfterSync(mergedSnapshot, databaseVersion)
        return if (databaseWriteSuccessful) {
            Timber.v("Sync has been performed successfully, scheduling repeated sync")
            val request =
                PeriodicWorkRequest.Builder(
                    SyncWorker::class.java,
                    15,
                    TimeUnit.MINUTES,
                    5,
                    TimeUnit.MINUTES
                ).build()
            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
            Result.success()
        } else {
            Result.retry()
        }
    }

    /**
     * Tries to take lock of Google Drive.
     * @return true if successful, false if should be retried later
     */
    private fun tryLockGDrive(): Boolean {
        var lockFileId = googleDriveModel.getFileId(LOCKFILE_NAME)
        if (lockFileId == null) {
            lockFileId = googleDriveModel.createFile(LOCKFILE_NAME)
        }
        val lockFileData = readLockFile(lockFileId)
        val currentTime = System.currentTimeMillis()
        if (abs(lockFileData.lockStartMillis - currentTime) < MAX_LOCK_TIME_MILLIS) {
            return false
        }
        val newLockFileData = LockFileData(currentTime, identity, lockFileData.dataVersion)
        googleDriveModel.updateFile(lockFileId, gson.toJson(newLockFileData).encodeToByteArray())
        // Waiting a bit and reading the file again to prevent concurrent lock acquisition
        Thread.sleep(LOCK_CONFIRM_WAIT_TIME_MILLIS)
        val updatedLockFileData = readLockFile(lockFileId)
        if (updatedLockFileData.lockOwner != identity) {
            return false
        }
        return true
    }

    private fun loadDataFromGDrive(email: String): AccountSnapshot {
        var dataFileId = googleDriveModel.getFileId(DATAFILE_NAME)
        if (dataFileId == null) {
            dataFileId = googleDriveModel.createFile(DATAFILE_NAME)
        }
        val dataFile = googleDriveModel.downloadFile(dataFileId)
        val dataFileData = try {
            gson.fromJson(dataFile.decodeToString(), AccountSnapshot::class.java).apply { version.dataVersion }
        } catch (ex: Exception) {
            val versionEntity = VersionEntity(
                accountName = email,
                dataVersion = UUID.randomUUID(),
                mustBeProcessed = false
            )
            AccountSnapshot(emptyList(), emptyList(), emptyList(), versionEntity, email)
        }
        Timber.v("Downloaded snapshot: $dataFileData")
        return dataFileData
    }

    private fun readLockFile(lockFileId: String): LockFileData {
        val lockFile = googleDriveModel.downloadFile(lockFileId)
        val lockFileData = try {
            gson.fromJson(lockFile.decodeToString(), LockFileData::class.java)
        } catch (ex: JsonSyntaxException) {
            LockFileData(-1L, null, null)
        }
        return lockFileData
    }

    private fun writeDataToGDrive(newSnapshot: AccountSnapshot) {
        val newVersion = newSnapshot.version.dataVersion
        val newLockFileData = LockFileData(-1L, identity, newVersion)
        val newData = gson.toJson(newSnapshot).encodeToByteArray()
        val newLock = gson.toJson(newLockFileData).encodeToByteArray()
        val lockFileId = googleDriveModel.getFileId(LOCKFILE_NAME)!!
        val dataFileId = googleDriveModel.getFileId(DATAFILE_NAME)!!
        googleDriveModel.updateFile(dataFileId, newData)
        googleDriveModel.updateFile(lockFileId, newLock)
    }

    companion object {
        private const val LOCKFILE_NAME = "com-mirage-todolist-lockfile.json"
        private const val DATAFILE_NAME = "com-mirage-todolist-datafile.json"
        private const val MAX_LOCK_TIME_MILLIS = 60 * 1000L
        private const val LOCK_CONFIRM_WAIT_TIME_MILLIS = 5 * 1000L
        private const val SYNC_TIMEOUT_TIME_MILLIS = 40 * 1000L
        const val UNIQUE_WORK_NAME = "com-mirage-todolist-sync-work"
    }
}
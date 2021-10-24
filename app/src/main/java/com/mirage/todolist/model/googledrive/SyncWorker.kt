package com.mirage.todolist.model.googledrive

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.mirage.todolist.R
import com.mirage.todolist.model.database.DatabaseModel
import com.mirage.todolist.model.database.DatabaseSnapshot
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

class SyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    @Inject
    lateinit var googleDriveModel: GoogleDriveModel
    @Inject
    lateinit var databaseModel: DatabaseModel
    @Inject
    lateinit var gson: Gson
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    @Inject
    lateinit var resources: Resources
    @Inject
    lateinit var snapshotMerger: SnapshotMerger

    /** Worker's identity used to acquire a lock file in Google Drive */
    private val identity = UUID.randomUUID()

    override fun doWork(): Result {
        val emailKey = resources.getString(R.string.key_sync_select_acc)
        val email = sharedPreferences.getString(emailKey, "")
        if (email.isNullOrBlank()) return Result.success()
        val connectionCompleted = googleDriveModel.connectBlocking(email)
        if (!connectionCompleted) {
            Timber.v("Connection to Google Drive from SyncWorker failed")
            return Result.retry()
        }
        val lockSuccessful = tryLockGDrive()
        if (!lockSuccessful) return Result.retry()
        val syncStartTime = System.currentTimeMillis()
        val (databaseData, driveData) = runBlocking(Dispatchers.IO) {
            val driveDataAsync = async {
                loadDataFromGDrive()
            }
            val databaseDataAsync = async {
                databaseModel.getDatabaseSnapshot()
            }
            Pair(databaseDataAsync.await(), driveDataAsync.await())
        }
        val databaseVersion = databaseData.getDataVersion(email)
        val mergedSnapshot = snapshotMerger.mergeSnapshots(email, databaseData, driveData)
        val syncEndTime = System.currentTimeMillis()
        // If sync took too long, abort and retry later
        if (syncEndTime - syncStartTime > SYNC_TIMEOUT_TIME_MILLIS) {
            Timber.e("Sync took too long, retrying later")
            return Result.retry()
        }
        writeDataToGDrive(email, mergedSnapshot)
        databaseModel.updateDatabaseAfterSync(mergedSnapshot, databaseVersion)
        val databaseWriteSuccessful = databaseModel.updateDatabaseAfterSync(mergedSnapshot, databaseVersion)
        return if (databaseWriteSuccessful) Result.success() else Result.retry()
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

    private fun loadDataFromGDrive(): DatabaseSnapshot {
        var dataFileId = googleDriveModel.getFileId(DATAFILE_NAME)
        if (dataFileId == null) {
            dataFileId = googleDriveModel.createFile(DATAFILE_NAME)
        }
        val dataFile = googleDriveModel.downloadFile(dataFileId)
        val dataFileData = try {
            gson.fromJson(dataFile.decodeToString(), DatabaseSnapshot::class.java)
        } catch (ex: JsonSyntaxException) {
            DatabaseSnapshot(emptyList(), emptyList(), emptyList(), emptyList())
        }
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

    private fun writeDataToGDrive(email: String, newSnapshot: DatabaseSnapshot) {
        val newVersion = newSnapshot.getDataVersion(email)
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
    }
}
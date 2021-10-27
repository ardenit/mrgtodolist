package com.mirage.todolist.model.googledrive

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.mirage.todolist.BuildConfig
import com.mirage.todolist.di.App
import com.mirage.todolist.di.ApplicationContext
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Model that works with Google Drive and handles connecting, uploading and downloading data there
 */
class GoogleDriveModel {

    @ApplicationContext
    @Inject
    lateinit var context: Context
    @Volatile
    private lateinit var gDrive: Drive
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(job + Dispatchers.IO)

    init {
        App.instance.appComponent.inject(this)
    }

    /**
     * Sets the email for synchronization and connects to Google Drive using that email
     * Blocks the calling thread until connection is finished,
     * returns true if it was successful and false otherwise
     * Designed to be used from SyncWorker
     */
    fun connectBlocking(email: String): Boolean {
        Timber.v("Connecting to Google Drive using email $email")
        gDrive = Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA)
            ).setSelectedAccountName(email)
        ).setApplicationName(GDRIVE_APP_NAME).build()
        val successful = try {
            gDrive.files().get("root").setFields("title").execute()
            true
        } catch (ex: GoogleJsonResponseException) {
            ex.statusCode == 404
        } catch (ex: Exception) {
            false
        }
        if (successful) {
            Timber.v("Successfully connected to Google Drive")
        } else {
            Timber.v("Connection to Google Drive failed")
        }
        return successful
    }

    /**
     * Sets the email for synchronization and connects to Google Drive using that email in a background thread
     * Designed to be used from user UI after selecting account email
     * Calls [exceptionHandler] using the main thread dispatcher
     */
    fun connectAsync(email: String, exceptionHandler: GoogleDriveConnectExceptionHandler) {
        Timber.v("Connecting to Google Drive using email $email")
        coroutineScope.launch(Dispatchers.IO) {
            gDrive = Drive.Builder(
                NetHttpTransport(),
                GsonFactory(),
                GoogleAccountCredential.usingOAuth2(
                    context, listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA)
                ).setSelectedAccountName(email)
            ).setApplicationName(GDRIVE_APP_NAME).build()
            withContext(Dispatchers.Main) {
                try {
                    withContext(Dispatchers.IO) {
                        gDrive.files().get("root").setFields("title").execute()
                    }
                    exceptionHandler.onSuccessfulConnect()
                } catch (ex: UserRecoverableAuthIOException) {
                    exceptionHandler.onUserRecoverableFailure(ex)
                } catch (ex: GoogleAuthIOException) {
                    exceptionHandler.onGoogleAuthFailure(ex)
                } catch (ex: GoogleJsonResponseException) {
                    // '404 not found' in FILE scope, consider connected
                    if (ex.statusCode == 404) {
                        exceptionHandler.onSuccessfulConnect()
                    } else {
                        exceptionHandler.onUnspecifiedFailure(ex)
                    }
                } catch (ex: Exception) {
                    exceptionHandler.onUnspecifiedFailure(ex)
                }
            }
        }
    }

    fun createFile(fileName: String): String {
        val file = File()
        file.name = fileName
        file.parents = listOf("appDataFolder")
        val inputStream = ByteArrayInputStream("{}".encodeToByteArray())
        val driveFile = gDrive.Files()
            .create(file, InputStreamContent("application/json", inputStream))
            .setFields("id")
            .execute()
        return driveFile.id
    }

    fun getFileId(fileName: String): String? {
        return gDrive.Files().list()
            .setSpaces("appDataFolder")
            .execute()
            .files
            .filter { it.name == fileName }
            .map { it.id }
            .firstOrNull()
    }

    fun downloadFile(fileId: String): ByteArray {
        val outputStream = ByteArrayOutputStream()
        gDrive.Files().get(fileId)
            .executeMediaAndDownloadTo(outputStream)
        return outputStream.toByteArray()
    }

    fun updateFile(fileId: String, newData: ByteArray) {
        val inputStream = ByteArrayInputStream(newData)
        gDrive.Files()
            .update(fileId, null, InputStreamContent("application/json", inputStream))
            .execute()
    }

    companion object {
        const val OAUTH_CLIENT_KEY = BuildConfig.OAUTH_CLIENT_ID
        private const val GDRIVE_APP_NAME = "com.mirage.todolist"
    }
}
package com.mirage.todolist.model.gdrive

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.mirage.todolist.BuildConfig
import com.mirage.todolist.R
import kotlinx.coroutines.*

const val OAUTH_CLIENT_KEY = BuildConfig.OAUTH_CLIENT_ID
const val GDRIVE_APP_NAME = "com.mirage.todolist"

class GDriveRestApi {

    private lateinit var gDrive: Drive
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(job + Dispatchers.IO)

    fun init(ctx: Context, email: String?) {
        gDrive = Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            GoogleAccountCredential.usingOAuth2(ctx, listOf(DriveScopes.DRIVE_FILE)
            ).setSelectedAccountName(email)).setApplicationName(GDRIVE_APP_NAME).build()
    }

    fun connect(exHandler: GDriveConnectExceptionHandler) {
        val connectResult = coroutineScope.async(Dispatchers.IO) {
            println("Starting async gdrive connect")
            gDrive.files().get("root").setFields("title").execute()
            println("Succ end async gdrive connect")
        }
        coroutineScope.launch(Dispatchers.Main) {
            try {
                connectResult.await()
                exHandler.onSuccessfulConnect()
            } catch (ex: UserRecoverableAuthIOException) {
                exHandler.onUserRecoverableFailure(ex)
            } catch (ex: GoogleAuthIOException) {
                exHandler.onGoogleAuthFailure(ex)
            } catch (ex: GoogleJsonResponseException) {
                // '404 not found' in FILE scope, consider connected
                if (ex.statusCode == 404) {
                    exHandler.onSuccessfulConnect()
                }
                else {
                    println("GoogleJsonResponseException ${ex.statusCode} ${ex.statusMessage} ${ex.message}")
                }
            } catch (ex: Exception) {
                exHandler.onUnspecifiedFailure(ex)
            }
        }
    }

    //TODO
    fun disconnect() {

    }

}

/**
 * Exception handler for GDriveRestApi.connect() method
 * All functions are invoked within UI (main) thread
 */
interface GDriveConnectExceptionHandler {

    suspend fun onSuccessfulConnect()

    // standard authorization failure - user fixable
    suspend fun onUserRecoverableFailure(ex: UserRecoverableAuthIOException)

    // usually PackageName /SHA1 mismatch in DevConsole
    suspend fun onGoogleAuthFailure(ex: GoogleAuthIOException)

    // "the name must not be empty" indicates
    // UNREGISTERED / EMPTY account in 'setSelectedAccountName()' above
    suspend fun onUnspecifiedFailure(ex: Exception)

}
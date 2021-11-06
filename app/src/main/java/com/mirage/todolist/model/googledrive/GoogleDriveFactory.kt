package com.mirage.todolist.model.googledrive

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes

open class GoogleDriveFactory(private val context: Context) {

    open fun createDrive(email: String): Drive = Drive.Builder(
        NetHttpTransport(),
        GsonFactory(),
        GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA)
        ).setSelectedAccountName(email)
    ).setApplicationName(GDRIVE_APP_NAME).build()

    companion object {
        private const val GDRIVE_APP_NAME = "com.mirage.todolist"
    }
}
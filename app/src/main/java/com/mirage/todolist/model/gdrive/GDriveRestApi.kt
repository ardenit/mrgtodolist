package com.mirage.todolist.model.gdrive

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.mirage.todolist.BuildConfig

const val TEST_EMAIL = BuildConfig.TEST_EMAIL
const val OAUTH_CLIENT_KEY = BuildConfig.OAUTH_CLIENT_ID

class GDriveRestApi {

    var gDrive: Drive? = null

    fun init(ctx: Context) {
        gDrive = Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            GoogleAccountCredential.usingOAuth2(ctx, listOf(DriveScopes.DRIVE_FILE)
            ).setSelectedAccountName("email")).build()
    }


}
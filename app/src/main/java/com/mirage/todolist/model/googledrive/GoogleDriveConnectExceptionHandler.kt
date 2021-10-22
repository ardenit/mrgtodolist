package com.mirage.todolist.model.googledrive

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException


/**
 * Exception handler for GoogleDriveRestApi.connect() method
 * All functions are invoked within UI (main) thread
 */
interface GoogleDriveConnectExceptionHandler {

    suspend fun onSuccessfulConnect()

    // standard authorization failure - user fixable
    suspend fun onUserRecoverableFailure(ex: UserRecoverableAuthIOException)

    // usually PackageName /SHA1 mismatch in DevConsole
    suspend fun onGoogleAuthFailure(ex: GoogleAuthIOException)

    // "the name must not be empty" indicates
    // UNREGISTERED / EMPTY account in 'setSelectedAccountName()' above
    suspend fun onUnspecifiedFailure(ex: Exception)

}
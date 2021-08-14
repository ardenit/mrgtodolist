package com.mirage.todolist.model

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.mirage.todolist.model.gdrive.GDriveConnectExceptionHandler
import com.mirage.todolist.model.gdrive.GDriveRestApi

private const val ACC_NAME_KEY = "account_name"
@SuppressLint("StaticFieldLeak")
val todolistModel = TodolistModelImpl()

class TodolistModelImpl {

    private val gDriveRestApi = GDriveRestApi()

    private lateinit var appCtx: Context
    private lateinit var prefs: SharedPreferences

    private var email: String? = null

    fun getGDriveAccountEmail(): String? {
        if (email == null) email = prefs.getString(ACC_NAME_KEY, null)
        return email
    }

    /**
     * Changes Google Drive account
     * [connectToGDriveAccount] should be called after this method to connect to new account
     */
    fun setGDriveAccount(email: String?) {
        this.email = email
        prefs.edit().putString(ACC_NAME_KEY, email).apply()
        gDriveRestApi.init(appCtx, email)
    }

    /**
     * Connects to Google Drive account
     * Callbacks from [exHandler] will be executed asynchronously in the main (UI) thread
     * Google Drive account must be selected via [setGDriveAccount] prior to this method's call
     */
    fun connectToGDriveAccount(exHandler: GDriveConnectExceptionHandler) {
        gDriveRestApi.connect(exHandler)
    }

    fun init(ctx: Context) {
        appCtx = ctx.applicationContext
        prefs = PreferenceManager.getDefaultSharedPreferences(appCtx)
        gDriveRestApi.init(appCtx, email)
    }



}
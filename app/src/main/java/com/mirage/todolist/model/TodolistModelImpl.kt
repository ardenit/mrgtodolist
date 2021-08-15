package com.mirage.todolist.model

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.mirage.todolist.model.gdrive.GDriveConnectExceptionHandler
import com.mirage.todolist.model.gdrive.GDriveRestApi
import com.mirage.todolist.viewmodel.LiveTask
import com.mirage.todolist.viewmodel.MutableLiveTask
import com.mirage.todolist.viewmodel.TaskID
import com.mirage.todolist.viewmodel.TasklistType
import java.util.*
import kotlin.collections.HashMap

private const val ACC_NAME_KEY = "account_name"

class TodolistModelImpl: TodolistModel {

    private val gDriveRestApi = GDriveRestApi()
    private lateinit var appCtx: Context
    private lateinit var prefs: SharedPreferences
    private var email: String? = null

    /** Local cache for tasks, key is task's unique taskID */
    private var localTasks: MutableMap<TaskID, MutableLiveTask> = HashMap()
    private val tasklistSizes: MutableMap<Int, Int> = HashMap()


    override fun init(appCtx: Context) {
        this.appCtx = appCtx.applicationContext
        prefs = PreferenceManager.getDefaultSharedPreferences(this.appCtx)
        gDriveRestApi.init(this.appCtx, email)
    }

    override fun getGDriveAccountEmail(): String? {
        if (email == null) email = prefs.getString(ACC_NAME_KEY, null)
        return email
    }

    override fun setGDriveAccountEmail(newEmail: String, exHandler: GDriveConnectExceptionHandler) {
        email = newEmail
        prefs.edit().putString(ACC_NAME_KEY, email).apply()
        gDriveRestApi.init(appCtx, email)
        gDriveRestApi.connect(exHandler)
    }

    override fun createNewTask(tasklistID: Int): LiveTask {
        val taskIndex = tasklistSizes[tasklistID] ?: 0
        val taskID = UUID.randomUUID()
        val task = MutableLiveTask(taskID, tasklistID, taskIndex, "", "")
        tasklistSizes[tasklistID] = taskIndex + 1
        localTasks[taskID] = task
        //TODO room query
        return task
    }

    override fun modifyTask(
        taskID: TaskID,
        tasklistID: Int?,
        taskIndex: Int?,
        title: String?,
        description: String?
    ) {
        val task = localTasks[taskID] ?: return

    }

    override fun deleteTask(taskID: TaskID) {
        TODO("Not yet implemented")
    }

    override fun getAllTasks(): MutableList<MutableLiveTask> {
        TODO("Not yet implemented")
    }

    override fun addOnNewTaskListener(listener: OnNewTaskListener) {
        TODO("Not yet implemented")
    }

    override fun removeOnNewTaskListener(listener: OnNewTaskListener) {
        TODO("Not yet implemented")
    }

    override fun addOnMoveTaskListener(listener: OnMoveTaskListener) {
        TODO("Not yet implemented")
    }

    override fun removeOnMoveTaskListener(listener: OnMoveTaskListener) {
        TODO("Not yet implemented")
    }

    override fun addFullTasklistUpdateListener(listener: OnFullUpdateListener) {
        TODO("Not yet implemented")
    }

    override fun removeFullTasklistUpdateListener(listener: OnFullUpdateListener) {
        TODO("Not yet implemented")
    }
}
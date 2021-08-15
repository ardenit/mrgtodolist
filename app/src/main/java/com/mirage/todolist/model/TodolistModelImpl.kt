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
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap

private const val ACC_NAME_KEY = "account_name"
private const val HIDDEN_TASKLIST_ID = -1

/**
 * See [TodolistModel] for API documentation
 */
class TodolistModelImpl: TodolistModel {

    private val gDriveRestApi = GDriveRestApi()
    private lateinit var appCtx: Context
    private lateinit var prefs: SharedPreferences
    private var email: String? = null

    /** Local cache for tasks, key is task's unique taskID */
    private var localTasks: MutableMap<TaskID, MutableLiveTask> = LinkedHashMap()
    private val tasklistSizes: MutableMap<Int, Int> = LinkedHashMap()

    private val onNewTaskListeners: MutableSet<OnNewTaskListener> = LinkedHashSet()
    private val onMoveTaskListeners: MutableSet<OnMoveTaskListener> = LinkedHashSet()
    private val onFullUpdateListeners: MutableSet<OnFullUpdateListener> = LinkedHashSet()

    override fun init(appCtx: Context) {
        this.appCtx = appCtx.applicationContext
        prefs = PreferenceManager.getDefaultSharedPreferences(this.appCtx)
        gDriveRestApi.init(this.appCtx, email)

        //TODO load tasks from room and start sync with gdrive
        repeat(3) { tasklistID ->
            val tasksCount = 20
            repeat(tasksCount) { taskIndex ->
                val id = UUID.randomUUID()
                val task = MutableLiveTask(id, tasklistID, taskIndex, "Title", "init $tasklistID $taskIndex")
                localTasks[id] = task
            }
            tasklistSizes[tasklistID] = tasksCount
        }
    }

    override fun getGDriveAccountEmail(): String? {
        if (email == null) email = prefs.getString(ACC_NAME_KEY, null)
        return email
    }

    override fun setGDriveAccountEmail(newEmail: String?, exHandler: GDriveConnectExceptionHandler) {
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
        val oldTasklistID = task.tasklistID.value ?: HIDDEN_TASKLIST_ID
        val oldTaskIndex = task.taskIndex.value ?: 0
        if (tasklistID != null && tasklistID != oldTasklistID) {
            task.tasklistID.value = tasklistID
            onMoveTaskListeners.forEach { listener ->
                listener(task, oldTasklistID, tasklistID, oldTaskIndex, taskIndex ?: oldTaskIndex)
            }
        }
        if (taskIndex != null && taskIndex != task.taskIndex.value) {
            task.taskIndex.value = taskIndex
        }
        if (title != null && title != task.title.value) {
            task.title.value = title
        }
        if (description != null && description != task.description.value) {
            task.description.value = description
        }
    }

    override fun deleteTask(taskID: TaskID) {
        modifyTask(taskID, HIDDEN_TASKLIST_ID, null, null, null)
    }

    override fun getAllTasks(): Map<TaskID, LiveTask> {
        return localTasks
    }

    override fun addOnNewTaskListener(listener: OnNewTaskListener) {
        onNewTaskListeners.add(listener)
    }

    override fun removeOnNewTaskListener(listener: OnNewTaskListener) {
        onNewTaskListeners.remove(listener)
    }

    override fun addOnMoveTaskListener(listener: OnMoveTaskListener) {
        onMoveTaskListeners.add(listener)
    }

    override fun removeOnMoveTaskListener(listener: OnMoveTaskListener) {
        onMoveTaskListeners.remove(listener)
    }

    override fun addOnFullUpdateListener(listener: OnFullUpdateListener) {
        onFullUpdateListeners.add(listener)
    }

    override fun removeOnFullUpdateListener(listener: OnFullUpdateListener) {
        onFullUpdateListeners.remove(listener)
    }
}
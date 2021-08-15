package com.mirage.todolist.model

import android.content.Context
import com.google.android.gms.tasks.Task
import com.mirage.todolist.model.gdrive.GDriveConnectExceptionHandler
import com.mirage.todolist.viewmodel.LiveTask
import com.mirage.todolist.viewmodel.TasklistType
import com.mirage.todolist.viewmodel.TaskID

//TODO Inject
private val todolistModelInstance: TodolistModel = TodolistModelImpl()
fun getTodolistModel(): TodolistModel = todolistModelInstance

typealias OnNewTaskListener = (newTask: LiveTask) -> Unit
typealias OnMoveTaskListener = (task: LiveTask, oldTasklistID: Int, newTasklistID: Int, oldTaskIndex: Int, newTaskIndex: Int) -> Unit
typealias OnFullUpdateListener = (newTasks: Map<TaskID, LiveTask>) -> Unit

interface TodolistModel {

    /**
     * Initiates the model.
     * Must be called at least once before doing any other operations with model.
     * May be safely called multiple times during application lifecycle.
     * [appCtx] - application context.
     */
    fun init(appCtx: Context)

    /**
     * Returns current Google Drive account email used for synchronization, or null if sync is not configured.
     */
    fun getGDriveAccountEmail(): String?

    /**
     * Changes Google Drive email.
     * Should be invoked when user selects new account for synchronization.
     */
    fun setGDriveAccountEmail(newEmail: String?, exHandler: GDriveConnectExceptionHandler)

    /**
     * Creates a new task in a given [tasklistID] and returns it.
     */
    fun createNewTask(tasklistID: Int): LiveTask

    /**
     * Modifies the task with ID [taskID].
     * If a parameter's value is null, its value remains unchanged.
     * This method automatically updates "last modified" time.
     */
    fun modifyTask(
        taskID: TaskID,
        tasklistID: Int?,
        taskIndex: Int?,
        title: String?,
        description: String?
    )

    /**
     * Deletes the task with ID [taskID]
     * (actually just moves to hidden tasklist to simplify diff calculation).
     * This method automatically updates "last modified" time.
     */
    fun deleteTask(taskID: TaskID)

    /**
     * Returns a map of all tasks (including hidden/removed ones).
     */
    fun getAllTasks(): Map<TaskID, LiveTask>

    /**
     * Adds a listener for new task event
     */
    fun addOnNewTaskListener(listener: OnNewTaskListener)

    fun removeOnNewTaskListener(listener: OnNewTaskListener)

    /**
     * Adds a listener for task's tasklist change event (including removal, i.e. moving to -1)
     */
    fun addOnMoveTaskListener(listener: OnMoveTaskListener)

    fun removeOnMoveTaskListener(listener: OnMoveTaskListener)

    /**
     * Adds a listener for tasklist update events coming from model independently from user actions
     * (i.e. updates performed on another device using the same Google Drive account).
     */
    fun addOnFullUpdateListener(listener: OnFullUpdateListener)

    fun removeOnFullUpdateListener(listener: OnFullUpdateListener)

}
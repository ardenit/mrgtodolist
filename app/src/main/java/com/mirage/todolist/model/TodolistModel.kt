package com.mirage.todolist.model

import android.content.Context
import com.mirage.todolist.content.TasklistType
import com.mirage.todolist.viewmodel.MutableLiveTask

interface TodolistModel {

    /**
     * Initiates the model.
     * Must be called at least once before doing any other operations with model.
     * May be safely called multiple times during application lifecycle.
     * [appCtx] - application context.
     */
    fun init(appCtx: Context)

    /**
     * Changes Google Drive email.
     * Should be invoked when user selects new account for synchronization.
     */
    fun setGDriveAccountEmail(newEmail: String)

    /**
     * Creates a new task in a given [tasklist] and returns a generated unique ID for the task.
     */
    fun createNewTask(tasklist: TasklistType): Long

    /**
     * Modifies the task with ID [taskID].
     * If a parameter's value is null, its value remains unchanged.
     * This method automatically updates "last modified" time.
     */
    fun modifyTask(
        taskID: Long,
        title: String?,
        description: String?
    )

    /**
     * Deletes the task with ID [taskID]
     * (actually just moves to hidden tasklist to simplify diff calculation).
     * This method automatically updates "last modified" time.
     */
    fun deleteTask(taskID: Long)

    /**
     * Returns a list of all tasks (including hidden/removed ones).
     * This method is intended to be used on view initialization and from listeners
     * registered by [addTasklistUpdateListener] to update the view.
     * Returned list may be saved and then used in view model.
     * Model never uses the returned list, mutability is there to avoid unnecessary copying in viewmodel.
     */
    fun getAllTasks(): MutableList<MutableLiveTask>

    /**
     * Adds a listener for tasklist update events coming from model independently from user actions
     * (i.e. updates performed on another device using the same Google Drive account).
     */
    fun addTasklistUpdateListener(listener: () -> Unit)

}
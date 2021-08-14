package com.mirage.todolist.viewmodel

/**
 * View model of the todolist screen
 */
interface TodolistViewModel {

    /**
     * Registers a listener for task creation event
     */
    fun addOnNewTaskListener(onNewTask: (LiveTask) -> Unit)

    /**
     * Registers a listener for full tasklist overwrite event
     * This event may happen after synchronization with Google Drive
     */
    fun addOnFullTasklistUpdateListener(onFullUpdate: (List<LiveTask>) -> Unit)

    /**
     * Returns a [LiveTask] with a given [taskID], or null if there is no task with this ID
     */
    fun getTask(taskID: Long): LiveTask?

}
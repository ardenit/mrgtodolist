package com.mirage.todolist.viewmodel

import androidx.lifecycle.ViewModel

/**
 * View model of a single tasklist page
 */
abstract class TasklistViewModel : ViewModel() {

    /**
     * Registers a listener for task creation event
     */
    abstract fun addOnNewTaskListener(onNewTask: (LiveTask) -> Unit)

    /**
     * Registers a listener for full tasklist overwrite event
     * This event may happen after synchronization with Google Drive
     */
    abstract fun addOnFullTasklistUpdateListener(onFullUpdate: (List<LiveTask>) -> Unit)

    /**
     * Returns a [LiveTask] with a given [taskID], or null if there is no task with this ID
     */
    abstract fun getTask(taskID: Long): LiveTask?

    /**
     * Returns a [LiveTask] with a given [taskIndex] in this tasklist, or null if index is invalid
     */
    abstract fun getTaskByIndex(taskIndex: Int): LiveTask?

    /**
     * Returns the number of tasks currently in this tasklist
     */
    abstract fun getTaskCount(): Int

}
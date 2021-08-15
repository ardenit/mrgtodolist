package com.mirage.todolist.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel

/**
 * View model of the todolist screen
 */
abstract class TodolistViewModel : ViewModel(){

    /**
     * Creates a new task in the bottom of a given tasklist
     * Should be called in floating "add" button click listener
     */
    abstract fun createNewTask(tasklist: Int): LiveTask

    /**
     * Registers a listener for task creation event
     */
    abstract fun addOnNewTaskListener(owner: LifecycleOwner, onNewTask: (LiveTask) -> Unit)

    /**
     * Registers a listener for full tasklist overwrite event
     * This event may happen after synchronization with Google Drive
     */
    abstract fun addOnFullTasklistUpdateListener(owner: LifecycleOwner, onFullUpdate: (List<LiveTask>) -> Unit)

    /**
     * Returns a [LiveTask] with a given [taskID], or null if there is no task with this ID
     */
    abstract fun getTask(taskID: TaskID): LiveTask?

}
package com.mirage.todolist.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.mirage.todolist.model.OnFullUpdateListener
import com.mirage.todolist.model.OnNewTaskListener

typealias OnRemoveTaskListener = (task: LiveTask, taskIndex: Int) -> Unit

/**
 * View model of a single tasklist page
 */
abstract class TasklistViewModel : ViewModel() {

    /**
     * [parentViewModel] - [TodolistViewModel] of todolist activity to which this tasklist is bound
     * [tasklistID] - id of tasklist processed by this view model
     */
    abstract fun init(parentViewModel: TodolistViewModel, tasklistID: Int)

    /**
     * Registers a listener for task creation event in this tasklist
     */
    abstract fun addOnNewTaskListener(owner: LifecycleOwner, listener: OnNewTaskListener)

    /**
     * Registers a listener for removing a task from this tasklist
     */
    abstract fun addOnRemoveTaskListener(owner: LifecycleOwner, listener: OnRemoveTaskListener)

    /**
     * Registers a listener for full tasklist overwrite event
     * This event may happen after synchronization with Google Drive
     */
    abstract fun addOnFullTasklistUpdateListener(owner: LifecycleOwner, listener: OnFullUpdateListener)

    /**
     * Returns a [LiveTask] with a given [taskID], or null if there is no task with this ID
     */
    abstract fun getTask(taskID: TaskID): LiveTask?

    /**
     * Returns a [LiveTask] with a given [taskIndex] in this tasklist, or null if index is invalid
     */
    abstract fun getTaskByIndex(taskIndex: Int): LiveTask?

    /**
     * Returns the number of tasks currently in this tasklist
     */
    abstract fun getTaskCount(): Int

}
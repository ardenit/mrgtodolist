package com.mirage.todolist.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.mirage.todolist.model.tasks.*

typealias OnRemoveTaskListener = (task: LiveTask, taskIndex: Int) -> Unit

/**
 * View model of a single tasklist page
 */
abstract class TaskRecyclerViewModel : ViewModel() {

    /**
     * [tasklistID] - id of tasklist processed by this view model
     */
    abstract fun init(tasklistID: Int)

    /**
     * Returns the ID of a tasklist associated with this viewmodel
     */
    abstract fun getTasklistID(): Int

    /**
     * Moves task to another tasklist
     */
    abstract fun swipeTaskLeft(taskIndex: Int)

    /**
     * Moves task to another tasklist
     */
    abstract fun swipeTaskRight(taskIndex: Int)

    /**
     * Moves task to another position in the same tasklist
     */
    abstract fun dragTask(fromIndex: Int, toIndex: Int)

    /** Starts searching for tasks with given tag */
    abstract fun searchForTag(tag: LiveTag)

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
     * This event may happen after synchronization with Google Drive, or on task search query
     */
    abstract fun addOnFullUpdateTaskListener(owner: LifecycleOwner, listener: OnFullUpdateTaskListener)

    /**
     * Returns a [LiveTask] with a given [taskID], or null if there is no task with this ID
     */
    abstract fun getTask(taskID: TaskID): LiveTask?

    /**
     * Returns a [LiveTask] with a given visible [position] in this tasklist, or null if index is invalid
     */
    abstract fun getTaskByVisibleIndex(position: Int): LiveTask?

    /**
     * Returns the number of tasks currently visible in this tasklist
     */
    abstract fun getVisibleTaskCount(): Int

}
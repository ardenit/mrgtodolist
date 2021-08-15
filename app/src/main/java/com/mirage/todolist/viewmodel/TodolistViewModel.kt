package com.mirage.todolist.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.mirage.todolist.model.OnFullUpdateListener
import com.mirage.todolist.model.OnMoveTaskListener
import com.mirage.todolist.model.OnNewTaskListener

/**
 * View model of the todolist screen
 */
abstract class TodolistViewModel : ViewModel(){

    /**
     * Creates a new task in the bottom of a given tasklist
     * Should be called in floating "add" button click listener
     */
    abstract fun createNewTask(tasklistID: Int): LiveTask

    /**
     * Modifies the task with ID [taskID].
     * If a parameter's value is null, its value remains unchanged.
     * This method automatically updates "last modified" time.
     */
    abstract fun modifyTask(
        taskID: TaskID,
        title: String?,
        description: String?
    )

    /**
     * Moves the task with ID [taskID] to another tasklist.
     * Should be called on task swipe.
     */
    abstract fun moveTask(
        taskID: TaskID,
        newTasklistID: Int
    )

    /**
     * Moves the task to another position inside the tasklist.
     * Should be called on task drag-and-drop.
     */
    abstract fun moveTaskInList(
        taskID: TaskID,
        newTaskIndex: Int
    )

    /**
     * Registers a listener for task creation event
     */
    abstract fun addOnNewTaskListener(owner: LifecycleOwner, listener: OnNewTaskListener)

    /**
     * Registers a prioritized listener, which will be called before ones registered by regular methods.
     * These methods are intended to be used by child view models to intercept updates and update their tasklist cache.
     */
    abstract fun addOnNewTaskPrioritizedListener(listener: OnNewTaskListener)
    abstract fun removeOnNewTaskPrioritizedListener(listener: OnNewTaskListener)

    /**
     * Registers a listener for task's tasklist change event (including removal)
     */
    abstract fun addOnMoveTaskListener(owner: LifecycleOwner, listener: OnMoveTaskListener)

    /**
     * Registers a prioritized listener, which will be called before ones registered by regular methods.
     * These methods are intended to be used by child view models to intercept updates and update their tasklist cache.
     */
    abstract fun addOnMoveTaskPrioritizedListener(listener: OnMoveTaskListener)
    abstract fun removeOnMoveTaskPrioritizedListener(listener: OnMoveTaskListener)

    /**
     * Registers a listener for full tasklist overwrite event
     * This event may happen after synchronization with Google Drive
     */
    abstract fun addOnFullUpdateListener(owner: LifecycleOwner, listener: OnFullUpdateListener)

    /**
     * Registers a prioritized listener, which will be called before ones registered by regular methods.
     * These methods are intended to be used by child view models to intercept updates and update their tasklist cache.
     */
    abstract fun addOnFullUpdatePrioritizedListener(listener: OnFullUpdateListener)
    abstract fun removeOnFullUpdatePrioritizedListener(listener: OnFullUpdateListener)

    /**
     * Returns a [LiveTask] with a given [taskID], or null if there is no task with this ID
     */
    abstract fun getTask(taskID: TaskID): LiveTask?

    abstract fun getAllTasks(): Map<TaskID, LiveTask>

}
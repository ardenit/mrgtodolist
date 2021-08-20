package com.mirage.todolist.viewmodel

import androidx.lifecycle.ViewModel
import com.mirage.todolist.model.tasks.LiveTask
import com.mirage.todolist.model.tasks.TaskID

//TODO Delete? Refactor?
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
     * Removes the task with ID [taskID].
     */
    abstract fun removeTask(
        taskID: TaskID
    )
}
package com.mirage.todolist.ui.todolist

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import com.mirage.todolist.di.App
import com.mirage.todolist.model.repository.LiveTask
import com.mirage.todolist.model.repository.TodoRepository
import java.util.*
import javax.inject.Inject

/**
 * View model of the todolist screen
 */
class TodolistViewModel @Inject constructor(
    private val application: App,
    private val todoRepository: TodoRepository,
    private val preferences: SharedPreferences,
    private val resources: Resources
) : ViewModel() {

    /**
     * Creates a new task in the bottom of a given tasklist
     * Should be called in floating "add" button click listener
     */
    fun createNewTask(tasklistID: Int): LiveTask {
        return todoRepository.createNewTask(tasklistID)
    }

    /**
     * Modifies the task with ID [taskID].
     * If a parameter's value is null, its value remains unchanged.
     * This method automatically updates "last modified" time.
     */
    fun modifyTask(
        taskID: UUID,
        title: String?,
        description: String?
    ) {
        todoRepository.modifyTask(taskID, title, description, null, null, null, null, null)
    }

    /**
     * Removes the task with ID [taskID].
     */
    fun removeTask(taskID: UUID) {
        todoRepository.removeTask(taskID)
    }

    /**
     * Starts searching for tasks using [searchQuery], altering [LiveTask.isVisible] flag
     */
    fun searchTasks(searchQuery: String) {
        todoRepository.searchTasks(searchQuery)
    }

    /**
     * Cancels searching for tasks, setting [LiveTask.isVisible] to true for all current tasks
     */
    fun cancelTaskSearch() {
        todoRepository.cancelTaskSearch()
    }
}
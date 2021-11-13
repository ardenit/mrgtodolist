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
    private val todoRepository: TodoRepository
) : ViewModel() {

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
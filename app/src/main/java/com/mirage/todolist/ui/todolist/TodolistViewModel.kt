package com.mirage.todolist.ui.todolist

import androidx.lifecycle.ViewModel
import com.mirage.todolist.model.repository.LiveTask
import com.mirage.todolist.model.repository.TaskID
import com.mirage.todolist.model.repository.TodoRepository
import javax.inject.Inject

//TODO Delete? Refactor?
/**
 * View model of the todolist screen
 */
abstract class TodolistViewModel : ViewModel(){

    @Inject
    lateinit var todoRepository: TodoRepository

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
        taskID: TaskID,
        title: String?,
        description: String?
    ) {
        todoRepository.modifyTask(taskID, title, description, null, null, null, null)
    }

    /**
     * Removes the task with ID [taskID].
     */
    fun removeTask(taskID: TaskID) {
        todoRepository.removeTask(taskID)
    }
}
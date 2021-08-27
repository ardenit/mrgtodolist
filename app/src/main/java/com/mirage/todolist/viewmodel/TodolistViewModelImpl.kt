package com.mirage.todolist.viewmodel

import com.mirage.todolist.model.tasks.LiveTask
import com.mirage.todolist.model.tasks.TaskID
import com.mirage.todolist.model.tasks.TodolistModel
import com.mirage.todolist.model.tasks.getTodolistModel

/**
 * See [TodolistViewModel] for API documentation
 */
class TodolistViewModelImpl : TodolistViewModel() {

    //todo Inject
    private val todolistModel: TodolistModel = getTodolistModel()

    override fun createNewTask(tasklistID: Int): LiveTask {
        return todolistModel.createNewTask(tasklistID)
    }

    override fun modifyTask(
        taskID: TaskID,
        title: String?,
        description: String?
    ) {
        todolistModel.modifyTask(taskID, title, description, null)
    }

    override fun removeTask(taskID: TaskID) {
        todolistModel.removeTask(taskID)
    }
}
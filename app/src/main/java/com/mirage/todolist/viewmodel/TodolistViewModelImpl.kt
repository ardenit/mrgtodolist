package com.mirage.todolist.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.mirage.todolist.model.TodolistModel
import com.mirage.todolist.model.getTodolistModel

class TodolistViewModelImpl : TodolistViewModel() {

    private val onNewTaskObservable = MutableLiveData<LiveTask>()
    private val onFullUpdateObservable = MutableLiveData<List<LiveTask>>()
    private val todolistModel: TodolistModel = getTodolistModel()

    init {
        todolistModel.addFullTasklistUpdateListener {

        }
    }

    override fun createNewTask(tasklist: Int): LiveTask {
        TODO("Not yet implemented")
    }

    override fun addOnNewTaskListener(owner: LifecycleOwner, onNewTask: (LiveTask) -> Unit) {
        onNewTaskObservable.observe(owner, onNewTask)
    }

    override fun addOnFullTasklistUpdateListener(owner: LifecycleOwner, onFullUpdate: (List<LiveTask>) -> Unit) {
        onFullUpdateObservable.observe(owner, onFullUpdate)
    }

    override fun getTask(taskID: TaskID): LiveTask? {
        TODO()
    }

    override fun onCleared() {

    }

}
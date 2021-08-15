package com.mirage.todolist.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mirage.todolist.model.*

/**
 * See [TodolistViewModel] for API documentation
 */
class TodolistViewModelImpl : TodolistViewModel() {

    private val onNewTaskObservable = MutableLiveData<LiveTask>()
    private val onNewTaskPrioritizedListeners: MutableSet<OnNewTaskListener> = LinkedHashSet()
    private val onMoveTaskObservable = MutableLiveData<MoveTaskCommand>()
    private val onMoveTaskPrioritizedListeners: MutableSet<OnMoveTaskListener> = LinkedHashSet()
    private val onFullUpdateObservable = MutableLiveData<Map<TaskID, LiveTask>>()
    private val onFullUpdatePrioritizedListeners: MutableSet<OnFullUpdateListener> = LinkedHashSet()

    private val todolistModel: TodolistModel = getTodolistModel()

    private val modelOnNewTaskListener: OnNewTaskListener = { newTask ->
        onNewTaskPrioritizedListeners.forEach { it.invoke(newTask) }
        onNewTaskObservable.value = newTask
    }
    private val modelOnMoveTaskListener: OnMoveTaskListener = { task, oldTasklistID, newTasklistID, oldTaskIndex, newTaskIndex ->
        onMoveTaskPrioritizedListeners.forEach { it.invoke(task, oldTasklistID, newTasklistID, oldTaskIndex, newTaskIndex) }
        onMoveTaskObservable.value = MoveTaskCommand(task, oldTasklistID, newTasklistID, oldTaskIndex, newTaskIndex)
    }
    private val modelOnFullUpdateListener: OnFullUpdateListener = { newTasks ->
        onFullUpdatePrioritizedListeners.forEach { it.invoke(newTasks) }
        onFullUpdateObservable.value = newTasks
    }

    init {
        todolistModel.addOnNewTaskListener(modelOnNewTaskListener)
        todolistModel.addOnMoveTaskListener(modelOnMoveTaskListener)
        todolistModel.addOnFullUpdateListener(modelOnFullUpdateListener)
    }

    override fun createNewTask(tasklistID: Int): LiveTask {
        return todolistModel.createNewTask(tasklistID)
    }

    override fun addOnNewTaskListener(owner: LifecycleOwner, listener: OnNewTaskListener) {
        onNewTaskObservable.observe(owner, listener)
    }

    override fun addOnNewTaskPrioritizedListener(listener: OnNewTaskListener) {
        onNewTaskPrioritizedListeners += listener
    }

    override fun removeOnNewTaskPrioritizedListener(listener: OnNewTaskListener) {
        onNewTaskPrioritizedListeners -= listener
    }

    override fun addOnMoveTaskListener(owner: LifecycleOwner, listener: OnMoveTaskListener) {
        onMoveTaskObservable.observe(owner) { (task, oldTasklistID, newTasklistID, oldTaskIndex, newTaskIndex) ->
            listener(task, oldTasklistID, newTasklistID, oldTaskIndex, newTaskIndex)
        }
    }

    override fun addOnMoveTaskPrioritizedListener(listener: OnMoveTaskListener) {
        onMoveTaskPrioritizedListeners += listener
    }

    override fun removeOnMoveTaskPrioritizedListener(listener: OnMoveTaskListener) {
        onMoveTaskPrioritizedListeners -= listener
    }

    override fun addOnFullUpdateListener(owner: LifecycleOwner, listener: OnFullUpdateListener) {
        onFullUpdateObservable.observe(owner, listener)
    }

    override fun addOnFullUpdatePrioritizedListener(listener: OnFullUpdateListener) {
        onFullUpdatePrioritizedListeners += listener
    }

    override fun removeOnFullUpdatePrioritizedListener(listener: OnFullUpdateListener) {
        onFullUpdatePrioritizedListeners -= listener
    }

    override fun getTask(taskID: TaskID): LiveTask? {
        return todolistModel.getAllTasks()[taskID]
    }

    override fun onCleared() {
        todolistModel.removeOnNewTaskListener(modelOnNewTaskListener)
        todolistModel.removeOnMoveTaskListener(modelOnMoveTaskListener)
        todolistModel.removeOnFullUpdateListener(modelOnFullUpdateListener)
    }
}

private data class MoveTaskCommand(
    val task: LiveTask,
    val oldTasklistID: Int,
    val newTasklistID: Int,
    val oldTaskIndex: Int,
    val newTaskIndex: Int
)
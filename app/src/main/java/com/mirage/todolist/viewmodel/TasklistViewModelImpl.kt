package com.mirage.todolist.viewmodel

import androidx.lifecycle.LifecycleOwner
import com.mirage.todolist.model.OnFullUpdateListener
import com.mirage.todolist.model.OnMoveTaskListener
import com.mirage.todolist.model.OnNewTaskListener

/**
 * See [TasklistViewModel] for API documentation
 */
class TasklistViewModelImpl : TasklistViewModel() {

    private lateinit var todolistViewModel: TodolistViewModel
    private lateinit var onNewTaskListener: OnNewTaskListener
    private lateinit var onMoveTaskListener: OnMoveTaskListener
    private lateinit var onFullUpdateListener: OnFullUpdateListener

    private var tasklistID: Int = -1
    private var tasksSlice: MutableMap<TaskID, LiveTask> = LinkedHashMap()
    private var initialized = false

    override fun init(parentViewModel: TodolistViewModel, tasklistID: Int) {
        if (initialized && this.todolistViewModel == parentViewModel && this.tasklistID == tasklistID) return
        if (initialized) {
            todolistViewModel.removeOnNewTaskPrioritizedListener(onNewTaskListener)
            todolistViewModel.removeOnMoveTaskPrioritizedListener(onMoveTaskListener)
            todolistViewModel.removeOnFullUpdatePrioritizedListener(onFullUpdateListener)
        }
        initialized = true
        todolistViewModel = parentViewModel
        this.tasklistID = tasklistID
        tasksSlice = todolistViewModel.getAllTasks().filter { (_, task) ->
            task.tasklistID == this.tasklistID
        }.toMutableMap()
        onNewTaskListener = { newTask ->
            if (newTask.tasklistID == this.tasklistID) {
                tasksSlice[newTask.taskID] = newTask
            }
        }
        onMoveTaskListener = { task, oldTasklistID, newTasklistID, _, _ ->
            if (oldTasklistID == this.tasklistID) {
                tasksSlice.remove(task.taskID)
            }
            if (newTasklistID == this.tasklistID) {
                tasksSlice[task.taskID] = task
            }
        }
        onFullUpdateListener = { newTasks ->
            tasksSlice = newTasks.filter { (_, task) ->
                task.tasklistID == this.tasklistID
            }.toMutableMap()
        }
        todolistViewModel.addOnNewTaskPrioritizedListener(onNewTaskListener)
        todolistViewModel.addOnMoveTaskPrioritizedListener(onMoveTaskListener)
        todolistViewModel.addOnFullUpdatePrioritizedListener(onFullUpdateListener)
    }

    override fun getTasklistID(): Int {
        return tasklistID
    }

    override fun swipeTaskLeft(taskIndex: Int) {
        if (tasklistID < 1) return
        val task = getTaskByIndex(taskIndex) ?: return
        todolistViewModel.moveTask(task.taskID, tasklistID - 1)
    }

    override fun swipeTaskRight(taskIndex: Int) {
        if (tasklistID > TasklistType.values().size - 2) return
        val task = getTaskByIndex(taskIndex) ?: return
        todolistViewModel.moveTask(task.taskID, tasklistID + 1)
    }

    override fun dragTask(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in 0 until getTaskCount()) return
        if (toIndex !in 0 until getTaskCount()) return
        val task = getTaskByIndex(fromIndex) ?: return
        todolistViewModel.moveTaskInList(task.taskID, toIndex)
    }

    override fun addOnNewTaskListener(owner: LifecycleOwner, listener: OnNewTaskListener) {
        todolistViewModel.addOnNewTaskListener(owner) { newTask ->
            if (newTask.tasklistID == this.tasklistID) {
                listener(newTask)
            }
        }
        todolistViewModel.addOnMoveTaskListener(owner) { task, _, newTasklistID, _, _ ->
            if (newTasklistID == this.tasklistID) {
                listener(task)
            }
        }
    }

    override fun addOnRemoveTaskListener(owner: LifecycleOwner, listener: OnRemoveTaskListener) {
        todolistViewModel.addOnMoveTaskListener(owner) { task, oldTasklistID, _, oldTaskIndex, _ ->
            if (oldTasklistID == this.tasklistID) {
                listener(task, oldTaskIndex)
            }
        }
    }

    override fun addOnFullTasklistUpdateListener(owner: LifecycleOwner, listener: OnFullUpdateListener) {
        todolistViewModel.addOnFullUpdateListener(owner) { _ ->
            listener(tasksSlice)
        }
    }

    override fun getTask(taskID: TaskID): LiveTask? {
        return tasksSlice[taskID]
    }

    override fun getTaskByIndex(taskIndex: Int): LiveTask? {
        return tasksSlice.values.find { it.taskIndex == taskIndex }
    }

    override fun getTaskCount(): Int {
        return tasksSlice.size
    }

    override fun onCleared() {
        todolistViewModel.removeOnNewTaskPrioritizedListener(onNewTaskListener)
        todolistViewModel.removeOnMoveTaskPrioritizedListener(onMoveTaskListener)
        todolistViewModel.removeOnFullUpdatePrioritizedListener(onFullUpdateListener)
    }

}
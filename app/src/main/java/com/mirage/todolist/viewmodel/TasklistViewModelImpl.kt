package com.mirage.todolist.viewmodel

import androidx.lifecycle.LifecycleOwner
import com.mirage.todolist.model.OnFullUpdateListener
import com.mirage.todolist.model.OnNewTaskListener

/**
 * See [TasklistViewModel] for API documentation
 */
class TasklistViewModelImpl : TasklistViewModel() {

    private lateinit var todolistViewModel: TodolistViewModel
    private var tasklistID: Int = -1
    private var tasksSlice: MutableMap<TaskID, LiveTask> = LinkedHashMap()

    override fun init(parentViewModel: TodolistViewModel, tasklistID: Int) {
        todolistViewModel = parentViewModel
        this.tasklistID = tasklistID
        todolistViewModel.addOnNewTaskPrioritizedListener { newTask ->
            if (newTask.tasklistID.value == this.tasklistID) {
                tasksSlice[newTask.taskID] = newTask
            }
        }
        todolistViewModel.addOnMoveTaskPrioritizedListener { task, oldTasklistID, newTasklistID, _, _ ->
            if (oldTasklistID == this.tasklistID) {
                tasksSlice.remove(task.taskID)
            }
            if (newTasklistID == this.tasklistID) {
                tasksSlice[task.taskID] = task
            }
        }
        todolistViewModel.addOnFullUpdatePrioritizedListener { newTasks ->
            tasksSlice = newTasks.filter { (_, task) ->
                task.tasklistID.value == this.tasklistID
            }.toMutableMap()
        }
    }

    override fun addOnNewTaskListener(owner: LifecycleOwner, listener: OnNewTaskListener) {
        todolistViewModel.addOnNewTaskListener(owner) { newTask ->
            if (newTask.tasklistID.value == this.tasklistID) {
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
        return tasksSlice.values.find { it.taskIndex.value == taskIndex }
    }

    override fun getTaskCount(): Int {
        return tasksSlice.size
    }
}
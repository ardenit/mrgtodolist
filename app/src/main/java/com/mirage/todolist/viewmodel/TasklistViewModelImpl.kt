package com.mirage.todolist.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.mirage.todolist.model.tasks.*

/**
 * See [TasklistViewModel] for API documentation
 */
class TasklistViewModelImpl : TasklistViewModel() {

    private lateinit var onNewTaskListener: OnNewTaskListener
    private lateinit var onMoveTaskListener: OnMoveTaskListener
    private lateinit var onFullUpdateTaskListener: OnFullUpdateTaskListener

    private val onNewTaskObservable = MutableLiveData<LiveTask>()
    private val onRemoveTaskObservable = MutableLiveData<Pair<LiveTask, Int>>()
    private val onFullUpdateObservable = MutableLiveData<Map<TaskID, LiveTask>>()

    private var tasklistID: Int = -1
    private var tasksSlice: MutableMap<TaskID, LiveTask> = LinkedHashMap()
    private var initialized = false

    //TODO Inject
    private val todolistModel: TodolistModel = getTodolistModel()

    override fun init(tasklistID: Int) {
        if (initialized && this.tasklistID == tasklistID) return
        if (initialized) {
            todolistModel.removeOnNewTaskListener(onNewTaskListener)
            todolistModel.removeOnMoveTaskListener(onMoveTaskListener)
            todolistModel.removeOnFullUpdateTaskListener(onFullUpdateTaskListener)
        }
        initialized = true
        this.tasklistID = tasklistID
        tasksSlice = todolistModel.getAllTasks().filter { (_, task) ->
            task.tasklistID == this.tasklistID
        }.toMutableMap()
        onNewTaskListener = { newTask ->
            if (newTask.tasklistID == this.tasklistID) {
                tasksSlice[newTask.taskID] = newTask
                onNewTaskObservable.value = newTask
            }
        }
        onMoveTaskListener = { task, oldTasklistID, newTasklistID, _, _ ->
            if (oldTasklistID == this.tasklistID) {
                tasksSlice.remove(task.taskID)
                onRemoveTaskObservable.value = Pair(task, task.taskIndex)
            }
            if (newTasklistID == this.tasklistID) {
                tasksSlice[task.taskID] = task
                onNewTaskObservable.value = task
            }
        }
        onFullUpdateTaskListener = { newTasks ->
            tasksSlice = newTasks.filter { (_, task) ->
                task.tasklistID == this.tasklistID
            }.toMutableMap()
            onFullUpdateObservable.value = tasksSlice
        }
        todolistModel.addOnNewTaskListener(onNewTaskListener)
        todolistModel.addOnMoveTaskListener(onMoveTaskListener)
        todolistModel.addOnFullUpdateTaskListener(onFullUpdateTaskListener)
    }

    override fun getTasklistID(): Int {
        return tasklistID
    }

    override fun swipeTaskLeft(taskIndex: Int) {
        if (tasklistID < 1) return
        val task = getTaskByIndex(taskIndex) ?: return
        todolistModel.moveTask(task.taskID, tasklistID - 1)
    }

    override fun swipeTaskRight(taskIndex: Int) {
        if (tasklistID > TasklistType.values().size - 2) return
        val task = getTaskByIndex(taskIndex) ?: return
        todolistModel.moveTask(task.taskID, tasklistID + 1)
    }

    override fun dragTask(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in 0 until getTaskCount()) return
        if (toIndex !in 0 until getTaskCount()) return
        val task = getTaskByIndex(fromIndex) ?: return
        todolistModel.moveTaskInList(task.taskID, toIndex)
    }

    override fun addOnNewTaskListener(owner: LifecycleOwner, listener: OnNewTaskListener) {
        onNewTaskObservable.observe(owner, listener)
    }

    override fun addOnRemoveTaskListener(owner: LifecycleOwner, listener: OnRemoveTaskListener) {
        onRemoveTaskObservable.observe(owner) { (task, index) ->
            listener(task, index)
        }
    }

    override fun addOnFullTasklistUpdateListener(owner: LifecycleOwner, listener: OnFullUpdateTaskListener) {
        onFullUpdateObservable.observe(owner, listener)
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
        todolistModel.removeOnNewTaskListener(onNewTaskListener)
        todolistModel.removeOnMoveTaskListener(onMoveTaskListener)
        todolistModel.removeOnFullUpdateTaskListener(onFullUpdateTaskListener)
    }
}
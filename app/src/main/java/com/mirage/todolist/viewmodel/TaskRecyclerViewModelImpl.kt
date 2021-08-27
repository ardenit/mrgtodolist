package com.mirage.todolist.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.mirage.todolist.model.tasks.*

/**
 * See [TaskRecyclerViewModel] for API documentation
 */
class TaskRecyclerViewModelImpl : TaskRecyclerViewModel() {

    private lateinit var onNewTaskListener: OnNewTaskListener
    private lateinit var onMoveTaskListener: OnMoveTaskListener
    private lateinit var onFullUpdateTaskListener: OnFullUpdateTaskListener

    private val onNewTaskObservable = MutableLiveData<LiveTask>()
    private val onRemoveTaskObservable = MutableLiveData<Pair<LiveTask, Int>>()
    private val onFullUpdateObservable = MutableLiveData<Map<TaskID, LiveTask>>()

    private var tasklistID: Int = -1
    /** Slice of actual tasks in this tasklist (including temporarily hidden ones) */
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
            onFullUpdateObservable.value = tasksSlice.filterValues { it.isVisible }
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
        val task = getTaskByVisibleIndex(taskIndex) ?: return
        todolistModel.moveTask(task.taskID, tasklistID - 1)
    }

    override fun swipeTaskRight(taskIndex: Int) {
        if (tasklistID > TasklistType.values().size - 2) return
        val task = getTaskByVisibleIndex(taskIndex) ?: return
        todolistModel.moveTask(task.taskID, tasklistID + 1)
    }

    override fun dragTask(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in 0 until getVisibleTaskCount()) return
        if (toIndex !in 0 until getVisibleTaskCount()) return
        val task = getTaskByVisibleIndex(fromIndex) ?: return
        val visibleTasks = tasksSlice.values.filter { it.isVisible }
        if (toIndex < visibleTasks.size) {
            val newIndex = visibleTasks[toIndex].taskIndex
            todolistModel.moveTaskInList(task.taskID, newIndex)
        }
        else {
            val newIndex = visibleTasks[visibleTasks.size - 1].taskIndex + 1
            todolistModel.moveTaskInList(task.taskID, newIndex)
        }
    }

    override fun addOnNewTaskListener(owner: LifecycleOwner, listener: OnNewTaskListener) {
        onNewTaskObservable.observe(owner, listener)
    }

    override fun addOnRemoveTaskListener(owner: LifecycleOwner, listener: OnRemoveTaskListener) {
        onRemoveTaskObservable.observe(owner) { (task, index) ->
            listener(task, index)
        }
    }

    override fun addOnFullUpdateTaskListener(owner: LifecycleOwner, listener: OnFullUpdateTaskListener) {
        onFullUpdateObservable.observe(owner, listener)
    }

    override fun getTask(taskID: TaskID): LiveTask? {
        return tasksSlice[taskID]
    }

    override fun getTaskByVisibleIndex(position: Int): LiveTask? {
        val visibleTasks = tasksSlice.values.filter { it.isVisible }.sortedBy { it.taskIndex }
        return if (position in visibleTasks.indices) visibleTasks[position] else null
    }

    override fun getVisibleTaskCount(): Int {
        return tasksSlice.values.filter { it.isVisible }.count()
    }

    override fun onCleared() {
        todolistModel.removeOnNewTaskListener(onNewTaskListener)
        todolistModel.removeOnMoveTaskListener(onMoveTaskListener)
        todolistModel.removeOnFullUpdateTaskListener(onFullUpdateTaskListener)
    }
}
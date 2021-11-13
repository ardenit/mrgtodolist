package com.mirage.todolist.ui.todolist.tasks

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mirage.todolist.model.repository.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.LinkedHashMap

typealias OnRemoveTaskListener = (task: LiveTask, taskIndex: Int) -> Unit

/**
 * View model of a single tasklist page
 */
class TaskRecyclerViewModel @Inject constructor(
    private var todoRepository: TodoRepository
): ViewModel() {

    private lateinit var onNewTaskListener: OnNewTaskListener
    private lateinit var onMoveTaskListener: OnMoveTaskListener
    private lateinit var onFullUpdateTaskListener: OnFullUpdateTaskListener

    private val onNewTaskObservable = MutableLiveData<LiveTask>()
    private val onRemoveTaskObservable = MutableLiveData<Pair<LiveTask, Int>>()
    private val onFullUpdateObservable = MutableLiveData<Map<UUID, LiveTask>>()

    private var tasklistId: Int = -1
    /** Slice of actual tasks in this tasklist (including temporarily hidden ones) */
    private var tasksSlice: MutableMap<UUID, LiveTask> = LinkedHashMap()
    private var initialized = false

    /**
     * [tasklistId] - id of tasklist processed by this view model
     */
    fun init(tasklistId: Int) {
        Timber.v("TaskRecyclerViewModel - starting init with tasklistId = $tasklistId, already initialized = $initialized")
        if (initialized && this.tasklistId == tasklistId) return
        if (initialized) {
            todoRepository.removeOnNewTaskListener(onNewTaskListener)
            todoRepository.removeOnMoveTaskListener(onMoveTaskListener)
            todoRepository.removeOnFullUpdateTaskListener(onFullUpdateTaskListener)
        }
        initialized = true
        this.tasklistId = tasklistId
        Timber.v("All tasks: ${todoRepository.getAllTasks()}")
        tasksSlice = todoRepository.getAllTasks().filter { (_, task) ->
            task.tasklistId == this.tasklistId
        }.toMutableMap()
        Timber.v("Task slice: $tasksSlice")
        onNewTaskListener = { newTask ->
            if (newTask.tasklistId == this.tasklistId) {
                tasksSlice[newTask.taskId] = newTask
                onNewTaskObservable.value = newTask
            }
        }
        onMoveTaskListener = { task, oldTasklistID, newTasklistID, _, _ ->
            if (oldTasklistID == this.tasklistId) {
                tasksSlice.remove(task.taskId)
                onRemoveTaskObservable.value = Pair(task, task.taskIndex)
            }
            if (newTasklistID == this.tasklistId) {
                tasksSlice[task.taskId] = task
                onNewTaskObservable.value = task
            }
        }
        onFullUpdateTaskListener = { newTasks ->
            tasksSlice = newTasks.filter { (_, task) ->
                task.tasklistId == this.tasklistId
            }.toMutableMap()
            onFullUpdateObservable.value = tasksSlice.filterValues { it.isVisible }
        }
        todoRepository.addOnNewTaskListener(onNewTaskListener)
        todoRepository.addOnMoveTaskListener(onMoveTaskListener)
        todoRepository.addOnFullUpdateTaskListener(onFullUpdateTaskListener)
    }

    /**
     * Returns the ID of a tasklist associated with this viewmodel
     */
    fun getTasklistID(): Int {
        return tasklistId
    }

    /**
     * Moves task to another tasklist
     */
    fun swipeTaskLeft(taskIndex: Int) {
        if (tasklistId < 1) return
        val task = getTaskByVisibleIndex(taskIndex) ?: return
        todoRepository.moveTask(task.taskId, tasklistId - 1)
    }

    /**
     * Moves task to another tasklist
     */
    fun swipeTaskRight(taskIndex: Int) {
        if (tasklistId > TasklistType.values().size - 2) return
        val task = getTaskByVisibleIndex(taskIndex) ?: return
        todoRepository.moveTask(task.taskId, tasklistId + 1)
    }

    /**
     * Moves task to another position in the same tasklist
     */
    fun dragTask(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in 0 until getVisibleTaskCount()) return
        if (toIndex !in 0 until getVisibleTaskCount()) return
        val task = getTaskByVisibleIndex(fromIndex) ?: return
        val visibleTasks = tasksSlice.values.filter { it.isVisible }
        if (toIndex < visibleTasks.size) {
            val newIndex = visibleTasks[toIndex].taskIndex
            todoRepository.moveTaskInList(task.taskId, newIndex)
        }
        else {
            val newIndex = visibleTasks[visibleTasks.size - 1].taskIndex + 1
            todoRepository.moveTaskInList(task.taskId, newIndex)
        }
    }

    /**
     * Registers a listener for task creation event in this tasklist
     */
    fun addOnNewTaskListener(owner: LifecycleOwner, listener: OnNewTaskListener) {
        onNewTaskObservable.observe(owner, listener)
    }

    /**
     * Registers a listener for removing a task from this tasklist
     */
    fun addOnRemoveTaskListener(owner: LifecycleOwner, listener: OnRemoveTaskListener) {
        onRemoveTaskObservable.observe(owner) { (task, index) ->
            if (task.tasklistId == TodoRepository.HIDDEN_TASKLIST_ID) {
                listener(task, index)
            }
        }
    }

    /**
     * Registers a listener for full tasklist overwrite event
     * This event may happen after synchronization with Google Drive, or on task search query
     */
    fun addOnFullUpdateTaskListener(owner: LifecycleOwner, listener: OnFullUpdateTaskListener) {
        onFullUpdateObservable.observe(owner, listener)
    }

    /**
     * Returns a [LiveTask] with a given visible [position] in this tasklist, or null if index is invalid
     */
    fun getTaskByVisibleIndex(position: Int): LiveTask? {
        val visibleTasks = tasksSlice.values.filter { it.isVisible }.sortedBy { it.taskIndex }
        return if (position in visibleTasks.indices) visibleTasks[position] else null
    }

    /**
     * Returns the number of tasks currently visible in this tasklist
     */
    fun getVisibleTaskCount(): Int {
        return tasksSlice.values.filter { it.isVisible }.count()
    }

    override fun onCleared() {
        todoRepository.removeOnNewTaskListener(onNewTaskListener)
        todoRepository.removeOnMoveTaskListener(onMoveTaskListener)
        todoRepository.removeOnFullUpdateTaskListener(onFullUpdateTaskListener)
    }
}
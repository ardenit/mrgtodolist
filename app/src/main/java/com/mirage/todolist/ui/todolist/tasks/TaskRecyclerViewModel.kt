package com.mirage.todolist.ui.todolist.tasks

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mirage.todolist.model.repository.*
import com.mirage.todolist.ui.todolist.tasks.TasklistType
import javax.inject.Inject

typealias OnRemoveTaskListener = (task: LiveTask, taskIndex: Int) -> Unit

/**
 * View model of a single tasklist page
 */
class TaskRecyclerViewModel @Inject constructor(): ViewModel() {

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

    @Inject
    lateinit var todoRepository: TodoRepository

    /**
     * [tasklistID] - id of tasklist processed by this view model
     */
    fun init(tasklistID: Int) {
        if (initialized && this.tasklistID == tasklistID) return
        if (initialized) {
            todoRepository.removeOnNewTaskListener(onNewTaskListener)
            todoRepository.removeOnMoveTaskListener(onMoveTaskListener)
            todoRepository.removeOnFullUpdateTaskListener(onFullUpdateTaskListener)
        }
        initialized = true
        this.tasklistID = tasklistID
        tasksSlice = todoRepository.getAllTasks().filter { (_, task) ->
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
        todoRepository.addOnNewTaskListener(onNewTaskListener)
        todoRepository.addOnMoveTaskListener(onMoveTaskListener)
        todoRepository.addOnFullUpdateTaskListener(onFullUpdateTaskListener)
    }

    /**
     * Returns the ID of a tasklist associated with this viewmodel
     */
    fun getTasklistID(): Int {
        return tasklistID
    }

    /**
     * Moves task to another tasklist
     */
    fun swipeTaskLeft(taskIndex: Int) {
        if (tasklistID < 1) return
        val task = getTaskByVisibleIndex(taskIndex) ?: return
        todoRepository.moveTask(task.taskID, tasklistID - 1)
    }

    /**
     * Moves task to another tasklist
     */
    fun swipeTaskRight(taskIndex: Int) {
        if (tasklistID > TasklistType.values().size - 2) return
        val task = getTaskByVisibleIndex(taskIndex) ?: return
        todoRepository.moveTask(task.taskID, tasklistID + 1)
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
            todoRepository.moveTaskInList(task.taskID, newIndex)
        }
        else {
            val newIndex = visibleTasks[visibleTasks.size - 1].taskIndex + 1
            todoRepository.moveTaskInList(task.taskID, newIndex)
        }
    }

    /** Starts searching for tasks with given tag */
    fun searchForTag(tag: LiveTag) {
        todoRepository.searchTasks("[${tag.name.value}]")
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
            listener(task, index)
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
     * Returns a [LiveTask] with a given [taskID], or null if there is no task with this ID
     */
    fun getTask(taskID: TaskID): LiveTask? {
        return tasksSlice[taskID]
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
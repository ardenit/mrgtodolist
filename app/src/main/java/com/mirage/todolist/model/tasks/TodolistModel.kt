package com.mirage.todolist.model.tasks

import android.content.Context
import com.mirage.todolist.model.gdrive.GDriveConnectExceptionHandler

//TODO Inject
private val todolistModelInstance: TodolistModel = TodolistModelImpl()
fun getTodolistModel(): TodolistModel = todolistModelInstance

typealias OnNewTaskListener = (newTask: LiveTask) -> Unit
typealias OnMoveTaskListener = (task: LiveTask, oldTasklistID: Int, newTasklistID: Int, oldTaskIndex: Int, newTaskIndex: Int) -> Unit
typealias OnFullUpdateTaskListener = (newTasks: Map<TaskID, LiveTask>) -> Unit

typealias OnNewTagListener = (newTag: LiveTag) -> Unit
typealias OnRemoveTagListener = (tag: LiveTag, tagIndex: Int) -> Unit
typealias OnFullUpdateTagListener = (newTags: Map<TagID, LiveTag>) -> Unit

interface TodolistModel {

    /**
     * Initiates the model.
     * Must be called at least once before doing any other operations with model.
     * May be safely called multiple times during application lifecycle.
     * [appCtx] - application context.
     */
    fun init(appCtx: Context)

    /**
     * Returns current Google Drive account email used for synchronization, or null if sync is not configured.
     */
    fun getGDriveAccountEmail(): String?

    /**
     * Changes Google Drive email.
     * Should be invoked when user selects new account for synchronization.
     */
    fun setGDriveAccountEmail(newEmail: String?, exHandler: GDriveConnectExceptionHandler)

    /**
     * Creates a new task in a given [tasklistID] and returns it.
     */
    fun createNewTask(tasklistID: Int): LiveTask

    /**
     * Modifies the task with ID [taskID].
     * If a parameter's value is null, its value remains unchanged.
     * This method automatically updates "last modified" time.
     */
    fun modifyTask(
        taskID: TaskID,
        title: String?,
        description: String?,
        tags: List<LiveTag>?,
        date: TaskDate?,
        time: TaskTime?,
        period: TaskPeriod?
    )

    /**
     * Deletes the task with ID [taskID]
     * (actually just moves to hidden tasklist to simplify diff calculation).
     * This method automatically updates "last modified" time.
     */
    fun removeTask(taskID: TaskID)

    /**
     * Moves the task with ID [taskID] to another tasklist.
     */
    fun moveTask(
        taskID: TaskID,
        newTasklistID: Int
    )

    /**
     * Moves the task to another position inside the tasklist.
     */
    fun moveTaskInList(
        taskID: TaskID,
        newTaskIndex: Int
    )

    /**
     * Processes the search query for tasks, altering their visibility.
     */
    fun searchTasks(
        searchQuery: String
    )

    /**
     * Cancels searching for tasks, switching their visibility back to normal.
     */
    fun cancelTaskSearch()

    /**
     * Returns a map of all tasks (including hidden/removed ones).
     */
    fun getAllTasks(): Map<TaskID, LiveTask>

    /**
     * Creates a new tag and returns it.
     */
    fun createNewTag(): LiveTag

    /**
     * Modifies the tag with ID [tagID].
     * If a parameter's value is null, its value remains unchanged.
     * This method automatically updates "last modified" time.
     */
    fun modifyTag(
        tagID: TagID,
        name: String?,
        styleIndex: Int?
    )

    /**
     * Deletes the tag with ID [tagID]
     * (actually just moves to hidden taglist to simplify diff calculation).
     * This method automatically updates "last modified" time.
     */
    fun removeTag(tagID: TagID)

    /**
     * Returns a map of all tags
     */
    fun getAllTags(): Map<TagID, LiveTag>

    /**
     * Adds a listener for new task event
     */
    fun addOnNewTaskListener(listener: OnNewTaskListener)

    fun removeOnNewTaskListener(listener: OnNewTaskListener)

    /**
     * Adds a listener for task's tasklist change event (including removal, i.e. moving to -1)
     */
    fun addOnMoveTaskListener(listener: OnMoveTaskListener)

    fun removeOnMoveTaskListener(listener: OnMoveTaskListener)

    /**
     * Adds a listener for tasklist update events coming from model independently from user actions
     * (i.e. updates performed on another device using the same Google Drive account).
     */
    fun addOnFullUpdateTaskListener(listener: OnFullUpdateTaskListener)

    fun removeOnFullUpdateTaskListener(listener: OnFullUpdateTaskListener)

    fun addOnNewTagListener(listener: OnNewTagListener)

    fun removeOnNewTagListener(listener: OnNewTagListener)

    fun addOnRemoveTagListener(listener: OnRemoveTagListener)

    fun removeOnRemoveTagListener(listener: OnRemoveTagListener)

    fun addOnFullUpdateTagListener(listener: OnFullUpdateTagListener)

    fun removeOnFullUpdateTagListener(listener: OnFullUpdateTagListener)
}
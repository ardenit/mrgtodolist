package com.mirage.todolist.model.room

import android.content.Context
import com.mirage.todolist.model.tasks.LiveTask
import com.mirage.todolist.model.tasks.TagID
import com.mirage.todolist.model.tasks.TaskID

/**
 * Interface for interacting with Room database
 * This model encapsulates working with its own thread,
 * so no external thread switching or synchronization is required.
 * Most operations do not require waiting for the result.
 */
interface DatabaseModel {

    /**
     * Initiates the database and sets the listener to handle Google Drive synchronization events
     * Listener is also invoked immediately after this method finishes initialization
     */
    fun init(appCtx: Context, onSyncUpdateListener: suspend (DatabaseSnapshot) -> Unit)

    /** Returns [TaskID] immediately without waiting for DB query to complete */
    fun createNewTask(tasklistId: Int): TaskID

    fun removeTask(taskId: TaskID)

    fun moveTask(taskId: TaskID, newTasklistId: Int)

    fun moveTaskInList(taskId: TaskID, newTaskIndex: Int)

    fun setTaskTitle(taskId: TaskID, title: String)

    fun setTaskDescription(taskId: TaskID, description: String)

    fun setTaskTags(taskId: TaskID, tagIds: List<TagID>)

    fun setTaskDate(taskId: TaskID, year: Int, monthOfYear: Int, dayOfMonth: Int)

    fun setTaskTime(taskId: TaskID, hour: Int, minute: Int)

    fun setTaskPeriod(taskId: TaskID, periodId: Int)

    fun createNewTag(): TagID

    fun setTagName(tagId: TagID, name: String)

    fun setTagStyleIndex(tagId: TagID, styleIndex: Int)

    fun removeTag(tagId: TagID)
}
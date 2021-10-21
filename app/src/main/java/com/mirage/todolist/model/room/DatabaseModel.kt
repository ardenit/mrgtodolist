package com.mirage.todolist.model.room

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.mirage.todolist.model.tasks.LiveTask
import com.mirage.todolist.model.tasks.TagID
import com.mirage.todolist.model.tasks.TaskID
import java.util.*

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
    /**
     * Initiates the database without listening for content changes
     */
    fun init(appCtx: Context): DatabaseSnapshot

    /**
     * Fully updates the database to store the new snapshot, merged with data loaded from Google Drive
     * @return true if database was successfully updated, false if sync must be retried later
     * (e.g. if user updates the tasklist, sync was done with outdated local database snapshot and must be redone)
     */
    fun updateDatabaseAfterSync(newSnapshot: DatabaseSnapshot, oldDatabaseVersion: UUID): Boolean

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
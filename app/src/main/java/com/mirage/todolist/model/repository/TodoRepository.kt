package com.mirage.todolist.model.repository

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import com.mirage.todolist.R
import com.mirage.todolist.di.App
import com.mirage.todolist.model.database.DatabaseModel
import com.mirage.todolist.model.database.DatabaseSnapshot
import com.mirage.todolist.model.googledrive.GoogleDriveConnectExceptionHandler
import com.mirage.todolist.model.googledrive.GoogleDriveModel
import com.mirage.todolist.model.workers.scheduleAllDatetimeNotifications
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

typealias OnFullUpdateTaskListener = (newTasks: Map<UUID, LiveTask>) -> Unit
typealias OnFullUpdateTagListener = (newTags: Map<UUID, LiveTag>) -> Unit

/**
 * Repository class that encapsulates data manipulations with database and remote drive storage.
 * Intended to be used by ViewModels from the main UI thread.
 */
class TodoRepository {

    @Inject
    lateinit var appCtx: Context
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var resources: Resources
    @Inject
    lateinit var databaseModel: DatabaseModel
    @Inject
    lateinit var googleDriveModel: GoogleDriveModel
    @Volatile
    private var currentEmail: String = ""

    /** Local in-memory cache for tasks bound to the current Google account */
    private var localTasks: MutableMap<UUID, MutableLiveTask> = LinkedHashMap()
    /** Local in-memory cache for tags bound to the current Google account */
    private var localTags: MutableMap<UUID, MutableLiveTag> = LinkedHashMap()

    /** Listeners for full cache update after Google Drive sync */
    private val onFullUpdateTaskListeners: MutableSet<OnFullUpdateTaskListener> = LinkedHashSet()
    private val onFullUpdateTagListeners: MutableSet<OnFullUpdateTagListener> = LinkedHashSet()

    init {
        App.instance.appComponent.inject(this)
        currentEmail = preferences.getString(resources.getString(R.string.key_sync_select_acc), "") ?: ""
        databaseModel.setOnSyncUpdateListener {
            reloadData(it)
        }
    }

    /**
     * Changes the email used for Google Drive synchronization.
     * Should be invoked when user selects a new account.
     */
    fun setGDriveAccountEmail(newEmail: String, exHandler: GoogleDriveConnectExceptionHandler) {
        googleDriveModel.connectAsync(newEmail, exHandler)
    }

    /**
     * Creates a new task in a given [tasklistID] and returns it.
     */
    fun createNewTask(tasklistID: Int): LiveTask {
        val taskIndex = tasklistSizes[tasklistID] ?: 0
        val taskId = databaseModel.createNewTask(tasklistID)
        val title = appCtx.resources.getString(R.string.task_default_title)
        val description = appCtx.resources.getString(R.string.task_default_description)
        val task = MutableLiveTask(taskId, tasklistID, taskIndex, true, title, description)
        tasklistSizes[tasklistID] = taskIndex + 1
        localTasks[taskId] = task
        onNewTaskListeners.forEach { it.invoke(task) }
        updateNotifications()
        return task
    }

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
    ) {
        val task = localTasks[taskID] ?: return
        if (title != null && title != task.title.value) {
            task.title.value = title
            databaseModel.setTaskTitle(task.taskID, title)
        }
        if (description != null && description != task.description.value) {
            task.description.value = description
            task.title.value = title
            databaseModel.setTaskDescription(task.taskID, description)
        }
        if (tags != null && tags != task.tags.value) {
            task.tags.value = tags
            databaseModel.setTaskTags(task.taskID, tags.map { it.tagID })
        }
        if (date != null && date != task.date.value) {
            task.date.value = date
            databaseModel.setTaskDate(task.taskID, date.year, date.monthOfYear, date.dayOfMonth)
        }
        if (time != null && time != task.time.value) {
            task.time.value = time
            databaseModel.setTaskTime(task.taskID, time.hour, time.minute)
        }
        if (period != null && period != task.period.value) {
            task.period.value = period
            databaseModel.setTaskPeriod(task.taskID, TaskPeriod.values().indexOf(period))
        }
        updateNotifications()
    }

    /**
     * Deletes the task with ID [taskID]
     * (actually just moves to hidden tasklist to simplify diff calculation).
     * This method automatically updates "last modified" time.
     */
    fun removeTask(taskID: TaskID) {
        moveTask(taskID, HIDDEN_TASKLIST_ID)
        updateNotifications()
    }

    /**
     * Moves the task with ID [taskID] to another tasklist.
     */
    fun moveTask(
        taskID: TaskID,
        newTasklistID: Int
    ) {
        val task = localTasks[taskID] ?: return
        val oldTasklistID = task.tasklistID
        if (oldTasklistID == newTasklistID) return
        val oldTaskIndex = task.taskIndex
        val newTaskIndex = tasklistSizes[newTasklistID] ?: 0
        tasklistSizes[oldTasklistID] = (tasklistSizes[oldTasklistID] ?: 1) - 1
        tasklistSizes[newTasklistID] = (tasklistSizes[newTasklistID] ?: 0) + 1
        localTasks.values.asSequence()
            .filter { it.tasklistID == oldTasklistID }
            .filter { it.taskIndex > oldTaskIndex }
            .forEach { it.taskIndex = it.taskIndex - 1 }
        localTasks.values.asSequence()
            .filter { it.tasklistID == newTasklistID }
            .filter { it.taskIndex >= newTaskIndex }
            .forEach { it.taskIndex = it.taskIndex + 1 }
        task.tasklistID = newTasklistID
        task.taskIndex = newTaskIndex
        databaseModel.moveTask(task.taskID, newTasklistID)
        onMoveTaskListeners.forEach { listener ->
            listener(task, oldTasklistID, newTasklistID, oldTaskIndex, newTaskIndex)
        }
        updateNotifications()
    }

    /**
     * Moves the task to another position inside the tasklist.
     */
    fun moveTaskInList(
        taskID: TaskID,
        newTaskIndex: Int
    ) {
        val task = localTasks[taskID] ?: return
        val tasklistID = task.tasklistID
        val oldTaskIndex = task.taskIndex
        if (oldTaskIndex == newTaskIndex) return
        if (oldTaskIndex < newTaskIndex) {
            localTasks.values.asSequence()
                .filter { it.tasklistID == tasklistID }
                .filter { it.taskIndex in (oldTaskIndex + 1)..newTaskIndex }
                .forEach { it.taskIndex = it.taskIndex - 1 }
        }
        else {
            localTasks.values.asSequence()
                .filter { it.tasklistID == tasklistID }
                .filter { it.taskIndex in newTaskIndex until oldTaskIndex }
                .forEach { it.taskIndex = it.taskIndex + 1 }
        }
        task.taskIndex = newTaskIndex
        databaseModel.moveTaskInList(task.taskID, newTaskIndex)
    }

    /**
     * Processes the search query for tasks, altering their visibility.
     */
    fun searchTasks(
        searchQuery: String
    ) {
        if (searchQuery.isBlank()) {
            cancelTaskSearch()
            return
        }
        var modificationFlag = false
        var searchTag = ""
        var searchTask = searchQuery
        if (searchQuery.startsWith('[')) {
            val tagEndIndex = searchQuery.indexOf(']')
            if (tagEndIndex != -1) {
                searchTag = searchQuery.substring(1 until tagEndIndex)
                searchTask = searchQuery.substring(tagEndIndex + 1)
            }
        }
        searchTag = searchTag.trim()
        searchTask = searchTask.trim()
        localTasks.values.forEach { task ->
            val title = task.title.value ?: ""
            val description = task.description.value ?: ""
            val matchesTaskName = searchTask.isBlank() || (searchTask in title) || (searchTask in description)
            val tags = task.tags.value ?: listOf()
            val matchesTagName = searchTag.isEmpty() || tags.any { tag ->
                tag.name.value == searchTag
            }
            val newVisibility = matchesTagName && matchesTaskName
            if (task.isVisible != newVisibility) {
                task.isVisible = newVisibility
                modificationFlag = true
            }
        }
        if (modificationFlag) {
            onFullUpdateTaskListeners.forEach {
                it.invoke(localTasks)
            }
        }
    }

    /**
     * Cancels searching for tasks, switching their visibility back to normal.
     */
    fun cancelTaskSearch() {
        var modificationFlag = false
        localTasks.values.forEach {
            if (!it.isVisible) {
                it.isVisible = true
                modificationFlag = true
            }
        }
        if (modificationFlag) {
            onFullUpdateTaskListeners.forEach {
                it.invoke(localTasks)
            }
        }
    }

    /**
     * Creates a new tag and returns it.
     */
    fun createNewTag(): LiveTag {
        val tagIndex = localTags.size
        val tagID = databaseModel.createNewTag()
        val tag = MutableLiveTag(tagID, tagIndex, "", 0)
        localTags[tagID] = tag
        return tag
    }

    /**
     * Modifies the tag with ID [tagID].
     * If a parameter's value is null, its value remains unchanged.
     * This method automatically updates "last modified" time.
     */
    fun modifyTag(
        tagID: UUID,
        name: String?,
        styleIndex: Int?
    ) {
        val tag = localTags[tagID] ?: return
        if (name != null && name != tag.name.value) {
            tag.name.value = name
            databaseModel.setTagName(tag.tagID, name)
        }
        if (styleIndex != null && styleIndex != tag.styleIndex.value) {
            tag.styleIndex.value = styleIndex
            databaseModel.setTagStyleIndex(tag.tagID, styleIndex)
        }
    }

    /**
     * Deletes the [tag]
     * (actually just moves to hidden taglist to simplify diff calculation).
     * This method automatically updates "last modified" time.
     */
    fun removeTag(tag: LiveTag) {
        localTags.remove(tag.tagID)
        localTags.values.forEach {
            if (it.tagIndex > tag.tagIndex) {
                --it.tagIndex
            }
        }
        localTasks.values.forEach {
            val oldTags = it.tags.value ?: return@forEach
            if (tag in oldTags) {
                it.tags.value = oldTags.filterNot { oldTag -> oldTag == tag }
            }
        }
        databaseModel.removeTag(tag.tagID)
    }

    /**
     * Adds a listener for tasklist update events coming from model independently from user actions
     * (i.e. updates performed on another device using the same Google Drive account).
     */
    fun addOnFullUpdateTaskListener(listener: OnFullUpdateTaskListener) {
        onFullUpdateTaskListeners += listener
    }

    fun removeOnFullUpdateTaskListener(listener: OnFullUpdateTaskListener) {
        onFullUpdateTaskListeners -= listener
    }

    fun addOnFullUpdateTagListener(listener: OnFullUpdateTagListener) {
        onFullUpdateTagListeners += listener
    }

    fun removeOnFullUpdateTagListener(listener: OnFullUpdateTagListener) {
        onFullUpdateTagListeners -= listener
    }

    private suspend fun reloadData(dbSnapshot: DatabaseSnapshot) {
        val newLocalTasks = LinkedHashMap<UUID, MutableLiveTask>()
        val newLocalTags = LinkedHashMap<UUID, MutableLiveTag>()
        dbSnapshot.tags.forEach { tagEntity ->
            val tagId = tagEntity.tagId
            val tag = MutableLiveTag(
                tagID = tagId,
                tagIndex = tagEntity.tagIndex,
                name = tagEntity.name,
                styleIndex = tagEntity.styleIndex
            )
            newLocalTags[tagId] = tag
        }
        dbSnapshot.tasks.forEach { taskEntity ->
            val tags: List<LiveTag> = dbSnapshot.relations
                .asSequence()
                .filterNot { it.deleted }
                .filter { it.taskId == taskEntity.taskId }
                .map { it.tagId }
                .mapNotNull { newLocalTags[it] }
                .toList()
            val taskId = taskEntity.taskId
            val tasklistId = taskEntity.tasklistId
            val task = MutableLiveTask(
                taskId = taskId,
                tasklistId = tasklistId,
                taskIndex = taskEntity.taskIndex,
                title = taskEntity.title,
                description = taskEntity.description,
                date = taskEntity.date,
                time = taskEntity.time,
                period = taskEntity.period,
                tags = tags,
                isVisible = true
            )
            newLocalTasks[taskId] = task
        }
        val newLocalTasksSync = ConcurrentHashMap(newLocalTasks)
        val newLocalTagsSync = ConcurrentHashMap(newLocalTags)
        withContext(Dispatchers.Main) {
            localTasks.clear()
            localTasks.putAll(newLocalTasksSync)
            localTags.clear()
            localTags.putAll(newLocalTagsSync)
            onFullUpdateTagListeners.forEach { it.invoke(localTags) }
            onFullUpdateTaskListeners.forEach { it.invoke(localTasks) }
            updateNotifications()
        }
    }

    private fun updateNotifications() {
        scheduleAllDatetimeNotifications(appCtx, localTasks.values.filter { it.tasklistId == 1 })
    }

    companion object {
        private const val HIDDEN_TASKLIST_ID = -1
    }
}
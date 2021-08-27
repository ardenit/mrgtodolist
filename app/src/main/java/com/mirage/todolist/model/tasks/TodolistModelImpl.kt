package com.mirage.todolist.model.tasks

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.preference.PreferenceManager
import com.mirage.todolist.model.gdrive.GDriveConnectExceptionHandler
import com.mirage.todolist.model.gdrive.GDriveRestApi
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

/**
 * See [TodolistModel] for API documentation
 */
class TodolistModelImpl: TodolistModel {

    private val gDriveRestApi = GDriveRestApi()
    private lateinit var appCtx: Context
    private lateinit var prefs: SharedPreferences
    private var email: String? = null

    /** Local cache for tasks, key is task's unique taskID */
    private var localTasks: MutableMap<TaskID, MutableLiveTask> = LinkedHashMap()
    private val tasklistSizes: MutableMap<Int, Int> = LinkedHashMap()
    private val onNewTaskListeners: MutableSet<OnNewTaskListener> = LinkedHashSet()
    private val onMoveTaskListeners: MutableSet<OnMoveTaskListener> = LinkedHashSet()
    private val onFullUpdateTaskListeners: MutableSet<OnFullUpdateTaskListener> = LinkedHashSet()

    /** Local cache for tags, key is tag's unique TagID */
    private var localTags: MutableMap<TagID, MutableLiveTag> = LinkedHashMap()
    private val onNewTagListeners: MutableSet<OnNewTagListener> = LinkedHashSet()
    private val onRemoveTagListeners: MutableSet<OnRemoveTagListener> = LinkedHashSet()
    private val onFullUpdateTagListeners: MutableSet<OnFullUpdateTagListener> = LinkedHashSet()

    private var initialized = false

    override fun init(appCtx: Context) {
        this.appCtx = appCtx.applicationContext
        prefs = PreferenceManager.getDefaultSharedPreferences(this.appCtx)
        if (initialized) return
        initialized = true
        gDriveRestApi.init(this.appCtx, email)

        repeat(4) { tagIndex ->
            val id = UUID.randomUUID()
            val tag = MutableLiveTag(id, tagIndex, tagIndex.toString().repeat(tagIndex + 1), 0)
            localTags[id] = tag
        }

        //TODO load tasks from room and start sync with gdrive
        repeat(3) { tasklistID ->
            val tasksCount = 20
            repeat(tasksCount) { taskIndex ->
                val id = UUID.randomUUID()
                val task = MutableLiveTask(id, tasklistID, taskIndex, true, "init $tasklistID $taskIndex", "init $tasklistID $taskIndex", localTags.values.toList())
                localTasks[id] = task
            }
            tasklistSizes[tasklistID] = tasksCount
        }
    }

    override fun getGDriveAccountEmail(): String? {
        if (email == null) email = prefs.getString(ACC_NAME_KEY, null)
        return email
    }

    override fun setGDriveAccountEmail(newEmail: String?, exHandler: GDriveConnectExceptionHandler) {
        email = newEmail
        prefs.edit().putString(ACC_NAME_KEY, email).apply()
        gDriveRestApi.init(appCtx, email)
        gDriveRestApi.connect(exHandler)
    }

    override fun createNewTask(tasklistID: Int): LiveTask {
        val taskIndex = tasklistSizes[tasklistID] ?: 0
        val taskID = UUID.randomUUID()
        val task = MutableLiveTask(taskID, tasklistID, taskIndex, true, "", "", listOf())
        tasklistSizes[tasklistID] = taskIndex + 1
        localTasks[taskID] = task
        //TODO room query
        onNewTaskListeners.forEach { it.invoke(task) }
        return task
    }

    override fun modifyTask(
        taskID: TaskID,
        title: String?,
        description: String?,
        tags: List<LiveTag>?
    ) {
        val task = localTasks[taskID] ?: return
        //TODO room query
        if (title != null && title != task.title.value) {
            task.title.value = title
        }
        if (description != null && description != task.description.value) {
            task.description.value = description
        }
        if (tags != null && tags != task.tags.value) {
            task.tags.value = tags
        }
    }

    override fun removeTask(taskID: TaskID) {
        moveTask(taskID, HIDDEN_TASKLIST_ID)
    }

    override fun moveTask(taskID: TaskID, newTasklistID: Int) {
        val task = localTasks[taskID] ?: return
        val oldTasklistID = task.tasklistID
        if (oldTasklistID == newTasklistID) return
        val oldTaskIndex = task.taskIndex
        val newTaskIndex = tasklistSizes[newTasklistID] ?: 0
        //TODO Room query for task move and index shift
        tasklistSizes[oldTasklistID] = (tasklistSizes[oldTasklistID] ?: 1) - 1
        tasklistSizes[newTasklistID] = (tasklistSizes[newTasklistID] ?: 0) + 1
        localTasks.values.asSequence()
            .filter { it.tasklistID == oldTasklistID }
            .filter { it.taskIndex > oldTaskIndex }
            .forEach { it.taskIndex = (it.taskIndex ?: 1) - 1 }
        localTasks.values.asSequence()
            .filter { it.tasklistID == newTasklistID }
            .filter { it.taskIndex >= newTaskIndex }
            .forEach { it.taskIndex = (it.taskIndex ?: -1) + 1 }
        task.tasklistID = newTasklistID
        task.taskIndex = newTaskIndex
        onMoveTaskListeners.forEach { listener ->
            listener(task, oldTasklistID, newTasklistID, oldTaskIndex, newTaskIndex)
        }
    }

    override fun moveTaskInList(taskID: TaskID, newTaskIndex: Int) {
        val task = localTasks[taskID] ?: return
        val tasklistID = task.tasklistID
        val oldTaskIndex = task.taskIndex
        if (oldTaskIndex == newTaskIndex) return
        if (oldTaskIndex < newTaskIndex) {
            //TODO Room query for index shift
            localTasks.values.asSequence()
                .filter { it.tasklistID == tasklistID }
                .filter { it.taskIndex in (oldTaskIndex + 1)..newTaskIndex }
                .forEach { it.taskIndex = it.taskIndex - 1 }
        }
        else {
            //TODO Room query for index shift
            localTasks.values.asSequence()
                .filter { it.tasklistID == tasklistID }
                .filter { it.taskIndex in newTaskIndex until oldTaskIndex }
                .forEach { it.taskIndex = it.taskIndex + 1 }
        }
        task.taskIndex = newTaskIndex
    }

    override fun searchTasks(searchQuery: String) {
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

    override fun cancelTaskSearch() {
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

    override fun getAllTasks(): Map<TaskID, LiveTask> = localTasks

    override fun createNewTag(): LiveTag {
        val tagIndex = localTags.size
        val tagID = UUID.randomUUID()
        val tag = MutableLiveTag(tagID, tagIndex, "", 0)
        localTags[tagID] = tag
        //TODO room query
        onNewTagListeners.forEach { it.invoke(tag) }
        return tag
    }

    override fun modifyTag(tagID: TagID, name: String?, styleIndex: Int?) {
        val tag = localTags[tagID] ?: return
        //TODO room query
        if (name != null && name != tag.name.value) {
            tag.name.value = name
        }
        if (styleIndex != null && styleIndex != tag.styleIndex.value) {
            tag.styleIndex.value = styleIndex
        }
    }

    override fun removeTag(tagID: TagID) {
        val tag = localTags[tagID] ?: return
        onRemoveTagListeners.forEach { it.invoke(tag, tag.tagIndex) }
        localTags.remove(tagID)
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
        //TODO SQL query to mark as deleted
    }

    override fun getAllTags(): Map<TagID, LiveTag> = localTags

    override fun addOnNewTaskListener(listener: OnNewTaskListener) {
        onNewTaskListeners += listener
    }

    override fun removeOnNewTaskListener(listener: OnNewTaskListener) {
        onNewTaskListeners -= listener
    }

    override fun addOnMoveTaskListener(listener: OnMoveTaskListener) {
        onMoveTaskListeners += listener
    }

    override fun removeOnMoveTaskListener(listener: OnMoveTaskListener) {
        onMoveTaskListeners -= listener
    }

    override fun addOnFullUpdateTaskListener(listener: OnFullUpdateTaskListener) {
        onFullUpdateTaskListeners += listener
    }

    override fun removeOnFullUpdateTaskListener(listener: OnFullUpdateTaskListener) {
        onFullUpdateTaskListeners -= listener
    }

    override fun addOnNewTagListener(listener: OnNewTagListener) {
        onNewTagListeners += listener
    }

    override fun removeOnNewTagListener(listener: OnNewTagListener) {
        onNewTagListeners -= listener
    }

    override fun addOnRemoveTagListener(listener: OnRemoveTagListener) {
        onRemoveTagListeners += listener
    }

    override fun removeOnRemoveTagListener(listener: OnRemoveTagListener) {
        onRemoveTagListeners -= listener
    }

    override fun addOnFullUpdateTagListener(listener: OnFullUpdateTagListener) {
        onFullUpdateTagListeners += listener
    }

    override fun removeOnFullUpdateTagListener(listener: OnFullUpdateTagListener) {
        onFullUpdateTagListeners -= listener
    }

    companion object {

        private const val ACC_NAME_KEY = "account_name"
        private const val HIDDEN_TASKLIST_ID = -1
    }
}
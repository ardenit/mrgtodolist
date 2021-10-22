package com.mirage.todolist.model.tasks

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.mirage.todolist.R
import com.mirage.todolist.model.room.DatabaseModel
import com.mirage.todolist.model.room.DatabaseSnapshot
import com.mirage.todolist.model.googledrive.GoogleDriveConnectExceptionHandler
import com.mirage.todolist.model.googledrive.GoogleDriveModel
import com.mirage.todolist.model.workers.scheduleAllDatetimeNotifications
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap

typealias OnNewTaskListener = (newTask: LiveTask) -> Unit
typealias OnMoveTaskListener = (task: LiveTask, oldTasklistID: Int, newTasklistID: Int, oldTaskIndex: Int, newTaskIndex: Int) -> Unit
typealias OnFullUpdateTaskListener = (newTasks: Map<TaskID, LiveTask>) -> Unit

typealias OnFullUpdateTagListener = (newTags: Map<TagID, LiveTag>) -> Unit

class TodolistModel {

    private val gDriveRestApi = GoogleDriveModel()
    private lateinit var appCtx: Context
    private lateinit var prefs: SharedPreferences
    //TODO Inject?
    private lateinit var databaseModel: DatabaseModel
    private var email: String? = null

    /** Local cache for tasks, key is task's unique taskID */
    private var localTasks: MutableMap<TaskID, MutableLiveTask> = LinkedHashMap()
    private val tasklistSizes: MutableMap<Int, Int> = LinkedHashMap()
    private val onNewTaskListeners: MutableSet<OnNewTaskListener> = LinkedHashSet()
    private val onMoveTaskListeners: MutableSet<OnMoveTaskListener> = LinkedHashSet()
    private val onFullUpdateTaskListeners: MutableSet<OnFullUpdateTaskListener> = LinkedHashSet()

    /** Local cache for tags, key is tag's unique TagID */
    private var localTags: MutableMap<TagID, MutableLiveTag> = LinkedHashMap()
    private val onFullUpdateTagListeners: MutableSet<OnFullUpdateTagListener> = LinkedHashSet()

    private var initialized = false

    /**
     * Initiates the model.
     * Must be called at least once before doing any other operations with model.
     * May be safely called multiple times during application lifecycle.
     * [appCtx] - application context.
     */
    fun init(appCtx: Context) {
        if (initialized) return
        initialized = true
        this.appCtx = appCtx.applicationContext
        prefs = PreferenceManager.getDefaultSharedPreferences(this.appCtx)
        databaseModel = DatabaseModel()
        databaseModel.init(appCtx) {
            reloadData(it)
        }
        gDriveRestApi.init(this.appCtx, prefs.getString(ACC_NAME_KEY, null).let { if (it.isNullOrBlank()) null else it})
        getGDriveAccountEmail()
    }

    /**
     * Returns current Google Drive account email used for synchronization, or null if sync is not configured.
     */
    fun getGDriveAccountEmail(): String? {
        prefs = PreferenceManager.getDefaultSharedPreferences(this.appCtx)
        val newEmail = prefs.getString(ACC_NAME_KEY, null)
        if (newEmail != email) {
            email = newEmail
            println("Sync email changed to $newEmail")
            GlobalScope.launch (Dispatchers.Main + CoroutineExceptionHandler { coroutineContext, throwable -> println("ERROR $throwable ${(throwable as Exception).message}") }) {
                gDriveRestApi.run {
                    var id = getFileId("testfilelol")
                    if (id == null) {
                        println("creating file")
                        id = createFile("testfilelol")
                        id = getFileId("testfilelol")!!
                    }
                    println("file id: $id")
                    val bytes = downloadFile(id)
                    println("bytes: |${bytes.decodeToString()}|")
                    updateFile(id, "ROFLAN".encodeToByteArray())
                    val bytess = downloadFile(id)
                    println("bytes: |${bytess.decodeToString()}|")
                }
            }
        }
        return email
    }

    /**
     * Changes Google Drive email.
     * Should be invoked when user selects new account for synchronization.
     */
    fun setGDriveAccountEmail(newEmail: String?, exHandler: GoogleDriveConnectExceptionHandler) {
        gDriveRestApi.init(appCtx, newEmail)
        gDriveRestApi.connect(exHandler)
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
     * Returns a map of all tasks (including hidden/removed ones).
     */
    fun getAllTasks(): Map<TaskID, LiveTask> = localTasks

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
        tagID: TagID,
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
     * Returns a map of all tags
     */
    fun getAllTags(): Map<TagID, LiveTag> = localTags

    /**
     * Adds a listener for new task event
     */
    fun addOnNewTaskListener(listener: OnNewTaskListener) {
        onNewTaskListeners += listener
    }

    fun removeOnNewTaskListener(listener: OnNewTaskListener) {
        onNewTaskListeners -= listener
    }

    /**
     * Adds a listener for task's tasklist change event (including removal, i.e. moving to -1)
     */
    fun addOnMoveTaskListener(listener: OnMoveTaskListener) {
        onMoveTaskListeners += listener
    }

    fun removeOnMoveTaskListener(listener: OnMoveTaskListener) {
        onMoveTaskListeners -= listener
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
        val newLocalTasks = LinkedHashMap<TaskID, MutableLiveTask>()
        val newLocalTags = LinkedHashMap<TagID, MutableLiveTag>()
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
            val date = TaskDate(taskEntity.dateYear, taskEntity.dateMonth, taskEntity.dateDay)
            val time = TaskTime(taskEntity.timeHour, taskEntity.timeMinute)
            val task = MutableLiveTask(
                taskID = taskId,
                tasklistID = tasklistId,
                taskIndex = taskEntity.taskIndex,
                isVisible = true,
                title = taskEntity.title,
                description = taskEntity.description,
                tags = tags,
                date = date,
                time = time,
                period = TaskPeriod.values()[taskEntity.periodId.coerceIn(TaskPeriod.values().indices)]
            )
            newLocalTasks[taskId] = task
            tasklistSizes[tasklistId] = (tasklistSizes[tasklistId] ?: 0) + 1
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
        scheduleAllDatetimeNotifications(appCtx, localTasks.values.filter { it.tasklistID == 1 })
    }

    companion object {
        private const val HIDDEN_TASKLIST_ID = -1
    }
}
package com.mirage.todolist.model.repository

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.mirage.todolist.R
import com.mirage.todolist.di.App
import com.mirage.todolist.di.ApplicationContext
import com.mirage.todolist.model.database.AccountSnapshot
import com.mirage.todolist.model.database.DatabaseModel
import com.mirage.todolist.model.googledrive.GoogleDriveConnectExceptionHandler
import com.mirage.todolist.model.googledrive.GoogleDriveModel
import com.mirage.todolist.model.workers.SyncWorker
import com.mirage.todolist.model.workers.scheduleAllDatetimeNotifications
import com.mirage.todolist.util.OptionalDate
import com.mirage.todolist.util.OptionalTaskLocation
import com.mirage.todolist.util.OptionalTime
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.Math.random
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

typealias OnNewTaskListener = (newTask: LiveTask) -> Unit
typealias OnMoveTaskListener = (task: LiveTask, oldTasklistID: Int, newTasklistID: Int, oldTaskIndex: Int, newTaskIndex: Int) -> Unit
typealias OnFullUpdateTaskListener = (newTasks: Map<UUID, LiveTask>) -> Unit
typealias OnFullUpdateTagListener = (newTags: Map<UUID, LiveTag>) -> Unit

/**
 * Repository class that encapsulates data manipulations with database and remote drive storage.
 * Intended to be used by ViewModels from the main UI thread.
 */
class TodoRepository {

    @ApplicationContext
    @Inject
    lateinit var appCtx: Context
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var resources: Resources
    @Inject
    lateinit var workManager: WorkManager
    @Inject
    lateinit var databaseModel: DatabaseModel
    @Inject
    lateinit var googleDriveModel: GoogleDriveModel

    @Volatile
    private var currentEmail: String = ""
    private var lastSyncStartTimeMillis: Long = 0L
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Local in-memory cache for tasks bound to the current Google account */
    private var localTasks: MutableMap<UUID, MutableLiveTask> = LinkedHashMap()

    /** Cache for sizes of each tasklist used to speed up some requests */
    private val tasklistSizes: MutableMap<Int, Int> = LinkedHashMap()

    /** Local in-memory cache for tags bound to the current Google account */
    private var localTags: MutableMap<UUID, MutableLiveTag> = LinkedHashMap()

    /** Listeners for tasklist changes made by user */
    private val onNewTaskListeners: MutableSet<OnNewTaskListener> = LinkedHashSet()
    private val onMoveTaskListeners: MutableSet<OnMoveTaskListener> = LinkedHashSet()

    /** Listeners for full cache update after Google Drive sync */
    private val onFullUpdateTaskListeners: MutableSet<OnFullUpdateTaskListener> = LinkedHashSet()
    private val onFullUpdateTagListeners: MutableSet<OnFullUpdateTagListener> = LinkedHashSet()

    init {
        App.instance.appComponent.inject(this)
        currentEmail =
            preferences.getString(resources.getString(R.string.key_sync_select_acc), "") ?: ""
        Timber.v("TodoRepository init with email $currentEmail")
        databaseModel.setOnSyncUpdateListener {
            reloadData(it)
        }
        databaseModel.getAccountSnapshot {
            reloadData(it)
        }
        startSync(currentEmail)
        coroutineScope.launch {
            while (isActive) {
                delay((MIN_SYNC_INTERVAL * (1 + 2 * random())).toLong())
                startSyncIfNeeded()
            }
        }
    }

    /**
     * Tries to connect to the email used for Google Drive synchronization.
     * Should be invoked when user selects a new account.
     */
    fun tryChangeGoogleAccount(newEmail: String, exHandler: GoogleDriveConnectExceptionHandler) {
        Timber.v("Trying to connect to $newEmail")
        googleDriveModel.connectAsync(newEmail, exHandler)
    }

    /**
     * Completely changes current Google account and schedules a worker to perform Drive sync.
     * Updates in-memory task cache immediately with new tasks and triggers onFullUpdate listeners.
     */
    fun startSync(newEmail: String) {
        Timber.v("Changing current account to $newEmail")
        currentEmail = newEmail
        databaseModel.startObservingAccount(newEmail)
        databaseModel.getAccountSnapshot {
            reloadData(it)
        }
        startSyncWorker(newEmail)
    }

    /**
     * Starts or restarts SyncWorker that performs synchronization with Google Drive using given email.
     */
    private fun startSyncWorker(syncEmail: String) {
        Timber.v("Starting SyncWorker with email $syncEmail")
        val data = Data.Builder().putBoolean(SyncWorker.DATA_KEY_ACTIVE, false).build()
        val request = OneTimeWorkRequest.Builder(SyncWorker::class.java).setInputData(data).build()
        workManager.beginUniqueWork(
            SyncWorker.PERIODIC_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        ).enqueue()
    }

    private fun startSyncIfNeeded() {
        val currentTime = System.currentTimeMillis()
        if (currentTime >= lastSyncStartTimeMillis + MIN_SYNC_INTERVAL) {
            lastSyncStartTimeMillis = currentTime
            Timber.v("Starting upload sync with email $currentEmail")
            val data = Data.Builder().putBoolean(SyncWorker.DATA_KEY_ACTIVE, true).build()
            val request = OneTimeWorkRequest.Builder(SyncWorker::class.java).setInputData(data).build()
            workManager.beginUniqueWork(
                SyncWorker.ACTIVE_SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            ).enqueue()
        }
    }

    /**
     * Creates a new task in a given [tasklistId] and returns it.
     */
    fun createNewTask(tasklistId: Int): LiveTask {
        Timber.v("Creating new task in tasklist $tasklistId")
        val taskIndex = tasklistSizes[tasklistId] ?: 0
        val (taskId, _) = databaseModel.createNewTask(tasklistId)
        val task = MutableLiveTask(
            taskId = taskId,
            tasklistId = tasklistId,
            taskIndex = taskIndex,
            title = appCtx.resources.getString(R.string.task_default_title),
            description = appCtx.resources.getString(R.string.task_default_description),
            location = OptionalTaskLocation.NOT_SET,
            date = OptionalDate.NOT_SET,
            time = OptionalTime.NOT_SET,
            period = TaskPeriod.NOT_REPEATABLE,
            tags = emptyList(),
            isVisible = true
        )
        tasklistSizes[tasklistId] = taskIndex + 1
        localTasks[taskId] = task
        onNewTaskListeners.forEach { it.invoke(task) }
        updateNotifications()
        startSyncIfNeeded()
        return task
    }

    /**
     * Modifies the task with ID [taskID].
     * If a parameter's value is null, its value remains unchanged.
     * This method automatically updates "last modified" time.
     */
    fun modifyTask(
        taskID: UUID,
        title: String?,
        description: String?,
        tags: List<LiveTag>?,
        location: OptionalTaskLocation?,
        date: OptionalDate?,
        time: OptionalTime?,
        period: TaskPeriod?
    ) {
        val task = localTasks[taskID] ?: return
        if (title != null && title != task.title.value) {
            task.title.value = title
            databaseModel.setTaskTitle(task.taskId, title)
        }
        if (description != null && description != task.description.value) {
            task.description.value = description
            task.title.value = title
            databaseModel.setTaskDescription(task.taskId, description)
        }
        if (tags != null && tags != task.tags.value) {
            task.tags.value = tags
            databaseModel.setTaskTags(task.taskId, tags.map { it.tagId })
        }
        if (location != null && location != task.location.value) {
            task.location.value = location
            databaseModel.setTaskLocation(task.taskId, location)
        }
        if (date != null && date != task.date.value) {
            task.date.value = date
            databaseModel.setTaskDate(task.taskId, date)
        }
        if (time != null && time != task.time.value) {
            task.time.value = time
            databaseModel.setTaskTime(task.taskId, time)
        }
        if (period != null && period != task.period.value) {
            task.period.value = period
            databaseModel.setTaskPeriod(task.taskId, period)
        }
        updateNotifications()
        startSyncIfNeeded()
    }

    /**
     * Deletes the task with ID [taskID]
     * (actually just moves to hidden tasklist to simplify diff calculation).
     * This method automatically updates "last modified" time.
     */
    fun removeTask(taskID: UUID) {
        moveTask(taskID, HIDDEN_TASKLIST_ID)
        updateNotifications()
        startSyncIfNeeded()
    }

    /**
     * Moves the task with ID [taskID] to another tasklist.
     */
    fun moveTask(
        taskID: UUID,
        newTasklistID: Int
    ) {
        val task = localTasks[taskID] ?: return
        val oldTasklistID = task.tasklistId
        if (oldTasklistID == newTasklistID) return
        val oldTaskIndex = task.taskIndex
        val newTaskIndex = tasklistSizes[newTasklistID] ?: 0
        tasklistSizes[oldTasklistID] = (tasklistSizes[oldTasklistID] ?: 1) - 1
        tasklistSizes[newTasklistID] = (tasklistSizes[newTasklistID] ?: 0) + 1
        localTasks.values.asSequence()
            .filter { it.tasklistId == oldTasklistID }
            .filter { it.taskIndex > oldTaskIndex }
            .forEach { it.taskIndex = it.taskIndex - 1 }
        localTasks.values.asSequence()
            .filter { it.tasklistId == newTasklistID }
            .filter { it.taskIndex >= newTaskIndex }
            .forEach { it.taskIndex = it.taskIndex + 1 }
        task.tasklistId = newTasklistID
        task.taskIndex = newTaskIndex
        databaseModel.moveTask(task.taskId, newTasklistID)
        onMoveTaskListeners.forEach { listener ->
            listener(task, oldTasklistID, newTasklistID, oldTaskIndex, newTaskIndex)
        }
        updateNotifications()
        startSyncIfNeeded()
    }

    /**
     * Moves the task to another position inside the tasklist.
     */
    fun moveTaskInList(
        taskID: UUID,
        newTaskIndex: Int
    ) {
        val task = localTasks[taskID] ?: return
        val tasklistID = task.tasklistId
        val oldTaskIndex = task.taskIndex
        if (oldTaskIndex == newTaskIndex) return
        if (oldTaskIndex < newTaskIndex) {
            localTasks.values.asSequence()
                .filter { it.tasklistId == tasklistID }
                .filter { it.taskIndex in (oldTaskIndex + 1)..newTaskIndex }
                .forEach { it.taskIndex = it.taskIndex - 1 }
        } else {
            localTasks.values.asSequence()
                .filter { it.tasklistId == tasklistID }
                .filter { it.taskIndex in newTaskIndex until oldTaskIndex }
                .forEach { it.taskIndex = it.taskIndex + 1 }
        }
        task.taskIndex = newTaskIndex
        databaseModel.moveTaskInList(task.taskId, newTaskIndex)
        startSyncIfNeeded()
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
            val matchesTaskName =
                searchTask.isBlank() || (searchTask in title) || (searchTask in description)
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
        val (tagID, _) = databaseModel.createNewTag()
        val tag = MutableLiveTag(tagID, tagIndex, "", 0)
        localTags[tagID] = tag
        startSyncIfNeeded()
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
            databaseModel.setTagName(tag.tagId, name)
        }
        if (styleIndex != null && styleIndex != tag.styleIndex.value) {
            tag.styleIndex.value = styleIndex
            databaseModel.setTagStyleIndex(tag.tagId, styleIndex)
        }
        startSyncIfNeeded()
    }

    /**
     * Deletes the [tag]
     * (actually just moves to hidden taglist to simplify diff calculation).
     * This method automatically updates "last modified" time.
     */
    fun removeTag(tag: LiveTag) {
        localTags.remove(tag.tagId)
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
        databaseModel.removeTag(tag.tagId)
        startSyncIfNeeded()
    }

    fun getAllTasks(): Map<UUID, LiveTask> = localTasks

    fun getAllTags(): Map<UUID, LiveTag> = localTags

    fun addOnNewTaskListener(listener: OnNewTaskListener) {
        onNewTaskListeners += listener
    }

    fun removeOnNewTaskListener(listener: OnNewTaskListener) {
        onNewTaskListeners -= listener
    }

    fun addOnMoveTaskListener(listener: OnMoveTaskListener) {
        onMoveTaskListeners += listener
    }

    fun removeOnMoveTaskListener(listener: OnMoveTaskListener) {
        onMoveTaskListeners -= listener
    }

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

    private suspend fun reloadData(dbSnapshot: AccountSnapshot) {
        val newLocalTasks = LinkedHashMap<UUID, MutableLiveTask>()
        val newLocalTags = LinkedHashMap<UUID, MutableLiveTag>()
        dbSnapshot.tags.forEach { tagEntity ->
            val tagId = tagEntity.tagId
            val tag = MutableLiveTag(
                tagId = tagId,
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
                location = taskEntity.location,
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
            tasklistSizes.clear()
            localTasks.values.forEach {
                tasklistSizes[it.tasklistId] = (tasklistSizes[it.tasklistId] ?: 0) + 1
            }
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
        private const val MIN_SYNC_INTERVAL = 20 * 1000L
    }
}
package com.mirage.todolist.model.database

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.mirage.todolist.di.App
import com.mirage.todolist.R
import com.mirage.todolist.model.repository.TaskPeriod
import kotlinx.coroutines.*
import timber.log.Timber
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Class for interacting with Room database
 * This model encapsulates working with its own thread,
 * so no external thread switching or synchronization is required.
 */
class DatabaseModel {

    @Inject
    lateinit var database: AppDatabase
    @Inject
    lateinit var taskDao: TaskDao
    @Inject
    lateinit var tagDao: TagDao
    @Inject
    lateinit var relationDao: RelationDao
    @Inject
    lateinit var versionDao: VersionDao
    @Inject
    lateinit var appCtx: Context
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var resources: Resources

    @Volatile
    private var onSyncUpdateListener: suspend (DatabaseSnapshot) -> Unit = {}
    @Volatile
    private var liveVersionObservable: LiveData<UUID>? = null
    @Volatile
    private var liveVersionObserver: Observer<UUID>? = null
    @Volatile
    private var currentEmail: String = ""

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        (throwable as? Exception)?.printStackTrace()
    }
    private val coroutineContext = dispatcher + exceptionHandler
    private val coroutineScope = CoroutineScope(SupervisorJob() + coroutineContext)

    init {
        App.instance.appComponent.inject(this)
        val currentEmail = preferences.getString(resources.getString(R.string.key_sync_select_acc), "")
        if (currentEmail.isNullOrBlank()) {
            Timber.v("Synchronization email is empty! No need to observe it.")
        } else {
            Timber.v("Observing data version of email $currentEmail")
            //TODO remove
            Timber.v("Init thread ${Thread.currentThread().id}")
            coroutineScope.launch {
                Timber.v("Coroutine thread ${Thread.currentThread().id}")
                database.runInTransaction {
                    Timber.v("Transaction thread ${Thread.currentThread().id}")
                }
            }
            startObservingAccount(currentEmail)
        }
    }

    suspend fun getDatabaseSnapshot(): DatabaseSnapshot = withContext(coroutineContext) {
        val allTasks = taskDao.getAllTasks()
        val allTags = tagDao.getAllTags()
        val allRelations = relationDao.getAllRelations()
        val allVersions = versionDao.getAllVersions()
        DatabaseSnapshot(allTasks, allTags, allRelations, allVersions)
    }

    /**
     * Registers a listener to observe full database update events coming from Google Drive sync worker.
     * [onSyncUpdate] will be called on the database thread dispatcher
     */
    fun setOnSyncUpdateListener(onSyncUpdate: suspend (DatabaseSnapshot) -> Unit) {
        onSyncUpdateListener = onSyncUpdate
    }

    /**
     * Fully updates the database to store the new snapshot, merged with data loaded from Google Drive
     * @return true if database was successfully updated, false if sync must be retried later
     * (e.g. if user updates the tasklist, sync was done with outdated local database snapshot and must be redone)
     */
    fun updateDatabaseAfterSync(newSnapshot: DatabaseSnapshot, oldDatabaseVersion: UUID): Boolean {
        val result = AtomicBoolean(true)
        val resultWasSet = AtomicBoolean(false)
        database.runInTransaction {
            val localVersion = versionDao.getDataVersion(currentEmail).firstOrNull() ?: UUID.randomUUID()
            if (localVersion != oldDatabaseVersion) {
                result.set(false)
                resultWasSet.set(true)
                return@runInTransaction
            }
            taskDao.removeAllTasks(currentEmail)
            taskDao.insertAllTasks(newSnapshot.tasks.toList())
            tagDao.removeAllTags(currentEmail)
            tagDao.insertAllTags(newSnapshot.tags.toList())
            relationDao.removeAllRelations(currentEmail)
            relationDao.insertAllRelations(newSnapshot.relations.toList())
            val newVersion = newSnapshot.meta.firstOrNull { it.accountName == currentEmail }?.dataVersion ?: UUID.randomUUID()
            versionDao.setDataVersion(currentEmail,  newVersion, true)
            resultWasSet.set(true)
            result.set(true)
        }
        if (!resultWasSet.get()) {
            Timber.e("Result has not yet been set!")
        }
        return result.get()
    }

    /**
     * Starts observing the data version of the given account [email]
     * Should be used every time current synchronization account changes
     */
    fun startObservingAccount(accountEmail: String) {
        coroutineScope.launch {
            currentEmail = accountEmail
            val liveVersion = versionDao.getLiveDataVersion(accountEmail)
            val observer = Observer<UUID> {
                Timber.v("Database version changed")
                coroutineScope.launch {
                    database.runInTransaction {
                        val mustBeProcessed = versionDao.getMustBeProcessed(currentEmail)
                        if (mustBeProcessed) {
                            Timber.v("Version was changed after sync, calling listeners")
                            val newTasks = taskDao.getAllTasks()
                            val newTags = tagDao.getAllTags()
                            val newRelations = relationDao.getAllRelations()
                            val newVersions = versionDao.getAllVersions()
                            val newSnapshot =
                                DatabaseSnapshot(newTasks, newTags, newRelations, newVersions)
                            versionDao.setMustBeProcessed(currentEmail, false)
                            coroutineScope.launch {
                                onSyncUpdateListener(newSnapshot)
                            }
                        } else {
                            Timber.v("Version was changed by user action, not calling listeners")
                        }
                    }
                }
            }
            liveVersionObserver?.let {
                liveVersionObservable?.removeObserver(it)
            }
            liveVersionObservable = liveVersion
            liveVersionObserver = observer
            liveVersion.observeForever(observer)
        }
    }

    /** Returns task ID immediately without waiting for DB query to complete */
    fun createNewTask(tasklistId: Int): UUID {
        val taskId = UUID.randomUUID()
        coroutineScope.launch {
            val defaultTitle = appCtx.resources.getString(R.string.task_default_title)
            val defaultDescription = appCtx.resources.getString(R.string.task_default_description)
            database.runInTransaction {
                val taskIndex = taskDao.getTasklistSize(tasklistId)
                val taskEntity = TaskEntity(
                    taskId = taskId,
                    accountName = currentEmail,
                    tasklistId = tasklistId,
                    taskIndex = taskIndex,
                    title = defaultTitle,
                    description = defaultDescription,
                    date = null,
                    time = null,
                    period = TaskPeriod.NOT_REPEATABLE,
                    lastModified = Clock.systemUTC().instant()
                )
                taskDao.insertTask(taskEntity)
                versionDao.updateVersion(currentEmail)
            }
        }
        return taskId
    }

    fun moveTask(taskId: UUID, newTasklistId: Int) = launchTaskTransaction {
        val oldTasklistId = getTasklistId(taskId)
        val oldTaskIndex = getTaskIndex(taskId)
        val newTaskIndex = getTasklistSize(newTasklistId)
        val instant = Clock.systemUTC().instant()
        setTimeModifiedInSlice(oldTasklistId, oldTaskIndex + 1, Int.MAX_VALUE, instant)
        shiftTaskIndicesInSlice(oldTasklistId, oldTaskIndex + 1, Int.MAX_VALUE, -1)
        setTaskIndex(taskId, newTaskIndex)
        setTasklistId(taskId, newTasklistId)
        setTaskLastModifiedTime(taskId, instant)
    }

    fun moveTaskInList(taskId: UUID, newTaskIndex: Int) = launchTaskTransaction {
        val tasklistId = getTasklistId(taskId)
        val oldTaskIndex = getTaskIndex(taskId)
        val instant = Clock.systemUTC().instant()
        if (oldTaskIndex < newTaskIndex) {
            setTimeModifiedInSlice(tasklistId, oldTaskIndex + 1, newTaskIndex + 1, instant)
            shiftTaskIndicesInSlice(tasklistId, oldTaskIndex + 1, newTaskIndex + 1, -1)
            setTaskIndex(taskId, newTaskIndex)
        } else if (oldTaskIndex > newTaskIndex) {
            setTimeModifiedInSlice(tasklistId, newTaskIndex, oldTaskIndex, instant)
            shiftTaskIndicesInSlice(tasklistId, newTaskIndex, oldTaskIndex, 1)
            setTaskIndex(taskId, newTaskIndex)
        }
        setTaskLastModifiedTime(taskId, instant)
    }

    fun setTaskTitle(taskId: UUID, title: String) = modifyTask(taskId) {
        setTaskTitle(taskId, title)
    }

    fun setTaskDescription(taskId: UUID, description: String) = modifyTask(taskId) {
        setTaskDescription(taskId, description)
    }

    fun setTaskTags(taskId: UUID, tagIds: List<UUID>) {
        val tagIdsSync = CopyOnWriteArrayList(tagIds)
        launchTaskTransaction {
            tagIdsSync.forEach { tagId ->
                val relationsCount = relationDao.checkRelation(taskId, tagId)
                if (relationsCount == 0) {
                    val relation = RelationEntity(taskId, tagId, currentEmail, false, Clock.systemUTC().instant())
                    relationDao.insertRelation(relation)
                }
                else {
                    relationDao.restoreRelation(taskId, tagId)
                    relationDao.setRelationModifiedTime(taskId, tagId, Clock.systemUTC().instant())
                }
            }
        }
    }

    fun setTaskDate(taskId: UUID, taskDate: LocalDate) = modifyTask(taskId) {
        taskDao.setTaskDate(taskId, taskDate)
    }

    fun setTaskTime(taskId: UUID, taskTime: LocalTime) = modifyTask(taskId) {
        setTaskTime(taskId, taskTime)
    }

    fun setTaskPeriod(taskId: UUID, period: TaskPeriod) = modifyTask(taskId) {
        setTaskPeriod(taskId, period)
    }

    fun createNewTag(): UUID {
        val tagId = UUID.randomUUID()
        launchTagTransaction {
            val tagIndex = getTagsCount()
            val tagEntity = TagEntity(
                tagId = tagId,
                tagIndex = tagIndex,
                name = "",
                styleIndex = 0,
                deleted = false,
                lastModified = Clock.systemUTC().instant()
            )
            insertTag(tagEntity)
        }
        return tagId
    }

    fun setTagName(tagId: UUID, name: String) = modifyTag(tagId) {
        setTagName(tagId, name)
    }

    fun setTagStyleIndex(tagId: UUID, styleIndex: Int) = modifyTag(tagId) {
        setTagStyleIndex(tagId, styleIndex)
    }

    fun removeTag(tagId: UUID) = modifyTag(tagId) {
        setTagDeleted(tagId, true)
    }

    private fun launchTaskTransaction(block: TaskDao.() -> Unit) {
        coroutineScope.launch {
            database.runInTransaction {
                taskDao.block()
                versionDao.updateVersion(currentEmail)
            }
        }
    }

    private fun launchTagTransaction(block: TagDao.() -> Unit) {
        coroutineScope.launch {
            database.runInTransaction {
                tagDao.block()
                versionDao.updateVersion(currentEmail)
            }
        }
    }

    private fun modifyTask(taskId: UUID, block: TaskDao.(UUID) -> Unit) = launchTaskTransaction {
        block(taskId)
        setTaskLastModifiedTime(taskId, Clock.systemUTC().instant())
    }

    private fun modifyTag(tagId: UUID, block: TagDao.(UUID) -> Unit) = launchTagTransaction {
        block(tagId)
        setTagLastModifiedTime(tagId, Clock.systemUTC().instant())
    }
}
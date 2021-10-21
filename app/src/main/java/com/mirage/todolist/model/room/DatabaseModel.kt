package com.mirage.todolist.model.room

import android.content.Context
import com.mirage.todolist.di.App
import com.mirage.todolist.R
import com.mirage.todolist.model.tasks.TagID
import com.mirage.todolist.model.tasks.TaskID
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Interface for interacting with Room database
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
    lateinit var metaDao: MetaDao
    @Inject
    lateinit var appCtx: Context

    private val onSyncUpdateListeners: MutableList<suspend (DatabaseSnapshot) -> Unit> = CopyOnWriteArrayList()

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        (throwable as? Exception)?.printStackTrace()
    }
    private val coroutineContext = dispatcher + exceptionHandler
    private val coroutineScope = CoroutineScope(SupervisorJob() + coroutineContext)

    init {
        App.instance.appComponent.inject(this)
        //TODO remove
        Timber.v("Init thread ${Thread.currentThread().id}")
        coroutineScope.launch {
            Timber.v("Coroutine thread ${Thread.currentThread().id}")
            database.runInTransaction {
                val flag = metaDao.getMustBeProcessed()
                Timber.v("Transaction thread ${Thread.currentThread().id} mustBeProcessed=$flag")
            }
        }
        coroutineScope.launch {
            val liveVersion = metaDao.getLiveDataVersion()
            liveVersion.observeForever {
                Timber.v("Database version changed")
                coroutineScope.launch {
                    database.runInTransaction {
                        val mustBeProcessed = metaDao.getMustBeProcessed()
                        if (mustBeProcessed) {
                            Timber.v("Version was changed after sync, calling listeners")
                            val newTasks = taskDao.getAllTasks()
                            val newTags = tagDao.getAllTags()
                            val newRelations = relationDao.getAllRelations()
                            val newMeta = metaDao.getAllMeta()
                            val newSnapshot = DatabaseSnapshot(newTasks, newTags, newRelations, newMeta)
                            metaDao.setMustBeProcessed(false)
                            coroutineScope.launch(Dispatchers.Main) {
                                onSyncUpdateListeners.forEach { it(newSnapshot) }
                            }
                        } else {
                            Timber.v("Version was changed by user action, not calling listeners")
                        }
                    }
                }
            }
        }
    }

    suspend fun getDatabaseSnapshot(): DatabaseSnapshot = withContext(coroutineContext) {
        val allTasks = taskDao.getAllTasks()
        val allTags = tagDao.getAllTags()
        val allRelations = relationDao.getAllRelations()
        val allMeta = metaDao.getAllMeta()
        DatabaseSnapshot(allTasks, allTags, allRelations, allMeta)
    }

    /**
     * Registers a listener to observe full database update events coming from Google Drive sync worker.
     * [onSyncUpdate] will be called on the main dispatcher
     */
    fun addOnSyncUpdateListener(onSyncUpdate: suspend (DatabaseSnapshot) -> Unit) {
        onSyncUpdateListeners += onSyncUpdate
    }

    /**
     * Removes the [onSyncUpdate] listener
     */
    fun removeOnSyncUpdateListener(onSyncUpdate: suspend (DatabaseSnapshot) -> Unit) {
        onSyncUpdateListeners -= onSyncUpdate
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
            val allMeta = metaDao.getAllMeta()
            val localVersion = allMeta.firstOrNull()?.value ?: UUID.randomUUID()
            if (localVersion != oldDatabaseVersion) {
                result.set(false)
                resultWasSet.set(true)
                return@runInTransaction
            }
            taskDao.removeAllTasks()
            taskDao.insertAllTasks(newSnapshot.tasks.toList())
            tagDao.removeAllTags()
            tagDao.insertAllTags(newSnapshot.tags.toList())
            relationDao.removeAllRelations()
            relationDao.insertAllRelations(newSnapshot.relations.toList())
            metaDao.setDataVersion(newSnapshot.dataVersion, true)
            resultWasSet.set(true)
            result.set(true)
        }
        if (!resultWasSet.get()) {
            Timber.e("RESULT WAS NOT YET SET!")
        }
        return result.get()
    }

    /** Returns [TaskID] immediately without waiting for DB query to complete */
    fun createNewTask(tasklistId: Int, accountName: String): TaskID {
        val taskId = UUID.randomUUID()
        coroutineScope.launch {
            val defaultTitle = appCtx.resources.getString(R.string.task_default_title)
            val defaultDescription = appCtx.resources.getString(R.string.task_default_description)
            database.runInTransaction {
                val taskIndex = taskDao.getTasklistSize(tasklistId)
                val taskEntity = TaskEntity(
                    taskId = taskId,
                    tasklistId = tasklistId,
                    taskIndex = taskIndex,
                    title = defaultTitle,
                    description = defaultDescription,
                    lastModifiedTimeMillis = System.currentTimeMillis(),
                    accountName = accountName
                )
                taskDao.insertTask(taskEntity)
                metaDao.updateVersion()
            }
        }
        return taskId
    }

    fun moveTask(taskId: TaskID, newTasklistId: Int) = launchTaskTransaction {
        val oldTasklistId = getTasklistId(taskId)
        val oldTaskIndex = getTaskIndex(taskId)
        val newTaskIndex = getTasklistSize(newTasklistId)
        shiftTaskIndicesInSlice(oldTasklistId, oldTaskIndex + 1, Int.MAX_VALUE, -1)
        setTaskIndex(taskId, newTaskIndex)
        setTasklistId(taskId, newTasklistId)
        setTaskModifiedTime(taskId, System.currentTimeMillis())
    }

    fun moveTaskInList(taskId: TaskID, newTaskIndex: Int) = launchTaskTransaction {
        val tasklistId = getTasklistId(taskId)
        val oldTaskIndex = getTaskIndex(taskId)
        if (oldTaskIndex < newTaskIndex) {
            setTimeModifiedInSlice(tasklistId, oldTaskIndex + 1, newTaskIndex + 1, System.currentTimeMillis())
            shiftTaskIndicesInSlice(tasklistId, oldTaskIndex + 1, newTaskIndex + 1, -1)
            setTaskIndex(taskId, newTaskIndex)
        } else if (oldTaskIndex > newTaskIndex) {
            setTimeModifiedInSlice(tasklistId, newTaskIndex, oldTaskIndex, System.currentTimeMillis())
            shiftTaskIndicesInSlice(tasklistId, newTaskIndex, oldTaskIndex, 1)
            setTaskIndex(taskId, newTaskIndex)
        }
        setTaskModifiedTime(taskId, System.currentTimeMillis())
    }

    fun setTaskTitle(taskId: TaskID, title: String) = launchTaskTransaction {
        setTaskTitle(taskId, title)
        setTaskModifiedTime(taskId, System.currentTimeMillis())
    }

    fun setTaskDescription(taskId: TaskID, description: String) = launchTaskTransaction {
        setTaskDescription(taskId, description)
        setTaskModifiedTime(taskId, System.currentTimeMillis())
    }

    fun setTaskTags(taskId: TaskID, tagIds: List<TagID>) {
        val tagIdsSync = CopyOnWriteArrayList(tagIds)
        launchTaskTransaction {
            tagIdsSync.forEach { tagId ->
                val relationsCount = relationDao.checkRelation(taskId, tagId)
                if (relationsCount == 0) {
                    val relation = RelationEntity(taskId, tagId, false, System.currentTimeMillis())
                    relationDao.insertRelation(relation)
                }
                else {
                    relationDao.restoreRelation(taskId, tagId)
                    relationDao.setRelationModifiedTime(taskId, tagId, System.currentTimeMillis())
                }
            }
        }
    }

    fun setTaskDate(taskId: TaskID, year: Int, monthOfYear: Int, dayOfMonth: Int) = launchTaskTransaction {
        setTaskDate(taskId, year, monthOfYear, dayOfMonth)
        setTaskModifiedTime(taskId, System.currentTimeMillis())
    }

    fun setTaskTime(taskId: TaskID, hour: Int, minute: Int) = launchTaskTransaction {
        setTaskTime(taskId, hour, minute)
        setTaskModifiedTime(taskId, System.currentTimeMillis())
    }

    fun setTaskPeriod(taskId: TaskID, periodId: Int) = launchTaskTransaction {
        setTaskPeriodId(taskId, periodId)
        setTaskModifiedTime(taskId, System.currentTimeMillis())
    }

    fun createNewTag(): TagID {
        val tagId = UUID.randomUUID()
        launchTagTransaction {
            val tagIndex = getTagsCount()
            val tagEntity = TagEntity(
                tagId = tagId,
                tagIndex = tagIndex,
                name = "",
                styleIndex = 0,
                deleted = false,
                lastModifiedTimeMillis = System.currentTimeMillis()
            )
            insertTag(tagEntity)
        }
        return tagId
    }

    fun setTagName(tagId: TagID, name: String) = launchTagTransaction {
        setTagName(tagId, name)
        setTagLastModifiedTime(tagId, System.currentTimeMillis())
    }

    fun setTagStyleIndex(tagId: TagID, styleIndex: Int) = launchTagTransaction {
        setTagStyleIndex(tagId, styleIndex)
        setTagLastModifiedTime(tagId, System.currentTimeMillis())
    }

    fun removeTag(tagId: TagID) = launchTagTransaction {
        setTagDeleted(tagId, true)
        setTagLastModifiedTime(tagId, System.currentTimeMillis())
    }

    private fun launchTaskTransaction(block: TaskDao.() -> Unit) {
        coroutineScope.launch {
            database.runInTransaction {
                taskDao.block()
                metaDao.updateVersion()
            }
        }
    }

    private fun launchTagTransaction(block: TagDao.() -> Unit) {
        coroutineScope.launch {
            database.runInTransaction {
                tagDao.block()
                metaDao.updateVersion()
            }
        }
    }
}
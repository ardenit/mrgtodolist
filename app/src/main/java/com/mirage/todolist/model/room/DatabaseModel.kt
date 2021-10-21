package com.mirage.todolist.model.room

import android.content.Context
import androidx.room.Room
import com.mirage.todolist.R
import com.mirage.todolist.model.tasks.TagID
import com.mirage.todolist.model.tasks.TaskID
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Interface for interacting with Room database
 * This model encapsulates working with its own thread,
 * so no external thread switching or synchronization is required.
 * Most operations do not require waiting for the result.
 */
class DatabaseModel {

    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var tagDao: TagDao
    private lateinit var relationDao: RelationDao
    private lateinit var metaDao: MetaDao

    @Volatile
    private lateinit var appCtx: Context
    @Volatile
    private var onSyncUpdateListener: suspend (DatabaseSnapshot) -> Unit = {}

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        (throwable as? Exception)?.printStackTrace()
    }
    private val coroutineContext = dispatcher + exceptionHandler
    private val coroutineScope = CoroutineScope(SupervisorJob() + coroutineContext)

    /**
     * Initiates the database
     */
    suspend fun init(appCtx: Context) {
        this@DatabaseModel.appCtx = appCtx
        withContext(coroutineContext) {
            taskDao = database.getTaskDao()
            tagDao = database.getTagDao()
            relationDao = database.getRelationDao()
            metaDao = database.getMetaDao()
            val liveVersion = metaDao.getLiveDataVersion()
            liveVersion.observeForever {
                println("LIVE VERSION CHANGED")
                coroutineScope.launch {
                    val newTasks = taskDao.getAllTasks()
                    val newTags = tagDao.getAllTags()
                    val newRelations = relationDao.getAllRelations()
                    val newMeta = metaDao.getAllMeta()
                    val newSnapshot = DatabaseSnapshot(newTasks, newTags, newRelations, newMeta)
                    onSyncUpdateListener(newSnapshot)
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

    }

    /**
     * Removes the [onSyncUpdate] listener
     */
    fun removeOnSyncUpdateListener(onSyncUpdate: suspend (DatabaseSnapshot) -> Unit) {

    }

    /**
     * Fully updates the database to store the new snapshot, merged with data loaded from Google Drive
     * @return true if database was successfully updated, false if sync must be retried later
     * (e.g. if user updates the tasklist, sync was done with outdated local database snapshot and must be redone)
     */
    fun updateDatabaseAfterSync(newSnapshot: DatabaseSnapshot, oldDatabaseVersion: UUID): Boolean {
        val result = AtomicBoolean(true)
        database.runInTransaction {
            val allMeta = metaDao.getAllMeta()
            val localVersion = allMeta.firstOrNull()?.value ?: UUID.randomUUID()
            if (localVersion != oldDatabaseVersion) {
                result.set(false)
                return@runInTransaction
            }
            taskDao.removeAllTasks()
            taskDao.insertAllTasks(newSnapshot.tasks.toList())
            tagDao.removeAllTags()
            tagDao.insertAllTags(newSnapshot.tags.toList())
            relationDao.removeAllRelations()
            relationDao.insertAllRelations(newSnapshot.relations.toList())
            metaDao.setDataVersion(newSnapshot.dataVersion, false)
            result.set(true)
        }
        return result.get()
    }

    /** Returns [TaskID] immediately without waiting for DB query to complete */
    fun createNewTask(tasklistId: Int): TaskID {
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
                    lastModifiedTimeMillis = System.currentTimeMillis()
                )
                taskDao.insertTask(taskEntity)
            }
        }
        return taskId
    }

    fun removeTask(taskId: TaskID) = taskTransaction(taskId) { id ->
        setTasklistId(id, -1)
        setTaskModifiedTime(id, System.currentTimeMillis())
    }

    fun moveTask(taskId: TaskID, newTasklistId: Int) = taskTransaction(taskId) { id ->
        val oldTasklistId = getTasklistId(id)
        val oldTaskIndex = getTaskIndex(id)
        val newTaskIndex = getTasklistSize(newTasklistId)
        shiftTaskIndicesInSlice(oldTasklistId, oldTaskIndex + 1, Int.MAX_VALUE, -1)
        setTaskIndex(id, newTaskIndex)
        setTasklistId(id, newTasklistId)
        setTaskModifiedTime(id, System.currentTimeMillis())
    }

    fun moveTaskInList(taskId: TaskID, newTaskIndex: Int) = taskTransaction(taskId) { id ->
        val tasklistId = getTasklistId(id)
        val oldTaskIndex = getTaskIndex(id)
        if (oldTaskIndex < newTaskIndex) {
            setTimeModifiedInSlice(tasklistId, oldTaskIndex + 1, newTaskIndex + 1, System.currentTimeMillis())
            shiftTaskIndicesInSlice(tasklistId, oldTaskIndex + 1, newTaskIndex + 1, -1)
            setTaskIndex(id, newTaskIndex)
        } else if (oldTaskIndex > newTaskIndex) {
            setTimeModifiedInSlice(tasklistId, newTaskIndex, oldTaskIndex, System.currentTimeMillis())
            shiftTaskIndicesInSlice(tasklistId, newTaskIndex, oldTaskIndex, 1)
            setTaskIndex(id, newTaskIndex)
        }
        setTaskModifiedTime(id, System.currentTimeMillis())
    }

    fun setTaskTitle(taskId: TaskID, title: String) = taskTransaction(taskId) { id ->
        setTaskTitle(id, title)
        setTaskModifiedTime(id, System.currentTimeMillis())
    }

    fun setTaskDescription(taskId: TaskID, description: String) = taskTransaction(taskId) { id ->
        setTaskDescription(id, description)
        setTaskModifiedTime(id, System.currentTimeMillis())
    }

    fun setTaskTags(taskId: TaskID, tagIds: List<TagID>) {
        val tagIdsSync = CopyOnWriteArrayList(tagIds)
        transaction {
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

    fun setTaskDate(taskId: TaskID, year: Int, monthOfYear: Int, dayOfMonth: Int) =
        taskTransaction(taskId) { id ->
            setTaskDate(id, year, monthOfYear, dayOfMonth)
            setTaskModifiedTime(id, System.currentTimeMillis())
        }

    fun setTaskTime(taskId: TaskID, hour: Int, minute: Int) =
        taskTransaction(taskId) { id ->
            setTaskTime(id, hour, minute)
            setTaskModifiedTime(id, System.currentTimeMillis())
        }

    fun setTaskPeriod(taskId: TaskID, periodId: Int) = taskTransaction(taskId) { id ->
        setTaskPeriodId(id, periodId)
        setTaskModifiedTime(id, System.currentTimeMillis())
    }

    fun createNewTag(): TagID {
        val tagId = UUID.randomUUID()
        coroutineScope.launch {
            database.runInTransaction {
                val tagIndex = tagDao.getTagsCount()
                val tagEntity = TagEntity(
                    tagId = tagId,
                    tagIndex = tagIndex,
                    name = "",
                    styleIndex = 0,
                    deleted = false,
                    lastModifiedTimeMillis = System.currentTimeMillis()
                )
                tagDao.insertTag(tagEntity)
            }
        }
        return tagId
    }

    fun setTagName(tagId: TagID, name: String) = tagTransaction(tagId) { id ->
        setTagName(id, name)
        setTagLastModifiedTime(id, System.currentTimeMillis())
    }

    fun setTagStyleIndex(tagId: TagID, styleIndex: Int) = tagTransaction(tagId) { id ->
        setTagStyleIndex(id, styleIndex)
        setTagLastModifiedTime(id, System.currentTimeMillis())
    }

    fun removeTag(tagId: TagID) = tagTransaction(tagId) { id ->
        setTagDeleted(id, true)
        setTagLastModifiedTime(id, System.currentTimeMillis())
    }

    private fun transaction(block: () -> Unit) {
        coroutineScope.launch {
            database.runInTransaction(block)
        }
    }

    private fun taskTransaction(taskId: TaskID, block: TaskDao.(TaskID) -> Unit) {
        coroutineScope.launch {
            database.runInTransaction {
                taskDao.block(taskId)
            }
        }
    }

    private fun tagTransaction(tagId: TagID, block: TagDao.(TagID) -> Unit) {
        coroutineScope.launch {
            database.runInTransaction {
                tagDao.block(tagId)
            }
        }
    }
}
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

/**
 * @see [DatabaseModel] for API documentation
 */
class DatabaseModelImpl : DatabaseModel {

    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var tagDao: TagDao
    private lateinit var taskTagDao: TaskTagDao

    @Volatile
    private lateinit var appCtx: Context
    @Volatile
    private var onSyncUpdateListener: suspend (DatabaseSnapshot) -> Unit = {}

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        (throwable as? Exception)?.printStackTrace()
    }
    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatcher + exceptionHandler)

    override fun init(appCtx: Context, onSyncUpdateListener: suspend (DatabaseSnapshot) -> Unit) {
        this.onSyncUpdateListener = onSyncUpdateListener
        this.appCtx = appCtx
        coroutineScope.launch {
            database = Room.databaseBuilder(appCtx, AppDatabase::class.java, "mirage_todolist_db")
                .fallbackToDestructiveMigration()
                .build()
            taskDao = database.getTaskDao()
            tagDao = database.getTagDao()
            taskTagDao = database.getTaskTagDao()
            val allTasks = taskDao.getAllTasks()
            val allTags = tagDao.getAllTags()
            val allRelations = taskTagDao.getAllRelations()
            val snapshot = DatabaseSnapshot(allTasks, allTags, allRelations)
            onSyncUpdateListener(snapshot)
        }
    }

    override fun createNewTask(tasklistId: Int): TaskID {
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

    override fun removeTask(taskId: TaskID) = taskTransaction(taskId) { id ->
        setTasklistId(id, -1)
        setTaskModifiedTime(id, System.currentTimeMillis())
    }

    override fun moveTask(taskId: TaskID, newTasklistId: Int) = taskTransaction(taskId) { id ->
        val oldTasklistId = getTasklistId(id)
        val oldTaskIndex = getTaskIndex(id)
        val newTaskIndex = getTasklistSize(newTasklistId)
        shiftTaskIndicesInSlice(oldTasklistId, oldTaskIndex + 1, Int.MAX_VALUE, -1)
        setTaskIndex(id, newTaskIndex)
        setTasklistId(id, newTasklistId)
        setTaskModifiedTime(id, System.currentTimeMillis())
    }

    override fun moveTaskInList(taskId: TaskID, newTaskIndex: Int) = taskTransaction(taskId) { id ->
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

    override fun setTaskTitle(taskId: TaskID, title: String) = taskTransaction(taskId) { id ->
        setTaskTitle(id, title)
        setTaskModifiedTime(id, System.currentTimeMillis())
    }

    override fun setTaskDescription(taskId: TaskID, description: String) = taskTransaction(taskId) { id ->
        setTaskDescription(id, description)
        setTaskModifiedTime(id, System.currentTimeMillis())
    }

    override fun setTaskTags(taskId: TaskID, tagIds: List<TagID>) {
        val tagIdsSync = CopyOnWriteArrayList(tagIds)
        transaction {
            val taskIdFirst = taskId.mostSignificantBits
            val taskIdLast = taskId.leastSignificantBits
            tagIdsSync.forEach { tagId ->
                val tagIdFirst = tagId.mostSignificantBits
                val tagIdLast = tagId.leastSignificantBits
                val relationsCount = taskTagDao.checkRelation(taskId, tagId)
                if (relationsCount == 0) {
                    val relation = TaskTagEntity(taskId, tagId, false, System.currentTimeMillis())
                    taskTagDao.insertRelation(relation)
                }
                else {
                    taskTagDao.restoreRelation(taskId, tagId)
                    taskTagDao.setRelationModifiedTime(taskId, tagId, System.currentTimeMillis())
                }
            }
        }
    }

    override fun setTaskDate(taskId: TaskID, year: Int, monthOfYear: Int, dayOfMonth: Int) =
        taskTransaction(taskId) { id ->
        setTaskDate(id, year, monthOfYear, dayOfMonth)
        setTaskModifiedTime(id, System.currentTimeMillis())
    }

    override fun setTaskTime(taskId: TaskID, hour: Int, minute: Int) =
        taskTransaction(taskId) { id ->
            setTaskTime(id, hour, minute)
            setTaskModifiedTime(id, System.currentTimeMillis())
        }

    override fun setTaskPeriod(taskId: TaskID, periodId: Int) = taskTransaction(taskId) { id ->
        setTaskPeriodId(id, periodId)
        setTaskModifiedTime(id, System.currentTimeMillis())
    }

    override fun createNewTag(): TagID {
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

    override fun setTagName(tagId: TagID, name: String) = tagTransaction(tagId) { id ->
        setTagName(id, name)
        setTagLastModifiedTime(id, System.currentTimeMillis())
    }

    override fun setTagStyleIndex(tagId: TagID, styleIndex: Int) = tagTransaction(tagId) { id ->
        setTagStyleIndex(id, styleIndex)
        setTagLastModifiedTime(id, System.currentTimeMillis())
    }

    override fun removeTag(tagId: TagID) = tagTransaction(tagId) { id ->
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
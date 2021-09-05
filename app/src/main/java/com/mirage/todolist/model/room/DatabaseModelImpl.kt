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
    private val coroutineScope = CoroutineScope(Job() + dispatcher)

    override fun init(appCtx: Context, onSyncUpdateListener: suspend (DatabaseSnapshot) -> Unit) {
        this.onSyncUpdateListener = onSyncUpdateListener
        this.appCtx = appCtx
        coroutineScope.launch {
            database = Room.databaseBuilder(appCtx, AppDatabase::class.java, "mirage_todolist_db").build()
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
                    taskIdFirst = taskId.mostSignificantBits,
                    taskIdLast = taskId.leastSignificantBits,
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

    override fun removeTask(taskId: TaskID) = taskTransaction(taskId) { idFirst, idLast ->
        setTasklistId(idFirst, idLast, -1)
        setTaskModifiedTime(idFirst, idLast, System.currentTimeMillis())
    }

    override fun moveTask(taskId: TaskID, newTasklistId: Int) = taskTransaction(taskId) { idFirst, idLast ->
        val oldTasklistId = getTasklistId(idFirst, idLast)
        val oldTaskIndex = getTaskIndex(idFirst, idLast)
        val newTaskIndex = getTasklistSize(newTasklistId)
        shiftTaskIndicesInSlice(oldTasklistId, oldTaskIndex + 1, Int.MAX_VALUE, -1)
        setTaskIndex(idFirst, idLast, newTaskIndex)
        setTasklistId(idFirst, idLast, newTasklistId)
        setTaskModifiedTime(idFirst, idLast, System.currentTimeMillis())
    }

    override fun moveTaskInList(taskId: TaskID, newTaskIndex: Int) = taskTransaction(taskId) { idFirst, idLast ->
        val tasklistId = getTasklistId(idFirst, idLast)
        val oldTaskIndex = getTaskIndex(idFirst, idLast)
        if (oldTaskIndex < newTaskIndex) {
            setTimeModifiedInSlice(tasklistId, oldTaskIndex + 1, newTaskIndex + 1, System.currentTimeMillis())
            shiftTaskIndicesInSlice(tasklistId, oldTaskIndex + 1, newTaskIndex + 1, -1)
            setTaskIndex(idFirst, idLast, newTaskIndex)
        } else if (oldTaskIndex > newTaskIndex) {
            setTimeModifiedInSlice(tasklistId, newTaskIndex, oldTaskIndex, System.currentTimeMillis())
            shiftTaskIndicesInSlice(tasklistId, newTaskIndex, oldTaskIndex, 1)
            setTaskIndex(idFirst, idLast, newTaskIndex)
        }
        setTaskModifiedTime(idFirst, idLast, System.currentTimeMillis())
    }

    override fun setTaskTitle(taskId: TaskID, title: String) = taskTransaction(taskId) { idFirst, idLast ->
        setTaskTitle(idFirst, idLast, title)
        setTaskModifiedTime(idFirst, idLast, System.currentTimeMillis())
    }

    override fun setTaskDescription(taskId: TaskID, description: String) = taskTransaction(taskId) { idFirst, idLast ->
        setTaskDescription(idFirst, idLast, description)
        setTaskModifiedTime(idFirst, idLast, System.currentTimeMillis())
    }

    override fun setTaskTags(taskId: TaskID, tagIds: List<TagID>) {
        val tagIdsSync = CopyOnWriteArrayList(tagIds)
        transaction {
            val taskIdFirst = taskId.mostSignificantBits
            val taskIdLast = taskId.leastSignificantBits
            tagIdsSync.forEach { tagId ->
                val tagIdFirst = tagId.mostSignificantBits
                val tagIdLast = tagId.leastSignificantBits
                val relationsCount = taskTagDao.checkRelation(taskIdFirst, taskIdLast, tagIdFirst, tagIdLast)
                if (relationsCount == 0) {
                    val relation = TaskTagEntity(taskIdFirst, taskIdLast, tagIdFirst, tagIdLast, false, System.currentTimeMillis())
                    taskTagDao.insertRelation(relation)
                }
                else {
                    taskTagDao.restoreRelation(taskIdFirst, taskIdLast, tagIdFirst, tagIdLast)
                    taskTagDao.setRelationModifiedTime(taskIdFirst, taskIdLast, tagIdFirst, tagIdLast, System.currentTimeMillis())
                }
            }
        }
    }

    override fun setTaskDate(taskId: TaskID, year: Int, monthOfYear: Int, dayOfMonth: Int) =
        taskTransaction(taskId) { idFirst, idLast ->
        setTaskDate(idFirst, idLast, year, monthOfYear, dayOfMonth)
        setTaskModifiedTime(idFirst, idLast, System.currentTimeMillis())
    }

    override fun setTaskTime(taskId: TaskID, hour: Int, minute: Int) =
        taskTransaction(taskId) { idFirst, idLast ->
            setTaskTime(idFirst, idLast, hour, minute)
            setTaskModifiedTime(idFirst, idLast, System.currentTimeMillis())
        }

    override fun setTaskPeriod(taskId: TaskID, periodId: Int) = taskTransaction(taskId) { idFirst, idLast ->
        setTaskPeriodId(idFirst, idLast, periodId)
        setTaskModifiedTime(idFirst, idLast, System.currentTimeMillis())
    }

    override fun createNewTag(): TagID {
        val tagId = UUID.randomUUID()
        coroutineScope.launch {
            database.runInTransaction {
                val tagIndex = tagDao.getTagsCount()
                val tagEntity = TagEntity(
                    tagIdFirst = tagId.mostSignificantBits,
                    tagIdLast = tagId.leastSignificantBits,
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

    override fun setTagName(tagId: TagID, name: String) = tagTransaction(tagId) { idFirst, idLast ->
        setTagName(idFirst, idLast, name)
        setTagLastModifiedTime(idFirst, idLast, System.currentTimeMillis())
    }

    override fun setTagStyleIndex(tagId: TagID, styleIndex: Int) = tagTransaction(tagId) { idFirst, idLast ->
        setTagStyleIndex(idFirst, idLast, styleIndex)
        setTagLastModifiedTime(idFirst, idLast, System.currentTimeMillis())
    }

    override fun removeTag(tagId: TagID) = tagTransaction(tagId) { idFirst, idLast ->
        setTagDeleted(idFirst, idLast, true)
        setTagLastModifiedTime(idFirst, idLast, System.currentTimeMillis())
    }

    private fun transaction(block: () -> Unit) {
        coroutineScope.launch {
            database.runInTransaction(block)
        }
    }

    private fun taskTransaction(taskId: TaskID, block: TaskDao.(Long, Long) -> Unit) {
        val taskIdFirst = taskId.mostSignificantBits
        val taskIdLast = taskId.leastSignificantBits
        coroutineScope.launch {
            database.runInTransaction {
                taskDao.block(taskIdFirst, taskIdLast)
            }
        }
    }

    private fun tagTransaction(tagId: TagID, block: TagDao.(Long, Long) -> Unit) {
        val tagIdFirst = tagId.mostSignificantBits
        val tagIdLast = tagId.leastSignificantBits
        coroutineScope.launch {
            database.runInTransaction {
                tagDao.block(tagIdFirst, tagIdLast)
            }
        }
    }
}
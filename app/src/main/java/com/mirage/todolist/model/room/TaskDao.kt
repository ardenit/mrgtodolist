package com.mirage.todolist.model.room

import androidx.room.*
import com.mirage.todolist.content.TasklistType

@Dao
interface TaskDao {

    @Transaction
    fun addNewTask(taskTypeIndex: Int): TaskEntity {
        val taskRowId = createTaskRow(taskTypeIndex)
        val taskId = getTaskIdByRowId(taskRowId)
        val tasklistSize = getTasklistSize(taskTypeIndex)
        setTaskIndex(taskId, tasklistSize - 1)
        return getTask(taskId)
    }

    @Query("""INSERT INTO task VALUES (:taskTypeIndex, -1, '', '')""")
    fun createTaskRow(taskTypeIndex: Int): Long

    @Query("SELECT id FROM task WHERE rowId = :rowId")
    fun getTaskIdByRowId(rowId: Long): Long

    @Query("SELECT count(*) FROM task WHERE taskTypeIndex = :taskTypeIndex")
    fun getTasklistSize(taskTypeIndex: Int): Int

    @Query("UPDATE task SET taskIndex = :taskIndex WHERE id = :taskId")
    fun setTaskIndex(taskId: Long, taskIndex: Int)

    @Query("SELECT * FROM task WHERE id = :taskId")
    fun getTask(taskId: Long): TaskEntity

    @Query("SELECT * FROM task")
    fun getAllTasks(): List<TaskEntity>

    @Query("SELECT * FROM task WHERE taskTypeIndex = :tasklistIndex")
    fun getTasklist(tasklistIndex: Int): List<TaskEntity>

    @Query("DELETE FROM task WHERE id = :taskId")
    fun removeTask(taskId: Long)

}

fun TaskDao.getArchivedTasks(): List<TaskEntity> = getTasklist(TasklistType.ARCHIVE.index)

fun TaskDao.getCurrentTasks(): List<TaskEntity> = getTasklist(TasklistType.TODO.index)

fun TaskDao.getCompletedTasks(): List<TaskEntity> = getTasklist(TasklistType.DONE.index)
package com.mirage.todolist.model.room

import androidx.room.*
import com.mirage.todolist.viewmodel.TasklistType

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

    @Query("""INSERT INTO TaskEntity VALUES (0, :taskTypeIndex, -1, '', '')""")
    fun createTaskRow(taskTypeIndex: Int): Long

    @Query("SELECT id FROM TaskEntity WHERE rowId = :rowId")
    fun getTaskIdByRowId(rowId: Long): Long

    @Query("SELECT count(*) FROM TaskEntity WHERE taskTypeIndex = :taskTypeIndex")
    fun getTasklistSize(taskTypeIndex: Int): Int

    @Query("UPDATE TaskEntity SET taskIndex = :taskIndex WHERE id = :taskId")
    fun setTaskIndex(taskId: Long, taskIndex: Int)

    @Query("SELECT * FROM TaskEntity WHERE id = :taskId")
    fun getTask(taskId: Long): TaskEntity

    @Query("SELECT * FROM TaskEntity")
    fun getAllTasks(): List<TaskEntity>

    @Query("SELECT * FROM TaskEntity WHERE taskTypeIndex = :tasklistIndex")
    fun getTasklist(tasklistIndex: Int): List<TaskEntity>

    @Query("DELETE FROM TaskEntity WHERE id = :taskId")
    fun removeTask(taskId: Long)

}

fun TaskDao.getArchivedTasks(): List<TaskEntity> = getTasklist(TasklistType.ARCHIVE.index)

fun TaskDao.getCurrentTasks(): List<TaskEntity> = getTasklist(TasklistType.TODO.index)

fun TaskDao.getCompletedTasks(): List<TaskEntity> = getTasklist(TasklistType.DONE.index)
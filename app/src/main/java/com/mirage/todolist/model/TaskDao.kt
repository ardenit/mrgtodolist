package com.mirage.todolist.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mirage.todolist.content.TasklistType

@Dao
interface TaskDao {

    @Insert
    fun insertAll(vararg tasks: Task)

    @Delete
    fun delete(task: Task)

    @Query("SELECT * FROM task")
    fun getAllTasks(): List<Task>

    @Query("SELECT * FROM task WHERE tasklistIndex = :tasklistIndex")
    fun getTasklist(tasklistIndex: Int): List<Task>

}

fun TaskDao.getArchivedTasks(): List<Task> = getTasklist(TasklistType.ARCHIVE.index)

fun TaskDao.getCurrentTasks(): List<Task> = getTasklist(TasklistType.TODO.index)

fun TaskDao.getCompletedTasks(): List<Task> = getTasklist(TasklistType.DONE.index)
package com.mirage.todolist.model.room

import androidx.room.*
import com.mirage.todolist.model.tasks.LiveTag
import com.mirage.todolist.model.tasks.LiveTask
import com.mirage.todolist.viewmodel.TasklistType
import java.util.*

@Dao
interface TaskDao {

    @Query("""
        UPDATE tasks
        SET task_index = task_index + :add
        WHERE tasklist_id = :tasklistId AND task_index >= :startIndex AND task_index < :endIndex
    """)
    fun shiftTaskIndicesInSlice(tasklistId: Int, startIndex: Int, endIndex: Int, add: Int)

    @Query("""
        UPDATE tasks
        SET last_modified = :timeModified
        WHERE tasklist_id = :tasklistId AND task_index >= :startIndex AND task_index < :endIndex
    """)
    fun setTimeModifiedInSlice(tasklistId: Int, startIndex: Int, endIndex: Int, timeModified: Long)

    @Query("SELECT count(*) FROM tasks WHERE tasklist_id = :tasklistId")
    fun getTasklistSize(tasklistId: Int): Int

    @Query("""
        UPDATE tasks
        SET task_index = :newTaskIndex
        WHERE task_id = :taskId
        """)
    fun setTaskIndex(taskId: UUID, newTaskIndex: Int)

    @Query("""
        UPDATE tasks
        SET tasklist_id = :newTasklistId
        WHERE task_id = :taskId
        """)
    fun setTasklistId(taskId: UUID, newTasklistId: Int)

    @Query("""
        SELECT task_index
        FROM tasks
        WHERE task_id = :taskId
    """)
    fun getTaskIndex(taskId: UUID): Int

    @Query("""
        SELECT tasklist_id
        FROM tasks
        WHERE task_id = :taskId
    """)
    fun getTasklistId(taskId: UUID): Int

    @Insert
    fun insertTask(task: TaskEntity)

    @Query(value = "SELECT * FROM tasks")
    fun getAllTasks(): List<TaskEntity>

    @Query("""
        UPDATE tasks
        SET title = :title
        WHERE task_id = :taskId
        """)
    fun setTaskTitle(taskId: UUID, title: String)

    @Query("""
        UPDATE tasks
        SET description = :description
        WHERE task_id = :taskId
        """)
    fun setTaskDescription(taskId: UUID, description: String)

    @Query("""
        UPDATE tasks
        SET date_year = :year, date_month = :monthOfYear, date_day = :dayOfMonth
        WHERE task_id = :taskId
        """)
    fun setTaskDate(taskId: UUID, year: Int, monthOfYear: Int, dayOfMonth: Int)

    @Query("""
        UPDATE tasks
        SET time_hour = :hour, time_minute = :minute
        WHERE task_id = :taskId
        """)
    fun setTaskTime(taskId: UUID, hour: Int, minute: Int)

    @Query("""
        UPDATE tasks
        SET period_id = :periodId
        WHERE task_id = :taskId
        """)
    fun setTaskPeriodId(taskId: UUID, periodId: Int)

    @Query("""
        UPDATE tasks
        SET last_modified = :modifiedTimeMillis
        WHERE task_id = :taskId
        """)
    fun setTaskModifiedTime(taskId: UUID, modifiedTimeMillis: Long)
}
package com.mirage.todolist.model.room

import androidx.room.*
import com.mirage.todolist.model.tasks.LiveTag
import com.mirage.todolist.model.tasks.LiveTask
import com.mirage.todolist.viewmodel.TasklistType

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
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast
        """)
    fun setTaskIndex(taskIdFirst: Long, taskIdLast: Long, newTaskIndex: Int)

    @Query("""
        UPDATE tasks
        SET tasklist_id = :newTasklistId
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast
        """)
    fun setTasklistId(taskIdFirst: Long, taskIdLast: Long, newTasklistId: Int)

    @Query("""
        SELECT task_index
        FROM tasks
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast
    """)
    fun getTaskIndex(taskIdFirst: Long, taskIdLast: Long): Int

    @Query("""
        SELECT tasklist_id
        FROM tasks
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast
    """)
    fun getTasklistId(taskIdFirst: Long, taskIdLast: Long): Int

    @Insert
    fun insertTask(task: TaskEntity)

    @Query(value = "SELECT * FROM tasks")
    fun getAllTasks(): List<TaskEntity>

    @Query("""
        UPDATE tasks
        SET title = :title
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast
        """)
    fun setTaskTitle(taskIdFirst: Long, taskIdLast: Long, title: String)

    @Query("""
        UPDATE tasks
        SET description = :description
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast
        """)
    fun setTaskDescription(taskIdFirst: Long, taskIdLast: Long, description: String)

    @Query("""
        UPDATE tasks
        SET date_year = :year, date_month = :monthOfYear, date_day = :dayOfMonth
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast
        """)
    fun setTaskDate(taskIdFirst: Long, taskIdLast: Long, year: Int, monthOfYear: Int, dayOfMonth: Int)

    @Query("""
        UPDATE tasks
        SET time_hour = :hour, time_minute = :minute
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast
        """)
    fun setTaskTime(taskIdFirst: Long, taskIdLast: Long, hour: Int, minute: Int)

    @Query("""
        UPDATE tasks
        SET period_id = :periodId
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast
        """)
    fun setTaskPeriodId(taskIdFirst: Long, taskIdLast: Long, periodId: Int)

    @Query("""
        UPDATE tasks
        SET last_modified = :modifiedTimeMillis
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast
        """)
    fun setTaskModifiedTime(taskIdFirst: Long, taskIdLast: Long, modifiedTimeMillis: Long)
}
package com.mirage.todolist.model.database

import androidx.room.*
import com.mirage.todolist.model.repository.TaskPeriod
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
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
        SET last_modified = :lastModified
        WHERE tasklist_id = :tasklistId AND task_index >= :startIndex AND task_index < :endIndex
    """)
    fun setTimeModifiedInSlice(tasklistId: Int, startIndex: Int, endIndex: Int, lastModified: Instant)

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
        SET date = :date
        WHERE task_id = :taskId
        """)
    fun setTaskDate(taskId: UUID, date: LocalDate?)

    @Query("""
        UPDATE tasks
        SET time = :time
        WHERE task_id = :taskId
        """)
    fun setTaskTime(taskId: UUID, time: LocalTime?)

    @Query("""
        UPDATE tasks
        SET period = :period
        WHERE task_id = :taskId
        """)
    fun setTaskPeriod(taskId: UUID, period: TaskPeriod)

    @Query("""
        UPDATE tasks
        SET last_modified = :lastModified
        WHERE task_id = :taskId
        """)
    fun setTaskLastModifiedTime(taskId: UUID, lastModified: Instant)

    @Query("""
        DELETE FROM tasks
        WHERE account_name = :email
    """)
    fun removeAllTasks(email: String)

    @Insert
    fun insertAllTasks(tasks: List<TaskEntity>)
}
package com.mirage.todolist.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.mirage.todolist.model.repository.TaskPeriod
import com.mirage.todolist.util.OptionalDate
import com.mirage.todolist.util.OptionalTaskLocation
import com.mirage.todolist.util.OptionalTime
import java.time.Instant
import java.util.*

@Dao
interface TaskDao {

    @Query("""
        UPDATE tasks
        SET task_index = task_index + :add
        WHERE account_name = :accountName AND tasklist_id = :tasklistId AND task_index >= :startIndex AND task_index < :endIndex
    """)
    fun shiftTaskIndicesInSlice(tasklistId: Int, startIndex: Int, endIndex: Int, add: Int, accountName: String)

    @Query("""
        UPDATE tasks
        SET last_modified = :lastModified
        WHERE account_name = :accountName AND tasklist_id = :tasklistId AND task_index >= :startIndex AND task_index < :endIndex
    """)
    fun setTimeModifiedInSlice(tasklistId: Int, startIndex: Int, endIndex: Int, lastModified: Instant, accountName: String)

    @Query("""
        SELECT count(*) FROM tasks
        WHERE account_name = :accountName AND tasklist_id = :tasklistId
        """)
    fun getTasklistSize(tasklistId: Int, accountName: String): Int

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

    @Insert(onConflict = REPLACE)
    fun insertTask(task: TaskEntity)

    @Query("""
        SELECT * FROM tasks
        WHERE task_id = :taskId
    """)
    fun getTask(taskId: UUID): TaskEntity

    @Query(value = "SELECT * FROM tasks")
    fun getAllTasks(): List<TaskEntity>

    @Query("""
        SELECT * FROM tasks
        WHERE account_name = :accountName
    """)
    fun getAllTasks(accountName: String): List<TaskEntity>

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
        SET location = :location
        WHERE task_id = :taskId
        """)
    fun setTaskLocation(taskId: UUID, location: OptionalTaskLocation)

    @Query("""
        UPDATE tasks
        SET date = :date
        WHERE task_id = :taskId
        """)
    fun setTaskDate(taskId: UUID, date: OptionalDate)

    @Query("""
        UPDATE tasks
        SET time = :time
        WHERE task_id = :taskId
        """)
    fun setTaskTime(taskId: UUID, time: OptionalTime)

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
        WHERE account_name = :accountName
    """)
    fun removeAllTasks(accountName: String)

    @Query("""
        DELETE FROM tasks
        WHERE 1
    """)
    fun clear()

    @Insert(onConflict = REPLACE)
    fun insertAllTasks(tasks: List<TaskEntity>)
}
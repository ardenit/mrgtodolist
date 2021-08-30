package com.mirage.todolist.model.room

import androidx.room.*
import com.mirage.todolist.model.tasks.LiveTag
import com.mirage.todolist.model.tasks.LiveTask
import com.mirage.todolist.viewmodel.TasklistType

@Dao
interface TaskDao {

    /*
    @Transaction
    fun addNewTask(taskTypeIndex: Int): TaskEntity {
        val taskRowId = createTaskRow(taskTypeIndex)
        val taskId = getTaskIdByRowId(taskRowId)
        val tasklistSize = getTasklistSize(taskTypeIndex)
        setTaskIndex(taskId, tasklistSize - 1)
        return getTask(taskId)
    }*/

    @Transaction
    fun moveTask(
        taskIdFirst: Long,
        taskIdLast: Long,
        oldTasklistId: Int,
        newTasklistId: Int,
        oldTaskIndex: Int,
        newTaskIndex: Int
    ) {
        changeTaskIndexInSlice(oldTasklistId, oldTaskIndex, Int.MAX_VALUE, -1)
        changeTaskIndexInSlice(newTasklistId, newTaskIndex, Int.MAX_VALUE, 1)
        setTaskTasklistId(taskIdFirst, taskIdLast, newTasklistId)
        setTaskIndex(taskIdFirst, taskIdLast, newTaskIndex)
    }

    @Transaction
    fun moveTaskInList(taskIdFirst: Long, taskIdLast: Long, tasklistId: Int, oldTaskIndex: Int, newTaskIndex: Int) {
        if (oldTaskIndex < newTaskIndex) {
            changeTaskIndexInSlice(tasklistId, oldTaskIndex + 1, newTaskIndex, -1)
        }
        else {
            changeTaskIndexInSlice(tasklistId, newTaskIndex, oldTaskIndex - 1, 1)
        }
        setTaskIndex(taskIdFirst, taskIdLast, newTaskIndex)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: TaskEntity)

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
        SET date_year = :dateYear, date_month = :dateMonth, date_day = :dateDay
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast
        """)
    fun setTaskDate(taskIdFirst: Long, taskIdLast: Long, dateYear: Int, dateMonth: Int, dateDay: Int)

    @Query("""
        UPDATE tasks
        SET time_hour = :timeHour, time_minute = :timeMinute
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast
        """)
    fun setTaskTime(taskIdFirst: Long, taskIdLast: Long, timeHour: Int, timeMinute: Int)

    @Query("""
        UPDATE tasks
        SET period_id = :periodId
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast
        """)
    fun setTaskPeriod(taskIdFirst: Long, taskIdLast: Long, periodId: Int)

    @Query("""
        UPDATE tasks
        SET task_index = task_index + :add
        WHERE tasklist_id = :tasklistId AND task_index >= :startTaskIndex AND task_index <= :endTaskIndex
        """)
    fun changeTaskIndexInSlice(tasklistId: Int, startTaskIndex: Int, endTaskIndex: Int, add: Int)
/*
    @Query("""INSERT INTO tasks VALUES (0, :taskTypeIndex, -1, '', '')""")
    fun createTaskRow(taskTypeIndex: Int): Long
*/
    @Query("SELECT task_id_first, task_id_last FROM tasks WHERE rowId = :rowId")
    fun getTaskIdByRowId(rowId: Long): LongArray

    @Query("SELECT count(*) FROM tasks WHERE tasklist_id = :tasklistId")
    fun getTasklistSize(tasklistId: Int): Int

    @Query("UPDATE tasks SET task_index = :newTaskIndex WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast")
    fun setTaskIndex(taskIdFirst: Long, taskIdLast: Long, newTaskIndex: Int)

    @Query("UPDATE tasks SET tasklist_id = :newTasklistId WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast")
    fun setTaskTasklistId(taskIdFirst: Long, taskIdLast: Long, newTasklistId: Int)

    @Query("SELECT * FROM tasks WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast")
    fun getTask(taskIdFirst: Long, taskIdLast: Long): TaskEntity

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE tasklist_id = :tasklistId")
    fun getTasklist(tasklistId: Int): List<TaskEntity>

    @Query("DELETE FROM tasks WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast")
    fun removeTask(taskIdFirst: Long, taskIdLast: Long)

}
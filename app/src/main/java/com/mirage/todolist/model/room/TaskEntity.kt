package com.mirage.todolist.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks", primaryKeys = ["task_id_first", "task_id_last"])
data class TaskEntity(
    /** Most significant bits of task's unique ID */
    @ColumnInfo(name = "task_id_first")
    val taskIdFirst: Long,
    /** Least significant bits of task's unique ID */
    @ColumnInfo(name = "task_id_last")
    val taskIdLast: Long,
    /**
     * Index of the task's tasklist
     * */
    @ColumnInfo(name = "tasklist_id")
    val tasklistId: Int,
    /**
     * Task's index in its current tasklist
     * Each tasklist MUST contain all the indices from 0 to (size - 1)
     * That's an invariant that persists after every transaction
     */
    @ColumnInfo(name = "task_index")
    val taskIndex: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "date_year")
    val dateYear: Int,
    @ColumnInfo(name = "date_month")
    val dateMonth: Int,
    @ColumnInfo(name = "date_day")
    val dateDay: Int,
    @ColumnInfo(name = "time_hour")
    val timeHour: Int,
    @ColumnInfo(name = "time_minute")
    val timeMinute: Int,
    @ColumnInfo(name = "period_id")
    val periodId: Int
)
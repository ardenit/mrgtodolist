package com.mirage.todolist.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "tasks")
data class TaskEntity(
    /** Task's unique ID */
    @PrimaryKey
    @ColumnInfo(name = "task_id")
    val taskId: UUID,
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
    val title: String = "",
    @ColumnInfo(name = "description")
    val description: String = "",
    @ColumnInfo(name = "date_year")
    val dateYear: Int = -1,
    @ColumnInfo(name = "date_month")
    val dateMonth: Int = -1,
    @ColumnInfo(name = "date_day")
    val dateDay: Int = -1,
    @ColumnInfo(name = "time_hour")
    val timeHour: Int = -1,
    @ColumnInfo(name = "time_minute")
    val timeMinute: Int = -1,
    @ColumnInfo(name = "period_id")
    val periodId: Int = 0,
    @ColumnInfo(name = "last_modified")
    val lastModifiedTimeMillis: Long = System.currentTimeMillis()
)
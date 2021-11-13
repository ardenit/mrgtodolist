package com.mirage.todolist.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mirage.todolist.model.repository.TaskPeriod
import com.mirage.todolist.util.OptionalDate
import com.mirage.todolist.util.OptionalTaskLocation
import com.mirage.todolist.util.OptionalTime
import java.time.Clock
import java.time.Instant
import java.util.*

@Entity(
    tableName = "tasks",
    indices = [Index(value = arrayOf("account_name"), unique = false)]
)
data class TaskEntity(
    /** Task's unique ID */
    @PrimaryKey
    @ColumnInfo(name = "task_id")
    val taskId: UUID,
    @ColumnInfo(name = "account_name")
    val accountName: String = "",
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
    @ColumnInfo(name = "location")
    val location: OptionalTaskLocation = OptionalTaskLocation.NOT_SET,
    @ColumnInfo(name = "date")
    val date: OptionalDate = OptionalDate.NOT_SET,
    @ColumnInfo(name = "time")
    val time: OptionalTime = OptionalTime.NOT_SET,
    @ColumnInfo(name = "period")
    val period: TaskPeriod = TaskPeriod.NOT_REPEATABLE,
    @ColumnInfo(name = "last_modified")
    val lastModified: Instant = Clock.systemUTC().instant()
)
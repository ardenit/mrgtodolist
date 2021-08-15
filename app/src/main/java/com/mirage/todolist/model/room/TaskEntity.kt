package com.mirage.todolist.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TaskEntity(
    /** Unique ID of the task. Persists through moving task to another list or changing its position */
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    /**
     * Index of the task's type
     * @see [com.mirage.todolist.content.TasklistType]
     * */
    @ColumnInfo(defaultValue = "1")
    val taskTypeIndex: Int,
    /**
     * Task's index in its current tasklist
     * Each tasklist MUST contain all the indices from 0 to (size - 1)
     * That's an invariant that persists after every transaction
     */
    val taskIndex: Int,
    @ColumnInfo(defaultValue = "Title")
    val title: String,
    @ColumnInfo(defaultValue = "No description")
    val description: String
)
package com.mirage.todolist.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    @PrimaryKey val id: Long,
    val tasklistIndex: Int,
    val title: String,
    val description: String
)
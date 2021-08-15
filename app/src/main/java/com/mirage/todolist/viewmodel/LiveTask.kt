package com.mirage.todolist.viewmodel

import androidx.lifecycle.LiveData
import java.util.*

typealias TaskID = UUID

interface LiveTask {
    val taskID: TaskID
    val tasklistID: LiveData<Int>
    val taskIndex: LiveData<Int>
    val title: LiveData<String>
    val description: LiveData<String>
}
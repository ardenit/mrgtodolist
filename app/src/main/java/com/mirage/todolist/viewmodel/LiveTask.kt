package com.mirage.todolist.viewmodel

import androidx.lifecycle.LiveData

interface LiveTask {
    val taskID: Long
    val tasklistID: LiveData<Int>
    val taskIndex: LiveData<Int>
    val title: LiveData<String>
    val description: LiveData<String>
}
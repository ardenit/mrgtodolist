package com.mirage.todolist.content

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mirage.todolist.model.Task

/**
 * View model of a single tasklist page
 */
class TasklistViewModel : ViewModel() {

    val type = MutableLiveData<TasklistType>()
    val taskCount = MutableLiveData<Int>()
    val tasks: MutableList<MutableLiveData<Task>> = ArrayList()

    fun removeTask(index: Int) {
        if (index !in tasks.indices) return
        tasks.removeAt(index)

    }

}
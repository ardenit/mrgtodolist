package com.mirage.todolist.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mirage.todolist.content.TasklistType
import com.mirage.todolist.model.room.TaskEntity

/**
 * View model of a single tasklist page
 */
class TasklistViewModelImpl : TasklistViewModel() {

    val type = MutableLiveData<TasklistType>()
    val taskCount = MutableLiveData<Int>()
    val tasks: MutableList<MutableLiveData<TaskEntity>> = ArrayList()

    fun removeTask(index: Int) {
        if (index !in tasks.indices) return
        tasks.removeAt(index)

    }

    override fun addOnNewTaskListener(onNewTask: (LiveTask) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun addOnFullTasklistUpdateListener(onFullUpdate: (List<LiveTask>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getTask(taskID: Long): LiveTask? {
        TODO("Not yet implemented")
    }

    override fun getTaskCount(): Int {
        return 5
    }
}
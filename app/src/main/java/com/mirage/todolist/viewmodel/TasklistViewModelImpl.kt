package com.mirage.todolist.viewmodel

import androidx.lifecycle.MutableLiveData
import com.mirage.todolist.model.room.TaskEntity

/**
 * View model of a single tasklist page
 */
class TasklistViewModelImpl : TasklistViewModel() {



    override fun addOnNewTaskListener(onNewTask: (LiveTask) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun addOnFullTasklistUpdateListener(onFullUpdate: (List<LiveTask>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getTask(taskID: Long): LiveTask? {
        TODO("Not yet implemented")
    }

    override fun getTaskByIndex(taskIndex: Int): LiveTask? {
        TODO("Not yet implemented")
    }

    override fun getTaskCount(): Int {
        return 50
    }
}
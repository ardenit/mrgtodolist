package com.mirage.todolist.viewmodel

import androidx.lifecycle.MutableLiveData

class MutableLiveTask(
    override val taskID: TaskID,
    override var tasklistID: Int,
    override var taskIndex: Int,
    title: String,
    description: String
) : LiveTask {

    override val title: MutableLiveData<String> = MutableLiveData(title)
    override val description: MutableLiveData<String> = MutableLiveData(description)

}
package com.mirage.todolist.viewmodel

import androidx.lifecycle.MutableLiveData

class MutableLiveTask(
    override val taskID: TaskID,
    tasklistID: Int,
    taskIndex: Int,
    title: String,
    description: String
) : LiveTask {

    override val tasklistID: MutableLiveData<Int> = MutableLiveData(tasklistID)
    override val taskIndex: MutableLiveData<Int> = MutableLiveData(taskIndex)
    override val title: MutableLiveData<String> = MutableLiveData(title)
    override val description: MutableLiveData<String> = MutableLiveData(description)

}
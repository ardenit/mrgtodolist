package com.mirage.todolist.model.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Mutable implementation of [LiveTask] used only in the model
 */
class MutableLiveTask(
    override val taskID: TaskID,
    override var tasklistID: Int,
    override var taskIndex: Int,
    override var isVisible: Boolean,
    title: String,
    description: String,
    tags: List<LiveTag>
) : LiveTask {

    override val title: MutableLiveData<String> = MutableLiveData(title)
    override val description: MutableLiveData<String> = MutableLiveData(description)
    override val tags: MutableLiveData<List<LiveTag>> = MutableLiveData(tags)
}
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
    tags: List<LiveTag> = listOf(),
    date: TaskDate = TaskDate(-1, -1, -1),
    time: TaskTime = TaskTime(-1, -1),
    period: TaskPeriod = TaskPeriod.NOT_REPEATABLE
) : LiveTask {

    override val title: MutableLiveData<String> = MutableLiveData(title)
    override val description: MutableLiveData<String> = MutableLiveData(description)
    override val tags: MutableLiveData<List<LiveTag>> = MutableLiveData(tags)
    override val date: MutableLiveData<TaskDate> = MutableLiveData(date)
    override val time: MutableLiveData<TaskTime> = MutableLiveData(time)
    override val period: MutableLiveData<TaskPeriod> = MutableLiveData(period)
}
package com.mirage.todolist.model.repository

import androidx.lifecycle.MutableLiveData
import com.mirage.todolist.util.OptionalDate
import com.mirage.todolist.util.OptionalTime
import java.util.*

/**
 * Mutable implementation of [LiveTask] used only in the repository
 */
class MutableLiveTask(
    override val taskId: UUID,
    override var tasklistId: Int,
    override var taskIndex: Int,
    title: String,
    description: String,
    date: OptionalDate = OptionalDate.NOT_SET,
    time: OptionalTime = OptionalTime.NOT_SET,
    period: TaskPeriod = TaskPeriod.NOT_REPEATABLE,
    tags: List<LiveTag> = listOf(),
    override var isVisible: Boolean
) : LiveTask {

    override val title: MutableLiveData<String> = MutableLiveData(title)
    override val description: MutableLiveData<String> = MutableLiveData(description)
    override val date: MutableLiveData<OptionalDate> = MutableLiveData(date)
    override val time: MutableLiveData<OptionalTime> = MutableLiveData(time)
    override val period: MutableLiveData<TaskPeriod> = MutableLiveData(period)
    override val tags: MutableLiveData<List<LiveTag>> = MutableLiveData(tags)
}
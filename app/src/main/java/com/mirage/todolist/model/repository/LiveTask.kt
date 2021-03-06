package com.mirage.todolist.model.repository

import androidx.lifecycle.LiveData
import com.mirage.todolist.util.OptionalDate
import com.mirage.todolist.util.OptionalTaskLocation
import com.mirage.todolist.util.OptionalTime
import java.util.*

interface LiveTask {
    /** Unique ID of the task. Never changes during task lifecycle */
    val taskId: UUID
    /**
     * ID of the tasklist this task is currently in.
     * May be changed by the model, observed by [com.mirage.todolist.model.repository.TodoRepository.addOnMoveTaskListener]
     * */
    val tasklistId: Int
    /** Current actual index of this task in its tasklist. May be changed by the model, can't be observed */
    val taskIndex: Int
    /** Title of the task. Recycler items should observe this data and react to it */
    val title: LiveData<String>
    /** Description of the task. Recycler items should observe this data and react to it */
    val description: LiveData<String>
    /** Location of the task using Google Maps API */
    val location: LiveData<OptionalTaskLocation>
    /** Date, time and period of the task. Null if not set. */
    val date: LiveData<OptionalDate>
    val time: LiveData<OptionalTime>
    val period: LiveData<TaskPeriod>
    /**
     * List of tags connected to this task.
     * The list itself is immutable and will not be changed, so this data can be easily observed.
     * */
    val tags: LiveData<List<LiveTag>>
    /**
     * Client-specific information about whether the task should be visible in the tasklist
     * (it may be temporarily hidden due to filtering by search query).
     */
    val isVisible: Boolean
}
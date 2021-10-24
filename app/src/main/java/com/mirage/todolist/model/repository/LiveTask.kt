package com.mirage.todolist.model.repository

import androidx.lifecycle.LiveData
import java.time.LocalDate
import java.time.LocalTime
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
    /** Date, time and period of the task. Null if not set. */
    val date: LiveData<LocalDate?>
    val time: LiveData<LocalTime?>
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
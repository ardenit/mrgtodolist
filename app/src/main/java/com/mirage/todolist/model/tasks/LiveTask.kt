package com.mirage.todolist.model.tasks

import androidx.lifecycle.LiveData
import java.util.*

typealias TaskID = UUID

interface LiveTask {
    /** Unique ID of the task. Never changes during task lifecycle */
    val taskID: TaskID
    /**
     * ID of the tasklist this task is currently in.
     * May be changed by the model, observed by [com.mirage.todolist.model.TodolistModel.addOnMoveTaskListener]
     * */
    val tasklistID: Int
    /** Current index of this task in its tasklist. May be changed by the model, can't be observed */
    val taskIndex: Int
    /** Title of the task. Recycler items should observe this data and react to it */
    val title: LiveData<String>
    /** Description of the task. Recycler items should observe this data and react to it */
    val description: LiveData<String>
    /**
     * List of tags connected to this task.
     * The list itself is immutable and will not be changed, so this data can be easily observed.
     * */
    val tags: LiveData<List<LiveTag>>
}
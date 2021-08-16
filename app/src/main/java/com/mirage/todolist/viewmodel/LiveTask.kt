package com.mirage.todolist.viewmodel

import androidx.lifecycle.LiveData
import java.util.*

typealias TaskID = UUID

interface LiveTask {
    /** Unique ID of the task. Never changes during task lifecycle */
    val taskID: TaskID
    /** ID of the tasklist this task is currently in. May be changed by the model, observed by [OnMoveTaskListener] */
    val tasklistID: Int
    /** Current index of this task in its tasklist. May be changed by the model, can't be observed */
    val taskIndex: Int
    /** Title of the task. Recycler items should observe this data and react to it */
    val title: LiveData<String>
    /** Description of the task. Recycler items should observe this data and react to it */
    val description: LiveData<String>
}
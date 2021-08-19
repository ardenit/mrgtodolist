package com.mirage.todolist.model.tasks

import androidx.lifecycle.LiveData
import java.util.*

typealias TagID = UUID

interface LiveTag {
    /** Unique ID of the tag. Never changes during tag lifecycle */
    val tagID: TagID
    /** Index of the tag in tag list. May be changed by the model, can't be observed */
    val tagIndex: Int
    /** Name of the tag. Recycler items should observe this data and react to it */
    val name: LiveData<String>
}
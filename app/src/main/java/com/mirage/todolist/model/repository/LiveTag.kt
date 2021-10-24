package com.mirage.todolist.model.repository

import androidx.lifecycle.LiveData
import java.util.*

interface LiveTag {
    /** Unique ID of the tag. Never changes during tag lifecycle */
    val tagID: UUID
    /** Index of the tag in tag list. May be changed by the model, can't be observed */
    val tagIndex: Int
    /** Name of the tag. Recycler items should observe this data and react to it */
    val name: LiveData<String>
    /** Index of the tag's style in tag style list. Recycler items should observe this data and react to it */
    val styleIndex: LiveData<Int>
}
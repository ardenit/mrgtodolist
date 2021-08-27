package com.mirage.todolist.model.tasks

import androidx.annotation.ColorInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Mutable implementation of [LiveTag] used only in the model
 */
class MutableLiveTag(
    override val tagID: TagID,
    override var tagIndex: Int,
    name: String,
    styleIndex: Int
) : LiveTag {

    override val name: MutableLiveData<String> = MutableLiveData(name)
    override val styleIndex: MutableLiveData<Int> = MutableLiveData(styleIndex)
}
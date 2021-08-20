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
    @ColorInt color: Int,
    @ColorInt textColor: Int
) : LiveTag {

    override val name: LiveData<String> = MutableLiveData(name)
    override val color: LiveData<Int> = MutableLiveData(color)
    override val textColor: LiveData<Int> = MutableLiveData(textColor)
}
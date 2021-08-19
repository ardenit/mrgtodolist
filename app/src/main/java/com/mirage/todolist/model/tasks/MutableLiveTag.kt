package com.mirage.todolist.model.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Mutable implementation of [LiveTag] used only in the model
 */
class MutableLiveTag(
    override val tagID: TagID,
    override var tagIndex: Int,
    name: String
) : LiveTag {

    override val name: LiveData<String> = MutableLiveData(name)
}
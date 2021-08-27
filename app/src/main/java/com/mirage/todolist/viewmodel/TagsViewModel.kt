package com.mirage.todolist.viewmodel

import androidx.annotation.ColorInt
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.mirage.todolist.model.tasks.*

abstract class TagsViewModel : ViewModel() {

    abstract fun init()

    abstract fun createNewTag(): LiveTag

    abstract fun removeTag(tagID: TagID)

    abstract fun modifyTag(
        tagID: TagID,
        newName: String? = null,
        newStyleIndex: Int? = null
    )

    abstract fun getAllTags(): Map<TagID, LiveTag>

    abstract fun addOnNewTagListener(owner: LifecycleOwner, listener: OnNewTagListener)

    abstract fun addOnRemoveTagListener(owner: LifecycleOwner, listener: OnRemoveTagListener)

    abstract fun addOnFullUpdateTagListener(owner: LifecycleOwner, listener: OnFullUpdateTagListener)
}
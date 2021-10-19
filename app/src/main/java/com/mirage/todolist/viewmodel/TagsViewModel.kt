package com.mirage.todolist.viewmodel

import androidx.annotation.ColorInt
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mirage.todolist.model.dagger.App
import com.mirage.todolist.model.tasks.*
import javax.inject.Inject

class TagsViewModel
@Inject constructor(
    private val todolistModel: TodolistModel
) : ViewModel() {

    private lateinit var onNewTagListener: OnNewTagListener
    private lateinit var onRemoveTagListener: OnRemoveTagListener
    private lateinit var onFullUpdateTagListener: OnFullUpdateTagListener

    private val newTagObservable = MutableLiveData<LiveTag>()
    private val removeTagObservable = MutableLiveData<Pair<LiveTag, Int>>()
    private val fullUpdateObservable = MutableLiveData<Map<TagID, LiveTag>>()

    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true
        onNewTagListener = { newTag ->
            newTagObservable.value = newTag
        }
        onRemoveTagListener = { tag, tagIndex ->
            removeTagObservable.value = Pair(tag, tagIndex)
        }
        onFullUpdateTagListener = { tags ->
            fullUpdateObservable.value = tags
        }
        todolistModel.addOnNewTagListener(onNewTagListener)
        todolistModel.addOnRemoveTagListener(onRemoveTagListener)
        todolistModel.addOnFullUpdateTagListener(onFullUpdateTagListener)
    }

    fun createNewTag(): LiveTag {
        return todolistModel.createNewTag()
    }

    fun removeTag(tagID: TagID) {
        todolistModel.removeTag(tagID)
    }

    fun modifyTag(tagID: TagID, newName: String? = null, newStyleIndex: Int? = null) {
        todolistModel.modifyTag(tagID, newName, newStyleIndex)
    }

    fun getAllTags(): Map<TagID, LiveTag> {
        return todolistModel.getAllTags()
    }

    fun addOnNewTagListener(owner: LifecycleOwner, listener: OnNewTagListener) {
        newTagObservable.observe(owner, listener)
    }

    fun addOnRemoveTagListener(owner: LifecycleOwner, listener: OnRemoveTagListener) {
        removeTagObservable.observe(owner) { (tag, tagIndex) ->
            listener(tag, tagIndex)
        }
    }

    fun addOnFullUpdateTagListener(owner: LifecycleOwner, listener: OnFullUpdateTagListener) {
        fullUpdateObservable.observe(owner, listener)
    }

    override fun onCleared() {
        todolistModel.removeOnNewTagListener(onNewTagListener)
        todolistModel.removeOnRemoveTagListener(onRemoveTagListener)
        todolistModel.removeOnFullUpdateTagListener(onFullUpdateTagListener)
    }
}
package com.mirage.todolist.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mirage.todolist.model.tasks.*
import javax.inject.Inject

class TagsViewModel
@Inject constructor(
    private val todolistModel: TodolistModel
) : ViewModel() {

    private lateinit var onFullUpdateTagListener: OnFullUpdateTagListener

    private val newTagObservable = MutableLiveData<LiveTag>()
    private val removeTagObservable = MutableLiveData<LiveTag>()
    private val fullUpdateObservable = MutableLiveData<Map<TagID, LiveTag>>()

    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true
        onRemoveTagListener = { tag ->
            removeTagObservable.value = tag
        }
        onFullUpdateTagListener = { tags ->
            fullUpdateObservable.value = tags
        }
        todolistModel.addOnFullUpdateTagListener(onFullUpdateTagListener)
    }

    fun createNewTag(): LiveTag {
        val newTag = todolistModel.createNewTag()
        newTagObservable.value = newTag
        return newTag
    }

    fun removeTag(tag: LiveTag) {
        todolistModel.removeTag(tag.tagID)
        removeTagObservable.value = tag
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
        todolistModel.removeOnFullUpdateTagListener(onFullUpdateTagListener)
    }
}
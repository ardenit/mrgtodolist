package com.mirage.todolist.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.mirage.todolist.model.tasks.*

class TagsViewModelImpl : TagsViewModel() {

    private lateinit var onNewTagListener: OnNewTagListener
    private lateinit var onRemoveTagListener: OnRemoveTagListener
    private lateinit var onFullUpdateTagListener: OnFullUpdateTagListener

    private val newTagObservable = MutableLiveData<LiveTag>()
    private val removeTagObservable = MutableLiveData<Pair<LiveTag, Int>>()
    private val fullUpdateObservable = MutableLiveData<Map<TagID, LiveTag>>()

    private var initialized = false

    //TODO Inject
    private val todolistModel: TodolistModel = getTodolistModel()

    override fun init() {
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

    override fun createNewTag(): LiveTag {
        return todolistModel.createNewTag()
    }

    override fun removeTag(tagID: TagID) {
        todolistModel.removeTag(tagID)
    }

    override fun modifyTag(tagID: TagID, newName: String?, newStyleIndex: Int?) {
        todolistModel.modifyTag(tagID, newName, newStyleIndex)
    }

    override fun getAllTags(): Map<TagID, LiveTag> {
        return todolistModel.getAllTags()
    }

    override fun addOnNewTagListener(owner: LifecycleOwner, listener: OnNewTagListener) {
        newTagObservable.observe(owner, listener)
    }

    override fun addOnRemoveTagListener(owner: LifecycleOwner, listener: OnRemoveTagListener) {
        removeTagObservable.observe(owner) { (tag, tagIndex) ->
            listener(tag, tagIndex)
        }
    }

    override fun addOnFullUpdateTagListener(owner: LifecycleOwner, listener: OnFullUpdateTagListener) {
        fullUpdateObservable.observe(owner, listener)
    }

    override fun onCleared() {
        todolistModel.removeOnNewTagListener(onNewTagListener)
        todolistModel.removeOnRemoveTagListener(onRemoveTagListener)
        todolistModel.removeOnFullUpdateTagListener(onFullUpdateTagListener)
    }
}
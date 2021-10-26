package com.mirage.todolist.ui.todolist.tags

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mirage.todolist.model.repository.*
import java.util.*
import javax.inject.Inject

class TagsViewModel @Inject constructor(
    private val todoRepository: TodoRepository
) : ViewModel() {

    private val newTagObservable = MutableLiveData<LiveTag>()
    private val removeTagObservable = MutableLiveData<LiveTag>()
    private val fullUpdateObservable = MutableLiveData<Map<UUID, LiveTag>>()

    private val onFullUpdateTagListener: OnFullUpdateTagListener = { tags ->
        fullUpdateObservable.value = tags
    }

    init {
        todoRepository.addOnFullUpdateTagListener(onFullUpdateTagListener)
    }

    fun createNewTag(): LiveTag {
        val newTag = todoRepository.createNewTag()
        newTagObservable.value = newTag
        return newTag
    }

    fun removeTag(tag: LiveTag) {
        todoRepository.removeTag(tag)
        removeTagObservable.value = tag
    }

    fun modifyTag(tagID: UUID, newName: String? = null, newStyleIndex: Int? = null) {
        todoRepository.modifyTag(tagID, newName, newStyleIndex)
    }

    fun getAllTags(): Map<UUID, LiveTag> {
        return todoRepository.getAllTags()
    }

    fun addOnNewTagListener(owner: LifecycleOwner, listener: (LiveTag) -> Unit) {
        newTagObservable.observe(owner, listener)
    }

    fun addOnRemoveTagListener(owner: LifecycleOwner, listener: (LiveTag) -> Unit) {
        removeTagObservable.observe(owner, listener)
    }

    fun addOnFullUpdateTagListener(owner: LifecycleOwner, listener: OnFullUpdateTagListener) {
        fullUpdateObservable.observe(owner, listener)
    }

    override fun onCleared() {
        todoRepository.removeOnFullUpdateTagListener(onFullUpdateTagListener)
    }
}
package com.mirage.todolist.model.repository

import androidx.lifecycle.MutableLiveData
import java.util.*

/**
 * Mutable implementation of [LiveTag] used only in the repository
 */
class MutableLiveTag(
    override val tagID: UUID,
    override var tagIndex: Int,
    name: String,
    styleIndex: Int
) : LiveTag {
    override val name: MutableLiveData<String> = MutableLiveData(name)
    override val styleIndex: MutableLiveData<Int> = MutableLiveData(styleIndex)
}
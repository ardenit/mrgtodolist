package com.mirage.todolist.model.room

import java.util.*


data class DatabaseSnapshot(
    val tasks: List<TaskEntity>,
    val tags: List<TagEntity>,
    val relations: List<RelationEntity>,
    val meta: List<VersionEntity>
) {
    fun getDataVersion(email: String) =
        meta.firstOrNull { it.accountName == email }?.dataVersion ?: UUID.randomUUID()
}
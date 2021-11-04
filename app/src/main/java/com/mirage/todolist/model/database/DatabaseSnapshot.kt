package com.mirage.todolist.model.database

import java.util.*

data class DatabaseSnapshot(
    val tasks: List<TaskEntity>,
    val tags: List<TagEntity>,
    val relations: List<RelationEntity>,
    val versions: List<VersionEntity>
) {
    fun getDataVersion(email: String): UUID =
        versions.firstOrNull { it.accountName == email }?.dataVersion ?: UUID.randomUUID()
}
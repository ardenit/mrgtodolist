package com.mirage.todolist.model.room

import java.util.*


data class DatabaseSnapshot(
    val tasks: Set<TaskEntity>,
    val tags: Set<TagEntity>,
    val relations: Set<TaskTagEntity>,
    val meta: Set<MetaEntity>
) {

    val dataVersion: UUID
        get() {
            val versionEntity = meta.find { it.name == "data_version" }
            return versionEntity?.value ?: UUID.randomUUID()
        }
}
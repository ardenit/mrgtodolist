package com.mirage.todolist.model.room

import java.util.*


data class DatabaseSnapshot(
    val tasks: List<TaskEntity>,
    val tags: List<TagEntity>,
    val relations: List<RelationEntity>,
    val meta: List<MetaEntity>
) {

    val dataVersion: UUID
        get() {
            val versionEntity = meta.find { it.key == "data_version" }
            return versionEntity?.value ?: UUID.randomUUID()
        }
}
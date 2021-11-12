package com.mirage.todolist.model.database

import java.util.*

/**
 * Snapshot of a database that contains all stored data.
 */
data class DatabaseSnapshot(
    val tasks: List<TaskEntity>,
    val tags: List<TagEntity>,
    val relations: List<RelationEntity>,
    val versions: List<VersionEntity>
)
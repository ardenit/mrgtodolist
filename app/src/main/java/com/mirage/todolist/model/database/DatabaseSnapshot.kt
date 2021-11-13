package com.mirage.todolist.model.database

/**
 * Snapshot of a database that contains all stored data.
 */
data class DatabaseSnapshot(
    val tasks: List<TaskEntity>,
    val tags: List<TagEntity>,
    val relations: List<RelationEntity>,
    val versions: List<VersionEntity>
)
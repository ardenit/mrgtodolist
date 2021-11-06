package com.mirage.todolist.model.database

/**
 * Slice of a [DatabaseSnapshot] that contains data related to a single account.
 */
data class AccountSnapshot(
    val tasks: List<TaskEntity>,
    val tags: List<TagEntity>,
    val relations: List<RelationEntity>,
    val version: VersionEntity,
    val accountName: String
)
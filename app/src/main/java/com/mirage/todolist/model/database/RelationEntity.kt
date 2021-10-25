package com.mirage.todolist.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import java.time.Clock
import java.time.Instant
import java.util.*

/**
 * Entity for resolving many-to-many relation between tasks and tags
 * If task, tag or relation have different account names, the relation is ignored
 */
@Entity(
    tableName = "relations",
    primaryKeys = ["task_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["task_id"],
            childColumns = ["task_id"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["tag_id"],
            childColumns = ["tag_id"],
            onDelete = CASCADE
        )],
    indices = [Index(value = arrayOf("account_name"), unique = false)]
)
data class RelationEntity(
    /** Task's unique ID */
    @ColumnInfo(name = "task_id")
    val taskId: UUID,
    /** Tag's unique ID */
    @ColumnInfo(name = "tag_id")
    val tagId: UUID,
    @ColumnInfo(name = "account_name")
    val accountName: String = "",
    /** Whether this relation is deleted and should be ignored (used for merging differences on sync) */
    @ColumnInfo(name = "deleted")
    val deleted: Boolean,
    @ColumnInfo(name = "last_modified")
    val lastModified: Instant = Clock.systemUTC().instant()
)
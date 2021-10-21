package com.mirage.todolist.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.util.*

/**
 * Entity for resolving many-to-many relation between tasks and tags
 * If task and tag have different account names, the relation is ignored
 */
@Entity(tableName = "relations", primaryKeys = ["task_id", "tag_id"])
data class RelationEntity(
    /** Task's unique ID */
    @ColumnInfo(name = "task_id")
    val taskId: UUID,
    /** Tag's unique ID */
    @ColumnInfo(name = "tag_id")
    val tagId: UUID,
    /** Whether this relation is deleted and should be ignored (used for merging differences on sync) */
    @ColumnInfo(name = "deleted")
    val deleted: Boolean,
    @ColumnInfo(name = "last_modified")
    val lastModifiedTimeMillis: Long
)
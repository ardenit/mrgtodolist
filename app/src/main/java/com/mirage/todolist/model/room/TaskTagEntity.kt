package com.mirage.todolist.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Entity for resolving many-to-many relation between tasks and tags
 */
@Entity(tableName = "tasks_x_tags", primaryKeys = ["task_id_first", "task_id_last", "tag_id_first", "tag_id_last"])
data class TaskTagEntity(
    /** Most significant bits of task's unique ID */
    @ColumnInfo(name = "task_id_first")
    val taskIdFirst: Long,
    /** Least significant bits of task's unique ID */
    @ColumnInfo(name = "task_id_last")
    val taskIdLast: Long,
    /** Most significant bits of tag's unique ID */
    @ColumnInfo(name = "tag_id_first")
    val tagIdFirst: Long,
    /** Least significant bits of tag's unique ID */
    @ColumnInfo(name = "tag_id_last")
    val tagIdLast: Long,
    /** Whether this relation is deleted and should be ignored (used for merging differences on sync) */
    @ColumnInfo(name = "deleted")
    val deleted: Boolean,
    @ColumnInfo(name = "last_modified")
    val lastModifiedTimeMillis: Long
)
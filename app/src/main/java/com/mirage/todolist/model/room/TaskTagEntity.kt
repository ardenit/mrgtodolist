package com.mirage.todolist.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.util.*

/**
 * Entity for resolving many-to-many relation between tasks and tags
 */
@Entity(tableName = "tasks_x_tags", primaryKeys = ["task_id", "tag_id"])
data class TaskTagEntity(
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
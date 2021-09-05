package com.mirage.todolist.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "tags", primaryKeys = ["tag_id_first", "tag_id_last"])
data class TagEntity(
    /** Most significant bits of tag's unique ID */
    @ColumnInfo(name = "tag_id_first")
    val tagIdFirst: Long,
    /** Least significant bits of tag's unique ID */
    @ColumnInfo(name = "tag_id_last")
    val tagIdLast: Long,
    @ColumnInfo(name = "tag_index")
    val tagIndex: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "style_index")
    val styleIndex: Int,
    @ColumnInfo(name = "deleted")
    val deleted: Boolean,
    @ColumnInfo(name = "last_modified")
    val lastModifiedTimeMillis: Long
)
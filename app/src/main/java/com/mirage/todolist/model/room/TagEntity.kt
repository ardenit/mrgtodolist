package com.mirage.todolist.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    @ColumnInfo(name = "tag_id")
    val tagId: UUID,
    @ColumnInfo(name = "tag_index")
    val tagIndex: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "style_index")
    val styleIndex: Int,
    @ColumnInfo(name = "deleted")
    val deleted: Boolean,
    @ColumnInfo(name = "last_modified")
    val lastModifiedTimeMillis: Long,
    @ColumnInfo(name = "account_name")
    val accountName: String = ""
)
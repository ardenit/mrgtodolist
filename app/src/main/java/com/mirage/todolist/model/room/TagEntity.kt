package com.mirage.todolist.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.*

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    @ColumnInfo(name = "tag_id")
    val tagId: UUID,
    @ColumnInfo(name = "account_name")
    val accountName: String = "",
    @ColumnInfo(name = "tag_index")
    val tagIndex: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "style_index")
    val styleIndex: Int,
    @ColumnInfo(name = "deleted")
    val deleted: Boolean,
    @ColumnInfo(name = "last_modified")
    val lastModified: Instant = Clock.System.now()
)